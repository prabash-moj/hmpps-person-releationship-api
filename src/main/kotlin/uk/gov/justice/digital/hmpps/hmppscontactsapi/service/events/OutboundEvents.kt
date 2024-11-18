package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events

import java.time.LocalDateTime

enum class OutboundEvent(val eventType: String) {
  CONTACT_CREATED("contacts-api.contact.created") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact has been created",
      )
  },
  CONTACT_AMENDED("contacts-api.contact.amended") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact has been amended",
      )
  },
  CONTACT_DELETED("contacts-api.contact.deleted") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact has been deleted",
      )
  },
  CONTACT_ADDRESS_CREATED("contacts-api.contact-address.created") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact address has been created",
      )
  },
  CONTACT_ADDRESS_AMENDED("contacts-api.contact-address.amended") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact address has been amended",
      )
  },
  CONTACT_ADDRESS_DELETED("contacts-api.contact-address.deleted") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact address has been deleted",
      )
  },
  CONTACT_PHONE_CREATED("contacts-api.contact-phone.created") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact phone number has been created",
      )
  },
  CONTACT_PHONE_AMENDED("contacts-api.contact-phone.amended") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact phone number has been amended",
      )
  },
  CONTACT_PHONE_DELETED("contacts-api.contact-phone.deleted") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact phone number has been deleted",
      )
  },
  CONTACT_EMAIL_CREATED("contacts-api.contact-email.created") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact email address has been created",
      )
  },
  CONTACT_EMAIL_AMENDED("contacts-api.contact-email.amended") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact email address has been amended",
      )
  },
  CONTACT_EMAIL_DELETED("contacts-api.contact-email.deleted") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact email address has been deleted",
      )
  },
  CONTACT_IDENTITY_CREATED("contacts-api.contact-identity.created") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact proof of identity has been created",
      )
  },
  CONTACT_IDENTITY_AMENDED("contacts-api.contact-identity.amended") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact proof of identity has been amended",
      )
  },
  CONTACT_IDENTITY_DELETED("contacts-api.contact-identity.deleted") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact proof of identity has been deleted",
      )
  },
  CONTACT_RESTRICTION_CREATED("contacts-api.contact-restriction.created") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact restriction has been created",
      )
  },
  CONTACT_RESTRICTION_AMENDED("contacts-api.contact-restriction.amended") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact restriction has been amended",
      )
  },
  CONTACT_RESTRICTION_DELETED("contacts-api.contact-restriction.deleted") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A contact restriction has been deleted",
      )
  },
  PRISONER_CONTACT_CREATED("contacts-api.prisoner-contact.created") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A prisoner contact has been created",
      )
  },
  PRISONER_CONTACT_AMENDED("contacts-api.prisoner-contact.amended") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A prisoner contact has been amended",
      )
  },
  PRISONER_CONTACT_DELETED("contacts-api.prisoner-contact.deleted") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A prisoner contact has been deleted",
      )
  },
  PRISONER_CONTACT_RESTRICTION_CREATED("contacts-api.prisoner-contact-restriction.created") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A prisoner contact restriction has been created",
      )
  },
  PRISONER_CONTACT_RESTRICTION_AMENDED("contacts-api.prisoner-contact-restriction.amended") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A prisoner contact restriction has been amended",
      )
  },
  PRISONER_CONTACT_RESTRICTION_DELETED("contacts-api.prisoner-contact-restriction.deleted") {
    override fun event(additionalInformation: AdditionalInformation, personReference: PersonReference?) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        personReference = personReference,
        description = "A prisoner contact restriction has been deleted",
      )
  },
  ;

  abstract fun event(
    additionalInformation: AdditionalInformation,
    personReference: PersonReference? = null,
  ): OutboundHMPPSDomainEvent
}

abstract class AdditionalInformation {
  val source: Source = Source.DPS
}

data class OutboundHMPPSDomainEvent(
  val eventType: String,
  val additionalInformation: AdditionalInformation,
  val personReference: PersonReference? = null,
  val version: String = "1",
  val description: String,
  val occurredAt: LocalDateTime = LocalDateTime.now(),
)

// Event content - this is mapped into JSON by the ObjectMapper as the event body
data class ContactInfo(val contactId: Long) : AdditionalInformation()
data class ContactAddressInfo(val contactAddressId: Long) : AdditionalInformation()
data class ContactPhoneInfo(val contactPhoneId: Long) : AdditionalInformation()
data class ContactEmailInfo(val contactEmailId: Long) : AdditionalInformation()
data class ContactIdentityInfo(val contactIdentityId: Long) : AdditionalInformation()
data class ContactRestrictionInfo(val contactRestrictionId: Long) : AdditionalInformation()
data class PrisonerContactInfo(val prisonerContactId: Long) : AdditionalInformation()
data class PrisonerContactRestrictionInfo(val prisonerContactRestrictionId: Long) : AdditionalInformation()
enum class Source { DPS }
enum class Identifier { NOMS, DPS_CONTACT_ID }
data class PersonIdentifier(val type: Identifier, val value: String)

class PersonReference(personIdentifiers: List<PersonIdentifier>) {
  constructor(nomsNumber: String, dpsContactId: Long) : this(
    listOf(
      PersonIdentifier(Identifier.NOMS, nomsNumber),
      PersonIdentifier(Identifier.DPS_CONTACT_ID, dpsContactId.toString()),
    ),
  )

  constructor(dpsContactId: Long) : this(
    listOf(
      PersonIdentifier(Identifier.DPS_CONTACT_ID, dpsContactId.toString()),
    ),
  )

  @Suppress("MemberVisibilityCanBePrivate")
  val identifiers: List<PersonIdentifier> = personIdentifiers

  fun nomsNumber(): String? = identifiers.find { it.type == Identifier.NOMS }?.value
  fun dpsContactId(): String? = identifiers.find { it.type == Identifier.DPS_CONTACT_ID }?.value
}
