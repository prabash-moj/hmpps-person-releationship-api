package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactAddressPhoneEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.AddContactRelationshipRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactRelationship
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactSearchRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactPhoneDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactSearchResultItem
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.GetContactResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactAddressDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactAddressPhoneRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactEmailDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactIdentityDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactPhoneDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactSearchRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRepository
import kotlin.jvm.optionals.getOrNull

@Service
class ContactService(
  private val contactRepository: ContactRepository,
  private val prisonerContactRepository: PrisonerContactRepository,
  private val prisonerService: PrisonerService,
  private val contactSearchRepository: ContactSearchRepository,
  private val contactAddressDetailsRepository: ContactAddressDetailsRepository,
  private val contactPhoneDetailsRepository: ContactPhoneDetailsRepository,
  private val contactAddressPhoneRepository: ContactAddressPhoneRepository,
  private val contactEmailDetailsRepository: ContactEmailDetailsRepository,
  private val contactIdentityDetailsRepository: ContactIdentityDetailsRepository,
  private val languageService: LanguageService,
  private val referenceCodeService: ReferenceCodeService,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun createContact(request: CreateContactRequest): GetContactResponse {
    if (request.relationship != null) {
      validateRelationship(request.relationship)
    }
    val newContact = request.toModel()
    val createdContact = contactRepository.saveAndFlush(newContact)
    val newRelationship = request.relationship?.toEntity(createdContact.contactId, request.createdBy)
      ?.let { prisonerContactRepository.saveAndFlush(it) }

    logger.info("Created new contact {}", createdContact)
    newRelationship?.let { logger.info("Created new relationship {}", newRelationship) }
    return enrichContact(createdContact)
  }

  fun getContact(id: Long): GetContactResponse? {
    return contactRepository.findById(id).getOrNull()
      ?.let { enrichContact(it) }
  }

  fun searchContacts(pageable: Pageable, request: ContactSearchRequest): Page<ContactSearchResultItem> =
    contactSearchRepository.searchContacts(request, pageable).toModel()

  @Transactional
  fun addContactRelationship(contactId: Long, request: AddContactRelationshipRequest) {
    validateRelationship(request.relationship)
    getContact(contactId) ?: throw EntityNotFoundException("Contact ($contactId) could not be found")
    prisonerContactRepository.saveAndFlush(request.relationship.toEntity(contactId, request.createdBy))
  }

  private fun validateRelationship(relationship: ContactRelationship) {
    prisonerService.getPrisoner(relationship.prisonerNumber)
      ?: throw EntityNotFoundException("Prisoner (${relationship.prisonerNumber}) could not be found")
  }

  private fun enrichContact(contactEntity: ContactEntity): GetContactResponse {
    val phoneNumbers = contactPhoneDetailsRepository.findByContactId(contactEntity.contactId).map { it.toModel() }
    val addressPhoneNumbers = contactAddressPhoneRepository.findByContactId(contactEntity.contactId)
    val addresses = contactAddressDetailsRepository.findByContactId(contactEntity.contactId)
      .map { address ->
        address.toModel(
          getAddressPhoneNumbers(
            address.contactAddressId,
            addressPhoneNumbers,
            phoneNumbers,
          ),
        )
      }
    val emailAddresses = contactEmailDetailsRepository.findByContactId(contactEntity.contactId).map { it.toModel() }
    val identities = contactIdentityDetailsRepository.findByContactId(contactEntity.contactId).map { it.toModel() }
    val languageDescription = contactEntity.languageCode?.let { languageService.getLanguageByNomisCode(it).nomisDescription }
    val domesticStatusDescription = contactEntity.domesticStatus?.let { referenceCodeService.getReferenceDataByGroupAndCode("DOMESTIC_STS", it)?.description }
    return GetContactResponse(
      id = contactEntity.contactId,
      title = contactEntity.title,
      lastName = contactEntity.lastName,
      firstName = contactEntity.firstName,
      middleNames = contactEntity.middleNames,
      dateOfBirth = contactEntity.dateOfBirth,
      estimatedIsOverEighteen = contactEntity.estimatedIsOverEighteen,
      isDeceased = contactEntity.isDeceased,
      deceasedDate = contactEntity.deceasedDate,
      languageCode = contactEntity.languageCode,
      languageDescription = languageDescription,
      interpreterRequired = contactEntity.interpreterRequired,
      addresses = addresses,
      phoneNumbers = phoneNumbers,
      emailAddresses = emailAddresses,
      identities = identities,
      domesticStatusCode = contactEntity.domesticStatus,
      domesticStatusDescription = domesticStatusDescription,
      createdBy = contactEntity.createdBy,
      createdTime = contactEntity.createdTime,
    )
  }

  private fun getAddressPhoneNumbers(
    contactAddressId: Long,
    addressPhoneNumbers: List<ContactAddressPhoneEntity>,
    phoneNumbers: List<ContactPhoneDetails>,
  ) = addressPhoneNumbers.filter { it.contactAddressId == contactAddressId }
    .mapNotNull { addressPhone -> phoneNumbers.find { it.contactPhoneId == addressPhone.contactPhoneId } }
}
