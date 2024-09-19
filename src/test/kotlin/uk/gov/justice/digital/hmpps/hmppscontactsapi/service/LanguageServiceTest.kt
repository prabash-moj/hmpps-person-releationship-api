package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.LanguageEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.helper.isEqualTo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.LanguageRepository
import java.util.*

class LanguageServiceTest {

  private lateinit var languageService: LanguageService

  @Mock
  private lateinit var languageRepository: LanguageRepository

  @BeforeEach
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    languageService = LanguageService(languageRepository)
  }

  @Nested
  inner class GetLanguageByLanguageId {

    @Test
    fun `should return a language when valid id is provided`() {
      // Given
      val languageId = 1L
      val language = LanguageEntity(
        languageId = languageId,
        nomisCode = "ABC",
        nomisDescription = "Test Language",
        isoAlpha2 = "TC",
        isoAlpha3 = "TCO",
        isoLanguageDesc = "Test Language",
        displaySequence = 123,
      )
      whenever(languageRepository.findByLanguageId(languageId)).thenReturn(Optional.of(language))

      // When
      val result = languageService.getLanguageById(languageId)

      // Then
      assertNotNull(result)
      assertThat(result.nomisCode).isEqualTo("ABC")
      assertThat(result.nomisDescription).isEqualTo("Test Language")
    }

    @Test
    fun `should return not found exception when language id does not exist`() {
      // Given
      val languageId = 1L
      whenever(languageRepository.findByLanguageId(languageId)).thenReturn(Optional.empty())

      // When
      val exception = assertThrows<EntityNotFoundException> {
        languageService.getLanguageById(languageId)
      }

      // Then
      exception.message isEqualTo "Language with id 1 not found"
    }
  }

  @Nested
  inner class GetAllCountries {

    @Test
    fun `should return a list of all countries`() {
      // Given
      val countries = listOf(
        LanguageEntity(
          languageId = 1L,
          nomisCode = "USA",
          isoAlpha2 = "US",
          nomisDescription = "Test Language",
          isoAlpha3 = "TCO",
          isoLanguageDesc = "Test Language",
          displaySequence = 123,
        ),
        LanguageEntity(
          languageId = 2L,
          nomisCode = "CAN",
          isoAlpha2 = "CA",
          nomisDescription = "Test Language",
          isoAlpha3 = "TCO",
          isoLanguageDesc = "Test Language",
          displaySequence = 123,
        ),
      )
      whenever(languageRepository.findAll()).thenReturn(countries)

      // When
      val result = languageService.getAllCountries()

      // Then
      assertThat(result.size).isEqualTo(2)
      assertThat(result[0].nomisCode).isEqualTo("USA")
      assertThat(result[1].nomisCode).isEqualTo("CAN")
    }
  }

  @Nested
  inner class GetCountryByISOAlpha2Code {

    @Test
    fun `should return a language by ISO Alpha2 code`() {
      // Given
      val isoAlpha2 = "US"
      val language = LanguageEntity(
        languageId = 1L,
        nomisCode = "USA",
        isoAlpha2 = isoAlpha2,
        isoAlpha3 = "USA",
        displaySequence = 123,
        isoLanguageDesc = "Test Language",
        nomisDescription = "Test Language",
      )
      whenever(languageRepository.findByIsoAlpha2(isoAlpha2)).thenReturn(Optional.of(language))

      // When
      val result = languageService.getLanguageByIsoAlpha2(isoAlpha2)

      // Then
      assertNotNull(result)
      assertThat(result.isoAlpha2).isEqualTo("US")
      assertThat(result.nomisCode).isEqualTo("USA")
    }

    @Test
    fun `should return not found exception when ISO Alpha2 code does not exist`() {
      // Given
      val isoAlpha2 = "XX"
      whenever(languageRepository.findByIsoAlpha2(isoAlpha2)).thenReturn(Optional.empty())

      // When

      val exception = assertThrows<EntityNotFoundException> {
        languageService.getLanguageByIsoAlpha2(isoAlpha2)
      }

      // Then
      exception.message isEqualTo "Language with alpha code 2 XX not found"
    }
  }

  @Nested
  inner class GetCountryByISOAlpha3Code {

    @Test
    fun `should return a language by ISO Alpha3 code`() {
      // Given
      val isoAlpha3 = "US"
      val language = LanguageEntity(
        languageId = 1L,
        nomisCode = "USA",
        isoAlpha2 = "US",
        isoAlpha3 = isoAlpha3,
        displaySequence = 123,
        isoLanguageDesc = "Test Language",
        nomisDescription = "Test Language",
      )
      whenever(languageRepository.findByIsoAlpha3(isoAlpha3)).thenReturn(Optional.of(language))

      // When
      val result = languageService.getLanguageByIsoAlpha3(isoAlpha3)

      // Then
      assertNotNull(result)
      assertThat(result.isoAlpha3).isEqualTo("US")
      assertThat(result.nomisCode).isEqualTo("USA")
    }

    @Test
    fun `should return not found exception when ISO Alpha3 code does not exist`() {
      // Given
      val isoAlpha3 = "XXX"
      whenever(languageRepository.findByIsoAlpha3(isoAlpha3)).thenReturn(Optional.empty())

      // When
      val exception = assertThrows<EntityNotFoundException> {
        languageService.getLanguageByIsoAlpha3(isoAlpha3)
      }

      // Then
      exception.message isEqualTo "Language with alpha code 3 XXX not found"
    }
  }

  @Nested
  inner class GetLanguageByNomisCode {
    @Test
    fun `should return a language by nomis code`() {
      // Given
      val nomisCode = "USA"
      val language = LanguageEntity(
        languageId = 1L,
        nomisCode = nomisCode,
        isoAlpha2 = "US",
        isoAlpha3 = "USA",
        displaySequence = 123,
        isoLanguageDesc = "Test Language",
        nomisDescription = "Test Language",
      )
      whenever(languageRepository.findByNomisCode(nomisCode)).thenReturn(Optional.of(language))

      // When
      val result = languageService.getLanguageByNomisCode(nomisCode)

      // Then
      assertNotNull(result)
      assertThat(result.nomisCode).isEqualTo("USA")
      assertThat(result.isoAlpha2).isEqualTo("US")
      assertThat(result.isoAlpha3).isEqualTo("USA")
    }

    @Test
    fun `should return not found exception when nomis code does not exist`() {
      // Given
      val nomisCode = "XXX"
      whenever(languageRepository.findByNomisCode(nomisCode)).thenReturn(Optional.empty())

      // When
      val exception = assertThrows<EntityNotFoundException> {
        languageService.getLanguageByIsoAlpha2(nomisCode)
      }

      // Then
      exception.message isEqualTo "Language with alpha code 2 XXX not found"
    }
  }
}
