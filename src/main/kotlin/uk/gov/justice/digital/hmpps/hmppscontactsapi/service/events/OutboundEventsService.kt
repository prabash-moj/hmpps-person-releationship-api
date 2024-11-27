package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.config.FeatureSwitches

@Service
class OutboundEventsService(
  private val publisher: OutboundEventsPublisher,
  private val featureSwitches: FeatureSwitches,
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun send(
    outboundEvent: OutboundEvent,
    identifier: Long,
    contactId: Long,
    noms: String = "",
    source: Source = Source.DPS,
  ) {
    if (featureSwitches.isEnabled(outboundEvent)) {
      log.info("Sending outbound event $outboundEvent with source $source for identifier $identifier  (contactId $contactId, noms $noms)")

      when (outboundEvent) {
        OutboundEvent.CONTACT_CREATED,
        OutboundEvent.CONTACT_UPDATED,
        OutboundEvent.CONTACT_DELETED,
        -> {
          sendSafely(outboundEvent, ContactInfo(identifier, source), PersonReference(dpsContactId = contactId))
        }

        OutboundEvent.CONTACT_ADDRESS_CREATED,
        OutboundEvent.CONTACT_ADDRESS_UPDATED,
        OutboundEvent.CONTACT_ADDRESS_DELETED,
        -> {
          sendSafely(outboundEvent, ContactAddressInfo(identifier, source), PersonReference(dpsContactId = contactId))
        }

        OutboundEvent.CONTACT_PHONE_CREATED,
        OutboundEvent.CONTACT_PHONE_UPDATED,
        OutboundEvent.CONTACT_PHONE_DELETED,
        -> {
          sendSafely(outboundEvent, ContactPhoneInfo(identifier, source), PersonReference(dpsContactId = contactId))
        }

        OutboundEvent.CONTACT_ADDRESS_PHONE_CREATED,
        OutboundEvent.CONTACT_ADDRESS_PHONE_UPDATED,
        OutboundEvent.CONTACT_ADDRESS_PHONE_DELETED,
        -> {
          sendSafely(outboundEvent, ContactAddressPhoneInfo(identifier, source), PersonReference(dpsContactId = contactId))
        }

        OutboundEvent.CONTACT_EMAIL_CREATED,
        OutboundEvent.CONTACT_EMAIL_UPDATED,
        OutboundEvent.CONTACT_EMAIL_DELETED,
        -> {
          sendSafely(outboundEvent, ContactEmailInfo(identifier, source), PersonReference(dpsContactId = contactId))
        }

        OutboundEvent.CONTACT_IDENTITY_CREATED,
        OutboundEvent.CONTACT_IDENTITY_UPDATED,
        OutboundEvent.CONTACT_IDENTITY_DELETED,
        -> {
          sendSafely(outboundEvent, ContactIdentityInfo(identifier, source), PersonReference(dpsContactId = contactId))
        }

        OutboundEvent.CONTACT_RESTRICTION_CREATED,
        OutboundEvent.CONTACT_RESTRICTION_UPDATED,
        OutboundEvent.CONTACT_RESTRICTION_DELETED,
        -> {
          sendSafely(outboundEvent, ContactRestrictionInfo(identifier, source), PersonReference(dpsContactId = contactId))
        }

        OutboundEvent.PRISONER_CONTACT_CREATED,
        OutboundEvent.PRISONER_CONTACT_UPDATED,
        OutboundEvent.PRISONER_CONTACT_DELETED,
        -> {
          sendSafely(outboundEvent, PrisonerContactInfo(identifier, source), PersonReference(dpsContactId = contactId, nomsNumber = noms))
        }

        OutboundEvent.PRISONER_CONTACT_RESTRICTION_CREATED,
        OutboundEvent.PRISONER_CONTACT_RESTRICTION_UPDATED,
        OutboundEvent.PRISONER_CONTACT_RESTRICTION_DELETED,
        -> {
          sendSafely(outboundEvent, PrisonerContactRestrictionInfo(identifier, source), PersonReference(dpsContactId = contactId, nomsNumber = noms))
        }
      }
    } else {
      log.warn("Outbound event type $outboundEvent feature is configured off.")
    }
  }

  private fun sendSafely(
    outboundEvent: OutboundEvent,
    additionalInformation: AdditionalInformation,
    personReference: PersonReference?,
  ) {
    try {
      publisher.send(outboundEvent.event(additionalInformation, personReference))
    } catch (e: Exception) {
      log.error(
        "Unable to send event with type {}, info {}, person {}",
        outboundEvent,
        additionalInformation,
        personReference,
        e,
      )
    }
  }
}
