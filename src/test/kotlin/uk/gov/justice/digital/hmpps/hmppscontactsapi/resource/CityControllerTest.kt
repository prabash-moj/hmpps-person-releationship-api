package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.City
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.CityService

class CityControllerTest {

  private val cityService: CityService = mock()
  private val cityController = CityController(cityService)

  @Test
  fun `getCityById should return city when found`() {
    val cityId = 1L
    val mockCity =
      getMockCity(1L)
    whenever(cityService.getCityById(cityId)).thenReturn(mockCity)

    val response: City = cityController.getCityById(cityId)

    assertThat(response).isEqualTo(mockCity)
    verify(cityService).getCityById(cityId)
  }

  @Test
  fun `getCityById should return 404 when city not found`() {
    val cityId = 1L
    whenever(cityService.getCityById(cityId)).thenReturn(null)

    val response: City = cityController.getCityById(cityId)

    assertNull(response)
    verify(cityService).getCityById(cityId)
  }

  @Test
  fun `getAllCountries should return list of countries`() {
    val mockCountries = listOf(
      getMockCity(1L),
      getMockCity(2L),
    )
    whenever(cityService.getAllCountries()).thenReturn(mockCountries)

    val response: List<City> = cityController.getAllCountries()

    assertThat(response).isEqualTo(mockCountries)
  }

  @Test
  fun `getCityByNomisCode should return city when found`() {
    val nomisCode = "GB"
    val mockCity =
      getMockCity(1L)
    whenever(cityService.getCityByNomisCode(nomisCode)).thenReturn(mockCity)

    val response: City = cityController.getCityByNomisCode(nomisCode)

    assertThat(response).isEqualTo(mockCity)
    verify(cityService).getCityByNomisCode(nomisCode)
  }

  private fun getMockCity(cityId: Long) =
    City(cityId = cityId, nomisCode = "GB", displaySequence = 99, nomisDescription = "United Kingdom")
}
