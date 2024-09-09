package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.County

class CountyIntegrationTest : IntegrationTestBase() {

  companion object {
    private const val GET_COUNTY_REFERENCE_DATA = "/county-reference"
  }

  @Nested
  inner class GetCountyByCountyId {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/county-reference/001")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("$GET_COUNTY_REFERENCE_DATA/001")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("$GET_COUNTY_REFERENCE_DATA/001")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return not found if no county found`() {
      webTestClient.get()
        .uri("$GET_COUNTY_REFERENCE_DATA/999")
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `should return county reference data when using the id`() {
      val countyReferences = webTestClient.getCountyReferenceData("$GET_COUNTY_REFERENCE_DATA/145")

      assertThat(countyReferences).extracting("nomisDescription").contains("Middlesbrough")
      assertThat(countyReferences).extracting("nomisCode").contains("MIDDLESBOR")
      assertThat(countyReferences).hasSize(1)
    }

    private fun WebTestClient.getCountyReferenceData(url: String): MutableList<County> =
      get()
        .uri(url)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(County::class.java)
        .returnResult().responseBody!!
  }

  @Nested
  inner class GetCountyByNomisCode {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/county-reference/nomis-code/YU")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("$GET_COUNTY_REFERENCE_DATA/nomis-code/YU")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("$GET_COUNTY_REFERENCE_DATA/nomis-code/YU")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return not found if no county found`() {
      webTestClient.get()
        .uri("$GET_COUNTY_REFERENCE_DATA/nomis-code/YY")
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `should return county reference data when using the nomis code`() {
      val countyReferences = webTestClient.getCountyReferenceData("$GET_COUNTY_REFERENCE_DATA/nomis-code/MIDDLESBOR")

      assertThat(countyReferences).extracting("nomisDescription").contains("Middlesbrough")
      assertThat(countyReferences).extracting("nomisCode").contains("MIDDLESBOR")
      assertThat(countyReferences).hasSize(1)
    }

    private fun WebTestClient.getCountyReferenceData(url: String): MutableList<County> =
      get()
        .uri(url)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(County::class.java)
        .returnResult().responseBody!!
  }

  @Nested
  inner class GetAllCountries {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_COUNTY_REFERENCE_DATA)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_COUNTY_REFERENCE_DATA)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_COUNTY_REFERENCE_DATA)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return not found if no county found`() {
      webTestClient.get()
        .uri("$GET_COUNTY_REFERENCE_DATA/999")
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `should return county reference data when get all counties`() {
      val countyReferences = webTestClient.getCountyReferenceData(GET_COUNTY_REFERENCE_DATA)

      val county = County(
        countyId = 1L,
        nomisCode = "ZWE",
        nomisDescription = "Zimbabwe",
        displaySequence = 99,
      )
      assertThat(countyReferences.contains(county))
      assertThat(countyReferences).hasSizeGreaterThan(10)
    }

    private fun WebTestClient.getCountyReferenceData(url: String): MutableList<County> =
      get()
        .uri(url)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(County::class.java)
        .returnResult().responseBody!!
  }
}
