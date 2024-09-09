package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify
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
    `when`(countryService.getCountryById(countryId)).thenReturn(mockCountry)

    val response = countryController.getCountryById(countryId)

    assertEquals(mockCountry, response)
    verify(countryService).getCountryById(countryId)
  }

  @Test
  fun `getCountryById should return 404 when country not found`() {
    val countryId = 1L
    `when`(countryService.getCountryById(countryId)).thenReturn(null)

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
    `when`(countryService.getAllCountries()).thenReturn(mockCountries)

    val response = countryController.getAllCountries()

    assertEquals(mockCountries, response)
    verify(countryService).getAllCountries()
  }

  @Test
  fun `getCountryByNomisCode should return country when found`() {
    val nomisCode = "GB"
    val mockCountry =
      getMockCountry(1L)
    `when`(countryService.getCountryByNomisCode(nomisCode)).thenReturn(mockCountry)

    val response = countryController.getCountryByNomisCode(nomisCode)

    assertEquals(mockCountry, response)
    verify(countryService).getCountryByNomisCode(nomisCode)
  }

  @Test
  fun `getCountryByIsoAlpha2 should return country when found`() {
    val isoAlpha2 = "GB"
    val mockCountry =
      getMockCountry(1L)
    `when`(countryService.getCountryByIsoAlpha2(isoAlpha2)).thenReturn(mockCountry)

    val response = countryController.getCountryByIsoAlpha2(isoAlpha2)

    assertEquals(mockCountry, response)
    verify(countryService).getCountryByIsoAlpha2(isoAlpha2)
  }

  @Test
  fun `getCountryByIsoAlpha3 should return country when found`() {
    val isoAlpha3 = "GBR"
    val mockCountry =
      getMockCountry(1L)
    `when`(countryService.getCountryByIsoAlpha3(isoAlpha3)).thenReturn(mockCountry)

    val response = countryController.getCountryByIsoAlpha3(isoAlpha3)

    assertEquals(mockCountry, response)
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
