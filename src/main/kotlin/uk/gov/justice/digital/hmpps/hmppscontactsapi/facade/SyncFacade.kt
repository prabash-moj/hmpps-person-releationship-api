package uk.gov.justice.digital.hmpps.hmppscontactsapi.facade

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateContactEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateContactIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateContactPhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreatePrisonerContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncUpdateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncUpdateContactEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncUpdateContactIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncUpdateContactPhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncUpdateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncUpdateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncUpdatePrisonerContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncUpdatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.Source
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncContactAddressService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncContactEmailService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncContactIdentityService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncContactPhoneService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncContactRestrictionService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncContactService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncPrisonerContactRestrictionService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncPrisonerContactService

/**
 * This class is a facade over the sync services as a thin layer
 * which is called by the sync controllers and in-turn calls the sync
 * service methods.
 *
 * Each method provides two purposes:
 * - To call the underlying sync services and apply the changes in a transactional method.
 * - To generate a domain event to inform subscribed services what has happened.
 *
 * All events generated as a result of a sync operation should generate domain events with the
 * additionalInformation.source = "NOMIS", which indicates that the actual source of the change
 * was in NOMIS.
 *
 * This is important as the Syscon sync service will ignore domain events with
 * a source of NOMIS but will action those with a source of DPS (changes originating within
 * this service).
 */
@Service
class SyncFacade(
  private val syncContactService: SyncContactService,
  private val syncContactPhoneService: SyncContactPhoneService,
  private val syncContactAddressService: SyncContactAddressService,
  private val syncContactEmailService: SyncContactEmailService,
  private val syncContactIdentityService: SyncContactIdentityService,
  private val syncContactRestrictionService: SyncContactRestrictionService,
  private val syncPrisonerContactService: SyncPrisonerContactService,
  private val syncPrisonerContactRestrictionService: SyncPrisonerContactRestrictionService,
  private val outboundEventsService: OutboundEventsService,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  // ================================================================
  //  Contact
  // ================================================================

  fun getContactById(contactId: Long) =
    syncContactService.getContactById(contactId)

  fun createContact(request: SyncCreateContactRequest) =
    syncContactService.createContact(request)
      .also {
        outboundEventsService.send(
          outboundEvent = OutboundEvent.CONTACT_CREATED,
          identifier = it.id,
          contactId = it.id,
          source = Source.NOMIS,
        )
      }

  fun updateContact(contactId: Long, request: SyncUpdateContactRequest) =
    syncContactService.updateContact(contactId, request)
      .also {
        outboundEventsService.send(
          outboundEvent = OutboundEvent.CONTACT_UPDATED,
          identifier = it.id,
          contactId = it.id,
          source = Source.NOMIS,
        )
      }

  fun deleteContact(contactId: Long) =
    syncContactService.deleteContact(contactId)
      .also {
        outboundEventsService.send(
          outboundEvent = OutboundEvent.CONTACT_DELETED,
          identifier = contactId,
          contactId = contactId,
          source = Source.NOMIS,
        )
      }

  // ================================================================
  //  Contact Phone
  // ================================================================

  fun getContactPhoneById(contactPhoneId: Long) =
    syncContactPhoneService.getContactPhoneById(contactPhoneId)

  fun createContactPhone(request: SyncCreateContactPhoneRequest) =
    syncContactPhoneService.createContactPhone(request)
      .also {
        outboundEventsService.send(
          outboundEvent = OutboundEvent.CONTACT_PHONE_CREATED,
          identifier = it.contactPhoneId,
          contactId = it.contactId,
          source = Source.NOMIS,
        )
      }

  fun updateContactPhone(contactPhoneId: Long, request: SyncUpdateContactPhoneRequest) =
    syncContactPhoneService.updateContactPhone(contactPhoneId, request)
      .also {
        outboundEventsService.send(
          outboundEvent = OutboundEvent.CONTACT_PHONE_UPDATED,
          identifier = it.contactPhoneId,
          contactId = it.contactId,
          source = Source.NOMIS,
        )
      }

  fun deleteContactPhone(contactPhoneId: Long) =
    syncContactPhoneService.deleteContactPhone(contactPhoneId)
      .also {
        outboundEventsService.send(
          outboundEvent = OutboundEvent.CONTACT_PHONE_DELETED,
          identifier = contactPhoneId,
          contactId = it.contactId,
          source = Source.NOMIS,
        )
      }

  // ================================================================
  //  Contact Email
  // ================================================================

  fun getContactEmailById(contactEmailId: Long) =
    syncContactEmailService.getContactEmailById(contactEmailId)

  fun createContactEmail(request: SyncCreateContactEmailRequest) =
    syncContactEmailService.createContactEmail(request)
      .also {
        outboundEventsService.send(
          outboundEvent = OutboundEvent.CONTACT_EMAIL_CREATED,
          identifier = it.contactEmailId,
          contactId = it.contactId,
          source = Source.NOMIS,
        )
      }

  fun updateContactEmail(contactEmailId: Long, request: SyncUpdateContactEmailRequest) =
    syncContactEmailService.updateContactEmail(contactEmailId, request)
      .also {
        outboundEventsService.send(
          outboundEvent = OutboundEvent.CONTACT_EMAIL_UPDATED,
          identifier = it.contactEmailId,
          contactId = it.contactId,
          source = Source.NOMIS,
        )
      }

  fun deleteContactEmail(contactEmailId: Long) =
    syncContactEmailService.deleteContactEmail(contactEmailId)
      .also {
        outboundEventsService.send(
          outboundEvent = OutboundEvent.CONTACT_EMAIL_DELETED,
          identifier = it.contactEmailId,
          contactId = it.contactId,
          source = Source.NOMIS,
        )
      }

  // ================================================================
  //  Contact Identity
  // ================================================================

  fun getContactIdentityById(contactIdentityId: Long) =
    syncContactIdentityService.getContactIdentityById(contactIdentityId)

  fun createContactIdentity(request: SyncCreateContactIdentityRequest) =
    syncContactIdentityService.createContactIdentity(request)
      .also {
        outboundEventsService.send(
          outboundEvent = OutboundEvent.CONTACT_IDENTITY_CREATED,
          identifier = it.contactIdentityId,
          contactId = it.contactId,
          source = Source.NOMIS,
        )
      }

  fun updateContactIdentity(contactIdentityId: Long, request: SyncUpdateContactIdentityRequest) =
    syncContactIdentityService.updateContactIdentity(contactIdentityId, request)
      .also {
        outboundEventsService.send(
          outboundEvent = OutboundEvent.CONTACT_IDENTITY_UPDATED,
          identifier = it.contactIdentityId,
          contactId = it.contactId,
          source = Source.NOMIS,
        )
      }

  fun deleteContactIdentity(contactIdentityId: Long) =
    syncContactIdentityService.deleteContactIdentity(contactIdentityId)
      .also {
        outboundEventsService.send(
          outboundEvent = OutboundEvent.CONTACT_IDENTITY_DELETED,
          identifier = it.contactIdentityId,
          contactId = it.contactId,
          source = Source.NOMIS,
        )
      }

  // ================================================================
  //  Contact Restriction
  // ================================================================

  fun getContactRestrictionById(contactRestrictionId: Long) =
    syncContactRestrictionService.getContactRestrictionById(contactRestrictionId)

  fun createContactRestriction(request: SyncCreateContactRestrictionRequest) =
    syncContactRestrictionService.createContactRestriction(request)
      .also {
        logger.info("TODO - Create contact restriction domain event")
      }

  fun updateContactRestriction(contactRestrictionId: Long, request: SyncUpdateContactRestrictionRequest) =
    syncContactRestrictionService.updateContactRestriction(contactRestrictionId, request)
      .also {
        logger.info("TODO - Update contact restriction domain event")
      }

  fun deleteContactRestriction(contactRestrictionId: Long) =
    syncContactRestrictionService.deleteContactRestriction(contactRestrictionId)
      .also {
        logger.info("TODO - Delete contact restriction domain event")
      }

  // ================================================================
  //  Contact Address
  // ================================================================

  fun getContactAddressById(contactAddressId: Long) =
    syncContactAddressService.getContactAddressById(contactAddressId)

  fun createContactAddress(request: SyncCreateContactAddressRequest) =
    syncContactAddressService.createContactAddress(request)
      .also {
        logger.info("TODO - Create contact address domain event")
      }

  fun updateContactAddress(contactAddressId: Long, request: SyncUpdateContactAddressRequest) =
    syncContactAddressService.updateContactAddress(contactAddressId, request)
      .also {
        logger.info("TODO - Update contact address domain event")
      }

  fun deleteContactAddress(contactAddressId: Long) =
    syncContactAddressService.deleteContactAddress(contactAddressId)
      .also {
        logger.info("TODO - Delete contact address domain event")
      }

  // ================================================================
  //  Prisoner Contact
  // ================================================================

  fun getPrisonerContactById(prisonerContactId: Long) =
    syncPrisonerContactService.getPrisonerContactById(prisonerContactId)

  fun createPrisonerContact(request: SyncCreatePrisonerContactRequest) =
    syncPrisonerContactService.createPrisonerContact(request)
      .also {
        logger.info("TODO - Create prisoner contact domain event")
      }

  fun updatePrisonerContact(prisonerContactId: Long, request: SyncUpdatePrisonerContactRequest) =
    syncPrisonerContactService.updatePrisonerContact(prisonerContactId, request)
      .also {
        logger.info("TODO - Update prisoner contact domain event")
      }

  fun deletePrisonerContact(prisonerContactId: Long) =
    syncPrisonerContactService.deletePrisonerContact(prisonerContactId)
      .also {
        logger.info("TODO - Delete prisoner contact domain event")
      }

  // ================================================================
  //  Prisoner Contact Restriction
  // ================================================================

  fun getPrisonerContactRestrictionById(prisonerContactRestrictionId: Long) =
    syncPrisonerContactRestrictionService.getPrisonerContactRestrictionById(prisonerContactRestrictionId)

  fun createPrisonerContactRestriction(request: SyncCreatePrisonerContactRestrictionRequest) =
    syncPrisonerContactRestrictionService.createPrisonerContactRestriction(request)
      .also {
        logger.info("TODO - Create prisoner contact restriction domain event")
      }

  fun updatePrisonerContactRestriction(prisonerContactRestrictionId: Long, request: SyncUpdatePrisonerContactRestrictionRequest) =
    syncPrisonerContactRestrictionService.updatePrisonerContactRestriction(prisonerContactRestrictionId, request)
      .also {
        logger.info("TODO - Update prisoner contact restriction domain event")
      }

  fun deletePrisonerContactRestriction(prisonerContactRestrictionId: Long) =
    syncPrisonerContactRestrictionService.deletePrisonerContactRestriction(prisonerContactRestrictionId)
      .also {
        logger.info("TODO - Delete prisoner contact restriction domain event")
      }
}
