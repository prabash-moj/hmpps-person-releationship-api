package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.migrate

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "The migration response for a contact/person and all of its sub-entities")
data class MigrateContactResponse(
  @Schema(description = "The pair of IDs for this person in NOMIS", example = "123456")
  val contact: IdPair,

  @Schema(description = "The last name of the contact created", example = "Doe")
  val lastName: String,

  @Schema(description = "The date of birth of the contact, if known", example = "1980-01-01", nullable = true)
  val dateOfBirth: LocalDate? = null,

  @Schema(description = "List of Nomis and DPS IDs for person phone numbers")
  val phoneNumbers: List<IdPair> = emptyList(),

  @Schema(description = "List of Nomis and DPS IDs for addresses and address-specific phone numbers")
  val addresses: List<AddressAndPhones> = emptyList(),

  @Schema(description = "List of Nomis and DPS IDs for person email addresses")
  val emailAddresses: List<IdPair> = emptyList(),

  @Schema(description = "List of Nomis and DPS IDs for person proofs of identity")
  val identities: List<IdPair> = emptyList(),

  @Schema(description = "List of Nomis and DPS IDs for person restrictions (visitor restrictions)")
  val restrictions: List<IdPair> = emptyList(),

  @Schema(description = "List of Nomis and DPS IDs for relationships and relationship-specific restrictions")
  val relationships: List<ContactsAndRestrictions> = emptyList(),

  @Schema(description = "List of Nomis and DPS IDs for employments (official contact types only)", nullable = true)
  val employments: List<IdPair>? = emptyList(),
)

/**
 * Class to group together a type and the NOMIS / DPS IDs for it.
 */
data class IdPair(
  @Schema(description = "The category of information returned", example = "PHONE")
  val elementType: ElementType,

  @Schema(description = "The unique ID for this piece of data provided in the request", example = "123435")
  val nomisId: Long,

  @Schema(description = "The unique ID created in the DPS contacts service", example = "1234")
  val dpsId: Long,
)

/**
 * Class to return the IDs for an address and a list of associated phone numbers
 */
data class AddressAndPhones(
  @Schema(description = "The unique IDs in NOMIS and DPS for this address")
  val address: IdPair,

  @Schema(description = "The pairs of IDs in NOMIS and DPS for address-specific phone numbers")
  val phones: List<IdPair>,
)

/**
 * Class to return the IDs for an address and a list of associated phone numbers
 */
data class ContactsAndRestrictions(
  @Schema(description = "The unique IDs in NOMIS and DPS for this relationship")
  val relationship: IdPair,

  @Schema(description = "The pairs of IDs in NOMIS and DPS for relationship-specific restrictions")
  val restrictions: List<IdPair>,
)

/**
 * Describes the valid type values for an IdPair object
 */
enum class ElementType(val elementType: String) {
  CONTACT("Contact"),
  PHONE("Phone"),
  EMAIL("Email"),
  ADDRESS("Address"),
  ADDRESS_PHONE("AddressPhone"),
  IDENTITY("Identity"),
  RESTRICTION("Restriction"),
  PRISONER_CONTACT("PrisonerContact"),
  PRISONER_CONTACT_RESTRICTION("PrisonerContactRestriction"),
  EMPLOYMENT("Employment"),
}
