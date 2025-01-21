package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.PostgresIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Country

class CountryIntegrationTest : PostgresIntegrationTestBase() {

  companion object {
    private const val GET_COUNTRY_REFERENCE_DATA = "/country-reference"
  }

  @Nested
  inner class GetCountryByCountryId {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/country-reference/001")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("$GET_COUNTRY_REFERENCE_DATA/001")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("$GET_COUNTRY_REFERENCE_DATA/001")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return not found if no country found`() {
      webTestClient.get()
        .uri("$GET_COUNTRY_REFERENCE_DATA/999")
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @ParameterizedTest
    @ValueSource(strings = ["ROLE_CONTACTS_ADMIN", "ROLE_CONTACTS__R", "ROLE_CONTACTS__RW"])
    fun `should return country reference data when using the id`(role: String) {
      val countryReferences = webTestClient.getCountryReferenceData(
        "$GET_COUNTRY_REFERENCE_DATA/264",
        role,
      )

      assertThat(countryReferences).extracting("nomisDescription").contains("Yugoslavia")
      assertThat(countryReferences).extracting("nomisCode").contains("YU")
      assertThat(countryReferences).hasSize(1)
    }

    private fun WebTestClient.getCountryReferenceData(url: String, role: String): MutableList<Country> =
      get()
        .uri(url)
        .headers(setAuthorisation(roles = listOf(role)))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(Country::class.java)
        .returnResult().responseBody!!
  }

  @Nested
  inner class GetCountryByNomisCode {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/country-reference/nomis-code/YU")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("$GET_COUNTRY_REFERENCE_DATA/nomis-code/YU")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("$GET_COUNTRY_REFERENCE_DATA/nomis-code/YU")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return not found if no country found`() {
      webTestClient.get()
        .uri("$GET_COUNTRY_REFERENCE_DATA/nomis-code/YY")
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @ParameterizedTest
    @ValueSource(strings = ["ROLE_CONTACTS_ADMIN", "ROLE_CONTACTS__R", "ROLE_CONTACTS__RW"])
    fun `should return country reference data when using the nomis code`(role: String) {
      val countryReferences = webTestClient.getCountryReferenceData(
        "$GET_COUNTRY_REFERENCE_DATA/nomis-code/YU",
        role,
      )

      assertThat(countryReferences).extracting("nomisDescription").contains("Yugoslavia")
      assertThat(countryReferences).extracting("nomisCode").contains("YU")
      assertThat(countryReferences).hasSize(1)
    }

    private fun WebTestClient.getCountryReferenceData(url: String, role: String): MutableList<Country> =
      get()
        .uri(url)
        .headers(setAuthorisation(roles = listOf(role)))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(Country::class.java)
        .returnResult().responseBody!!
  }

  @Nested
  inner class GetCountryByIsoAlpha2 {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/country-reference/iso-alpha2/b6")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("$GET_COUNTRY_REFERENCE_DATA/iso-alpha2/b6")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("$GET_COUNTRY_REFERENCE_DATA/iso-alpha2/b6")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return not found if no country found`() {
      webTestClient.get()
        .uri("$GET_COUNTRY_REFERENCE_DATA/iso-alpha2/z6")
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @ParameterizedTest
    @ValueSource(strings = ["ROLE_CONTACTS_ADMIN", "ROLE_CONTACTS__R", "ROLE_CONTACTS__RW"])
    fun `should return country reference data when using the iso alpha code 2`(role: String) {
      val countryReferences = webTestClient.getCountryReferenceData(
        "$GET_COUNTRY_REFERENCE_DATA/iso-alpha2/b6",
        role,
      )

      assertThat(countryReferences).extracting("nomisDescription").contains("Yugoslavia")
      assertThat(countryReferences).extracting("nomisCode").contains("YU")
      assertThat(countryReferences).hasSize(1)
    }

    private fun WebTestClient.getCountryReferenceData(url: String, role: String): MutableList<Country> =
      get()
        .uri(url)
        .headers(setAuthorisation(roles = listOf(role)))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(Country::class.java)
        .returnResult().responseBody!!
  }

  @Nested
  inner class GetCountryByIsoAlpha3 {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/country-reference/iso-alpha3/bn6")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("$GET_COUNTRY_REFERENCE_DATA/iso-alpha3/bn6")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("$GET_COUNTRY_REFERENCE_DATA/iso-alpha3/bn6")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return not found if no country found`() {
      webTestClient.get()
        .uri("$GET_COUNTRY_REFERENCE_DATA/iso-alpha3/z6")
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @ParameterizedTest
    @ValueSource(strings = ["ROLE_CONTACTS_ADMIN", "ROLE_CONTACTS__R", "ROLE_CONTACTS__RW"])
    fun `should return country reference data when using the iso alpha code 3`(role: String) {
      val countryReferences = webTestClient.getCountryReferenceData(
        "$GET_COUNTRY_REFERENCE_DATA/iso-alpha3/bn6",
        role,
      )

      assertThat(countryReferences).extracting("nomisDescription").contains("Yugoslavia")
      assertThat(countryReferences).extracting("nomisCode").contains("YU")
      assertThat(countryReferences).hasSize(1)
    }

    private fun WebTestClient.getCountryReferenceData(url: String, role: String): MutableList<Country> =
      get()
        .uri(url)
        .headers(setAuthorisation(roles = listOf(role)))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(Country::class.java)
        .returnResult().responseBody!!
  }

  @Nested
  inner class GetAllCountries {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_COUNTRY_REFERENCE_DATA)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_COUNTRY_REFERENCE_DATA)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_COUNTRY_REFERENCE_DATA)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return not found if no country found`() {
      webTestClient.get()
        .uri("$GET_COUNTRY_REFERENCE_DATA/10001")
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @ParameterizedTest
    @ValueSource(strings = ["ROLE_CONTACTS_ADMIN", "ROLE_CONTACTS__R", "ROLE_CONTACTS__RW"])
    fun `should return country reference data when get all countries`(role: String) {
      val countryReferences = webTestClient.getCountryReferenceData(GET_COUNTRY_REFERENCE_DATA, role)

      val country = Country(
        countryId = 1L,
        nomisCode = "ZWE",
        nomisDescription = "Zimbabwe",
        isoNumeric = 716,
        isoAlpha2 = "ZW",
        isoAlpha3 = "ZWE",
        isoCountryDesc = "Zimbabwe",
        displaySequence = 99,
      )
      assertThat(countryReferences.contains(country))
      assertThat(countryReferences).hasSizeGreaterThan(10)
    }

    private fun WebTestClient.getCountryReferenceData(url: String, role: String): MutableList<Country> =
      get()
        .uri(url)
        .headers(setAuthorisation(roles = listOf(role)))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(Country::class.java)
        .returnResult().responseBody!!
  }
}
