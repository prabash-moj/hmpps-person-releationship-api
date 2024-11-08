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
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactEmailDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.ContactEmailInfo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent

class CreateContactEmailIntegrationTest : H2IntegrationTestBase() {
  private var savedContactId = 0L

  @BeforeEach
  fun initialiseData() {
    savedContactId = testAPIClient.createAContact(
      CreateContactRequest(
        lastName = "email",
        firstName = "has",
        createdBy = "created",
      ),
    ).id
  }

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.post()
      .uri("/contact/$savedContactId/email")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalRequest())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.post()
      .uri("/contact/$savedContactId/email")
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
    webTestClient.post()
      .uri("/contact/$savedContactId/email")
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
      "emailAddress must not be null;{\"emailAddress\": null, \"createdBy\": \"created\"}",
      "emailAddress must not be null;{\"createdBy\": \"created\"}",
      "createdBy must not be null;{\"emailAddress\": \"test@example.com\", \"createdBy\": null}",
      "createdBy must not be null;{\"emailAddress\": \"test@example.com\"}",
    ],
    delimiter = ';',
  )
  fun `should return bad request if required fields are null`(expectedMessage: String, json: String) {
    val errors = webTestClient.post()
      .uri("/contact/$savedContactId/email")
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

  @ParameterizedTest
  @MethodSource("allFieldConstraintViolations")
  fun `should enforce field constraints`(expectedMessage: String, request: CreateEmailRequest) {
    val errors = webTestClient.post()
      .uri("/contact/$savedContactId/email")
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
  }

  @Test
  fun `should not create the email if the contact is not found`() {
    val request = aMinimalRequest()

    val errors = webTestClient.post()
      .uri("/contact/-321/email")
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
  fun `should not create the email if the email is not valid`() {
    val request = CreateEmailRequest(
      emailAddress = "@example.com",
      createdBy = "created",
    )

    val errors = webTestClient.post()
      .uri("/contact/$savedContactId/email")
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

    assertThat(errors.userMessage).isEqualTo("Validation failure: Email address is invalid")
  }

  @Test
  fun `should create the email`() {
    val request = aMinimalRequest()

    val created = testAPIClient.createAContactEmail(savedContactId, request)

    assertEqualsExcludingTimestamps(created, request)
    stubEvents.assertHasEvent(OutboundEvent.CONTACT_EMAIL_CREATED, ContactEmailInfo(created.contactEmailId))
  }

  private fun assertEqualsExcludingTimestamps(email: ContactEmailDetails, request: CreateEmailRequest) {
    with(email) {
      assertThat(emailAddress).isEqualTo(request.emailAddress)
      assertThat(createdBy).isEqualTo(request.createdBy)
      assertThat(createdTime).isNotNull()
    }
  }

  companion object {
    @JvmStatic
    fun allFieldConstraintViolations(): List<Arguments> {
      return listOf(
        Arguments.of("emailAddress must be <= 240 characters", aMinimalRequest().copy(emailAddress = "".padStart(241))),
        Arguments.of(
          "createdBy must be <= 100 characters",
          aMinimalRequest().copy(createdBy = "".padStart(101)),
        ),
      )
    }

    private fun aMinimalRequest() = CreateEmailRequest(
      emailAddress = "test@example.com",
      createdBy = "created",
    )
  }
}
