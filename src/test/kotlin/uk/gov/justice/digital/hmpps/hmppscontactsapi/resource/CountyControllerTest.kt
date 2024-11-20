package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
    whenever(countyService.getCountyById(countyId)).thenReturn(mockCounty)

    val response = countyController.getCountyById(countyId)

    assertThat(response).isEqualTo(mockCounty)
    verify(countyService).getCountyById(countyId)
  }

  @Test
  fun `getCountyById should return 404 when county not found`() {
    val countyId = 1L
    whenever(countyService.getCountyById(countyId)).thenReturn(null)

    val response = countyController.getCountyById(countyId)

    assertNull(response)
    verify(countyService).getCountyById(countyId)
  }

  @Test
  fun `getAllCounties should return list of counties`() {
    val mockCounties = listOf(
      getCountry(1L),
      getCountry(2L),
    )
    whenever(countyService.getAllCounties()).thenReturn(mockCounties)

    val response = countyController.getAllCounties()

    assertThat(response).isEqualTo(mockCounties)
    verify(countyService).getAllCounties()
  }

  @Test
  fun `getCountyByNomisCode should return county when found`() {
    val nomisCode = "GB"
    val mockCounty =
      getCountry(1L)
    whenever(countyService.getCountyByNomisCode(nomisCode)).thenReturn(mockCounty)

    val response = countyController.getCountyByNomisCode(nomisCode)

    assertThat(response).isEqualTo(mockCounty)
    verify(countyService).getCountyByNomisCode(nomisCode)
  }

  private fun getCountry(countyId: Long) =
    County(countyId = countyId, nomisCode = "GB", nomisDescription = "United Kingdom", displaySequence = 99)
}
