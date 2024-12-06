package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.PostgresIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactAddressResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.ContactAddressInfo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.PersonReference
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.Source
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

class CreateContactAddressIntegrationTest : PostgresIntegrationTestBase() {
  private var savedContactId = 0L

  @BeforeEach
  fun initialiseData() {
    savedContactId = testAPIClient.createAContact(
      CreateContactRequest(
        lastName = "address",
        firstName = "has",
        createdBy = "created",
      ),
    ).id
  }

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.post()
      .uri("/contact/$savedContactId/address")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalAddressRequest())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.post()
      .uri("/contact/$savedContactId/address")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalAddressRequest())
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    webTestClient.post()
      .uri("/contact/$savedContactId/address")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalAddressRequest())
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "addressType must not be null;{\"addressType\": null, \"createdBy\": \"ME\"}",
      "addressType must not be null;{\"createdBy\": \"ME\"}",
      "createdBy must not be null;{\"addressType\": \"HOME\", \"createdBy\": null}",
      "createdBy must not be null;{\"addressType\": \"HOME\"}",
    ],
    delimiter = ';',
  )
  fun `should return bad request if required fields are null`(expectedMessage: String, json: String) {
    val errors = webTestClient.post()
      .uri("/contact/$savedContactId/address")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .bodyValue(json)
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody!!

    assertThat(errors.userMessage).isEqualTo("Validation failure: $expectedMessage")
  }

  @Test
  fun `should not create the address if the contact is not found`() {
    val request = aMinimalAddressRequest()

    val errors = webTestClient.post()
      .uri("/contact/-321/address")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isNotFound
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody!!

    assertThat(errors.userMessage).isEqualTo("Entity not found : Contact (-321) not found")
  }

  @Test
  fun `should create the contact address`() {
    val request = aMinimalAddressRequest()

    val created = testAPIClient.createAContactAddress(savedContactId, request)

    assertEqualsExcludingTimestamps(created, request)

    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_ADDRESS_CREATED,
      additionalInfo = ContactAddressInfo(created.contactAddressId, Source.DPS),
      personReference = PersonReference(dpsContactId = created.contactId),
    )
  }

  @Test
  fun `should remove primary flag from current primary addresses if setting primary`() {
    val requestToCreatePrimary = aMinimalAddressRequest().copy(primaryAddress = true)
    val primary = testAPIClient.createAContactAddress(savedContactId, requestToCreatePrimary)

    val request = aMinimalAddressRequest().copy(primaryAddress = true)
    val created = testAPIClient.createAContactAddress(savedContactId, request)

    val addresses = testAPIClient.getContact(savedContactId).addresses
    assertThat(addresses.find { it.contactAddressId == primary.contactAddressId }!!.primaryAddress).isFalse()
    assertThat(addresses.find { it.contactAddressId == created.contactAddressId }!!.primaryAddress).isTrue()

    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_ADDRESS_CREATED,
      additionalInfo = ContactAddressInfo(created.contactAddressId, Source.DPS),
      personReference = PersonReference(dpsContactId = created.contactId),
    )
    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_ADDRESS_UPDATED,
      additionalInfo = ContactAddressInfo(primary.contactAddressId, Source.DPS),
      personReference = PersonReference(dpsContactId = created.contactId),
    )
  }

  @Test
  fun `should not remove primary flag from current primary addresses if not setting primary`() {
    val requestToCreatePrimary = aMinimalAddressRequest().copy(primaryAddress = true)
    val primary = testAPIClient.createAContactAddress(savedContactId, requestToCreatePrimary)

    val request = aMinimalAddressRequest().copy(primaryAddress = false)
    val created = testAPIClient.createAContactAddress(savedContactId, request)

    val addresses = testAPIClient.getContact(savedContactId).addresses
    assertThat(addresses.find { it.contactAddressId == primary.contactAddressId }!!.primaryAddress).isTrue()
    assertThat(addresses.find { it.contactAddressId == created.contactAddressId }!!.primaryAddress).isFalse()

    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_ADDRESS_CREATED,
      additionalInfo = ContactAddressInfo(created.contactAddressId, Source.DPS),
      personReference = PersonReference(dpsContactId = created.contactId),
    )
    stubEvents.assertHasNoEvents(event = OutboundEvent.CONTACT_ADDRESS_UPDATED)
  }

  @Test
  fun `should remove mail flag from current mail addresses if setting mail`() {
    val requestToCreateMail = aMinimalAddressRequest().copy(mailFlag = true)
    val mail = testAPIClient.createAContactAddress(savedContactId, requestToCreateMail)

    val request = aMinimalAddressRequest().copy(mailFlag = true)
    val created = testAPIClient.createAContactAddress(savedContactId, request)

    val addresses = testAPIClient.getContact(savedContactId).addresses
    assertThat(addresses.find { it.contactAddressId == mail.contactAddressId }!!.mailFlag).isFalse()
    assertThat(addresses.find { it.contactAddressId == created.contactAddressId }!!.mailFlag).isTrue()

    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_ADDRESS_CREATED,
      additionalInfo = ContactAddressInfo(created.contactAddressId, Source.DPS),
      personReference = PersonReference(dpsContactId = created.contactId),
    )
    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_ADDRESS_UPDATED,
      additionalInfo = ContactAddressInfo(mail.contactAddressId, Source.DPS),
      personReference = PersonReference(dpsContactId = created.contactId),
    )
  }

  @Test
  fun `should not remove mail flag from current mail addresses if not setting mail`() {
    val requestToCreateMail = aMinimalAddressRequest().copy(mailFlag = true, primaryAddress = false)
    val mail = testAPIClient.createAContactAddress(savedContactId, requestToCreateMail)

    val request = aMinimalAddressRequest().copy(mailFlag = false)
    val created = testAPIClient.createAContactAddress(savedContactId, request)

    val addresses = testAPIClient.getContact(savedContactId).addresses
    assertThat(addresses.find { it.contactAddressId == mail.contactAddressId }!!.mailFlag).isTrue()
    assertThat(addresses.find { it.contactAddressId == created.contactAddressId }!!.mailFlag).isFalse()

    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_ADDRESS_CREATED,
      additionalInfo = ContactAddressInfo(created.contactAddressId, Source.DPS),
      personReference = PersonReference(dpsContactId = created.contactId),
    )
    stubEvents.assertHasNoEvents(event = OutboundEvent.CONTACT_ADDRESS_UPDATED)
  }

  @Test
  fun `should remove primary and mail flag from current primary and mail addresses if setting primary and mail`() {
    val requestToCreatePrimary = aMinimalAddressRequest().copy(primaryAddress = true, mailFlag = false)
    val primary = testAPIClient.createAContactAddress(savedContactId, requestToCreatePrimary)

    val requestToCreateMail = aMinimalAddressRequest().copy(primaryAddress = false, mailFlag = true)
    val mail = testAPIClient.createAContactAddress(savedContactId, requestToCreateMail)

    val requestToCreateOtherAddress = aMinimalAddressRequest().copy(primaryAddress = false, mailFlag = false)
    val other = testAPIClient.createAContactAddress(savedContactId, requestToCreateOtherAddress)

    val request = aMinimalAddressRequest().copy(primaryAddress = true, mailFlag = true)
    val created = testAPIClient.createAContactAddress(savedContactId, request)

    val addresses = testAPIClient.getContact(savedContactId).addresses
    assertThat(addresses.find { it.contactAddressId == primary.contactAddressId }!!.primaryAddress).isFalse()
    assertThat(addresses.find { it.contactAddressId == mail.contactAddressId }!!.mailFlag).isFalse()
    assertThat(addresses.find { it.contactAddressId == created.contactAddressId }!!.primaryAddress).isTrue()
    assertThat(addresses.find { it.contactAddressId == created.contactAddressId }!!.mailFlag).isTrue()

    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_ADDRESS_CREATED,
      additionalInfo = ContactAddressInfo(created.contactAddressId, Source.DPS),
      personReference = PersonReference(dpsContactId = created.contactId),
    )
    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_ADDRESS_UPDATED,
      additionalInfo = ContactAddressInfo(primary.contactAddressId, Source.DPS),
      personReference = PersonReference(dpsContactId = created.contactId),
    )
    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_ADDRESS_UPDATED,
      additionalInfo = ContactAddressInfo(mail.contactAddressId, Source.DPS),
      personReference = PersonReference(dpsContactId = created.contactId),
    )
    stubEvents.assertHasNoEvents(
      event = OutboundEvent.CONTACT_ADDRESS_UPDATED,
      additionalInfo = ContactAddressInfo(other.contactAddressId, Source.DPS),
    )
  }

  @Test
  fun `should remove primary and mail flag from current combined primary mail address if setting primary and mail`() {
    val requestToCreatePrimaryAndMail = aMinimalAddressRequest().copy(primaryAddress = true, mailFlag = true)
    val primaryAndMail = testAPIClient.createAContactAddress(savedContactId, requestToCreatePrimaryAndMail)

    val request = aMinimalAddressRequest().copy(primaryAddress = true, mailFlag = true)
    val created = testAPIClient.createAContactAddress(savedContactId, request)

    val addresses = testAPIClient.getContact(savedContactId).addresses
    assertThat(addresses.find { it.contactAddressId == primaryAndMail.contactAddressId }!!.primaryAddress).isFalse()
    assertThat(addresses.find { it.contactAddressId == primaryAndMail.contactAddressId }!!.mailFlag).isFalse()
    assertThat(addresses.find { it.contactAddressId == created.contactAddressId }!!.primaryAddress).isTrue()
    assertThat(addresses.find { it.contactAddressId == created.contactAddressId }!!.mailFlag).isTrue()

    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_ADDRESS_CREATED,
      additionalInfo = ContactAddressInfo(created.contactAddressId, Source.DPS),
      personReference = PersonReference(dpsContactId = created.contactId),
    )
    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_ADDRESS_UPDATED,
      additionalInfo = ContactAddressInfo(primaryAndMail.contactAddressId, Source.DPS),
      personReference = PersonReference(dpsContactId = created.contactId),
    )
  }

  private fun assertEqualsExcludingTimestamps(address: ContactAddressResponse, request: CreateContactAddressRequest) {
    with(address) {
      assertThat(addressType).isEqualTo(request.addressType)
      assertThat(primaryAddress).isEqualTo(request.primaryAddress)
      assertThat(property).isEqualTo(request.property)
      assertThat(street).isEqualTo(request.street)
      assertThat(postcode).isEqualTo(request.postcode)
      assertThat(createdBy).isEqualTo(request.createdBy)
      assertThat(createdTime).isNotNull()
    }
  }

  private fun aMinimalAddressRequest() = CreateContactAddressRequest(
    addressType = "HOME",
    primaryAddress = true,
    property = "27",
    street = "Hello Road",
    createdBy = "created",
  )
}
