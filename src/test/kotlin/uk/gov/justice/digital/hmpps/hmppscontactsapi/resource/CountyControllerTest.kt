package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.County
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.CountyService

class CountyControllerTest {

  private val countyService: CountyService = mock()
  private val countyController = CountyController(countyService)

  @Test
  fun `getCountyById should return county when found`() {
    val countyId = 1L
    val mockCounty =
      getCountry(countyId)
    `when`(countyService.getCountyById(countyId)).thenReturn(mockCounty)

    val response = countyController.getCountyById(countyId)

    assertEquals(mockCounty, response)
    verify(countyService).getCountyById(countyId)
  }

  @Test
  fun `getCountyById should return 404 when county not found`() {
    val countyId = 1L
    `when`(countyService.getCountyById(countyId)).thenReturn(null)

    val response = countyController.getCountyById(countyId)

    assertNull(response)
    verify(countyService).getCountyById(countyId)
  }

  @Test
  fun `getAllCountries should return list of countries`() {
    val mockCountries = listOf(
      getCountry(1L),
      getCountry(2L),
    )
    `when`(countyService.getAllCountries()).thenReturn(mockCountries)

    val response = countyController.getAllCountries()

    assertEquals(mockCountries, response)
    verify(countyService).getAllCountries()
  }

  @Test
  fun `getCountyByNomisCode should return county when found`() {
    val nomisCode = "GB"
    val mockCounty =
      getCountry(1L)
    `when`(countyService.getCountyByNomisCode(nomisCode)).thenReturn(mockCounty)

    val response = countyController.getCountyByNomisCode(nomisCode)

    assertEquals(mockCounty, response)
    verify(countyService).getCountyByNomisCode(nomisCode)
  }

  private fun getCountry(countyId: Long) =
    County(countyId = countyId, nomisCode = "GB", nomisDescription = "United Kingdom", displaySequence = 99)
}
