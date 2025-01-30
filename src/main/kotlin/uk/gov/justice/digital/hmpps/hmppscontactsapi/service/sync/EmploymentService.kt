package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.EmploymentEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.internal.PatchEmploymentResult
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateEmploymentRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.PatchEmploymentsRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateEmploymentRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.EmploymentDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.OrganisationSummary
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.EmploymentRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.OrganisationService
import java.time.LocalDateTime

@Service
@Transactional
class EmploymentService(
  private val contactRepository: ContactRepository,
  private val employmentRepository: EmploymentRepository,
  private val organisationService: OrganisationService,
) {

  fun patchEmployments(contactId: Long, request: PatchEmploymentsRequest): PatchEmploymentResult {
    validateContactExists(contactId)
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

  fun getEmploymentDetails(contactId: Long) = employmentRepository.findByContactId(contactId).map { employment ->
    val org = organisationService.getOrganisationSummaryById(employment.organisationId)
    createEmploymentDetails(employment, org)
  }

  fun getEmployment(contactId: Long, employmentId: Long): EmploymentDetails {
    validateContactExists(contactId)
    val employment = validateEmploymentExists(employmentId)
    val org = organisationService.getOrganisationSummaryById(employment.organisationId)
    return createEmploymentDetails(employment, org)
  }

  fun createEmployment(contactId: Long, request: CreateEmploymentRequest): EmploymentDetails {
    validateContactExists(contactId)
    val organisation = validateOrganisationExists(request.organisationId)
    val created = employmentRepository.saveAndFlush(
      EmploymentEntity(
        employmentId = 0,
        organisationId = request.organisationId,
        contactId = contactId,
        active = request.isActive,
        createdBy = request.createdBy,
        createdTime = LocalDateTime.now(),
        updatedBy = null,
        updatedTime = null,
      ),
    )
    return createEmploymentDetails(created, organisation)
  }

  fun updateEmployment(contactId: Long, employmentId: Long, request: UpdateEmploymentRequest): EmploymentDetails {
    validateContactExists(contactId)
    val organisation = validateOrganisationExists(request.organisationId)
    val originalEntity = validateEmploymentExists(employmentId)
    val updated = employmentRepository.saveAndFlush(
      originalEntity.copy(
        organisationId = request.organisationId,
        active = request.isActive,
        updatedBy = request.updatedBy,
        updatedTime = LocalDateTime.now(),
      ),
    )
    return createEmploymentDetails(updated, organisation)
  }

  fun deleteEmployment(contactId: Long, employmentId: Long) {
    validateContactExists(contactId)
    val originalEntity = validateEmploymentExists(employmentId)
    employmentRepository.delete(originalEntity)
  }

  private fun validateContactExists(contactId: Long) {
    contactRepository.findById(contactId)
      .orElseThrow { EntityNotFoundException("Contact ($contactId) not found") }
  }

  private fun validateEmploymentExists(employmentId: Long): EmploymentEntity = employmentRepository.findById(employmentId)
    .orElseThrow { EntityNotFoundException("Employment ($employmentId) not found") }!!

  private fun validateOrganisationExists(organisationId: Long): OrganisationSummary = organisationService.getOrganisationSummaryById(organisationId)

  private fun createEmploymentDetails(
    employment: EmploymentEntity,
    org: OrganisationSummary,
  ) = EmploymentDetails(
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
