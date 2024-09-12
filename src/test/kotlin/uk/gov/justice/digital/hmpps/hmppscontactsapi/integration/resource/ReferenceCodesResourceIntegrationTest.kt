package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.helper.hasSize
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ReferenceCodeRepository

class ReferenceCodesResourceIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var referenceCodeRepository: ReferenceCodeRepository

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.get()
      .uri("/reference-codes/group/PHONE_TYPE")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.get()
      .uri("/reference-codes/group/PHONE_TYPE")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    webTestClient.get()
      .uri("/reference-codes/group/PHONE_TYPE")
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return empty list if no matching code found`() {
    assertThat(testAPIClient.getReferenceCodes("FOO")).isEmpty()
  }

  @Test
  fun `should return a list of relationship type reference codes`() {
    val groupCode = "RELATIONSHIP"
    referenceCodeRepository.findAllByGroupCodeEquals(groupCode) hasSize 7

    val listOfCodes = testAPIClient.getReferenceCodes(groupCode)

    assertThat(listOfCodes).hasSize(7)
    assertThat(listOfCodes)
      .extracting("code")
      .containsAll(listOf("MOTHER", "FATHER"))
    assertThat(listOfCodes).extracting("code").doesNotContainAnyElementsOf(listOf("WORK", "MOBILE", "HOME"))
  }

  @Test
  fun `should return a list of phone type reference codes`() {
    val groupCode = "PHONE_TYPE"
    referenceCodeRepository.findAllByGroupCodeEquals(groupCode) hasSize 3

    val listOfCodes = testAPIClient.getReferenceCodes(groupCode)

    assertThat(listOfCodes).extracting("code").containsExactlyInAnyOrder("WORK", "MOBILE", "HOME")
  }
}
