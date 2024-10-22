package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactPhoneEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreatePhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactPhoneDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactPhoneDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactPhoneRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository

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
    val type = validatePhoneType(request)
    validatePhoneNumber(request)
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
    return ContactPhoneDetails(
      created.contactPhoneId,
      created.contactId,
      created.phoneType,
      type.description,
      created.phoneNumber,
      created.extNumber,
      created.createdBy,
      created.createdTime,
      null,
      null,
    )
  }

  fun get(contactId: Long, contactPhoneId: Long): ContactPhoneDetails? {
    return contactPhoneDetailsRepository.findByContactIdAndContactPhoneId(contactId, contactPhoneId)?.toModel()
  }

  private fun validatePhoneNumber(request: CreatePhoneRequest) {
    if (!request.phoneNumber.matches(Regex("\\+?[\\d\\s()]+"))) {
      throw ValidationException("Phone number invalid, it can only contain numbers, () and whitespace with an optional + at the start")
    }
  }

  private fun validatePhoneType(request: CreatePhoneRequest): ReferenceCode {
    val type = referenceCodeService.getReferenceDataByGroupAndCode("PHONE_TYPE", request.phoneType)
      ?: throw ValidationException("Unsupported phone type (${request.phoneType})")
    return type
  }

  private fun validateContactExists(contactId: Long) {
    contactRepository.findById(contactId)
      .orElseThrow { EntityNotFoundException("Contact ($contactId) not found") }
  }
}
