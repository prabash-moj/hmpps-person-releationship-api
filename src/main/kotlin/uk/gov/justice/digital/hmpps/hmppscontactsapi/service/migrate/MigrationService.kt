package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.migrate

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactAddressEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEmailEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactIdentityEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactPhoneEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.migrate.MigrateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.migrate.ElementType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.migrate.IdPair
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.migrate.MigrateContactResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactAddressRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactEmailRepository
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
  private val contactPhoneRepository: ContactPhoneRepository,
  private val contactEmailRepository: ContactEmailRepository,
  private val contactIdentityRepository: ContactIdentityRepository,
  private val contactRestrictionRepository: ContactRestrictionRepository,
  private val prisonerContactRepository: PrisonerContactRepository,
  private val prisonerContactRestrictionRepository: PrisonerContactRestrictionRepository,
) {

  /**
   * Accept a full contact migration object, validate and create the equivalent objects
   * in the contacts database locally. Do NOT emit sync events for objects created as these
   * will already exist in NOMIS and the mapping tables - we're not generating new copies of
   * this data, as we did for A&A, these are the same copies as currently exist.
   *
   * Assemble a response object to contain both the NOMIS and DPS IDs for each
   * entity and sub-entity saved to allow the mapping service to link them.
   */
  fun migrateContact(request: MigrateContactRequest): MigrateContactResponse {
    // TODO: Any minimal additional validation? Over and above the request body annotated validations

    val contactPair = extractAndSaveContact(request)
    val phoneNumberPairs = extractAndSavePhones(request, contactPair.second.contactId)
    val addressPairs = extractAndSaveAddresses(request, contactPair.second.contactId)
    val emailPairs = extractAndSaveEmails(request, contactPair.second.contactId)
    val identityPairs = extractAndSaveIdentities(request, contactPair.second.contactId)

    // TODO: Add restrictions, prisoner contacts, prisoner contact restrictions, employments

    return MigrateContactResponse(
      nomisPersonId = contactPair.first,
      dpsContactId = contactPair.second.contactId,
      lastName = contactPair.second.lastName,
      phoneNumbers = phoneNumberPairs.map { IdPair(ElementType.PHONE, it.first, it.second.contactPhoneId) },
      addresses = addressPairs.map { IdPair(ElementType.ADDRESS, it.first, it.second.contactAddressId) },
      emailAddresses = emailPairs.map { IdPair(ElementType.EMAIL, it.first, it.second.contactEmailId) },
      identities = identityPairs.map { IdPair(ElementType.IDENTITY, it.first, it.second.contactIdentityId) },
      restrictions = emptyList(),
      prisonerContacts = emptyList(),
      prisonerContactRestrictions = emptyList(),
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
          createdBy = req.audit?.createUsername ?: "MIGRATION",
          createdTime = req.audit?.createDateTime ?: LocalDateTime.now(),
        ).also {
          it.staffFlag = req.staff
          it.gender = req.gender?.code
          it.languageCode = req.language?.code
          it.domesticStatus = req.domesticStatus?.code
          // it.active
          // it.placeOfBirth
          it.interpreterRequired = req.interpreterRequired
          // it.comments
          it.amendedBy = req.audit?.modifyUserId
          it.amendedTime = req.audit?.modifyDateTime
        },
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
            createdBy = req.audit?.createUsername ?: "MIGRATION",
            createdTime = req.audit?.createDateTime ?: LocalDateTime.now(),
          ).also {
            it.amendedBy = req.audit?.modifyUserId
            it.amendedTime = req.audit?.modifyDateTime
          },
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
            addressType = "HOME",
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
            // TODO: Check any linked phone numbers are also supplied as phone numbers!
            createdBy = req.audit?.createUsername ?: "MIGRATION",
            createdTime = req.audit?.createDateTime ?: LocalDateTime.now(),
          ).also {
            it.amendedBy = req.audit?.modifyUserId
            it.amendedTime = req.audit?.modifyDateTime
          },
        ),
      )
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
            createdBy = req.audit?.createUsername ?: "MIGRATION",
            createdTime = req.audit?.createDateTime ?: LocalDateTime.now(),
          ).also {
            it.amendedBy = req.audit?.modifyUserId
            it.amendedTime = req.audit?.modifyDateTime
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
            createdBy = req.audit?.createUsername ?: "MIGRATION",
            createdTime = req.audit?.createDateTime ?: LocalDateTime.now(),
          ).also {
            it.amendedBy = req.audit?.modifyUserId
            it.amendedTime = req.audit?.modifyDateTime
          },
        ),
      )
    }
}
