package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRestrictionsResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRestrictionDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRestrictionDetailsRepository

@Service
class RestrictionsService(
  private val contactRestrictionDetailsRepository: ContactRestrictionDetailsRepository,
  private val contactRepository: ContactRepository,
  private val prisonerContactRepository: PrisonerContactRepository,
  private val prisonerContactRestrictionDetailsRepository: PrisonerContactRestrictionDetailsRepository,
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

  fun getPrisonerContactRestrictions(prisonerContactId: Long): PrisonerContactRestrictionsResponse {
    val prisonerContact = prisonerContactRepository.findById(prisonerContactId)
      .orElseThrow { EntityNotFoundException("Prisoner contact ($prisonerContactId) could not be found") }
    return PrisonerContactRestrictionsResponse(
      prisonerContactRestrictions = prisonerContactRestrictionDetailsRepository.findAllByPrisonerContactId(prisonerContactId).map { entity ->
        PrisonerContactRestrictionDetails(
          prisonerContactRestrictionId = entity.prisonerContactRestrictionId,
          prisonerContactId = prisonerContactId,
          contactId = prisonerContact.contactId,
          prisonerNumber = prisonerContact.prisonerNumber,
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
      },
      contactEstateWideRestrictions = getEstateWideRestrictionsForContact(prisonerContact.contactId),
    )
  }

  fun validateContactExists(contactId: Long) {
    contactRepository.findById(contactId).orElseThrow { EntityNotFoundException("Contact ($contactId) could not be found") }
  }
}
