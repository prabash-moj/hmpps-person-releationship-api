package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.patch

import io.swagger.v3.oas.annotations.media.Schema
import org.openapitools.jackson.nullable.JsonNullable

@Schema(description = "Request to patch a new contact ", nullable = true)
data class PatchContactRequest(

  @Schema(description = "Whether an interpreter is required", example = "false", nullable = true)
  var interpreterRequired: JsonNullable<Boolean> = JsonNullable.undefined(),

  @Schema(description = "The language code of the contact", example = "EN", nullable = true)
  var languageCode: JsonNullable<String?> = JsonNullable.undefined(),

  @Schema(description = "The id of the user who updated the contact", example = "JD000001", nullable = true)
  val updatedBy: String,
)
