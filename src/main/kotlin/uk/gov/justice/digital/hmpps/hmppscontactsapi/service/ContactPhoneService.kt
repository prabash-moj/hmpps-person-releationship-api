package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactPhoneEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreatePhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdatePhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactPhoneDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactPhoneDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactPhoneRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import java.time.LocalDateTime

@Service
class ContactPhoneService(
  val contactRepository: ContactRepository,
  val contactPhoneRepository: ContactPhoneRepository,
  val contactPhoneDetailsRepository: ContactPhoneDetailsRepository,
  val referenceCodeService: ReferenceCodeService,
) {

  @Transactional
  fun create(contactId: Long, request: CreatePhoneRequest): ContactPhoneDetails {
    validateContactExists(contactId)
    val type = validatePhoneType(request.phoneType)
    validatePhoneNumber(request.phoneNumber)
    val created = contactPhoneRepository.saveAndFlush(
      ContactPhoneEntity(
        contactPhoneId = 0,
        contactId = contactId,
        phoneType = request.phoneType,
        phoneNumber = request.phoneNumber,
        extNumber = request.extNumber,
        createdBy = request.createdBy,
      ),
    )
    return created.toDomainWithType(type)
  }

  fun get(contactId: Long, contactPhoneId: Long): ContactPhoneDetails? {
    return contactPhoneDetailsRepository.findByContactIdAndContactPhoneId(contactId, contactPhoneId)?.toModel()
  }

  @Transactional
  fun update(contactId: Long, contactPhoneId: Long, request: UpdatePhoneRequest): ContactPhoneDetails {
    validateContactExists(contactId)
    val existing = contactPhoneRepository.findById(contactPhoneId)
      .orElseThrow { EntityNotFoundException("Contact phone ($contactPhoneId) not found") }
    val type = validatePhoneType(request.phoneType)
    validatePhoneNumber(request.phoneNumber)

    val updating = existing.copy(
      phoneType = request.phoneType,
      phoneNumber = request.phoneNumber,
      extNumber = request.extNumber,
      amendedBy = request.amendedBy,
      amendedTime = LocalDateTime.now(),
    )

    val updated = contactPhoneRepository.saveAndFlush(updating)

    return updated.toDomainWithType(type)
  }

  private fun validatePhoneNumber(phoneNumber: String) {
    if (!phoneNumber.matches(Regex("\\+?[\\d\\s()]+"))) {
      throw ValidationException("Phone number invalid, it can only contain numbers, () and whitespace with an optional + at the start")
    }
  }

  private fun validatePhoneType(phoneType: String): ReferenceCode {
    val type = referenceCodeService.getReferenceDataByGroupAndCode("PHONE_TYPE", phoneType)
      ?: throw ValidationException("Unsupported phone type ($phoneType)")
    return type
  }

  private fun validateContactExists(contactId: Long) {
    contactRepository.findById(contactId)
      .orElseThrow { EntityNotFoundException("Contact ($contactId) not found") }
  }

  private fun ContactPhoneEntity.toDomainWithType(
    type: ReferenceCode,
  ) = ContactPhoneDetails(
    this.contactPhoneId,
    this.contactId,
    this.phoneType,
    type.description,
    this.phoneNumber,
    this.extNumber,
    this.createdBy,
    this.createdTime,
    this.amendedBy,
    this.amendedTime,
  )
}
