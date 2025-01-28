package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.EmploymentEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.internal.PatchEmploymentResult
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.PatchEmploymentsRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.EmploymentDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.EmploymentRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.OrganisationService
import java.time.LocalDateTime

@Service
@Transactional
class EmploymentService(
  private val employmentRepository: EmploymentRepository,
  private val organisationService: OrganisationService,
) {

  fun patchEmployments(contactId: Long, request: PatchEmploymentsRequest): PatchEmploymentResult {
    val createdIds = mutableListOf<Long>()
    val updatedIds = mutableListOf<Long>()
    val deletedIds = mutableListOf<Long>()
    val existingEmployments = employmentRepository.findByContactId(contactId)
    request.createEmployments.onEach { newEmployment ->
      val created = employmentRepository.saveAndFlush(
        EmploymentEntity(
          employmentId = 0,
          organisationId = newEmployment.organisationId,
          contactId = contactId,
          active = newEmployment.isActive,
          createdBy = request.requestedBy,
          createdTime = LocalDateTime.now(),
          updatedBy = null,
          updatedTime = null,
        ),
      )
      createdIds.add(created.employmentId)
    }
    request.updateEmployments.onEach { updatedEmployment ->
      val existingEmployment = existingEmployments.find { it.employmentId == updatedEmployment.employmentId }
        ?: throw EntityNotFoundException("Employment with id ${updatedEmployment.employmentId} not found")
      employmentRepository.saveAndFlush(
        existingEmployment.copy(
          organisationId = updatedEmployment.organisationId,
          active = updatedEmployment.isActive,
          updatedBy = request.requestedBy,
          updatedTime = LocalDateTime.now(),
        ),
      )
      updatedIds.add(updatedEmployment.employmentId)
    }
    request.deleteEmployments.onEach { deletedEmploymentId ->
      val existingEmployment = existingEmployments.find { it.employmentId == deletedEmploymentId }
        ?: throw EntityNotFoundException("Employment with id $deletedEmploymentId not found")
      employmentRepository.delete(existingEmployment)
      deletedIds.add(deletedEmploymentId)
    }
    return PatchEmploymentResult(
      createdIds = createdIds,
      updatedIds = updatedIds,
      deletedIds = deletedIds,
      employmentsAfterUpdate = getEmploymentDetails(contactId),
    )
  }

  fun getEmploymentDetails(contactId: Long) =
    employmentRepository.findByContactId(contactId).map { employment ->
      val org = organisationService.getOrganisationSummaryById(employment.organisationId)
      EmploymentDetails(
        employmentId = employment.employmentId,
        contactId = employment.contactId,
        employer = org,
        isActive = employment.active,
        createdBy = employment.createdBy,
        createdTime = employment.createdTime,
        updatedBy = employment.updatedBy,
        updatedTime = employment.updatedTime,
      )
    }
}
