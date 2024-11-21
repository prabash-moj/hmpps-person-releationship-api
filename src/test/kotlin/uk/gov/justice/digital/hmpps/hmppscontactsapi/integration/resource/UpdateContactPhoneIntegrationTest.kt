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
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreatePhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdatePhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.ContactPhoneInfo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.PersonReference

class UpdateContactPhoneIntegrationTest : H2IntegrationTestBase() {
  private var savedContactId = 0L
  private var savedContactPhoneId = 0L

  @BeforeEach
  fun initialiseData() {
    savedContactId = testAPIClient.createAContact(
      CreateContactRequest(
        lastName = "phone",
        firstName = "has",
        createdBy = "created",
      ),
    ).id
    savedContactPhoneId = testAPIClient.createAContactPhone(
      savedContactId,
      CreatePhoneRequest(
        phoneType = "MOB",
        phoneNumber = "07777777777",
        extNumber = "123456",
        createdBy = "USER1",
      ),
    ).contactPhoneId
  }

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.put()
      .uri("/contact/$savedContactId/phone/$savedContactPhoneId")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalRequest())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.put()
      .uri("/contact/$savedContactId/phone/$savedContactPhoneId")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalRequest())
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    webTestClient.put()
      .uri("/contact/$savedContactId/phone/$savedContactPhoneId")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalRequest())
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "phoneType must not be null;{\"phoneType\": null, \"phoneNumber\": \"0123456789\", \"updatedBy\": \"amended\"}",
      "phoneType must not be null;{\"phoneNumber\": \"0123456789\", \"updatedBy\": \"amended\"}",
      "phoneNumber must not be null;{\"phoneType\": \"MOB\", \"phoneNumber\": null, \"updatedBy\": \"amended\"}",
      "phoneNumber must not be null;{\"phoneType\": \"MOB\", \"updatedBy\": \"amended\"}",
      "updatedBy must not be null;{\"phoneType\": \"MOB\", \"phoneNumber\": \"0123456789\", \"updatedBy\": null}",
      "updatedBy must not be null;{\"phoneType\": \"MOB\", \"phoneNumber\": \"0123456789\"}",
    ],
    delimiter = ';',
  )
  fun `should return bad request if required fields are null`(expectedMessage: String, json: String) {
    val errors = webTestClient.put()
      .uri("/contact/$savedContactId/phone/$savedContactPhoneId")
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
    stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_PHONE_UPDATED, ContactPhoneInfo(savedContactPhoneId))
  }

  @ParameterizedTest
  @MethodSource("allFieldConstraintViolations")
  fun `should enforce field constraints`(expectedMessage: String, request: UpdatePhoneRequest) {
    val errors = webTestClient.put()
      .uri("/contact/$savedContactId/phone/$savedContactPhoneId")
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
    stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_PHONE_UPDATED, ContactPhoneInfo(savedContactPhoneId))
  }

  @ParameterizedTest
  @CsvSource(
    "Plus only at start,123+456",
    "Hash not allowed,#",
  )
  fun `should not update the phone if the phone number contains unsupported chars`(case: String, phoneNumber: String) {
    val request = UpdatePhoneRequest(
      phoneType = "MOB",
      phoneNumber = phoneNumber,
      updatedBy = "amended",
    )

    val errors = webTestClient.put()
      .uri("/contact/$savedContactId/phone/$savedContactPhoneId")
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

    assertThat(errors.userMessage).isEqualTo("Validation failure: Phone number invalid, it can only contain numbers, () and whitespace with an optional + at the start")
    stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_PHONE_UPDATED, ContactPhoneInfo(savedContactPhoneId))
  }

  @Test
  fun `should not update the phone if the type is not supported`() {
    val request = UpdatePhoneRequest(
      phoneType = "SATELLITE",
      phoneNumber = "+44777777777 (0123)",
      updatedBy = "amended",
    )

    val errors = webTestClient.put()
      .uri("/contact/$savedContactId/phone/$savedContactPhoneId")
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

    assertThat(errors.userMessage).isEqualTo("Validation failure: Unsupported phone type (SATELLITE)")
    stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_PHONE_UPDATED, ContactPhoneInfo(savedContactPhoneId))
  }

  @Test
  fun `should not update the phone if the contact is not found`() {
    val request = aMinimalRequest()

    val errors = webTestClient.put()
      .uri("/contact/-321/phone/$savedContactPhoneId")
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
    stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_PHONE_UPDATED, ContactPhoneInfo(savedContactPhoneId))
  }

  @Test
  fun `should not update the phone if the phone is not found`() {
    val request = aMinimalRequest()

    val errors = webTestClient.put()
      .uri("/contact/$savedContactId/phone/-99")
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

    assertThat(errors.userMessage).isEqualTo("Entity not found : Contact phone (-99) not found")
    stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_PHONE_UPDATED, ContactPhoneInfo(-99))
  }

  @Test
  fun `should update the phone with minimal fields`() {
    val request = UpdatePhoneRequest(
      phoneType = "MOB",
      phoneNumber = "+44777777777 (0123)",
      extNumber = null,
      updatedBy = "amended",
    )

    val updated = testAPIClient.updateAContactPhone(savedContactId, savedContactPhoneId, request)

    with(updated) {
      assertThat(phoneType).isEqualTo(request.phoneType)
      assertThat(phoneNumber).isEqualTo(request.phoneNumber)
      assertThat(extNumber).isNull()
      assertThat(createdBy).isEqualTo("USER1")
      assertThat(createdTime).isNotNull()
      assertThat(updatedBy).isEqualTo("amended")
      assertThat(updatedTime).isNotNull()
    }

    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_PHONE_UPDATED,
      additionalInfo = ContactPhoneInfo(savedContactPhoneId),
      personReference = PersonReference(savedContactId),
    )
  }

  @Test
  fun `should update the phone with all fields`() {
    val request = UpdatePhoneRequest(
      phoneType = "MOB",
      phoneNumber = "+44777777777 (0123)",
      extNumber = "9999",
      updatedBy = "amended",
    )

    val updated = testAPIClient.updateAContactPhone(savedContactId, savedContactPhoneId, request)

    with(updated) {
      assertThat(phoneType).isEqualTo(request.phoneType)
      assertThat(phoneNumber).isEqualTo(request.phoneNumber)
      assertThat(extNumber).isEqualTo(request.extNumber)
      assertThat(createdBy).isEqualTo("USER1")
      assertThat(createdTime).isNotNull()
      assertThat(updatedBy).isEqualTo("amended")
      assertThat(updatedTime).isNotNull()
    }

    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_PHONE_UPDATED,
      additionalInfo = ContactPhoneInfo(savedContactPhoneId),
      personReference = PersonReference(dpsContactId = savedContactId),
    )
  }

  companion object {
    @JvmStatic
    fun allFieldConstraintViolations(): List<Arguments> {
      return listOf(
        Arguments.of("phoneType must be <= 12 characters", aMinimalRequest().copy(phoneType = "".padStart(13))),
        Arguments.of("phoneNumber must be <= 240 characters", aMinimalRequest().copy(phoneNumber = "".padStart(241))),
        Arguments.of(
          "extNumber must be <= 7 characters",
          aMinimalRequest().copy(extNumber = "".padStart(8)),
        ),
        Arguments.of(
          "updatedBy must be <= 100 characters",
          aMinimalRequest().copy(updatedBy = "".padStart(101)),
        ),
      )
    }

    private fun aMinimalRequest() = UpdatePhoneRequest(
      phoneType = "MOB",
      phoneNumber = "+44777777777 (0123)",
      updatedBy = "amended",
    )
  }
}
