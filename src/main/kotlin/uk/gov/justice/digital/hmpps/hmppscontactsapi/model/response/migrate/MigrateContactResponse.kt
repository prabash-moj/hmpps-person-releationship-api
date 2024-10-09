package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.migrate

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "The migration response for a contact and all of its sub-elements")
data class MigrateContactResponse(

  @Schema(description = "The original ID of the person in NOMIS", example = "123456")
  val nomisPersonId: Long,

  @Schema(description = "The new ID of this contact in the DPS contacts service", example = "123456")
  val dpsContactId: Long,

  @Schema(description = "The last name of the contact created", example = "Doe")
  val lastName: String,

  @Schema(description = "The date of birth of the contact, if known", example = "1980-01-01", nullable = true)
  val dateOfBirth: LocalDate? = null,

  @Schema(description = "The type code of the contact", example = "SOCIAL or OFFICIAL")
  val contactTypeCode: String,

  @Schema(description = "List of Nomis Id and DPS Id for phone numbers")
  val phoneNumbers: List<IdPair> = emptyList(),

  @Schema(description = "List of Nomis Id and DPS Id for addresses")
  val addresses: List<IdPair> = emptyList(),

  @Schema(description = "List of Nomis Id and DPS Id for email addresses")
  val emailAddresses: List<IdPair> = emptyList(),

  @Schema(description = "List of Nomis Id and DPS Id for proofs of identity")
  val identities: List<IdPair> = emptyList(),

  @Schema(description = "List of Nomis Id and DPS Id for restrictions")
  val restrictions: List<IdPair> = emptyList(),

  @Schema(description = "List of Nomis Id and DPS Id for prisoner contacts")
  val prisonerContacts: List<IdPair> = emptyList(),

  @Schema(description = "List of Nomis Id and DPS Id for prisoner contact restrictions")
  val prisonerContactRestrictions: List<IdPair> = emptyList(),
)

/**
 * Class to assemble a type, and two IDs - one in NOMIS, one in DPS Contacts.
 */
data class IdPair(
  @Schema(description = "The category of information returned", example = "PHONE")
  val elementType: ElementType,

  @Schema(description = "The unique ID for this piece of data provided in the request", example = "123435")
  val nomisId: Long = 0,

  @Schema(description = "The unique ID created in the DPS contacts service", example = "1234")
  val dpsId: Long = 0,
)

/**
 * Describes the valid type values for an IdPair object
 */
enum class ElementType(val elementType: String) {
  CONTACT("Contact"),
  PHONE("Phone"),
  EMAIL("Email"),
  ADDRESS("Address"),
  IDENTITY("Identity"),
  RESTRICTION("Restriction"),
  PRISONER_CONTACT("PrisonerContact"),
  PRISONER_CONTACT_RESTRICTION("PrisonerContactRestriction"),
}
