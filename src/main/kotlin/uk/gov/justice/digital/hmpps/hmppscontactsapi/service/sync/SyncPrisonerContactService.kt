package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync.toResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreatePrisonerContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.UpdatePrisonerContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.PrisonerContact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRepository

@Service
@Transactional
class SyncPrisonerContactService(
  val prisonerContactRepository: PrisonerContactRepository,
) {

  @Transactional(readOnly = true)
  fun getPrisonerContactById(prisonerContactId: Long): PrisonerContact {
    val contactEntity = prisonerContactRepository.findById(prisonerContactId)
      .orElseThrow { EntityNotFoundException("Prisoner contact with ID $prisonerContactId not found") }
    return contactEntity.toResponse()
  }

  fun deletePrisonerContact(prisonerContactId: Long) {
    prisonerContactRepository.findById(prisonerContactId)
      .orElseThrow { EntityNotFoundException("Prisoner contact with ID $prisonerContactId not found") }
    prisonerContactRepository.deleteById(prisonerContactId)
  }

  fun createPrisonerContact(request: CreatePrisonerContactRequest): PrisonerContact {
    return prisonerContactRepository.saveAndFlush(request.toEntity()).toResponse()
  }

  fun updatePrisonerContact(prisonerContactId: Long, request: UpdatePrisonerContactRequest): PrisonerContact {
    val contact = prisonerContactRepository.findById(prisonerContactId)
      .orElseThrow { EntityNotFoundException("Prisoner contact with ID $prisonerContactId not found") }

    val changedPrisonerContact = contact.copy(
      contactId = request.contactId,
      prisonerNumber = request.prisonerNumber,
      relationshipType = request.relationshipType,
      nextOfKin = request.nextOfKin,
      emergencyContact = request.emergencyContact,
      comments = request.comments,
    ).also {
      it.active = request.active
      it.approvedVisitor = request.approvedVisitor
      it.awareOfCharges = request.awareOfCharges
      it.canBeContacted = request.canBeContacted
      it.approvedBy = request.approvedBy
      it.approvedTime = request.approvedTime
      it.expiryDate = request.expiryDate
      it.createdAtPrison = request.createdAtPrison
      it.amendedBy = request.amendedBy
      it.amendedTime = request.updatedTime
    }

    return prisonerContactRepository.saveAndFlush(changedPrisonerContact).toResponse()
  }
}
