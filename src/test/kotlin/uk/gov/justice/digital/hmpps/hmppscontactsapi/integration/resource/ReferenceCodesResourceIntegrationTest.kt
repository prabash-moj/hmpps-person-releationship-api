package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
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
    referenceCodeRepository.findAllByGroupCodeEquals(groupCode, Sort.unsorted()) hasSize 36

    val listOfCodes = testAPIClient.getReferenceCodes(groupCode)

    assertThat(listOfCodes).hasSize(36)
    assertThat(listOfCodes)
      .extracting("code")
      .containsAll(listOf("MOT", "FA"))
    assertThat(listOfCodes).extracting("code").doesNotContainAnyElementsOf(listOf("WORK", "MOBILE", "HOME"))
  }

  @Test
  fun `should return a list of phone type reference codes`() {
    val groupCode = "PHONE_TYPE"
    referenceCodeRepository.findAllByGroupCodeEquals(groupCode, Sort.unsorted()) hasSize 3

    val listOfCodes = testAPIClient.getReferenceCodes(groupCode)

    assertThat(listOfCodes).extracting("code").containsExactlyInAnyOrder("WORK", "MOBILE", "HOME")
  }

  @Test
  fun `should return a list of domestic status type reference codes`() {
    val groupCode = "DOMESTIC_STS"
    referenceCodeRepository.findAllByGroupCodeEquals(groupCode, Sort.unsorted()) hasSize 7

    val listOfCodes = testAPIClient.getReferenceCodes(groupCode)

    assertThat(listOfCodes).hasSize(7)
    assertThat(listOfCodes)
      .extracting("code")
      .containsAll(listOf("S", "C", "M", "D", "P", "W", "N"))
  }

  @Test
  fun `should be able to sort reference codes`() {
    val groupCode = "DOMESTIC_STS"
    val listOfCodesInDisplayOrder = testAPIClient.getReferenceCodes(groupCode, "displayOrder")
    assertThat(listOfCodesInDisplayOrder)
      .extracting("code")
      .isEqualTo(listOf("S", "C", "M", "D", "P", "W", "N"))

    val listOfCodesInCodeOrder = testAPIClient.getReferenceCodes(groupCode, "code")
    assertThat(listOfCodesInCodeOrder)
      .extracting("code")
      .isEqualTo(listOf("C", "D", "M", "N", "P", "S", "W"))
  }
}
