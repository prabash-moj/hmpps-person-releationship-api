package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.openapitools.jackson.nullable.JsonNullable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearchapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.patch.PatchContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository

@TestPropertySource(
  properties = [
    "feature.event.contacts-api.prisoner-contact.amended=true",
    "feature.events.sns.enabled=false",
  ],
)
class PatchContactIntegrationTest : H2IntegrationTestBase() {

  private val contactId = 21L
  private val updatedByUser = "JD000001"

  @Autowired
  lateinit var contactRepository: ContactRepository

  @Nested
  inner class ErrorScenarios {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.patch()
        .uri("/contact/123456")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(aPatchContactRequest())
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.patch()
        .uri("/contact/123456")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(aPatchContactRequest())
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.patch()
        .uri("/contact/123456")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(aPatchContactRequest())
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun ` should return bad request when request is empty`() {
      webTestClient.patch()
        .uri("/contact/$contactId")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .bodyValue(
          """{
                  }""",
        )
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ErrorResponse::class.java)
        .returnResult().responseBody!!
    }

    private fun aPatchContactRequest() = PatchContactRequest(
      languageCode = JsonNullable.of("BEN"),
      updatedBy = updatedByUser,
    )
  }

  @Nested
  inner class LanguageCode {

    @Test
    fun `should not patch the Language code when not provided`() {
      resetLanguageCode()

      val req = PatchContactRequest(
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.languageCode).isEqualTo("ENG")
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
    }

    @Test
    fun `should successfully patch the Language code with null value`() {
      resetLanguageCode()

      val req = PatchContactRequest(
        languageCode = JsonNullable.of(null),
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.languageCode).isEqualTo(null)
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
    }

    @Test
    fun `should successfully patch the Language code with a value`() {
      resetLanguageCode()

      val req = PatchContactRequest(
        languageCode = JsonNullable.of("FRE-FRA"),
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.languageCode).isEqualTo("FRE-FRA")
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
    }

    private fun resetLanguageCode() {
      val req = PatchContactRequest(
        languageCode = JsonNullable.of("ENG"),
        updatedBy = updatedByUser,
      )
      val res =
        testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.languageCode).isEqualTo("ENG")
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
    }
  }

  @Nested
  inner class InterpreterRequired {

    @Test
    fun `should successfully patch the interpreter required with true`() {
      resetInterpreterRequired(false)

      val req = PatchContactRequest(
        interpreterRequired = JsonNullable.of(true),
        updatedBy = updatedByUser,
      )
      val res =
        testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.interpreterRequired).isEqualTo(true)
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
    }

    @Test
    fun `should not patch the interpreter required when not provided`() {
      resetInterpreterRequired(true)

      val req = PatchContactRequest(
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.interpreterRequired).isEqualTo(true)
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
    }

    @Test
    fun `should successfully patch the interpreter required with null value`() {
      resetInterpreterRequired(true)

      val req = PatchContactRequest(
        interpreterRequired = JsonNullable.of(null),
        updatedBy = updatedByUser,
      )
      val uri = UriComponentsBuilder.fromPath("/contact/$contactId")
        .build()
        .toUri()

      val errors = testAPIClient.getBadResponseErrorsWithPatch(req, uri)

      assertThat(errors.userMessage).isEqualTo("Validation failure: Unsupported interpreter required type null.")
    }

    private fun resetInterpreterRequired(resetValue: Boolean) {
      val req = PatchContactRequest(
        interpreterRequired = JsonNullable.of(resetValue),
        updatedBy = updatedByUser,
      )
      val res =
        testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.interpreterRequired).isEqualTo(resetValue)
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
    }
  }
}
