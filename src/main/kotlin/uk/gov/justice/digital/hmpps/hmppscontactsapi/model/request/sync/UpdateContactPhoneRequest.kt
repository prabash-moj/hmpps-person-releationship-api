package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Request to update a new contact phone number")
data class UpdateContactPhoneRequest(
  @Schema(description = "Unique identifier for the contact", example = "123")
  val contactId: Long,

  @Schema(description = "Type of phone", example = "MOBILE")
  val phoneType: String,

  @Schema(description = "Phone number", example = "+1234567890")
  val phoneNumber: String,

  @Schema(description = "Extension number", example = "123")
  val extNumber: String? = null,

  @Schema(description = "Indicates if this is the primary phone number", example = "true")
  val primaryPhone: Boolean,

  @Schema(description = "The id of the user who updated the contact phone", example = "JD000001")
  val updatedBy: String,

  @Schema(description = "The timestamp of when the contact phone was changed", example = "2024-01-01T00:00:00Z")
  val updatedTime: LocalDateTime,
)
