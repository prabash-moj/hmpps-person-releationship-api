package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.openapitools.jackson.nullable.JsonNullable

@Schema(description = "Request to update an existing relationship details")
data class UpdateRelationshipRequest(

  @Schema(description = "The relationship code between the prisoner and the contact", example = "FRI", nullable = false, type = "string", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  val relationshipCode: JsonNullable<String> = JsonNullable.undefined(),

  @Schema(description = "Whether they are the emergency contact for the prisoner", example = "boolean", nullable = false, type = "string", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty(required = true)
  val isEmergencyContact: JsonNullable<Boolean> = JsonNullable.undefined(),

  @Schema(description = "Whether they are the next of kin for the prisoner", example = "true", nullable = false, type = "boolean", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty(required = true)
  val isNextOfKin: JsonNullable<Boolean> = JsonNullable.undefined(),

  @Schema(description = "Whether the relationship is active", example = "true", nullable = false, type = "boolean", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty(required = true)
  val isRelationshipActive: JsonNullable<Boolean> = JsonNullable.undefined(),

  @Schema(description = "Comments about the contacts relationship with the prisoner", example = "Some additional information", nullable = true, type = "string", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  val comments: JsonNullable<String?> = JsonNullable.undefined(),

  @Schema(description = "The id of the user who updated the contact", example = "JD000001", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
  val updatedBy: String,

)
