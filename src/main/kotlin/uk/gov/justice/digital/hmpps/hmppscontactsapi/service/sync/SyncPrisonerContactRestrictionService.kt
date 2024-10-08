package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync.toResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.UpdatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.PrisonerContactRestriction
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRestrictionRepository

@Service
@Transactional
class SyncPrisonerContactRestrictionService(
  val prisonerContactRestrictionRepository: PrisonerContactRestrictionRepository,
) {

  @Transactional(readOnly = true)
  fun getPrisonerContactRestrictionById(prisonerContactRestrictionId: Long): PrisonerContactRestriction {
    val contactEntity = prisonerContactRestrictionRepository.findById(prisonerContactRestrictionId)
      .orElseThrow { EntityNotFoundException("Prisoner contact restriction with ID $prisonerContactRestrictionId not found") }
    return contactEntity.toResponse()
  }

  fun deletePrisonerContactRestriction(prisonerContactRestrictionId: Long) {
    prisonerContactRestrictionRepository.findById(prisonerContactRestrictionId)
      .orElseThrow { EntityNotFoundException("Prisoner contact restriction with ID $prisonerContactRestrictionId not found") }
    prisonerContactRestrictionRepository.deleteById(prisonerContactRestrictionId)
  }

  fun createPrisonerContactRestriction(request: CreatePrisonerContactRestrictionRequest): PrisonerContactRestriction {
    return prisonerContactRestrictionRepository.saveAndFlush(request.toEntity()).toResponse()
  }

  fun updatePrisonerContactRestriction(prisonerContactRestrictionId: Long, request: UpdatePrisonerContactRestrictionRequest): PrisonerContactRestriction {
    val contact = prisonerContactRestrictionRepository.findById(prisonerContactRestrictionId)
      .orElseThrow { EntityNotFoundException("Prisoner contact restriction with ID $prisonerContactRestrictionId not found") }

    val changedPrisonerContactRestriction = contact.copy(
      prisonerContactId = request.contactId,
      restrictionType = request.restrictionType,
      startDate = request.startDate,
      expiryDate = request.expiryDate,
      comments = request.comments,
      authorisedBy = request.authorisedBy,
      authorisedTime = request.authorisedTime,
    ).also {
      it.amendedBy = request.amendedBy
      it.amendedTime = request.amendedTime
    }

    return prisonerContactRestrictionRepository.saveAndFlush(changedPrisonerContactRestriction).toResponse()
  }
}
