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

  fun send(outboundEvent: OutboundEvent, identifier: Long) {
    if (featureSwitches.isEnabled(outboundEvent)) {
      log.info("Sending outbound event $outboundEvent for identifier $identifier")
      when (outboundEvent) {
        OutboundEvent.CONTACT_CREATED,
        OutboundEvent.CONTACT_AMENDED,
        OutboundEvent.CONTACT_DELETED,
        -> {
          sendSafely(outboundEvent, ContactInfo(identifier))
        }

        OutboundEvent.CONTACT_ADDRESS_CREATED,
        OutboundEvent.CONTACT_ADDRESS_AMENDED,
        OutboundEvent.CONTACT_ADDRESS_DELETED,
        -> {
          sendSafely(outboundEvent, ContactAddressInfo(identifier))
        }

        OutboundEvent.CONTACT_PHONE_CREATED,
        OutboundEvent.CONTACT_PHONE_AMENDED,
        OutboundEvent.CONTACT_PHONE_DELETED,
        -> {
          sendSafely(outboundEvent, ContactPhoneInfo(identifier))
        }

        OutboundEvent.CONTACT_EMAIL_CREATED,
        OutboundEvent.CONTACT_EMAIL_AMENDED,
        OutboundEvent.CONTACT_EMAIL_DELETED,
        -> {
          sendSafely(outboundEvent, ContactEmailInfo(identifier))
        }

        OutboundEvent.CONTACT_IDENTITY_CREATED,
        OutboundEvent.CONTACT_IDENTITY_AMENDED,
        OutboundEvent.CONTACT_IDENTITY_DELETED,
        -> {
          sendSafely(outboundEvent, ContactIdentityInfo(identifier))
        }

        OutboundEvent.CONTACT_RESTRICTION_CREATED,
        OutboundEvent.CONTACT_RESTRICTION_AMENDED,
        OutboundEvent.CONTACT_RESTRICTION_DELETED,
        -> {
          sendSafely(outboundEvent, ContactRestrictionInfo(identifier))
        }

        OutboundEvent.PRISONER_CONTACT_CREATED,
        OutboundEvent.PRISONER_CONTACT_AMENDED,
        OutboundEvent.PRISONER_CONTACT_DELETED,
        -> {
          sendSafely(outboundEvent, PrisonerContactInfo(identifier))
        }

        OutboundEvent.PRISONER_CONTACT_RESTRICTION_CREATED,
        OutboundEvent.PRISONER_CONTACT_RESTRICTION_AMENDED,
        OutboundEvent.PRISONER_CONTACT_RESTRICTION_DELETED,
        -> {
          sendSafely(outboundEvent, PrisonerContactRestrictionInfo(identifier))
        }
      }
    } else {
      log.warn("Outbound event type $outboundEvent feature is configured off.")
    }
  }

  private fun sendSafely(
    outboundEvent: OutboundEvent,
    info: AdditionalInformation,
  ) {
    try {
      publisher.send(outboundEvent.event(info))
    } catch (e: Exception) {
      log.error("Unable to send event with type {} and info {}", outboundEvent, info, e)
    }
  }
}
