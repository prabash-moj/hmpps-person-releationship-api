package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase

class OrganisationIntegrationTest : H2IntegrationTestBase() {

  companion object {
    private const val GET_ORGANISATION_DATA = "/organisation"
  }

  @Nested
  inner class GetOrganisationByOrganisationId {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("$GET_ORGANISATION_DATA/001")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("$GET_ORGANISATION_DATA/001")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("$GET_ORGANISATION_DATA/001")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @ParameterizedTest
    @ValueSource(strings = ["ROLE_CONTACTS_ADMIN", "ROLE_CONTACTS__R", "ROLE_CONTACTS__RW"])
    fun `should return not found if no organisation found`(role: String) {
      webTestClient.get()
        .uri("$GET_ORGANISATION_DATA/9999")
        .headers(setAuthorisation(roles = listOf(role)))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `should return organisation data when using a valid organisation id`() {
      val organisation = testAPIClient.getOrganisation(9900000L)

      with(organisation) {
        assertThat(organisationId).isEqualTo(9900000L)
        assertThat(organisationName).isEqualTo("Name")
        assertThat(programmeNumber).isEqualTo("P1")
        assertThat(vatNumber).isEqualTo("V1")
        assertThat(caseloadId).isEqualTo("C1")
        assertThat(comments).isEqualTo("C2")
        assertThat(active).isFalse()
        assertThat(deactivatedDate).isNotNull()
        assertThat(createdBy).isEqualTo("Created by")
        assertThat(createdTime).isNotNull()
        assertThat(updatedBy).isEqualTo("U1")
        assertThat(updatedTime).isNotNull()
      }
    }
  }
}
