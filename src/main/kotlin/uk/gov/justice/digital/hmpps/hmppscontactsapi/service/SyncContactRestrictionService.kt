package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactRestriction
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRestrictionRepository

@Service
@Transactional
class SyncContactRestrictionService(
  val contactRepository: ContactRepository,
  val contactRestrictionRepository: ContactRestrictionRepository,
) {

  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional(readOnly = true)
  fun getContactRestrictionById(contactRestrictionId: Long): ContactRestriction {
    val contactRestrictionEntity = contactRestrictionRepository.findById(contactRestrictionId)
      .orElseThrow { EntityNotFoundException("Contact restriction with ID $contactRestrictionId not found") }
    return contactRestrictionEntity.toModel()
  }

  fun deleteContactRestriction(contactRestrictionId: Long) {
    contactRestrictionRepository.findById(contactRestrictionId)
      .orElseThrow { EntityNotFoundException("Contact restriction with ID $contactRestrictionId not found") }
    contactRestrictionRepository.deleteById(contactRestrictionId)
  }

  fun createContactRestriction(request: CreateContactRestrictionRequest): ContactRestriction {
    contactRepository.findById(request.contactId)
      .orElseThrow { EntityNotFoundException("Contact with ID ${request.contactId} not found") }
    return contactRestrictionRepository.saveAndFlush(request.toEntity()).toModel()
  }

  fun updateContactRestriction(contactRestrictionId: Long, request: UpdateContactRestrictionRequest): ContactRestriction {
    val contact = contactRepository.findById(request.contactId)
      .orElseThrow { EntityNotFoundException("Contact with ID ${request.contactId} not found") }

    val restrictionEntity = contactRestrictionRepository.findById(contactRestrictionId)
      .orElseThrow { EntityNotFoundException("Contact restriction with ID $contactRestrictionId not found") }

    if (contact.contactId != restrictionEntity.contactId) {
      logger.error("Contact restriction update specified for a contact not linked to this restriction")
      throw ValidationException("Contact ID ${contact.contactId} is not linked to the restriction ${restrictionEntity.contactRestrictionId}")
    }

    val changedContactRestriction = restrictionEntity.copy(
      contactId = request.contactId,
      restrictionType = request.restrictionType,
      startDate = request.startDate,
      expiryDate = request.expiryDate,
      comments = request.comments,
    ).also {
      it.amendedBy = request.updatedBy
      it.amendedTime = request.updatedTime
    }

    return contactRestrictionRepository.saveAndFlush(changedContactRestriction).toModel()
  }
}
