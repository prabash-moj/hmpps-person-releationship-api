package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRestrictionDetailsRepository

@Service
class RestrictionsService(
  private val contactRestrictionDetailsRepository: ContactRestrictionDetailsRepository,
  private val contactRepository: ContactRepository,
) {

  fun getEstateWideRestrictionsForContact(contactId: Long): List<ContactRestrictionDetails> {
    validateContactExists(contactId)
    return contactRestrictionDetailsRepository.findAllByContactId(contactId).map { entity ->
      ContactRestrictionDetails(
        contactRestrictionId = entity.contactRestrictionId,
        contactId = entity.contactId,
        restrictionType = entity.restrictionType,
        restrictionTypeDescription = entity.restrictionTypeDescription,
        startDate = entity.startDate,
        expiryDate = entity.expiryDate,
        comments = entity.comments,
        staffUsername = entity.staffUsername,
        createdBy = entity.createdBy,
        createdTime = entity.createdTime,
        updatedBy = entity.amendedBy,
        updatedTime = entity.amendedTime,
      )
    }
  }

  fun validateContactExists(contactId: Long) {
    contactRepository.findById(contactId).orElseThrow { EntityNotFoundException("Contact ($contactId) could not be found") }
  }
}
