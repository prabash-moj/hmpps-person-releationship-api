package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Country
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.CountryService

class CountryControllerTest {

  private val countryService: CountryService = mock()
  private val countryController = CountryController(countryService)

  @Test
  fun `getCountryById should return country when found`() {
    val countryId = 1L
    val mockCountry =
      getMockCountry(countryId)
    whenever(countryService.getCountryById(countryId)).thenReturn(mockCountry)

    val response = countryController.getCountryById(countryId)

    assertThat(response).isEqualTo(mockCountry)
    verify(countryService).getCountryById(countryId)
  }

  @Test
  fun `getCountryById should return 404 when country not found`() {
    val countryId = 1L
    whenever(countryService.getCountryById(countryId)).thenReturn(null)

    val response = countryController.getCountryById(countryId)

    assertNull(response)
    verify(countryService).getCountryById(countryId)
  }

  @Test
  fun `getAllCountries should return list of countries`() {
    val mockCountries = listOf(
      getMockCountry(1L),
      getMockCountry(2L),
    )
    whenever(countryService.getAllCountries()).thenReturn(mockCountries)

    val response = countryController.getAllCountries()

    assertThat(response).isEqualTo(mockCountries)
    verify(countryService).getAllCountries()
  }

  @Test
  fun `getCountryByNomisCode should return country when found`() {
    val nomisCode = "GB"
    val mockCountry =
      getMockCountry(1L)
    whenever(countryService.getCountryByNomisCode(nomisCode)).thenReturn(mockCountry)

    val response = countryController.getCountryByNomisCode(nomisCode)

    assertThat(response).isEqualTo(mockCountry)
    verify(countryService).getCountryByNomisCode(nomisCode)
  }

  @Test
  fun `getCountryByIsoAlpha2 should return country when found`() {
    val isoAlpha2 = "GB"
    val mockCountry =
      getMockCountry(1L)
    whenever(countryService.getCountryByIsoAlpha2(isoAlpha2)).thenReturn(mockCountry)

    val response = countryController.getCountryByIsoAlpha2(isoAlpha2)

    assertThat(response).isEqualTo(mockCountry)
    verify(countryService).getCountryByIsoAlpha2(isoAlpha2)
  }

  @Test
  fun `getCountryByIsoAlpha3 should return country when found`() {
    val isoAlpha3 = "GBR"
    val mockCountry =
      getMockCountry(1L)
    whenever(countryService.getCountryByIsoAlpha3(isoAlpha3)).thenReturn(mockCountry)

    val response = countryController.getCountryByIsoAlpha3(isoAlpha3)

    assertThat(response).isEqualTo(mockCountry)
    verify(countryService).getCountryByIsoAlpha3(isoAlpha3)
  }

  private fun getMockCountry(countryId: Long) = Country(
    countryId = countryId,
    nomisCode = "GB",
    nomisDescription = "United Kingdom",
    isoAlpha2 = "GB",
    isoAlpha3 = "GBR",
    isoNumeric = 99,
    displaySequence = 99,
    isoCountryDesc = "United Kingdom",
  )
}
