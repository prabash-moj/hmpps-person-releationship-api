package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.UpdateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.ContactAddress
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactAddressRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import java.time.LocalDateTime

/**
 * The SyncService contains methods to manage the synchronisation of data to/from NOMIS.
 *
 * The UI services should not use these endpoints as they may have relaxed validation to cater
 * for incomplete or poor quality data from NOMIS, and the data may contain reference data which is
 * useful only for the purpose of synchronisation with NOMIS e.g. CITY code, COUNTY code.
 *
 * Entities here can be individually created, amended or deleted in NOMIS, and these endpoints
 * support the delivery of that operation to this service.
 *
 * Whenever these entities are inserted, updated or deleted in the contacts service, a sync event is
 * published on the hmpps-domain-events-topic, and the Syscon sync service will call the GET
 * endpoint on our sync controller to retrieve the data.
 */

@Service
@Transactional
class SyncContactAddressService(
  private val contactRepository: ContactRepository,
  private val contactAddressRepository: ContactAddressRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional(readOnly = true)
  fun getContactAddressById(contactAddressId: Long): ContactAddress {
    val contactAddress = contactAddressRepository.findById(contactAddressId)
      .orElseThrow { EntityNotFoundException("Contact address with ID $contactAddressId not found") }
    return contactAddress.toModel()
  }

  fun deleteContactAddressById(contactAddressId: Long) {
    contactAddressRepository.findById(contactAddressId)
      .orElseThrow { EntityNotFoundException("Contact address with ID $contactAddressId not found") }
    contactAddressRepository.deleteById(contactAddressId)
  }

  fun createContactAddress(request: CreateContactAddressRequest): ContactAddress {
    contactRepository.findById(request.contactId)
      .orElseThrow { EntityNotFoundException("Contact with ID ${request.contactId} not found") }
    return contactAddressRepository.saveAndFlush(request.toEntity()).toModel()
  }

  fun updateContactAddress(contactAddressId: Long, request: UpdateContactAddressRequest): ContactAddress {
    val contact = contactRepository.findById(request.contactId)
      .orElseThrow { EntityNotFoundException("Contact with ID ${request.contactId} not found") }

    val contactAddress = contactAddressRepository.findById(contactAddressId)
      .orElseThrow { EntityNotFoundException("Contact address with ID $contactAddressId not found") }

    if (contact.contactId != contactAddress.contactId) {
      logger.error("Contact address update specified for a contact not linked to this address")
      throw ValidationException("Contact ID ${contact.contactId} is not linked to the address ${contactAddress.contactAddressId}")
    }

    val changedContactAddress = contactAddress.copy(
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
    ).also {
      it.amendedBy = request.updatedBy
      it.amendedTime = request.updatedTime
      if (!contactAddress.verified && request.verified) {
        it.verifiedBy = request.updatedBy
        it.verifiedTime = LocalDateTime.now()
      }
    }

    return contactAddressRepository.saveAndFlush(changedContactAddress).toModel()
  }
}
