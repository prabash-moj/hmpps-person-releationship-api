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
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.OrganisationEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.helper.isEqualTo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.OrganisationRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class OrganisationServiceTest {

  private lateinit var organisationService: OrganisationService

  @Mock
  private lateinit var organisationRepository: OrganisationRepository

  @BeforeEach
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    organisationService = OrganisationService(organisationRepository)
  }

  @Nested
  inner class GetOrganisationByOrganisationId {

    @Test
    fun `should return a organisation when valid id is provided`() {
      // Given
      val organisationId = 1L
      val deactivatedDate = LocalDate.now()
      val createdTime = LocalDateTime.now().minusMinutes(20)
      val updatedTime = LocalDateTime.now().plusMinutes(20)
      val organisation = OrganisationEntity(
        organisationId = 1L,
        organisationName = "Name",
        programmeNumber = "P1",
        vatNumber = "V1",
        caseloadId = "C1",
        comments = "C2",
        active = false,
        deactivatedDate = deactivatedDate,
        createdBy = "Created by",
        createdTime = createdTime,
        updatedBy = "U1",
        updatedTime = updatedTime,
      )
      whenever(organisationRepository.findById(organisationId)).thenReturn(Optional.of(organisation))

      // When
      val result = organisationService.getOrganisationById(organisationId)

      // Then
      assertNotNull(result)
      assertThat(result.organisationId).isEqualTo(1L)
      assertThat(result.organisationName).isEqualTo("Name")
      assertThat(result.programmeNumber).isEqualTo("P1")
      assertThat(result.vatNumber).isEqualTo("V1")
      assertThat(result.caseloadId).isEqualTo("C1")
      assertThat(result.comments).isEqualTo("C2")
      assertThat(result.active).isFalse()
      assertThat(result.deactivatedDate).isEqualTo(deactivatedDate)
      assertThat(result.createdBy).isEqualTo("Created by")
      assertThat(result.createdTime).isEqualTo(createdTime)
      assertThat(result.updatedBy).isEqualTo("U1")
      assertThat(result.updatedTime).isEqualTo(updatedTime)
    }

    @Test
    fun `should return error when organisation id does not exist`() {
      // Given
      val organisationId = 1009L
      whenever(organisationRepository.findById(organisationId)).thenReturn(Optional.empty())

      // When
      val exception = assertThrows<EntityNotFoundException> {
        organisationService.getOrganisationById(organisationId)
      }

      // Then
      exception.message isEqualTo "Organisation with id 1009 not found"
    }
  }
}
