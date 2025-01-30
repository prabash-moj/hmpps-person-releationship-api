package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.PostgresIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.County

class CountyIntegrationTest : PostgresIntegrationTestBase() {

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

    @ParameterizedTest
    @ValueSource(strings = ["ROLE_CONTACTS_ADMIN", "ROLE_CONTACTS__R", "ROLE_CONTACTS__RW"])
    fun `should return county reference data when using the id`(role: String) {
      val countyReferences = webTestClient.getCountyReferenceData(
        "$GET_COUNTY_REFERENCE_DATA/145",
        role,
      )

      assertThat(countyReferences).extracting("nomisDescription").contains("Middlesbrough")
      assertThat(countyReferences).extracting("nomisCode").contains("MIDDLESBOR")
      assertThat(countyReferences).hasSize(1)
    }

    private fun WebTestClient.getCountyReferenceData(url: String, role: String): MutableList<County> = get()
      .uri(url)
      .headers(setAuthorisation(roles = listOf(role)))
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

    @ParameterizedTest
    @ValueSource(strings = ["ROLE_CONTACTS_ADMIN", "ROLE_CONTACTS__R", "ROLE_CONTACTS__RW"])
    fun `should return county reference data when using the nomis code`(role: String) {
      val countyReferences = webTestClient.getCountyReferenceData(
        "$GET_COUNTY_REFERENCE_DATA/nomis-code/MIDDLESBOR",
        role,
      )

      assertThat(countyReferences).extracting("nomisDescription").contains("Middlesbrough")
      assertThat(countyReferences).extracting("nomisCode").contains("MIDDLESBOR")
      assertThat(countyReferences).hasSize(1)
    }

    private fun WebTestClient.getCountyReferenceData(url: String, role: String): MutableList<County> = get()
      .uri(url)
      .headers(setAuthorisation(roles = listOf(role)))
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

    @ParameterizedTest
    @ValueSource(strings = ["ROLE_CONTACTS_ADMIN", "ROLE_CONTACTS__R", "ROLE_CONTACTS__RW"])
    fun `should return county reference data when get all counties`(role: String) {
      val countyReferences = webTestClient.getCountyReferenceData(GET_COUNTY_REFERENCE_DATA, role)

      val county = County(
        countyId = 1L,
        nomisCode = "ZWE",
        nomisDescription = "Zimbabwe",
        displaySequence = 99,
      )
      assertThat(countyReferences.contains(county))
      assertThat(countyReferences).hasSizeGreaterThan(10)
    }

    private fun WebTestClient.getCountyReferenceData(url: String, role: String): MutableList<County> = get()
      .uri(url)
      .headers(setAuthorisation(roles = listOf(role)))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(County::class.java)
      .returnResult().responseBody!!
  }
}
