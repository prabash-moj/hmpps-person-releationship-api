package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Describes the prisoner contact relationship")
data class PrisonerContactRelationship(

  @Schema(description = "The relationship code between the prisoner and the contact", example = "FRI")
  val relationshipCode: String,

  @Schema(description = "The description of the relationship", example = "Friend")
  val relationshipDescription: String,

  @Schema(description = "Is this contact the prisoner's emergency contact?", example = "true")
  val emergencyContact: Boolean,

  @Schema(description = "Is this contact the prisoner's next of kin?", example = "false")
  val nextOfKin: Boolean,

  @Schema(description = "Is this prisoner's contact relationship active?", example = "true")
  val isRelationshipActive: Boolean,

  @Schema(description = "Any additional comments", example = "Close family friend", nullable = true)
  val comments: String?,
)
