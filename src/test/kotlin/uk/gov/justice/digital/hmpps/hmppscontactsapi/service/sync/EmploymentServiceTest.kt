package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.EmploymentEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createEmploymentEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createOrganisationSummary
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.PatchEmploymentsNewEmployment
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.PatchEmploymentsRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.PatchEmploymentsUpdateEmployment
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.EmploymentRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.OrganisationService
import java.time.LocalDateTime

class EmploymentServiceTest {
  private val contactId = 99L
  private val employmentRepository: EmploymentRepository = mock()
  private val organisationService: OrganisationService = mock()
  private val service = EmploymentService(employmentRepository, organisationService)

  @Test
  fun `Patch employments should create a new employment`() {
    val employmentEntity1 = createEmploymentEntity(id = 1, organisationId = 1)
    val employmentEntity2 = createEmploymentEntity(id = 2, organisationId = 2)
    val org1 = createOrganisationSummary(id = 1, organisationName = "One")
    val org2 = createOrganisationSummary(id = 2, organisationName = "Two")

    whenever(employmentRepository.saveAndFlush(any())).thenReturn(employmentEntity2)
    whenever(employmentRepository.findByContactId(contactId))
      .thenReturn(listOf(employmentEntity1)) // before save
      .thenReturn(listOf(employmentEntity1, employmentEntity2)) // after save
    whenever(organisationService.getOrganisationSummaryById(1)).thenReturn(org1)
    whenever(organisationService.getOrganisationSummaryById(2)).thenReturn(org2)

    val employments = service.patchEmployments(
      contactId,
      PatchEmploymentsRequest(
        createEmployments = listOf(PatchEmploymentsNewEmployment(2, true)),
        updateEmployments = emptyList(),
        deleteEmployments = emptyList(),
        requestedBy = "USER1",
      ),
    )

    assertThat(employments.employmentsAfterUpdate).hasSize(2)
    assertThat(employments.createdIds).isEqualTo(listOf(2L))
    assertThat(employments.updatedIds).isEmpty()
    assertThat(employments.deletedIds).isEmpty()
    assertThat(employments.employmentsAfterUpdate[0].employmentId).isEqualTo(1)
    assertThat(employments.employmentsAfterUpdate[0].employer.organisationId).isEqualTo(1)
    assertThat(employments.employmentsAfterUpdate[1].employmentId).isEqualTo(2)
    assertThat(employments.employmentsAfterUpdate[1].employer.organisationId).isEqualTo(2)

    val employmentCaptor = argumentCaptor<EmploymentEntity>()
    verify(employmentRepository).saveAndFlush(employmentCaptor.capture())
    assertThat(employmentCaptor.firstValue).usingRecursiveComparison().ignoringFields("createdTime").isEqualTo(
      EmploymentEntity(
        employmentId = 0,
        organisationId = 2,
        contactId = contactId,
        active = true,
        createdBy = "USER1",
        createdTime = LocalDateTime.now(),
        updatedBy = null,
        updatedTime = null,
      ),
    )
  }

  @Test
  fun `Patch employments should update a new employment`() {
    val employmentEntityBeforeUpdate = createEmploymentEntity(
      id = 1,
      contactId = contactId,
      organisationId = 1,
      active = true,
      createdBy = "CREATED",
      updatedBy = null,
      updatedTime = null,
    )
    val employmentEntityAfterUpdate = createEmploymentEntity(
      id = 1,
      contactId = contactId,
      organisationId = 2,
      active = false,
      createdBy = "CREATED",
      updatedBy = "USER1",
      updatedTime = LocalDateTime.now(),
    )
    val org1 = createOrganisationSummary(id = 1, organisationName = "One")
    val org2 = createOrganisationSummary(id = 2, organisationName = "Two")

    whenever(employmentRepository.saveAndFlush(any())).thenReturn(employmentEntityAfterUpdate)
    whenever(employmentRepository.findByContactId(contactId))
      .thenReturn(listOf(employmentEntityBeforeUpdate)) // before save
      .thenReturn(listOf(employmentEntityAfterUpdate)) // after save
    whenever(organisationService.getOrganisationSummaryById(1)).thenReturn(org1)
    whenever(organisationService.getOrganisationSummaryById(2)).thenReturn(org2)

    val employments = service.patchEmployments(
      contactId,
      PatchEmploymentsRequest(
        createEmployments = emptyList(),
        updateEmployments = listOf(PatchEmploymentsUpdateEmployment(1, 2, false)),
        deleteEmployments = emptyList(),
        requestedBy = "USER1",
      ),
    )

    assertThat(employments.employmentsAfterUpdate).hasSize(1)
    assertThat(employments.employmentsAfterUpdate[0].employmentId).isEqualTo(1)
    assertThat(employments.employmentsAfterUpdate[0].employer.organisationId).isEqualTo(2)
    assertThat(employments.employmentsAfterUpdate[0].isActive).isFalse()
    assertThat(employments.createdIds).isEmpty()
    assertThat(employments.updatedIds).isEqualTo(listOf(1L))
    assertThat(employments.deletedIds).isEmpty()

    val employmentCaptor = argumentCaptor<EmploymentEntity>()
    verify(employmentRepository).saveAndFlush(employmentCaptor.capture())
    assertThat(employmentCaptor.firstValue).usingRecursiveComparison().ignoringFields("createdTime", "updatedTime")
      .isEqualTo(
        EmploymentEntity(
          employmentId = 1,
          organisationId = 2,
          contactId = contactId,
          active = false,
          createdBy = "CREATED",
          createdTime = LocalDateTime.now(),
          updatedBy = "USER1",
          updatedTime = LocalDateTime.now(),
        ),
      )
  }

  @Test
  fun `Patch employments should blow up if employment to be updated not found`() {
    whenever(employmentRepository.findByContactId(contactId)).thenReturn(emptyList())

    val exception = assertThrows<EntityNotFoundException> {
      service.patchEmployments(
        contactId,
        PatchEmploymentsRequest(
          createEmployments = emptyList(),
          updateEmployments = listOf(PatchEmploymentsUpdateEmployment(1, 2, false)),
          deleteEmployments = emptyList(),
          requestedBy = "USER1",
        ),
      )
    }
    assertThat(exception.message).isEqualTo("Employment with id 1 not found")
  }

  @Test
  fun `Patch employments should delete an employment`() {
    val employmentEntity = createEmploymentEntity(
      id = 1,
      contactId = contactId,
      organisationId = 1,
      active = true,
      createdBy = "CREATED",
      updatedBy = null,
      updatedTime = null,
    )
    val org1 = createOrganisationSummary(id = 1, organisationName = "One")

    doNothing().whenever(employmentRepository).delete(any())
    whenever(employmentRepository.findByContactId(contactId))
      .thenReturn(listOf(employmentEntity)) // before delete
      .thenReturn(emptyList()) // after delete
    whenever(organisationService.getOrganisationSummaryById(1)).thenReturn(org1)

    val employments = service.patchEmployments(
      contactId,
      PatchEmploymentsRequest(
        createEmployments = emptyList(),
        updateEmployments = emptyList(),
        deleteEmployments = listOf(1),
        requestedBy = "USER1",
      ),
    )

    assertThat(employments.employmentsAfterUpdate).isEmpty()
    assertThat(employments.createdIds).isEmpty()
    assertThat(employments.updatedIds).isEmpty()
    assertThat(employments.deletedIds).isEqualTo(listOf(1L))

    val employmentCaptor = argumentCaptor<EmploymentEntity>()
    verify(employmentRepository).delete(employmentCaptor.capture())
    assertThat(employmentCaptor.firstValue).isEqualTo(employmentEntity)
  }

  @Test
  fun `Patch employments should blow up if employment to be deleted is not found`() {
    whenever(employmentRepository.findByContactId(contactId)).thenReturn(emptyList())

    val exception = assertThrows<EntityNotFoundException> {
      service.patchEmployments(
        contactId,
        PatchEmploymentsRequest(
          createEmployments = emptyList(),
          updateEmployments = emptyList(),
          deleteEmployments = listOf(99),
          requestedBy = "USER1",
        ),
      )
    }
    assertThat(exception.message).isEqualTo("Employment with id 99 not found")
  }

  @Test
  fun `should get a contacts employments`() {
    val employmentEntity1 = createEmploymentEntity(id = 1, organisationId = 1)
    val employmentEntity2 = createEmploymentEntity(id = 2, organisationId = 2)
    val orgEntity1 = createOrganisationSummary(id = 1, organisationName = "One")
    val orgEntity2 = createOrganisationSummary(id = 2, organisationName = "Two")

    whenever(employmentRepository.findByContactId(contactId)).thenReturn(listOf(employmentEntity1, employmentEntity2))
    whenever(organisationService.getOrganisationSummaryById(1)).thenReturn(orgEntity1)
    whenever(organisationService.getOrganisationSummaryById(2)).thenReturn(orgEntity2)

    val employments = service.getEmploymentDetails(contactId)
    assertThat(employments).hasSize(2)
    assertThat(employments[0].employmentId).isEqualTo(1)
    assertThat(employments[0].employer.organisationId).isEqualTo(1)
    assertThat(employments[1].employmentId).isEqualTo(2)
    assertThat(employments[1].employer.organisationId).isEqualTo(2)
  }
}
