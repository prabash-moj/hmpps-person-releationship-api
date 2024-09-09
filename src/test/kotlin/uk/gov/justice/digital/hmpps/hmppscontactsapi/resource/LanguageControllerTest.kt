package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify
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
    `when`(languageService.getLanguageById(languageId)).thenReturn(mockLanguage)

    val response = languageController.getLanguageById(languageId)

    assertEquals(mockLanguage, response)
    verify(languageService).getLanguageById(languageId)
  }

  @Test
  fun `getLanguageById should return 404 when language not found`() {
    val languageId = 1L
    `when`(languageService.getLanguageById(languageId)).thenReturn(null)

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
    `when`(languageService.getAllCountries()).thenReturn(mockCountries)

    val response = languageController.getAllCountries()

    assertEquals(mockCountries, response)
    verify(languageService).getAllCountries()
  }

  @Test
  fun `getLanguageByNomisCode should return language when found`() {
    val nomisCode = "GB"
    val mockLanguage =
      getMockLanguage(1L)
    `when`(languageService.getLanguageByNomisCode(nomisCode)).thenReturn(mockLanguage)

    val response = languageController.getLanguageByNomisCode(nomisCode)

    assertEquals(mockLanguage, response)
    verify(languageService).getLanguageByNomisCode(nomisCode)
  }

  @Test
  fun `getLanguageByIsoAlpha2 should return language when found`() {
    val isoAlpha2 = "GB"
    val mockLanguage =
      getMockLanguage(1L)
    `when`(languageService.getLanguageByIsoAlpha2(isoAlpha2)).thenReturn(mockLanguage)

    val response = languageController.getLanguageByIsoAlpha2(isoAlpha2)

    assertEquals(mockLanguage, response)
    verify(languageService).getLanguageByIsoAlpha2(isoAlpha2)
  }

  @Test
  fun `getLanguageByIsoAlpha3 should return language when found`() {
    val isoAlpha3 = "GBR"
    val mockLanguage =
      getMockLanguage(1L)
    `when`(languageService.getLanguageByIsoAlpha3(isoAlpha3)).thenReturn(mockLanguage)

    val response = languageController.getLanguageByIsoAlpha3(isoAlpha3)

    assertEquals(mockLanguage, response)
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
