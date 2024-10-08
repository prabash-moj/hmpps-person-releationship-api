package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreateContactEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.UpdateContactEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.ContactEmail
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactEmailRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository

@Service
@Transactional
class SyncContactEmailService(
  val contactRepository: ContactRepository,
  val contactEmailRepository: ContactEmailRepository,
) {

  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional(readOnly = true)
  fun getContactEmailById(contactEmailId: Long): ContactEmail {
    val contactEmailEntity = contactEmailRepository.findById(contactEmailId)
      .orElseThrow { EntityNotFoundException("Contact email with ID $contactEmailId not found") }
    return contactEmailEntity.toModel()
  }

  fun deleteContactEmail(contactEmailId: Long) {
    contactEmailRepository.findById(contactEmailId)
      .orElseThrow { EntityNotFoundException("Contact email with ID $contactEmailId not found") }
    contactEmailRepository.deleteById(contactEmailId)
  }

  fun createContactEmail(request: CreateContactEmailRequest): ContactEmail {
    contactRepository.findById(request.contactId)
      .orElseThrow { EntityNotFoundException("Contact with ID ${request.contactId} not found") }
    return contactEmailRepository.saveAndFlush(request.toEntity()).toModel()
  }

  fun updateContactEmail(contactEmailId: Long, request: UpdateContactEmailRequest): ContactEmail {
    val contact = contactRepository.findById(request.contactId)
      .orElseThrow { EntityNotFoundException("Contact with ID ${request.contactId} not found") }

    val emailEntity = contactEmailRepository.findById(contactEmailId)
      .orElseThrow { EntityNotFoundException("Contact email with ID $contactEmailId not found") }

    if (contact.contactId != emailEntity.contactId) {
      logger.error("Contact email update specified for a contact not linked to this email")
      throw ValidationException("Contact ID ${contact.contactId} is not linked to the email ${emailEntity.contactEmailId}")
    }

    val changedContactEmail = emailEntity.copy(
      contactId = request.contactId,
      emailType = request.emailType,
      emailAddress = request.emailAddress,
      primaryEmail = request.primaryEmail,
    ).also {
      it.amendedBy = request.updatedBy
      it.amendedTime = request.updatedTime
    }

    return contactEmailRepository.saveAndFlush(changedContactEmail).toModel()
  }
}
