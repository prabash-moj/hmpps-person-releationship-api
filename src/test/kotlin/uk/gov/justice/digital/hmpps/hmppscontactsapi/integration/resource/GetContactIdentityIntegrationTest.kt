package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase

class GetContactIdentityIntegrationTest : H2IntegrationTestBase() {

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.get()
      .uri("/contact/1/identity/1")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.get()
      .uri("/contact/1/identity/1")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    webTestClient.get()
      .uri("/contact/1/identity/1")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should get identity details`() {
    val identity = testAPIClient.getContactIdentity(1, 1)

    with(identity) {
      assertThat(contactIdentityId).isEqualTo(1)
      assertThat(contactId).isEqualTo(1)
      assertThat(identityType).isEqualTo("DRIVING_LIC")
      assertThat(identityTypeDescription).isEqualTo("Driving licence")
      assertThat(identityValue).isEqualTo("LAST-87736799M")
      assertThat(issuingAuthority).isEqualTo("DVLA")
      assertThat(createdBy).isEqualTo("TIM")
    }
  }
}
