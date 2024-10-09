package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.migrate

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "Request to migrate a contact and all of its sub-elements from NOMIS into this service")
data class MigrateContactRequest(

  @Schema(description = "The person ID from NOMIS", example = "1233323")
  @field:NotNull(message = "The NOMIS person ID must be present in the request")
  val personId: Long,

  @Schema(description = "The title of the contact, if any", nullable = true)
  val title: CodedValue? = null,

  @Schema(description = "The last name of the contact", example = "Doe", maxLength = 35)
  @field:Size(max = 35, message = "lastName must be <= 35 characters")
  val lastName: String,

  @Schema(description = "The first name of the contact", example = "John", maxLength = 35)
  @field:Size(max = 35, message = "firstName must be <= 35 characters")
  val firstName: String,

  @Schema(description = "The middle name of the contact, if any", example = "William", nullable = true, maxLength = 35)
  @field:Size(max = 35, message = "middleName must be <= 35 characters")
  val middleName: String? = null,

  @Schema(description = "The date of birth of the contact, if known", example = "1980-01-01", nullable = true, format = "yyyy-MM-dd")
  @field:DateTimeFormat(pattern = "yyyy-MM-dd")
  val dateOfBirth: LocalDate? = null,

  @Schema(description = "The gender of the contact", nullable = true)
  val gender: CodedValue? = null,

  @Schema(description = "The main language spoken by this contact", nullable = true)
  val language: CodedValue? = null,

  @Schema(description = "Interpreter required", nullable = true)
  val interpreterRequired: Boolean = false,

  @Schema(description = "The domestic status coded value", nullable = true, example = "MARRIED")
  val domesticStatus: CodedValue? = null,

  @Schema(description = "The date this persons was marked as deceased", nullable = true)
  val deceasedDate: LocalDate? = null,

  @Schema(description = "This person is a remitter of funds to one or more prisoners")
  val remitter: Boolean = false,

  @Schema(description = "This person is staff")
  val staff: Boolean = false,

  @Schema(description = "Retain photo and fingerprint images for this person")
  val keepBiometrics: Boolean = false,

  @Schema(description = "Auditing information", nullable = true)
  val audit: MigrateAuditInfo? = null,

  @Schema(description = "Telephone numbers", nullable = true)
  val phoneNumbers: List<MigratePhoneNumber> = emptyList(),

  @Schema(description = "Addresses", nullable = true)
  val addresses: List<MigrateAddress> = emptyList(),

  @Schema(description = "Email addresses", nullable = true)
  val emailAddresses: List<MigrateEmailAddress> = emptyList(),

  @Schema(description = "Proofs of identity", nullable = true)
  val identifiers: List<MigrateIdentifier> = emptyList(),
)

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

  @Schema(description = "Phone number type", nullable = true, example = "HOME")
  val type: CodedValue,
)

data class MigrateAddress(
  @Schema(description = "Unique address ID in NOMIS", example = "123")
  val addressId: Long,

  @Schema(description = "Address type coded value", nullable = true, example = "HOME")
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
)

data class MigrateEmailAddress(
  @Schema(description = "Unique email ID in NOMIS", example = "123")
  val emailAddressId: Long,

  @Schema(description = "Email address", nullable = true, example = "sender@a.com")
  val email: String,
)

data class MigrateIdentifier(
  @Schema(description = "Unique sequence ID in NOMIS", example = "123")
  val sequence: Long,

  @Schema(description = "Coded value for proof of ID type", example = "XXX")
  val type: CodedValue,

  @Schema(description = "The identifying information e.g. driving licence number", example = "KJ 45544 JFKJK")
  val identifier: String?,

  @Schema(description = "The issuing authority for this identifier", example = "DVLA")
  val issuedAuthority: String?,
)

data class MigrateAuditInfo(
  @Schema(description = "The data and time the record was created", nullable = true)
  val createDateTime: LocalDateTime? = null,

  @Schema(description = "The username who created the row", nullable = true)
  val createUsername: String? = null,

  @Schema(description = "The display name of the user who created the row", nullable = true)
  val createDisplayName: String? = null,

  @Schema(description = "The username who last modified the row", nullable = true)
  val modifyUserId: String? = null,

  @Schema(description = "The display name of the user who last modified the row", nullable = true)
  val modifyDisplayName: String? = null,

  @Schema(description = "The date and time the record was last amended", nullable = true)
  val modifyDateTime: LocalDateTime? = null,

  @Schema(description = "The date and time of the audit record", nullable = true)
  val auditTimestamp: LocalDateTime? = null,

  @Schema(description = "The audit username", nullable = true)
  val auditUserId: String? = null,

  @Schema(description = "The audit module name", nullable = true)
  val auditModuleName: String? = null,

  @Schema(description = "The audit client id", nullable = true)
  val auditClientUserId: String? = null,

  @Schema(description = "The audit client IP address", nullable = true)
  val auditClientIpAddress: String? = null,

  @Schema(description = "The audit client workstation", nullable = true)
  val auditClientWorkstationName: String? = null,

  @Schema(description = "Audit additional info", nullable = true)
  val auditAdditionalInfo: String? = null,
)
