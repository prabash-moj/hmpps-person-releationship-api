package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactAddressResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactAddressRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import java.time.LocalDateTime

@Service
@Transactional
class ContactAddressService(
  private val contactRepository: ContactRepository,
  private val contactAddressRepository: ContactAddressRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional(readOnly = true)
  fun get(contactId: Long, contactAddressId: Long): ContactAddressResponse {
    val contact = validateContactExists(contactId)
    val existing = validateExistingAddress(contactAddressId)

    if (contact.contactId != existing.contactId) {
      logger.error("Contact address specified for a contact not linked to this address")
      throw ValidationException("Contact ID ${contact.contactId} is not linked to the address ${existing.contactAddressId}")
    }

    return existing.toModel()
  }

  fun create(contactId: Long, request: CreateContactAddressRequest): ContactAddressResponse {
    validateContactExists(contactId)
    return contactAddressRepository.saveAndFlush(request.toEntity(contactId)).toModel()
  }

  fun update(contactId: Long, contactAddressId: Long, request: UpdateContactAddressRequest): ContactAddressResponse {
    val contact = validateContactExists(contactId)
    val existing = validateExistingAddress(contactAddressId)

    if (contact.contactId != existing.contactId) {
      logger.error("Contact address update specified for a contact not linked to this address")
      throw ValidationException("Contact ID ${contact.contactId} is not linked to the address ${existing.contactAddressId}")
    }

    val changedContactAddress = existing.copy(
      primaryAddress = request.primaryAddress,
      addressType = request.addressType,
      flat = request.flat,
      property = request.property,
      street = request.street,
      area = request.area,
      cityCode = request.cityCode,
      countyCode = request.countyCode,
      countryCode = request.countryCode,
      postCode = request.postcode,
      verified = request.verified,
      mailFlag = request.mailFlag ?: false,
      startDate = request.startDate,
      endDate = request.endDate,
      noFixedAddress = request.noFixedAddress ?: false,
      comments = request.comments,
    ).also {
      it.amendedBy = request.updatedBy
      it.amendedTime = LocalDateTime.now()
      if (!existing.verified && request.verified) {
        it.verifiedBy = request.updatedBy
        it.verifiedTime = LocalDateTime.now()
      }
    }

    return contactAddressRepository.saveAndFlush(changedContactAddress).toModel()
  }

  fun delete(contactId: Long, contactAddressId: Long): ContactAddressResponse {
    val contact = validateContactExists(contactId)
    val rowToDelete = validateExistingAddress(contactAddressId)

    if (contact.contactId != rowToDelete.contactId) {
      logger.error("Contact address delete specified for a contact not linked to this address")
      throw ValidationException("Contact ID ${contact.contactId} is not linked to the address ${rowToDelete.contactAddressId}")
    }

    contactAddressRepository.deleteById(contactAddressId)

    return rowToDelete.toModel()
  }

  private fun validateContactExists(contactId: Long) =
    contactRepository
      .findById(contactId)
      .orElseThrow { EntityNotFoundException("Contact ($contactId) not found") }

  private fun validateExistingAddress(contactAddressId: Long) =
    contactAddressRepository
      .findById(contactAddressId)
      .orElseThrow { EntityNotFoundException("Contact address ($contactAddressId) not found") }
}
