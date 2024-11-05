package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactRelationship
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "Request to create a new contact")
data class CreateContactRequest(

  @Schema(description = "The title of the contact, if any", example = "Mr", nullable = true, maxLength = 12)
  val title: String? = null,

  @Schema(description = "The last name of the contact", example = "Doe", maxLength = 35)
  val lastName: String,

  @Schema(description = "The first name of the contact", example = "John", maxLength = 35)
  val firstName: String,

  @Schema(description = "The middle name of the contact, if any", example = "William", nullable = true, maxLength = 35)
  val middleName: String? = null,

  @Schema(description = "The date of birth of the contact, if known", example = "1980-01-01", nullable = true, format = "yyyy-MM-dd")
  val dateOfBirth: LocalDate? = null,

  @Schema(description = "If the date of birth is not known, this indicates whether they are believed to be over 18 or not", example = "YES", nullable = true)
  val estimatedIsOverEighteen: EstimatedIsOverEighteen? = null,

  @Schema(description = "A description of the relationship if the contact should be linked to a prisoner", nullable = true, exampleClasses = [ContactRelationship::class])
  val relationship: ContactRelationship? = null,

  @Schema(description = "The place of birth of the contact", example = "London", nullable = true)
  var placeOfBirth: String? = null,

  @Schema(description = "Whether the contact is active", example = "true", nullable = true)
  var active: Boolean? = false,

  @Schema(description = "Whether the contact is suspended", example = "false", nullable = true)
  var suspended: Boolean? = false,

  @Schema(description = "Whether the contact is a staff member", example = "false", nullable = false)
  var isStaff: Boolean = false,

  @Schema(description = "Whether the contact is a remitter", example = "false", nullable = false)
  var remitter: Boolean = false,

  @Schema(description = "Whether the contact is deceased", example = "false", nullable = true)
  var deceasedFlag: Boolean? = false,

  @Schema(description = "The date the contact was deceased, if applicable", example = "2023-05-01", nullable = true)
  var deceasedDate: LocalDate? = null,

  @Schema(description = "The coroner's number, if applicable", example = "CRN12345", nullable = true)
  var coronerNumber: String? = null,

  @Schema(description = "The gender of the contact", example = "Male", nullable = true)
  var gender: String? = null,

  @Schema(description = "The domestic status code of the contact", example = "S", nullable = true)
  var domesticStatus: String? = null,

  @Schema(description = "The language code of the contact", example = "EN", nullable = true)
  var languageCode: String? = null,

  @Schema(description = "The nationality code of the contact", example = "GB", nullable = true)
  var nationalityCode: String? = null,

  @Schema(description = "Whether an interpreter is required", example = "false", nullable = true)
  var interpreterRequired: Boolean? = false,

  @Schema(description = "The id of the user creating the contact", example = "JD000001", maxLength = 100)
  val createdBy: String,

  @Schema(description = "The timestamp of when the contact was created", example = "2024-01-01T00:00:00Z")
  val createdTime: LocalDateTime = LocalDateTime.now(),
)
