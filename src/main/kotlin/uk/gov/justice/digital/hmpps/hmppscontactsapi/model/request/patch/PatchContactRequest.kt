package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.patch

import io.swagger.v3.oas.annotations.media.Schema
import org.openapitools.jackson.nullable.JsonNullable

@Schema(description = "Request to patch a new contact ")
data class PatchContactRequest(

  @Schema(description = "Whether the contact is a staff member", example = "false", nullable = false, type = "boolean", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  var staffFlag: JsonNullable<Boolean> = JsonNullable.undefined(),

  @Schema(description = "The domestic status code of the contact", example = "S", nullable = true, type = "string", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  var domesticStatus: JsonNullable<String?> = JsonNullable.undefined(),

  @Schema(description = "Whether an interpreter is required", example = "false", nullable = false, type = "boolean", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  var interpreterRequired: JsonNullable<Boolean> = JsonNullable.undefined(),

  @Schema(description = "The language code of the contact", example = "EN", nullable = true, type = "string", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  var languageCode: JsonNullable<String?> = JsonNullable.undefined(),

  @Schema(description = "The id of the user who updated the contact", example = "JD000001", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
  val updatedBy: String,
)
