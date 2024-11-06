package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.migrate

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactAddressEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactAddressPhoneEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEmailEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEmploymentEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactIdentityEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactPhoneEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactRestrictionEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactRestrictionEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.migrate.MigrateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.migrate.AddressAndPhones
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.migrate.ContactsAndRestrictions
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.migrate.ElementType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.migrate.IdPair
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.migrate.MigrateContactResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactAddressPhoneRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactAddressRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactEmailRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactEmploymentRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactIdentityRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactPhoneRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRestrictionRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRestrictionRepository
import java.time.LocalDateTime

@Service
@Transactional
class MigrationService(
  private val contactRepository: ContactRepository,
  private val contactAddressRepository: ContactAddressRepository,
  private val contactAddressPhoneRepository: ContactAddressPhoneRepository,
  private val contactPhoneRepository: ContactPhoneRepository,
  private val contactEmailRepository: ContactEmailRepository,
  private val contactIdentityRepository: ContactIdentityRepository,
  private val contactRestrictionRepository: ContactRestrictionRepository,
  private val prisonerContactRepository: PrisonerContactRepository,
  private val prisonerContactRestrictionRepository: PrisonerContactRestrictionRepository,
  private val contactEmploymentRepository: ContactEmploymentRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  /**
   * Accept a full contact migration object, validate and create the equivalent entities
   * in the contacts database locally.
   *
   * Do NOT emit sync events for entities created, as these will already exist in NOMIS and
   * in the mapping tables - we are not generating new copies of this data.
   *
   * Assemble a response object to contain both the NOMIS and DPS IDs for each entity and
   * sub-entity saved to allow the mapping service to link the IDs for subsequent syncs.
   */
  fun migrateContact(request: MigrateContactRequest): MigrateContactResponse {
    logger.info(
      "Migrate PERSON ID {} with {} addresses, {} phones, {} emails, {} identities, {} restrictions, {} employments, {} relationships",
      request.personId,
      request.addresses.size,
      request.phoneNumbers.size,
      request.emailAddresses.size,
      request.identifiers.size,
      request.restrictions.size,
      request.employments.size,
      request.contacts.size,
    )

    // Extract, transform and save the different entities from the request and returning the NOMIS and DPS IDs for each
    val contactPair = extractAndSaveContact(request)
    val contactId = contactPair.second.contactId
    val phoneNumberPairs = extractAndSavePhones(request, contactId)
    val addressPairs = extractAndSaveAddresses(request, contactId)
    val addressPhonePairs = extractAndSaveAddressPhones(request, contactId, addressPairs)
    val emailPairs = extractAndSaveEmails(request, contactId)
    val identityPairs = extractAndSaveIdentities(request, contactId)
    val restrictionPairs = extractAndSaveRestrictions(request, contactId)
    val employmentPairs = extractAndSaveEmployments(request, contactId)
    val prisonerContactPairs = extractAndSavePrisonerContacts(request, contactId)
    val prisonerContactRestrictionPairs =
      extractAndSavePrisonerContactRestrictions(request, contactId, prisonerContactPairs)

    return MigrateContactResponse(
      contact = IdPair(ElementType.CONTACT, contactPair.first, contactPair.second.contactId),
      lastName = contactPair.second.lastName,
      dateOfBirth = contactPair.second.dateOfBirth,
      phoneNumbers = phoneNumberPairs.map { IdPair(ElementType.PHONE, it.first, it.second.contactPhoneId) },
      addresses = buildAddressesAndPhonesResponse(addressPairs, addressPhonePairs),
      emailAddresses = emailPairs.map { IdPair(ElementType.EMAIL, it.first, it.second.contactEmailId) },
      identities = identityPairs.map { IdPair(ElementType.IDENTITY, it.first, it.second.contactIdentityId) },
      restrictions = restrictionPairs.map { IdPair(ElementType.RESTRICTION, it.first, it.second.contactRestrictionId) },
      relationships = buildContactsAndRestrictionsResponse(prisonerContactPairs, prisonerContactRestrictionPairs),
      employments = employmentPairs.map { IdPair(ElementType.EMPLOYMENT, it.first, it.second.contactEmploymentId) },
    )
  }

  fun extractAndSaveContact(req: MigrateContactRequest): Pair<Long, ContactEntity> =
    Pair(
      req.personId,
      contactRepository.save(
        ContactEntity(
          contactId = 0L,
          title = req.title?.code,
          lastName = req.lastName,
          middleNames = req.middleName,
          firstName = req.firstName,
          dateOfBirth = req.dateOfBirth,
          deceasedDate = req.deceasedDate,
          isDeceased = req.deceasedDate != null,
          estimatedIsOverEighteen = EstimatedIsOverEighteen.DO_NOT_KNOW,
          createdBy = req.createUsername ?: "MIGRATION",
          createdTime = req.createDateTime ?: LocalDateTime.now(),
          staffFlag = req.staff,
          remitterFlag = req.remitter,
          gender = req.gender?.code,
          languageCode = req.language?.code,
          domesticStatus = req.domesticStatus?.code,
          interpreterRequired = req.interpreterRequired,
          amendedBy = req.modifyUsername,
          amendedTime = req.modifyDateTime,
        ),
      ),
    )

  fun extractAndSavePhones(req: MigrateContactRequest, contactId: Long): List<Pair<Long, ContactPhoneEntity>> =
    req.phoneNumbers.map { requestPhone ->
      Pair(
        requestPhone.phoneId,
        contactPhoneRepository.save(
          ContactPhoneEntity(
            contactPhoneId = 0L,
            contactId = contactId,
            phoneType = requestPhone.type.code,
            phoneNumber = requestPhone.number,
            extNumber = requestPhone.extension,
            createdBy = requestPhone.createUsername ?: "MIGRATION",
            createdTime = requestPhone.createDateTime ?: LocalDateTime.now(),
            amendedBy = requestPhone.modifyUsername,
            amendedTime = requestPhone.modifyDateTime,
          ),
        ),
      )
    }

  fun extractAndSaveAddresses(req: MigrateContactRequest, contactId: Long): List<Pair<Long, ContactAddressEntity>> =
    req.addresses.map { addr ->
      Pair(
        addr.addressId,
        contactAddressRepository.save(
          ContactAddressEntity(
            contactAddressId = 0L,
            contactId = contactId,
            addressType = addr.type?.code,
            primaryAddress = addr.primaryAddress,
            flat = addr.flat,
            property = addr.premise,
            street = addr.street,
            area = addr.locality,
            cityCode = addr.city?.code,
            countyCode = addr.county?.code,
            postCode = addr.postCode,
            countryCode = addr.country?.code,
            verified = addr.validatedPAF,
            mailFlag = addr.mailAddress,
            startDate = addr.startDate,
            endDate = addr.endDate,
            noFixedAddress = addr.noFixedAddress,
            comments = addr.comment,
            createdBy = addr.createUsername ?: "MIGRATION",
            createdTime = addr.createDateTime ?: LocalDateTime.now(),
          ).also {
            it.amendedBy = addr.modifyUsername
            it.amendedTime = addr.modifyDateTime
          },
        ),
      )
    }

  fun extractAndSaveAddressPhones(
    req: MigrateContactRequest,
    contactId: Long,
    contactAddresses: List<Pair<Long, ContactAddressEntity>>,
  ): List<Pair<Long, List<Pair<Long, ContactAddressPhoneEntity>>>> {
    val phones = req.addresses.map { addr ->

      val thisContactAddress = contactAddresses.find { it.first == addr.addressId }

      Pair(
        addr.addressId,
        addr.phoneNumbers.map { phone ->
          val contactPhone = contactPhoneRepository.save(
            ContactPhoneEntity(
              contactPhoneId = 0L,
              contactId = contactId,
              phoneType = phone.type.code,
              phoneNumber = phone.number,
              extNumber = phone.extension,
              createdBy = phone.createUsername ?: "MIGRATION",
              createdTime = phone.createDateTime ?: LocalDateTime.now(),
              amendedBy = phone.modifyUsername,
              amendedTime = phone.modifyDateTime,
            ),
          )

          Pair(
            phone.phoneId,
            contactAddressPhoneRepository.save(
              ContactAddressPhoneEntity(
                contactAddressPhoneId = 0L,
                contactId = contactId,
                contactAddressId = thisContactAddress!!.second.contactAddressId,
                contactPhoneId = contactPhone.contactPhoneId,
                createdBy = phone.createUsername ?: "MIGRATION",
                createdTime = phone.createDateTime ?: LocalDateTime.now(),
                amendedBy = phone.modifyUsername,
                amendedTime = phone.modifyDateTime,
              ),
            ),
          )
        },
      )
    }
    return phones
  }

  fun extractAndSaveEmails(req: MigrateContactRequest, contactId: Long): List<Pair<Long, ContactEmailEntity>> =
    req.emailAddresses.map { requestEmail ->
      Pair(
        requestEmail.emailAddressId,
        contactEmailRepository.save(
          ContactEmailEntity(
            contactEmailId = 0L,
            contactId = contactId,
            emailType = "PERSONAL",
            emailAddress = requestEmail.email,
            createdBy = requestEmail.createUsername ?: "MIGRATION",
            createdTime = requestEmail.createDateTime ?: LocalDateTime.now(),
          ).also {
            it.amendedBy = requestEmail.modifyUsername
            it.amendedTime = requestEmail.modifyDateTime
          },
        ),
      )
    }

  fun extractAndSaveIdentities(req: MigrateContactRequest, contactId: Long): List<Pair<Long, ContactIdentityEntity>> =
    req.identifiers.map { requestIdentifier ->
      Pair(
        requestIdentifier.sequence,
        contactIdentityRepository.save(
          ContactIdentityEntity(
            contactIdentityId = 0L,
            contactId = contactId,
            identityType = requestIdentifier.type.code,
            identityValue = requestIdentifier.identifier,
            issuingAuthority = requestIdentifier.issuedAuthority,
            createdBy = requestIdentifier.createUsername ?: "MIGRATION",
            createdTime = requestIdentifier.createDateTime ?: LocalDateTime.now(),
            amendedBy = requestIdentifier.modifyUsername,
            amendedTime = requestIdentifier.modifyDateTime,
          ),
        ),
      )
    }

  fun extractAndSaveRestrictions(
    req: MigrateContactRequest,
    contactId: Long,
  ): List<Pair<Long, ContactRestrictionEntity>> =
    req.restrictions.map { restriction ->
      Pair(
        restriction.id,
        contactRestrictionRepository.save(
          ContactRestrictionEntity(
            contactRestrictionId = 0L,
            contactId = contactId,
            restrictionType = restriction.type.code,
            startDate = restriction.effectiveDate,
            expiryDate = restriction.expiryDate,
            comments = restriction.comment,
            staffUsername = restriction.staffUsername,
            createdBy = restriction.createUsername ?: "MIGRATION",
            createdTime = restriction.createDateTime ?: LocalDateTime.now(),
          ).also {
            it.amendedBy = restriction.modifyUsername
            it.amendedTime = restriction.modifyDateTime
          },
        ),
      )
    }

  fun extractAndSaveEmployments(
    req: MigrateContactRequest,
    contactId: Long,
  ): List<Pair<Long, ContactEmploymentEntity>> =
    req.employments.map { employment ->
      Pair(
        employment.sequence,
        contactEmploymentRepository.save(
          ContactEmploymentEntity(
            contactEmploymentId = 0L,
            contactId = contactId,
            corporateId = employment.corporate?.id,
            corporateName = employment.corporate?.name,
            active = employment.active,
            createdBy = employment.createUsername ?: "MIGRATION",
            createdTime = employment.createDateTime ?: LocalDateTime.now(),
          ).also {
            it.amendedBy = employment.modifyUsername
            it.amendedTime = employment.modifyDateTime
          },
        ),
      )
    }

  fun buildAddressesAndPhonesResponse(
    addresses: List<Pair<Long, ContactAddressEntity>>,
    addressPhones: List<Pair<Long, List<Pair<Long, ContactAddressPhoneEntity>>>>,
  ) = addresses.map { addr ->
    val phonesForThisAddress = addressPhones.filter { it.first == addr.first }
    phonesForThisAddress.map { phone ->
      AddressAndPhones(
        address = IdPair(ElementType.ADDRESS, addr.first, addr.second.contactAddressId),
        phones = phone.second.map {
          IdPair(ElementType.ADDRESS_PHONE, it.first, it.second.contactAddressPhoneId)
        },
      )
    }
  }.flatten()

  fun buildContactsAndRestrictionsResponse(
    contacts: List<Pair<Long, PrisonerContactEntity>>,
    restrictions: List<Pair<Long, List<Pair<Long, PrisonerContactRestrictionEntity>>>>,
  ) = contacts.map { contact ->
    val restrictionsForThisContact = restrictions.filter { it.first == contact.first }
    restrictionsForThisContact.map { restriction ->
      ContactsAndRestrictions(
        relationship = IdPair(ElementType.PRISONER_CONTACT, contact.first, contact.second.prisonerContactId),
        restrictions = restriction.second.map {
          IdPair(ElementType.PRISONER_CONTACT_RESTRICTION, it.first, it.second.prisonerContactRestrictionId)
        },
      )
    }
  }.flatten()

  fun extractAndSavePrisonerContacts(req: MigrateContactRequest, contactId: Long) =
    req.contacts.map { relationship ->
      Pair(
        relationship.id,
        prisonerContactRepository.save(
          PrisonerContactEntity(
            prisonerContactId = 0L,
            contactId = contactId,
            prisonerNumber = relationship.prisonerNumber,
            contactType = relationship.contactType.code,
            relationshipType = relationship.relationshipType.code,
            nextOfKin = relationship.nextOfKin,
            emergencyContact = relationship.emergencyContact,
            comments = relationship.comment,
            active = relationship.active,
            approvedVisitor = relationship.approvedVisitor,
            currentTerm = relationship.currentTerm,
            createdBy = relationship.createUsername ?: "MIGRATION",
            createdTime = relationship.createDateTime ?: LocalDateTime.now(),
          ).also {
            it.amendedBy = relationship.modifyUsername
            it.amendedTime = relationship.modifyDateTime
            it.expiryDate = relationship.expiryDate
          },
        ),
      )
    }

  fun extractAndSavePrisonerContactRestrictions(
    req: MigrateContactRequest,
    contactId: Long,
    prisonerContactPairs: List<Pair<Long, PrisonerContactEntity>>,
  ) = req.contacts.map { relationship ->

    // We need to know the saved prisonerContactId for each of the relationship
    val thisRelationship = prisonerContactPairs.find { it.first == relationship.id }

    Pair(
      relationship.id,
      relationship.restrictions.map { restriction ->
        Pair(
          restriction.id,
          prisonerContactRestrictionRepository.save(
            PrisonerContactRestrictionEntity(
              prisonerContactRestrictionId = 0L,
              prisonerContactId = thisRelationship!!.second.prisonerContactId,
              restrictionType = restriction.restrictionType.code,
              startDate = restriction.startDate,
              expiryDate = restriction.expiryDate,
              comments = restriction.comment,
              staffUsername = restriction.staffUsername,
              createdBy = restriction.createUsername ?: "MIGRATION",
              createdTime = restriction.createDateTime ?: LocalDateTime.now(),
            ).also {
              it.amendedBy = restriction.modifyUsername
              it.amendedTime = restriction.modifyDateTime
            },
          ),
        )
      },
    )
  }
}
