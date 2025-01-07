package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactIdentityEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.ReferenceCodeGroup
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactIdentityDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactIdentityDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactIdentityRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.util.PNCValidator
import java.time.LocalDateTime

@Service
class ContactIdentityService(
  private val contactRepository: ContactRepository,
  private val contactIdentityRepository: ContactIdentityRepository,
  private val contactIdentityDetailsRepository: ContactIdentityDetailsRepository,
  private val referenceCodeService: ReferenceCodeService,
) {

  @Transactional
  fun create(contactId: Long, request: CreateIdentityRequest): ContactIdentityDetails {
    validateContactExists(contactId)
    validatePNC(request.identityType, request.identityValue)
    val type = validateIdentityType(request.identityType)
    val created = contactIdentityRepository.saveAndFlush(
      ContactIdentityEntity(
        contactIdentityId = 0,
        contactId = contactId,
        identityType = request.identityType,
        identityValue = request.identityValue,
        issuingAuthority = request.issuingAuthority,
        createdBy = request.createdBy,
      ),
    )
    return created.toDomainWithType(type)
  }

  @Transactional
  fun update(contactId: Long, contactIdentityId: Long, request: UpdateIdentityRequest): ContactIdentityDetails {
    validateContactExists(contactId)
    val existing = validateExistingIdentity(contactIdentityId)
    val type = validateIdentityType(request.identityType)
    validatePNC(request.identityType, request.identityValue)

    val updating = existing.copy(
      identityType = request.identityType,
      identityValue = request.identityValue,
      issuingAuthority = request.issuingAuthority,
      updatedBy = request.updatedBy,
      updatedTime = LocalDateTime.now(),
    )

    val updated = contactIdentityRepository.saveAndFlush(updating)

    return updated.toDomainWithType(type)
  }

  fun get(contactId: Long, contactIdentityId: Long): ContactIdentityDetails? {
    return contactIdentityDetailsRepository.findByContactIdAndContactIdentityId(contactId, contactIdentityId)?.toModel()
  }

  @Transactional
  fun delete(contactId: Long, contactIdentityId: Long) {
    validateContactExists(contactId)
    val existing = validateExistingIdentity(contactIdentityId)
    contactIdentityRepository.delete(existing)
  }

  private fun validateExistingIdentity(contactIdentityId: Long): ContactIdentityEntity {
    val existing = contactIdentityRepository.findById(contactIdentityId)
      .orElseThrow { EntityNotFoundException("Contact identity ($contactIdentityId) not found") }
    return existing
  }

  private fun validateIdentityType(identityType: String): ReferenceCode {
    val type = referenceCodeService.getReferenceDataByGroupAndCode(ReferenceCodeGroup.ID_TYPE, identityType)
      ?: throw ValidationException("Unsupported identity type ($identityType)")
    if (!type.isActive) {
      throw ValidationException("Identity type ($identityType) is no longer supported for creating or updating identities")
    }
    return type
  }

  private fun validatePNC(identityType: String, identityValue: String) {
    if (identityType == "PNC" && !PNCValidator.isValid(identityValue)) {
      throw ValidationException("Identity value ($identityValue) is not a valid PNC Number")
    }
  }

  private fun validateContactExists(contactId: Long) {
    contactRepository.findById(contactId)
      .orElseThrow { EntityNotFoundException("Contact ($contactId) not found") }
  }

  private fun ContactIdentityEntity.toDomainWithType(
    type: ReferenceCode,
  ) = ContactIdentityDetails(
    contactIdentityId = this.contactIdentityId,
    contactId = this.contactId,
    identityType = this.identityType,
    identityTypeDescription = type.description,
    identityTypeIsActive = type.isActive,
    identityValue = this.identityValue,
    issuingAuthority = this.issuingAuthority,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
    updatedBy = this.updatedBy,
    updatedTime = this.updatedTime,
  )
}
