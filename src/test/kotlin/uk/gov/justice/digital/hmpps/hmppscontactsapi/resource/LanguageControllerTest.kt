package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Language
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.LanguageService

class LanguageControllerTest {

  private val languageService: LanguageService = mock()
  private val languageController = LanguageController(languageService)

  @Test
  fun `getLanguageById should return language when found`() {
    val languageId = 1L
    val mockLanguage =
      getMockLanguage(languageId)
    whenever(languageService.getLanguageById(languageId)).thenReturn(mockLanguage)

    val response = languageController.getLanguageById(languageId)

    assertThat(response).isEqualTo(mockLanguage)
    verify(languageService).getLanguageById(languageId)
  }

  @Test
  fun `getLanguageById should return 404 when language not found`() {
    val languageId = 1L
    whenever(languageService.getLanguageById(languageId)).thenReturn(null)

    val response = languageController.getLanguageById(languageId)

    assertNull(response)
    verify(languageService).getLanguageById(languageId)
  }

  @Test
  fun `getAllCountries should return list of countries`() {
    val mockCountries = listOf(
      getMockLanguage(1L),
      getMockLanguage(2L),
    )
    whenever(languageService.getAllCountries()).thenReturn(mockCountries)

    val response = languageController.getAllCountries()

    assertThat(response).isEqualTo(mockCountries)
    verify(languageService).getAllCountries()
  }

  @Test
  fun `getLanguageByNomisCode should return language when found`() {
    val nomisCode = "GB"
    val mockLanguage =
      getMockLanguage(1L)
    whenever(languageService.getLanguageByNomisCode(nomisCode)).thenReturn(mockLanguage)

    val response = languageController.getLanguageByNomisCode(nomisCode)

    assertThat(response).isEqualTo(mockLanguage)
    verify(languageService).getLanguageByNomisCode(nomisCode)
  }

  @Test
  fun `getLanguageByIsoAlpha2 should return language when found`() {
    val isoAlpha2 = "GB"
    val mockLanguage =
      getMockLanguage(1L)
    whenever(languageService.getLanguageByIsoAlpha2(isoAlpha2)).thenReturn(mockLanguage)

    val response = languageController.getLanguageByIsoAlpha2(isoAlpha2)

    assertThat(response).isEqualTo(mockLanguage)
    verify(languageService).getLanguageByIsoAlpha2(isoAlpha2)
  }

  @Test
  fun `getLanguageByIsoAlpha3 should return language when found`() {
    val isoAlpha3 = "GBR"
    val mockLanguage =
      getMockLanguage(1L)
    whenever(languageService.getLanguageByIsoAlpha3(isoAlpha3)).thenReturn(mockLanguage)

    val response = languageController.getLanguageByIsoAlpha3(isoAlpha3)

    assertThat(response).isEqualTo(mockLanguage)
    verify(languageService).getLanguageByIsoAlpha3(isoAlpha3)
  }

  private fun getMockLanguage(languageId: Long) = Language(
    languageId = languageId,
    nomisCode = "GB",
    nomisDescription = "United Kingdom",
    isoAlpha2 = "GB",
    isoAlpha3 = "GBR",
    displaySequence = 99,
    isoLanguageDesc = "United Kingdom",
  )
}
