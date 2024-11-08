package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

@Schema(description = "Request to update an existing phone number")
data class UpdatePhoneRequest(
  @Schema(description = "Type of phone", example = "MOB")
  @field:Size(max = 12, message = "phoneType must be <= 12 characters")
  val phoneType: String,

  @Schema(description = "Phone number", example = "+1234567890")
  @field:Size(max = 240, message = "phoneNumber must be <= 240 characters")
  val phoneNumber: String,

  @Schema(description = "Extension number", example = "123", nullable = true)
  @field:Size(max = 7, message = "extNumber must be <= 7 characters")
  val extNumber: String? = null,

  @Schema(description = "User who updated the entry", example = "admin")
  @field:Size(max = 100, message = "amendedBy must be <= 100 characters")
  val amendedBy: String,
)
