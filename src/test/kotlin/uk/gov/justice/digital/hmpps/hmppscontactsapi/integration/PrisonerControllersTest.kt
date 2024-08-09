package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PrisonerControllersTest : IntegrationTestBase() {

  @Nested
  inner class ContactsPrisonerEndpoint {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/contacts/prisoner/P001")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("/contacts/prisoner/P001")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("/contacts/prisoner/P001")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return OK`() {
      stubPrisonSearchWithResponse("P001")
      webTestClient.get()
        .uri("/contacts/prisoner/P001")
        .headers(setAuthorisation(roles = listOf("PRISONER_SEARCH")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("prisonerNumber").isEqualTo("P001")
        .jsonPath("prisonId").isEqualTo("MDI")
    }
  }
}
