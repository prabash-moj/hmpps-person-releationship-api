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
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.CityEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.helper.isEqualTo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.CityRepository
import java.util.*

class CityServiceTest {

  private lateinit var cityService: CityService

  @Mock
  private lateinit var cityRepository: CityRepository

  @BeforeEach
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    cityService = CityService(cityRepository)
  }

  @Nested
  inner class GetCityByCityId {

    @Test
    fun `should return a city when valid id is provided`() {
      // Given
      val cityId = 1L
      val city = CityEntity(
        cityId = cityId,
        nomisCode = "ABC",
        nomisDescription = "Test City",
        displaySequence = 123,
      )
      whenever(cityRepository.findByCityId(cityId)).thenReturn(Optional.of(city))

      // When
      val result = cityService.getCityById(cityId)

      // Then
      assertNotNull(result)
      assertThat(result.nomisCode).isEqualTo("ABC")
      assertThat(result.nomisDescription).isEqualTo("Test City")
    }

    @Test
    fun `should return null when city id does not exist`() {
      // Given
      val cityId = 1009L
      whenever(cityRepository.findByCityId(cityId)).thenReturn(Optional.empty())

      // When
      val exception = assertThrows<EntityNotFoundException> {
        cityService.getCityById(cityId)
      }

      // Then
      exception.message isEqualTo "City with id 1009 not found"
    }
  }

  @Nested
  inner class GetAllCountries {

    @Test
    fun `should return a list of all countries`() {
      // Given
      val countries = listOf(
        CityEntity(
          cityId = 1L,
          nomisCode = "USA",
          nomisDescription = "Test City",
          displaySequence = 123,
        ),
        CityEntity(
          cityId = 2L,
          nomisCode = "CAN",
          nomisDescription = "Test City",
          displaySequence = 123,
        ),
      )
      whenever(cityRepository.findAll()).thenReturn(countries)

      // When
      val result = cityService.getAllCountries()

      // Then
      assertThat(result.size).isEqualTo(2)
      assertThat(result[0].nomisCode).isEqualTo("USA")
      assertThat(result[1].nomisCode).isEqualTo("CAN")
    }
  }

  @Nested
  inner class GetCityByNomisCode {
    @Test
    fun `should return a city by nomis code`() {
      // Given
      val nomisCode = "USA"
      val city = CityEntity(
        cityId = 1L,
        nomisCode = nomisCode,
        displaySequence = 123,
        nomisDescription = "Test City",
      )
      whenever(cityRepository.findByNomisCode(nomisCode)).thenReturn(Optional.of(city))

      // When
      val result = cityService.getCityByNomisCode(nomisCode)

      // Then
      assertNotNull(result)
      assertThat(result.nomisCode).isEqualTo("USA")
    }
  }
}
