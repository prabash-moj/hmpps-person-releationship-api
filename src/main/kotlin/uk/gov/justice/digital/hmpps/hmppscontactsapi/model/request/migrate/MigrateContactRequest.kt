package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.migrate

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.time.LocalDateTime

abstract class AbstractAuditable(
  @Schema(description = "The data and time the record was created", nullable = true)
  var createDateTime: LocalDateTime? = null,

  @Schema(description = "The username who created the row", nullable = true)
  var createUsername: String? = null,

  @Schema(description = "The date and time the record was last amended", nullable = true)
  var modifyDateTime: LocalDateTime? = null,

  @Schema(description = "The username who last modified the row", nullable = true)
  var modifyUsername: String? = null,
)

@Schema(description = "Request to migrate a contact and all of its sub-elements from NOMIS into this service")
data class MigrateContactRequest(
  @Schema(description = "The person ID from NOMIS", example = "1233323")
  @field:NotNull(message = "The NOMIS person ID must be present in the request")
  val personId: Long,

  @Schema(description = "The first name of the contact", example = "John", maxLength = 35)
  @field:Size(max = 35, message = "firstName must be <= 35 characters")
  val firstName: String,

  @Schema(description = "The last name of the contact", example = "Doe", maxLength = 35)
  @field:Size(max = 35, message = "lastName must be <= 35 characters")
  val lastName: String,

  @Schema(description = "The middle name of the contact, if any", example = "William", nullable = true, maxLength = 35)
  @field:Size(max = 35, message = "middleName must be <= 35 characters")
  val middleName: String? = null,

  @Schema(description = "The date of birth of the contact, if known", example = "1980-01-01", nullable = true, format = "yyyy-MM-dd")
  @field:DateTimeFormat(pattern = "yyyy-MM-dd")
  val dateOfBirth: LocalDate? = null,

  @Schema(description = "The gender of the contact", nullable = true)
  val gender: CodedValue? = null,

  @Schema(description = "The title of the contact, if any", nullable = true)
  val title: CodedValue? = null,

  @Schema(description = "The main language spoken by this contact", nullable = true)
  val language: CodedValue? = null,

  @Schema(description = "Interpreter required", nullable = true)
  val interpreterRequired: Boolean = false,

  @Schema(description = "The domestic status coded value", nullable = true, example = "MARRIED")
  val domesticStatus: CodedValue? = null,

  @Schema(description = "The date this persons was marked as deceased", nullable = true)
  val deceasedDate: LocalDate? = null,

  @Schema(description = "This person is staff")
  val staff: Boolean = false,

  @Schema(description = "Telephone numbers", nullable = true)
  val phoneNumbers: List<MigratePhoneNumber> = emptyList(),

  @Schema(description = "Addresses", nullable = true)
  val addresses: List<MigrateAddress> = emptyList(),

  @Schema(description = "Email addresses", nullable = true)
  val emailAddresses: List<MigrateEmailAddress> = emptyList(),

  @Schema(description = "Employments for official contacts only", nullable = true)
  val employments: List<MigrateEmployment> = emptyList(),

  @Schema(description = "Proofs of identity", nullable = true)
  val identifiers: List<MigrateIdentifier> = emptyList(),

  @Schema(description = "The relationships with prisoners including specific restrictions for each", nullable = true)
  val contacts: List<MigrateRelationship> = emptyList(),

  @Schema(description = "The restrictions which apply to this person only", nullable = true)
  val restrictions: List<MigrateRestriction> = emptyList(),

) : AbstractAuditable()

data class CodedValue(
  @Schema(description = "A code representation a NOMIS reference data item", maxLength = 12)
  @field:Size(max = 12, message = "Title coded value must be <= 12 characters")
  val code: String,

  @Schema(description = "The text description for this coded value in NOMIS")
  val description: String,
)

data class MigratePhoneNumber(
  @Schema(description = "Unique phone ID in NOMIS", example = "123")
  val phoneId: Long,

  @Schema(description = "Telephone number", example = "098989 98989893")
  val number: String,

  @Schema(description = "Extension number", nullable = true, example = "100")
  val extension: String,

  @Schema(description = "Phone number type", example = "HOME")
  val type: CodedValue,
) : AbstractAuditable()

data class MigrateAddress(
  @Schema(description = "Unique address ID in NOMIS", example = "123")
  val addressId: Long,

  @Schema(description = "Address type coded value", example = "HOME")
  val type: CodedValue,

  @Schema(description = "Flat number or identifier", nullable = true, example = "1B")
  val flat: String? = null,

  @Schema(description = "House name or number", nullable = true, example = "43")
  val premise: String? = null,

  @Schema(description = "Street or road", nullable = true, example = "Main Street")
  val street: String? = null,

  @Schema(description = "Locality", nullable = true, example = "Keighley")
  val locality: String? = null,

  @Schema(description = "Postcode", nullable = true, example = "BD12 8RD")
  val postCode: String? = null,

  @Schema(description = "City - code and description", nullable = true)
  val city: CodedValue? = null,

  @Schema(description = "County - code and description", nullable = true)
  val county: CodedValue? = null,

  @Schema(description = "Country - code and description", nullable = true)
  val country: CodedValue? = null,

  @Schema(description = "Address validated by postcode lookup", nullable = true, example = "false")
  val validatedPAF: Boolean = false,

  @Schema(description = "If true this address should be considered as no fixed address", nullable = true, example = "false")
  val noFixedAddress: Boolean = false,

  @Schema(description = "If true this address should be considered as the primary residential address", nullable = true, example = "true")
  val primaryAddress: Boolean = false,

  @Schema(description = "If true this address should be considered for sending mail to", nullable = true, example = "true")
  val mailAddress: Boolean = false,

  @Schema(description = "Comments relating to this address", nullable = true, example = "A comment")
  val comment: String? = null,

  @Schema(description = "The date this address should be considered valid from", nullable = true, example = "2018-10-01")
  val startDate: LocalDate? = null,

  @Schema(description = "The date this address should be considered valid to", nullable = true, example = "2022-04-04")
  val endDate: LocalDate? = null,

  @Schema(description = "A list of phone numbers which are linked to this address")
  val phoneNumbers: List<MigratePhoneNumber> = emptyList(),

) : AbstractAuditable()

data class MigrateEmailAddress(
  @Schema(description = "Unique email ID in NOMIS", example = "123")
  val emailAddressId: Long,

  @Schema(description = "Email address", example = "sender@a.com")
  val email: String,

) : AbstractAuditable()

data class MigrateIdentifier(
  @Schema(description = "Unique sequence ID in NOMIS", example = "123")
  val sequence: Long,

  @Schema(description = "Coded value for proof of ID type", example = "XXX")
  val type: CodedValue,

  @Schema(description = "The identifying information e.g. driving licence number", example = "KJ 45544 JFKJK")
  val identifier: String,

  @Schema(description = "The issuing authority for this identifier", nullable = true, example = "DVLA")
  val issuedAuthority: String?,

) : AbstractAuditable()

data class MigrateRestriction(
  @Schema(description = "Unique ID in NOMIS for this restriction", example = "123")
  val id: Long,

  @Schema(description = "Coded value for this restriction type", example = "NO")
  val type: CodedValue,

  @Schema(description = "Comments relating to this restriction", nullable = true, example = "A comment")
  val comment: String? = null,

  @Schema(description = "The date that this restriction is effective from", nullable = true, example = "2024-01-01")
  val effectiveDate: LocalDate,

  @Schema(description = "The date that this restriction expires and stops being enforced", nullable = true, example = "2024-03-01")
  val expiryDate: LocalDate? = null,

) : AbstractAuditable()

data class MigrateEmployment(
  @Schema(description = "Unique sequence ID in NOMIS for this employment", example = "123")
  val sequence: Long,

  @Schema(description = "The corporate organisation this person works for")
  val corporate: Corporate,

  @Schema(description = "Comments relating to this restriction", example = "true")
  val active: Boolean = false,

) : AbstractAuditable()

data class Corporate(
  @Schema(description = "The corporate ID in NOMIS", example = "123")
  val id: Long,

  @Schema(description = "The name of the corporate organisation", example = "West Midlands Police")
  val name: String,
)

data class MigrateRelationship(
  @Schema(description = "The ID in NOMIS", example = "123")
  val id: Long,

  @Schema(description = "Coded value indicating either a social or official contact", examples = ["SOCIAL", "OFFICIAL"])
  val contactType: CodedValue,

  @Schema(description = "Coded value indicating the type of relationship - from reference data", example = "MOTHER")
  val relationshipType: CodedValue,

  @Schema(description = "True if this relationship applies to the latest or current term in prison, false if a previous term", nullable = true, example = "true")
  val currentTerm: Boolean = true,

  @Schema(description = "The relationship is active", example = "true")
  val active: Boolean = false,

  @Schema(description = "The date that this relationship expired", nullable = true, example = "2024-03-01")
  val expiryDate: LocalDate? = null,

  @Schema(description = "Approved visitor", example = "true")
  val approvedVisitor: Boolean = false,

  @Schema(description = "Next of kin", example = "true")
  val nextOfKin: Boolean = false,

  @Schema(description = "Emergency contact", example = "true")
  val emergencyContact: Boolean = false,

  @Schema(description = "Comment on this relationship", nullable = true, example = "This is an optional comment")
  val comment: String?,

  @Schema(description = "The prisoner number (NOMS ID) related", example = "A1234AA")
  val prisonerNumber: String,

  @Schema(description = "The restrictions for this prisoner contact relationship")
  val restrictions: List<MigratePrisonerContactRestriction> = emptyList(),

) : AbstractAuditable()

data class MigratePrisonerContactRestriction(
  @Schema(description = "The ID in NOMIS", example = "123")
  val id: Long,

  @Schema(description = "Coded value indicating the restriction type from reference data", example = "NO_CONTACT")
  val restrictionType: CodedValue,

  @Schema(description = "Comment on this restriction", nullable = true, example = "Comment on restriction")
  val comment: String?,

  @Schema(description = "The date that this restriction took effect", example = "2024-03-01")
  val startDate: LocalDate,

  @Schema(description = "The date that this restriction expires", example = "2024-03-01")
  val expiryDate: LocalDate? = null,

) : AbstractAuditable()
