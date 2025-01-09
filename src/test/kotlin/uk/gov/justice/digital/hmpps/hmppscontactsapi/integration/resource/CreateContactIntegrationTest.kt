package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.ContactInfo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.PersonReference
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.Source
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.LocalDate

const val LOCAL_CONTACT_ID_SEQUENCE_MIN: Long = 20000000L

class CreateContactIntegrationTest : H2IntegrationTestBase() {

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.post()
      .uri("/contact")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalCreateContactRequest())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.post()
      .uri("/contact")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalCreateContactRequest())
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    webTestClient.post()
      .uri("/contact")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalCreateContactRequest())
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "firstName must not be null;{\"firstName\": null, \"lastName\": \"last\", \"createdBy\": \"created\"}",
      "firstName must not be null;{\"lastName\": \"last\", \"createdBy\": \"created\"}",
      "lastName must not be null;{\"firstName\": \"first\", \"lastName\": null, \"createdBy\": \"created\"}",
      "lastName must not be null;{\"firstName\": \"first\", \"createdBy\": \"created\"}",
      "createdBy must not be null;{\"firstName\": \"first\", \"lastName\": \"last\", \"createdBy\": null}",
      "createdBy must not be null;{\"firstName\": \"first\", \"lastName\": \"last\"}",
    ],
    delimiter = ';',
  )
  fun `should return bad request if required fields are null`(expectedMessage: String, json: String) {
    val errors = webTestClient.post()
      .uri("/contact")
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
      event = OutboundEvent.CONTACT_CREATED,
    )
  }

  @ParameterizedTest
  @MethodSource("allFieldConstraintViolations")
  fun `should enforce field constraints`(expectedMessage: String, request: CreateContactRequest) {
    val errors = webTestClient.post()
      .uri("/contact")
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
      event = OutboundEvent.CONTACT_CREATED,
    )
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "{\"firstName\": \"first\", \"lastName\": \"last\", \"createdBy\": \"created\", \"dateOfBirth\": \"1st Jan\"}",
      "{\"firstName\": \"first\", \"lastName\": \"last\", \"createdBy\": \"created\", \"dateOfBirth\": \"01-01-1970\"}",
    ],
    delimiter = ';',
  )
  fun `should handle invalid dob formats`(json: String) {
    val errors = webTestClient.post()
      .uri("/contact")
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

    assertThat(errors.userMessage).isEqualTo("Validation failure: dateOfBirth could not be parsed as a date")
    stubEvents.assertHasNoEvents(
      event = OutboundEvent.CONTACT_CREATED,
    )
  }

  @Test
  fun `should create the contact with minimal fields`() {
    val request = aMinimalCreateContactRequest()

    val contactReturnedOnCreate = testAPIClient.createAContact(request)

    assertContactsAreEqualExcludingTimestamps(contactReturnedOnCreate, request)
    assertThat(contactReturnedOnCreate).isEqualTo(
      testAPIClient.getContact(
        contactReturnedOnCreate.id,
      ),
    )

    // Verify that a contact created locally has an ID in the appropriate range - above 20,000,000
    assertThat(contactReturnedOnCreate.id).isGreaterThanOrEqualTo(LOCAL_CONTACT_ID_SEQUENCE_MIN)

    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_CREATED,
      additionalInfo = ContactInfo(contactReturnedOnCreate.id, Source.DPS),
      personReference = PersonReference(dpsContactId = contactReturnedOnCreate.id),
    )
  }

  @ParameterizedTest
  @ValueSource(strings = ["ROLE_CONTACTS_ADMIN", "ROLE_CONTACTS__RW"])
  fun `should create the contact with all fields`(role: String) {
    val request = CreateContactRequest(
      title = "MR",
      lastName = "last",
      firstName = "first",
      middleNames = "middle",
      dateOfBirth = LocalDate.of(1982, 6, 15),
      createdBy = "created",
    )

    val contact = testAPIClient.createAContact(request, role)

    assertContactsAreEqualExcludingTimestamps(contact, request)

    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_CREATED,
      additionalInfo = ContactInfo(contact.id, Source.DPS),
      personReference = PersonReference(contact.id),
    )
  }

  private fun assertContactsAreEqualExcludingTimestamps(contact: ContactDetails, request: CreateContactRequest) {
    with(contact) {
      assertThat(title).isEqualTo(request.title)
      assertThat(lastName).isEqualTo(request.lastName)
      assertThat(firstName).isEqualTo(request.firstName)
      assertThat(middleNames).isEqualTo(request.middleNames)
      assertThat(dateOfBirth).isEqualTo(request.dateOfBirth)
      assertThat(createdBy).isEqualTo(request.createdBy)
    }
  }

  companion object {
    @JvmStatic
    fun allFieldConstraintViolations(): List<Arguments> {
      return listOf(
        Arguments.of("title must be <= 12 characters", aMinimalCreateContactRequest().copy(title = "".padStart(13))),
        Arguments.of(
          "lastName must be <= 35 characters",
          aMinimalCreateContactRequest().copy(lastName = "".padStart(36)),
        ),
        Arguments.of(
          "firstName must be <= 35 characters",
          aMinimalCreateContactRequest().copy(firstName = "".padStart(36)),
        ),
        Arguments.of(
          "middleNames must be <= 35 characters",
          aMinimalCreateContactRequest().copy(middleNames = "".padStart(36)),
        ),
        Arguments.of(
          "createdBy must be <= 100 characters",
          aMinimalCreateContactRequest().copy(createdBy = "".padStart(101)),
        ),
      )
    }

    private fun aMinimalCreateContactRequest() = CreateContactRequest(
      lastName = "last",
      firstName = "first",
      createdBy = "created",
    )
  }
}
