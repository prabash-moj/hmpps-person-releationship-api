package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearchapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.ContactAddressInfo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.PersonReference
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.Source

class UpdateContactAddressIntegrationTest : H2IntegrationTestBase() {
  private var savedContactId = 0L
  private var savedContactAddressId = 0L

  @BeforeEach
  fun initialiseData() {
    savedContactId = testAPIClient.createAContact(
      CreateContactRequest(
        lastName = "address",
        firstName = "has",
        createdBy = "created",
      ),
    ).id

    savedContactAddressId = testAPIClient.createAContactAddress(
      savedContactId,
      CreateContactAddressRequest(
        primaryAddress = true,
        addressType = "HOME",
        flat = "1A",
        property = "27",
        street = "Acacia Avenue",
        area = "Hoggs Bottom",
        postcode = "HB10 2NB",
        createdBy = "created",
      ),
    ).contactAddressId
  }

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.put()
      .uri("/contact/$savedContactId/address/$savedContactAddressId")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalAddressRequest())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.put()
      .uri("/contact/$savedContactId/address/$savedContactAddressId")
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
    webTestClient.put()
      .uri("/contact/$savedContactId/address/$savedContactAddressId")
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
      "addressType must not be null;{\"addressType\": null, \"updatedBy\": \"updater\"}",
      "addressType must not be null;{\"updatedBy\": \"updater\"}",
      "updatedBy must not be null;{\"addressType\": \"HOME\", \"updatedBy\": null}",
      "updatedBy must not be null;{\"addressType\": \"HOME\"}",
    ],
    delimiter = ';',
  )
  fun `should return bad request if required fields are null`(expectedMessage: String, json: String) {
    val errors = webTestClient.put()
      .uri("/contact/$savedContactId/address/$savedContactAddressId")
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

    stubEvents.assertHasNoEvents(
      event = OutboundEvent.CONTACT_ADDRESS_UPDATED,
      additionalInfo = ContactAddressInfo(savedContactAddressId, Source.DPS),
    )
  }

  @ParameterizedTest
  @MethodSource("allFieldConstraintViolations")
  fun `should enforce field constraints`(expectedMessage: String, request: UpdateContactAddressRequest) {
    val errors = webTestClient.put()
      .uri("/contact/$savedContactId/address/$savedContactAddressId")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody!!

    assertThat(errors.userMessage).isEqualTo("Validation failure(s): $expectedMessage")

    stubEvents.assertHasNoEvents(
      event = OutboundEvent.CONTACT_ADDRESS_UPDATED,
      additionalInfo = ContactAddressInfo(savedContactAddressId, Source.DPS),
    )
  }

  @Test
  fun `should not update the address if the contact is not found`() {
    val request = aMinimalAddressRequest()

    val errors = webTestClient.put()
      .uri("/contact/-321/address/$savedContactAddressId")
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

    stubEvents.assertHasNoEvents(
      event = OutboundEvent.CONTACT_ADDRESS_UPDATED,
      additionalInfo = ContactAddressInfo(savedContactAddressId, Source.DPS),
    )
  }

  @Test
  fun `should not update the address if the id is not found`() {
    val request = aMinimalAddressRequest()

    val errors = webTestClient.put()
      .uri("/contact/$savedContactId/address/-99")
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

    assertThat(errors.userMessage).isEqualTo("Entity not found : Contact address (-99) not found")

    stubEvents.assertHasNoEvents(
      event = OutboundEvent.CONTACT_ADDRESS_UPDATED,
      additionalInfo = ContactAddressInfo(-99, Source.DPS),
    )
  }

  @Test
  fun `should update the address`() {
    val request = UpdateContactAddressRequest(
      addressType = "HOME",
      primaryAddress = true,
      property = "28",
      street = "Acacia Avenue",
      area = "Hoggs Bottom",
      postcode = "HB10 1DJ",
      updatedBy = "amended",
    )

    val updated = testAPIClient.updateAContactAddress(savedContactId, savedContactAddressId, request)

    with(updated) {
      assertThat(property).isEqualTo("28")
      assertThat(street).isEqualTo("Acacia Avenue")
      assertThat(area).isEqualTo("Hoggs Bottom")
      assertThat(postcode).isEqualTo("HB10 1DJ")
      assertThat(createdBy).isEqualTo("created")
      assertThat(createdTime).isNotNull()
      assertThat(updatedBy).isEqualTo("amended")
      assertThat(updatedTime).isNotNull()
    }

    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_ADDRESS_UPDATED,
      additionalInfo = ContactAddressInfo(savedContactAddressId, Source.DPS),
      personReference = PersonReference(dpsContactId = savedContactId),
    )
  }

  companion object {
    @JvmStatic
    fun allFieldConstraintViolations(): List<Arguments> {
      return listOf(
        Arguments.of(
          "addressType must be <= 12 characters",
          aMinimalAddressRequest().copy(addressType = "".padStart(13)),
        ),
        Arguments.of(
          "cityCode must be <= 12 characters",
          aMinimalAddressRequest().copy(cityCode = "".padStart(13)),
        ),
        Arguments.of(
          "countyCode must be <= 12 characters",
          aMinimalAddressRequest().copy(countyCode = "".padStart(13)),
        ),
        Arguments.of(
          "countryCode must be <= 12 characters",
          aMinimalAddressRequest().copy(countryCode = "".padStart(13)),
        ),
        Arguments.of(
          "updatedBy must be <= 100 characters",
          aMinimalAddressRequest().copy(updatedBy = "".padStart(101)),
        ),
      )
    }

    private fun aMinimalAddressRequest() = UpdateContactAddressRequest(
      addressType = "HOME",
      primaryAddress = true,
      property = "27",
      street = "Hello Road",
      updatedBy = "amended",
    )
  }
}
