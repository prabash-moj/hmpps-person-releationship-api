package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactAddressResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.ContactAddressInfo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.PersonReference
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.Source
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

class CreateContactAddressIntegrationTest : H2IntegrationTestBase() {
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
