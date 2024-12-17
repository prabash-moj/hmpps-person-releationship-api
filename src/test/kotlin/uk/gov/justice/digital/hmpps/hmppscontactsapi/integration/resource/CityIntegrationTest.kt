package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.City

class CityIntegrationTest : H2IntegrationTestBase() {

  companion object {
    private const val GET_CITY_REFERENCE_DATA = "/city-reference"
  }

  @Nested
  inner class GetCityByCityId {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("$GET_CITY_REFERENCE_DATA/001")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("$GET_CITY_REFERENCE_DATA/001")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("$GET_CITY_REFERENCE_DATA/001")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @ParameterizedTest
    @ValueSource(strings = ["ROLE_CONTACTS_ADMIN", "ROLE_CONTACTS__R", "ROLE_CONTACTS__RW"])
    fun `should return not found if no city found`(role: String) {
      webTestClient.get()
        .uri("$GET_CITY_REFERENCE_DATA/9999")
        .headers(setAuthorisation(roles = listOf(role)))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `should return city reference data when using the id`() {
      val cityReferences = webTestClient.getCityReferenceData("$GET_CITY_REFERENCE_DATA/500")

      assertThat(cityReferences).extracting("nomisDescription").contains("Tyn-Y-Gongl")
      assertThat(cityReferences).extracting("nomisCode").contains("12208")
      assertThat(cityReferences).hasSize(1)
    }

    private fun WebTestClient.getCityReferenceData(url: String): MutableList<City> =
      get()
        .uri(url)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(City::class.java)
        .returnResult().responseBody!!
  }

  @Nested
  inner class GetCityByNomisCode {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/city-reference/nomis-code/YU")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("$GET_CITY_REFERENCE_DATA/nomis-code/YU")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("$GET_CITY_REFERENCE_DATA/nomis-code/YU")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return not found if no city found`() {
      webTestClient.get()
        .uri("$GET_CITY_REFERENCE_DATA/nomis-code/YY")
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `should return city reference data when using the nomis code`() {
      val cityReferences = webTestClient.getCityReferenceData("$GET_CITY_REFERENCE_DATA/nomis-code/12208")

      assertThat(cityReferences).extracting("nomisDescription").contains("Tyn-Y-Gongl")
      assertThat(cityReferences).extracting("nomisCode").contains("12208")
      assertThat(cityReferences).hasSize(1)
    }

    private fun WebTestClient.getCityReferenceData(url: String): MutableList<City> =
      get()
        .uri(url)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(City::class.java)
        .returnResult().responseBody!!
  }

  @Nested
  inner class GetAllCities {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_CITY_REFERENCE_DATA)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_CITY_REFERENCE_DATA)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_CITY_REFERENCE_DATA)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return not found if no city found`() {
      webTestClient.get()
        .uri("$GET_CITY_REFERENCE_DATA/10009")
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `should return city reference data when get all cities`() {
      val cityReferences = webTestClient.getCityReferenceData(GET_CITY_REFERENCE_DATA)

      val city = City(
        cityId = 1L,
        nomisCode = "ZWE",
        nomisDescription = "Zimbabwe",
        displaySequence = 99,
      )
      assertThat(cityReferences.contains(city))
      assertThat(cityReferences).hasSizeGreaterThan(10)
    }

    private fun WebTestClient.getCityReferenceData(url: String): MutableList<City> =
      get()
        .uri(url)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(City::class.java)
        .returnResult().responseBody!!
  }
}
