package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events

import java.time.LocalDateTime

/**
 * An enum class containing all events that can be raised from the service.
 * Each can tailor its own AdditionalInformation and PersonReference content.
 */
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

/**
 * Base class for the additional information within events.
 * This is inherited and expanded individually for each event type.
 */

open class AdditionalInformation(open val source: Source)

/**
 * The class representing outbound domain events
 */
data class OutboundHMPPSDomainEvent(
  val eventType: String,
  val additionalInformation: AdditionalInformation,
  val personReference: PersonReference? = null,
  val version: String = "1",
  val description: String,
  val occurredAt: LocalDateTime = LocalDateTime.now(),
)

/**
 * These are classes which define the different event content for AdditionalInformation.
 * All inherit the base class AdditionalInformation and extend it to contain the required fields.
 * The additional information is mapped into JSON by the ObjectMapper as part of the event body.
 */

data class ContactInfo(val contactId: Long, override val source: Source = Source.DPS) : AdditionalInformation(source)
data class ContactAddressInfo(val contactAddressId: Long, override val source: Source = Source.DPS) : AdditionalInformation(source)
data class ContactPhoneInfo(val contactPhoneId: Long, override val source: Source = Source.DPS) : AdditionalInformation(source)
data class ContactEmailInfo(val contactEmailId: Long, override val source: Source = Source.DPS) : AdditionalInformation(source)
data class ContactIdentityInfo(val contactIdentityId: Long, override val source: Source = Source.DPS) : AdditionalInformation(source)
data class ContactRestrictionInfo(val contactRestrictionId: Long, override val source: Source = Source.DPS) : AdditionalInformation(source)
data class PrisonerContactInfo(val prisonerContactId: Long, override val source: Source = Source.DPS) : AdditionalInformation(source)
data class PrisonerContactRestrictionInfo(val prisonerContactRestrictionId: Long, override val source: Source = Source.DPS) : AdditionalInformation(source)

/**
 * The event source.
 * When data is changed within the DPS Contacts service by UI action or local process, events will have the source DPS.
 * When data is changed as a result of receiving a sync event, events will have the source NOMIS.
 */
enum class Source { DPS, NOMIS }

/**
 * Each event will provide a reference to the person (or people) it relates to.
 * In most cases this will be the contact, e.g. DPS_CONTACT_ID = contactId.
 * When an event relates to more than one person e.g. a relationship between prisoner and contact, the
 * PersonReference will contain both identifiers - e.g. NOMS = prisonerNumber, DPS_CONTACT_ID = contactId.
 */
enum class Identifier { NOMS, DPS_CONTACT_ID }
data class PersonIdentifier(val type: Identifier, val value: String)

/**
 * The PersonReference contain the list of identifiers related to the subject of the event.
 * Most events contain just one person reference - the DPS_CONTACT_ID.
 * Some events relate to a relationship with a prisoner, so also have the NOMS number.
 */
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
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as PersonReference

    return identifiers == other.identifiers
  }

  override fun toString(): String = this.identifiers.toString()
}
