package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactRestrictionEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactRestrictionEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRestrictionsResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRestrictionDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRestrictionRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRestrictionDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRestrictionRepository
import java.time.LocalDateTime

@Service
class RestrictionsService(
  private val contactRestrictionDetailsRepository: ContactRestrictionDetailsRepository,
  private val contactRestrictionRepository: ContactRestrictionRepository,
  private val contactRepository: ContactRepository,
  private val prisonerContactRepository: PrisonerContactRepository,
  private val prisonerContactRestrictionDetailsRepository: PrisonerContactRestrictionDetailsRepository,
  private val prisonerContactRestrictionRepository: PrisonerContactRestrictionRepository,
  private val referenceCodeService: ReferenceCodeService,
  private val manageUsersService: ManageUsersService,
) {

  fun getGlobalRestrictionsForContact(contactId: Long): List<ContactRestrictionDetails> {
    validateContactExists(contactId)
    val restrictionsWithEnteredBy = contactRestrictionDetailsRepository.findAllByContactId(contactId)
      .map { entity -> entity to (entity.amendedBy ?: entity.createdBy) }
    val enteredByMap = restrictionsWithEnteredBy
      .map { (_, enteredByUsername) -> enteredByUsername }
      .toSet().associateWith { enteredByUsername -> manageUsersService.getUserByUsername(enteredByUsername)?.name ?: enteredByUsername }
    return restrictionsWithEnteredBy.map { (entity, enteredByUsername) ->
      ContactRestrictionDetails(
        contactRestrictionId = entity.contactRestrictionId,
        contactId = entity.contactId,
        restrictionType = entity.restrictionType,
        restrictionTypeDescription = entity.restrictionTypeDescription,
        startDate = entity.startDate,
        expiryDate = entity.expiryDate,
        comments = entity.comments,
        enteredByUsername = enteredByUsername,
        enteredByDisplayName = enteredByMap[enteredByUsername] ?: enteredByUsername,
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
      prisonerContactRestrictions = prisonerContactRestrictionDetailsRepository.findAllByPrisonerContactId(
        prisonerContactId,
      ).map { entity ->
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
          createdBy = entity.createdBy,
          createdTime = entity.createdTime,
          updatedBy = entity.amendedBy,
          updatedTime = entity.amendedTime,
        )
      },
      contactGlobalRestrictions = getGlobalRestrictionsForContact(prisonerContact.contactId),
    )
  }

  fun createContactGlobalRestriction(
    contactId: Long,
    request: CreateContactRestrictionRequest,
  ): ContactRestrictionDetails {
    validateContactExists(contactId)
    val type = validateType(request.restrictionType)
    val created = contactRestrictionRepository.saveAndFlush(
      ContactRestrictionEntity(
        contactRestrictionId = 0,
        contactId = contactId,
        restrictionType = request.restrictionType,
        startDate = request.startDate,
        expiryDate = request.expiryDate,
        comments = request.comments,
        createdBy = request.createdBy,
      ),
    )
    return contactRestrictionDetails(created, type)
  }

  fun updateContactGlobalRestriction(
    contactId: Long,
    contactRestrictionId: Long,
    request: UpdateContactRestrictionRequest,
  ): ContactRestrictionDetails {
    validateContactExists(contactId)
    val contactRestriction = contactRestrictionRepository.findById(contactRestrictionId)
      .orElseThrow { EntityNotFoundException("Contact restriction ($contactRestrictionId) could not be found") }
    val type = validateType(request.restrictionType)
    val updated = contactRestrictionRepository.saveAndFlush(
      contactRestriction.copy(
        restrictionType = request.restrictionType,
        startDate = request.startDate,
        expiryDate = request.expiryDate,
        comments = request.comments,
        amendedBy = request.updatedBy,
        amendedTime = LocalDateTime.now(),
      ),
    )
    return contactRestrictionDetails(updated, type)
  }

  private fun contactRestrictionDetails(
    entity: ContactRestrictionEntity,
    type: ReferenceCode,
  ): ContactRestrictionDetails {
    val enteredByUsername = entity.amendedBy ?: entity.createdBy
    val enteredByDisplayName = manageUsersService.getUserByUsername(enteredByUsername)?.name ?: enteredByUsername
    return ContactRestrictionDetails(
      contactRestrictionId = entity.contactRestrictionId,
      contactId = entity.contactId,
      restrictionType = entity.restrictionType,
      restrictionTypeDescription = type.description,
      startDate = entity.startDate,
      expiryDate = entity.expiryDate,
      comments = entity.comments,
      enteredByUsername = enteredByUsername,
      enteredByDisplayName = enteredByDisplayName,
      createdBy = entity.createdBy,
      createdTime = entity.createdTime,
      updatedBy = entity.amendedBy,
      updatedTime = entity.amendedTime,
    )
  }

  fun createPrisonerContactRestriction(
    prisonerContactId: Long,
    request: CreatePrisonerContactRestrictionRequest,
  ): PrisonerContactRestrictionDetails {
    val relationship = prisonerContactRepository.findById(prisonerContactId)
      .orElseThrow { EntityNotFoundException("Prisoner contact ($prisonerContactId) could not be found") }
    val type = validateType(request.restrictionType)
    val created = prisonerContactRestrictionRepository.saveAndFlush(
      PrisonerContactRestrictionEntity(
        prisonerContactRestrictionId = 0,
        prisonerContactId = prisonerContactId,
        restrictionType = request.restrictionType,
        startDate = request.startDate,
        expiryDate = request.expiryDate,
        comments = request.comments,
        createdBy = request.createdBy,
      ),
    )
    return prisonerContactRestrictionDetails(created, relationship, type)
  }

  fun updatePrisonerContactRestriction(
    prisonerContactId: Long,
    prisonerContactRestrictionId: Long,
    request: UpdatePrisonerContactRestrictionRequest,
  ): PrisonerContactRestrictionDetails {
    val relationship = prisonerContactRepository.findById(prisonerContactId)
      .orElseThrow { EntityNotFoundException("Prisoner contact ($prisonerContactId) could not be found") }
    val prisonerContactRestriction = prisonerContactRestrictionRepository.findById(prisonerContactRestrictionId)
      .orElseThrow { EntityNotFoundException("Prisoner contact restriction ($prisonerContactRestrictionId) could not be found") }
    val type = validateType(request.restrictionType)
    val updated = prisonerContactRestrictionRepository.saveAndFlush(
      prisonerContactRestriction.copy(
        restrictionType = request.restrictionType,
        startDate = request.startDate,
        expiryDate = request.expiryDate,
        comments = request.comments,
        amendedBy = request.updatedBy,
        amendedTime = LocalDateTime.now(),
      ),
    )
    return prisonerContactRestrictionDetails(updated, relationship, type)
  }

  private fun prisonerContactRestrictionDetails(
    entity: PrisonerContactRestrictionEntity,
    relationship: PrisonerContactEntity,
    type: ReferenceCode,
  ) = PrisonerContactRestrictionDetails(
    prisonerContactRestrictionId = entity.prisonerContactRestrictionId,
    prisonerContactId = entity.prisonerContactId,
    contactId = relationship.contactId,
    prisonerNumber = relationship.prisonerNumber,
    restrictionType = entity.restrictionType,
    restrictionTypeDescription = type.description,
    startDate = entity.startDate,
    expiryDate = entity.expiryDate,
    comments = entity.comments,
    createdBy = entity.createdBy,
    createdTime = entity.createdTime,
    updatedBy = entity.amendedBy,
    updatedTime = entity.amendedTime,
  )

  private fun validateContactExists(contactId: Long) {
    contactRepository.findById(contactId)
      .orElseThrow { EntityNotFoundException("Contact ($contactId) could not be found") }
  }

  private fun validateType(restrictionType: String): ReferenceCode {
    val referenceCode = referenceCodeService.getReferenceDataByGroupAndCode("RESTRICTION", restrictionType)
      ?: throw ValidationException("Unsupported restriction type ($restrictionType)")
    if (!referenceCode.isActive) {
      throw ValidationException("Restriction type ($restrictionType) is no longer supported for creating or updating restrictions")
    }
    return referenceCode
  }
}
