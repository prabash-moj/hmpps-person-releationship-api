package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "Request to update a new contact ")
data class UpdateContactRequest(

  @Schema(description = "The title of the contact, if any", example = "Mr", nullable = true)
  val title: String? = null,

  @Schema(description = "The last name of the contact", example = "Doe")
  val lastName: String,

  @Schema(description = "The first name of the contact", example = "John")
  val firstName: String,

  @Schema(description = "The middle name of the contact, if any", example = "William", nullable = true)
  val middleName: String? = null,

  @Schema(description = "The date of birth of the contact, if known", example = "1980-01-01", nullable = true)
  val dateOfBirth: LocalDate? = null,

  @Schema(
    description = "Whether the contact is over 18, based on their date of birth if it is known",
    example = "YES",
  )
  val estimatedIsOverEighteen: EstimatedIsOverEighteen?,

  @Schema(description = "The type code of the contact", example = "PERSON", nullable = true)
  var contactTypeCode: String? = null,

  @Schema(description = "The place of birth of the contact", example = "London", nullable = true)
  var placeOfBirth: String? = null,

  @Schema(description = "Whether the contact is active", example = "true", nullable = true)
  var active: Boolean? = false,

  @Schema(description = "Whether the contact is suspended", example = "false", nullable = true)
  var suspended: Boolean? = false,

  @Schema(description = "Whether the contact is a staff member", example = "false", nullable = true)
  var staffFlag: Boolean? = false,

  @Schema(description = "Whether the contact is deceased", example = "false", nullable = true)
  var deceasedFlag: Boolean? = false,

  @Schema(description = "The date the contact was deceased, if applicable", example = "2023-05-01", nullable = true)
  var deceasedDate: LocalDate? = null,

  @Schema(description = "The coroner's number, if applicable", example = "CRN12345", nullable = true)
  var coronerNumber: String? = null,

  @Schema(description = "The gender of the contact", example = "Male", nullable = true)
  var gender: String? = null,

  @Schema(description = "The marital status of the contact", example = "Single", nullable = true)
  var maritalStatus: String? = null,

  @Schema(description = "The language code of the contact", example = "EN", nullable = true)
  var languageCode: String? = null,

  @Schema(description = "The nationality code of the contact", example = "GB", nullable = true)
  var nationalityCode: String? = null,

  @Schema(description = "Whether an interpreter is required", example = "false", nullable = true)
  var interpreterRequired: Boolean? = false,

  @Schema(
    description = "Additional comments about the contact",
    example = "This contact has special dietary requirements.",
    nullable = true,
  )
  var comments: String? = null,

  @Schema(description = "The id of the user who updated the contact", example = "JD000001")
  val updatedBy: String,

  @Schema(description = "The timestamp of when the contact was changed", example = "2024-01-01T00:00:00Z")
  val updatedTime: LocalDateTime,
)
