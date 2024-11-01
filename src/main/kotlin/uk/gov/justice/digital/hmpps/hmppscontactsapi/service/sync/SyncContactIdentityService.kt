package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreateContactIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.UpdateContactIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.ContactIdentity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactIdentityRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository

@Service
@Transactional
class SyncContactIdentityService(
  val contactRepository: ContactRepository,
  val contactIdentityRepository: ContactIdentityRepository,
) {

  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional(readOnly = true)
  fun getContactIdentityById(contactIdentityId: Long): ContactIdentity {
    val contactIdentityEntity = contactIdentityRepository.findById(contactIdentityId)
      .orElseThrow { EntityNotFoundException("Contact identity with ID $contactIdentityId not found") }
    return contactIdentityEntity.toModel()
  }

  fun deleteContactIdentity(contactIdentityId: Long) {
    contactIdentityRepository.findById(contactIdentityId)
      .orElseThrow { EntityNotFoundException("Contact identity with ID $contactIdentityId not found") }
    contactIdentityRepository.deleteById(contactIdentityId)
  }

  fun createContactIdentity(request: CreateContactIdentityRequest): ContactIdentity {
    contactRepository.findById(request.contactId)
      .orElseThrow { EntityNotFoundException("Contact with ID ${request.contactId} not found") }
    return contactIdentityRepository.saveAndFlush(request.toEntity()).toModel()
  }

  fun updateContactIdentity(contactIdentityId: Long, request: UpdateContactIdentityRequest): ContactIdentity {
    val contact = contactRepository.findById(request.contactId)
      .orElseThrow { EntityNotFoundException("Contact with ID ${request.contactId} not found") }

    val identityEntity = contactIdentityRepository.findById(contactIdentityId)
      .orElseThrow { EntityNotFoundException("Contact identity with ID $contactIdentityId not found") }

    if (contact.contactId != identityEntity.contactId) {
      logger.error("Contact identity update specified for a contact not linked to this identity")
      throw ValidationException("Contact ID ${contact.contactId} is not linked to the identity ${identityEntity.contactIdentityId}")
    }

    val changedContactIdentity = identityEntity.copy(
      contactId = request.contactId,
      identityType = request.identityType,
      identityValue = request.identityValue,
      amendedBy = request.updatedBy,
      amendedTime = request.updatedTime,
    )

    return contactIdentityRepository.saveAndFlush(changedContactIdentity).toModel()
  }
}
