package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.CountryEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.helper.isEqualTo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.CountryRepository
import java.util.*

class CountryServiceTest {

  private lateinit var countryService: CountryService

  @Mock
  private lateinit var countryRepository: CountryRepository

  @BeforeEach
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    countryService = CountryService(countryRepository)
  }

  @Nested
  inner class GetCountryByCountryId {

    @Test
    fun `should return a country when valid id is provided`() {
      // Given
      val countryId = 1L
      val country = CountryEntity(
        countryId = countryId,
        nomisCode = "ABC",
        nomisDescription = "Test Country",
        isoAlpha2 = "TC",
        isoAlpha3 = "TCO",
        isoCountryDesc = "Test Country",
        displaySequence = 123,
        isoNumeric = 111,
      )
      `when`(countryRepository.findByCountryId(countryId)).thenReturn(Optional.of(country))

      // When
      val result = countryService.getCountryById(countryId)

      // Then
      assertNotNull(result)
      assertEquals("ABC", result.nomisCode)
      assertEquals("Test Country", result.nomisDescription)
    }

    @Test
    fun `should return not found exception when country id does not exist`() {
      // Given
      val countryId = 1L
      `when`(countryRepository.findByCountryId(countryId)).thenReturn(Optional.empty())

      // When

      val exception = assertThrows<EntityNotFoundException> {
        countryService.getCountryById(countryId)
      }

      // Then
      exception.message isEqualTo "Country with id 1 not found"
    }
  }

  @Nested
  inner class GetAllCountries {

    @Test
    fun `should return a list of all countries`() {
      // Given
      val countries = listOf(
        CountryEntity(
          countryId = 1L,
          nomisCode = "USA",
          isoAlpha2 = "US",
          nomisDescription = "Test Country",
          isoAlpha3 = "TCO",
          isoCountryDesc = "Test Country",
          displaySequence = 123,
          isoNumeric = 111,
        ),
        CountryEntity(
          countryId = 2L,
          nomisCode = "CAN",
          isoAlpha2 = "CA",
          nomisDescription = "Test Country",
          isoAlpha3 = "TCO",
          isoCountryDesc = "Test Country",
          displaySequence = 123,
          isoNumeric = 111,
        ),
      )
      `when`(countryRepository.findAll()).thenReturn(countries)

      // When
      val result = countryService.getAllCountries()

      // Then
      assertEquals(2, result.size)
      assertEquals("USA", result[0].nomisCode)
      assertEquals("CAN", result[1].nomisCode)
    }
  }

  @Nested
  inner class GetCountrByISOAlpha2Code {

    @Test
    fun `should return a country by ISO Alpha2 code`() {
      // Given
      val isoAlpha2 = "US"
      val country = CountryEntity(
        countryId = 1L,
        nomisCode = "USA",
        isoAlpha2 = isoAlpha2,
        isoAlpha3 = "USA",
        displaySequence = 123,
        isoNumeric = 111,
        nomisDescription = "Test Country",
        isoCountryDesc = "Test Country",
      )
      `when`(countryRepository.findByIsoAlpha2(isoAlpha2)).thenReturn(Optional.of(country))

      // When
      val result = countryService.getCountryByIsoAlpha2(isoAlpha2)

      // Then
      assertNotNull(result)
      assertEquals("US", result.isoAlpha2)
      assertEquals("USA", result.nomisCode)
    }

    @Test
    fun `should return not found exception when ISO Alpha2 code does not exist`() {
      // Given
      val isoAlpha2 = "XX"
      `when`(countryRepository.findByIsoAlpha2(isoAlpha2)).thenReturn(Optional.empty())

      // When
      val exception = assertThrows<EntityNotFoundException> {
        countryService.getCountryByIsoAlpha2(isoAlpha2)
      }

      // Then
      exception.message isEqualTo "Country with iso alpha code 2 XX not found"
    }
  }

  @Nested
  inner class GetCountrByISOAlpha3Code {

    @Test
    fun `should return a country by ISO Alpha3 code`() {
      // Given
      val isoAlpha3 = "US"
      val country = CountryEntity(
        countryId = 1L,
        nomisCode = "USA",
        isoAlpha2 = "US",
        isoAlpha3 = isoAlpha3,
        displaySequence = 123,
        isoNumeric = 111,
        nomisDescription = "Test Country",
        isoCountryDesc = "Test Country",
      )
      `when`(countryRepository.findByIsoAlpha3(isoAlpha3)).thenReturn(Optional.of(country))

      // When
      val result = countryService.getCountryByIsoAlpha3(isoAlpha3)

      // Then
      assertNotNull(result)
      assertEquals("US", result.isoAlpha3)
      assertEquals("USA", result.nomisCode)
    }

    @Test
    fun `should return not found exception when ISO Alpha3 code does not exist`() {
      // Given
      val isoAlpha3 = "XXX"
      `when`(countryRepository.findByIsoAlpha3(isoAlpha3)).thenReturn(Optional.empty())

      // When
      val exception = assertThrows<EntityNotFoundException> {
        countryService.getCountryByIsoAlpha3(isoAlpha3)
      }

      // Then
      exception.message isEqualTo "Country with iso alpha code 3 XXX not found"
    }
  }

  @Nested
  inner class GetCountryByNomisCode {
    @Test
    fun `should return a country by nomis code`() {
      // Given
      val nomisCode = "USA"
      val country = CountryEntity(
        countryId = 1L,
        nomisCode = nomisCode,
        isoAlpha2 = "US",
        isoAlpha3 = "USA",
        displaySequence = 123,
        isoNumeric = 111,
        nomisDescription = "Test Country",
        isoCountryDesc = "Test Country",
      )
      `when`(countryRepository.findByNomisCode(nomisCode)).thenReturn(Optional.of(country))

      // When
      val result = countryService.getCountryByNomisCode(nomisCode)

      // Then
      assertNotNull(result)
      assertEquals("USA", result.nomisCode)
      assertEquals("US", result.isoAlpha2)
      assertEquals("USA", result.isoAlpha3)
    }

    @Test
    fun `should return not found exception when nomis code does not exist`() {
      // Given
      val nomisCode = "XXX"
      `when`(countryRepository.findByNomisCode(nomisCode)).thenReturn(Optional.empty())

      // When

      val exception = assertThrows<EntityNotFoundException> {
        countryService.getCountryByIsoAlpha2(nomisCode)
      }

      // Then
      exception.message isEqualTo "Country with iso alpha code 2 XXX not found"
    }
  }
}
