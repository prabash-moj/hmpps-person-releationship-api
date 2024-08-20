package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.PrisonerContactSummary

private const val GET_PRISONER_CONTACT = "/prisoner-contacts/prisoner/A1234BB"

class PrisonerContactsResourceIntegrationTest : IntegrationTestBase() {

  @Nested
  inner class ContactsPrisonerEndpoint {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/prisoner-contacts/prisoner/P001")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("/prisoner-contacts/prisoner/P001")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("/prisoner-contacts/prisoner/P001")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return not found if no prisoner found`() {
      stubPrisonSearchWithNotFoundResponse("A1234BB")

      webTestClient.get()
        .uri("/prisoner-contacts/prisoner/P001")
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `should return OK`() {
      stubPrisonSearchWithResponse("A1234BB")

      var contacts = webTestClient.getContacts(GET_PRISONER_CONTACT)

      assertThat(contacts).extracting("surname").contains("Last")
      assertThat(contacts).hasSize(1)
    }
  }

  private fun WebTestClient.getContacts(URL: String): MutableList<PrisonerContactSummary> =
    get()
      .uri(URL)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(PrisonerContactSummary::class.java)
      .returnResult().responseBody!!
}
