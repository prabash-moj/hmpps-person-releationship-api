package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactPhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactPhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactPhone
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactPhoneRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository

@Service
@Transactional
class SyncContactPhoneService(
  val contactRepository: ContactRepository,
  val contactPhoneRepository: ContactPhoneRepository,
) {

  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional(readOnly = true)
  fun getContactPhoneById(contactPhoneId: Long): ContactPhone {
    val contactPhoneEntity = contactPhoneRepository.findById(contactPhoneId)
      .orElseThrow { EntityNotFoundException("Contact phone with ID $contactPhoneId not found") }
    return contactPhoneEntity.toModel()
  }

  fun deleteContactPhone(contactPhoneId: Long) {
    contactPhoneRepository.findById(contactPhoneId)
      .orElseThrow { EntityNotFoundException("Contact phone with ID $contactPhoneId not found") }
    contactPhoneRepository.deleteById(contactPhoneId)
  }

  fun createContactPhone(request: CreateContactPhoneRequest): ContactPhone {
    contactRepository.findById(request.contactId)
      .orElseThrow { EntityNotFoundException("Contact with ID ${request.contactId} not found") }
    return contactPhoneRepository.saveAndFlush(request.toEntity()).toModel()
  }

  fun updateContactPhone(contactPhoneId: Long, request: UpdateContactPhoneRequest): ContactPhone {
    val contact = contactRepository.findById(request.contactId)
      .orElseThrow { EntityNotFoundException("Contact with ID ${request.contactId} not found") }

    val phoneEntity = contactPhoneRepository.findById(contactPhoneId)
      .orElseThrow { EntityNotFoundException("Contact phone with ID $contactPhoneId not found") }

    if (contact.contactId != phoneEntity.contactId) {
      logger.error("Contact phone update specified for a contact not linked to this phone")
      throw ValidationException("Contact ID ${contact.contactId} is not linked to the phone ${phoneEntity.contactPhoneId}")
    }

    val changedContactPhone = phoneEntity.copy(
      contactId = request.contactId,
      phoneType = request.phoneType,
      phoneNumber = request.phoneNumber,
      extNumber = request.extNumber,
      primaryPhone = request.primaryPhone,
    ).also {
      it.amendedBy = request.updatedBy
      it.amendedTime = request.updatedTime
    }

    return contactPhoneRepository.saveAndFlush(changedContactPhone).toModel()
  }
}
