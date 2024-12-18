package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Details of the relationship between the prisoner and contact")
data class LinkedPrisonerRelationshipDetails(
  @Schema(description = "The unique identifier for the prisoner contact relationship", example = "123456")
  val prisonerContactId: Long,

  @Schema(
    description =
    """
      Coded value indicating either a social or official contact (mandatory).
      This is a coded value from the group code CONTACT_TYPE in reference data.
      Known values are (S) Social/Family or (O) official.
      """,
    example = "S",
  )
  val contactType: String,

  @Schema(description = "The description of the contact type", example = "Official")
  val contactTypeDescription: String,

  @Schema(description = "The relationship code between the prisoner and the contact", example = "FRI")
  val relationshipCode: String,

  @Schema(description = "The description of the relationship", example = "Friend", nullable = true)
  val relationshipDescription: String?,

)
