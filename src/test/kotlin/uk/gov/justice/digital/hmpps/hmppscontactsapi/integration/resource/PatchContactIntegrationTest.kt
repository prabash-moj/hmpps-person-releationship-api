package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openapitools.jackson.nullable.JsonNullable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearchapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.patch.PatchContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository

@TestPropertySource(properties = ["feature.event.contacts-api.prisoner-contact.amended=true"])
@TestPropertySource(properties = ["feature.events.sns.enabled=false"])
class PatchContactIntegrationTest : H2IntegrationTestBase() {

  val contactId = 21L

  @Autowired
  lateinit var contactRepository: ContactRepository

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
  fun `should successfully patch the contact`() {
    val noLanguageCodePatchRequest = PatchContactRequest(
      updatedBy = "JD000001",
    )
    val noLanguageCodePatchResponse = testAPIClient.patchAContact(noLanguageCodePatchRequest, "/contact/$contactId")

    with(noLanguageCodePatchResponse) {
      assertThat(languageCode).isEqualTo("ENG")
      assertThat(amendedBy).isEqualTo("JD000001")
    }

    val nullLanguageCodePatchRequest = PatchContactRequest(
      languageCode = JsonNullable.of(null),
      updatedBy = "JD000001",
    )
    val nullLanguageCodePatchResponse = testAPIClient.patchAContact(nullLanguageCodePatchRequest, "/contact/$contactId")

    with(nullLanguageCodePatchResponse) {
      assertThat(languageCode).isEqualTo(null)
      assertThat(amendedBy).isEqualTo("JD000001")
    }

    val withLanguageCodePatchRequest = PatchContactRequest(
      languageCode = JsonNullable.of("BEN"),
      updatedBy = "JD000001",
    )
    val withLanguageCodePatchResponse = testAPIClient.patchAContact(withLanguageCodePatchRequest, "/contact/$contactId")

    with(withLanguageCodePatchResponse) {
      assertThat(languageCode).isEqualTo("BEN")
      assertThat(amendedBy).isEqualTo("JD000001")
    }
  }

  @Test
  fun `should patch do not have amended by then return bad request`() {
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
    updatedBy = "JD000001",
  )
}
