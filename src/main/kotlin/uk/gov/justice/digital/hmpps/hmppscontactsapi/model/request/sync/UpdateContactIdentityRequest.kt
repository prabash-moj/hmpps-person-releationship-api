package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Request to update a new contact identity ")
data class UpdateContactIdentityRequest(
  @Schema(description = "Unique identifier for the contact", example = "123")
  val contactId: Long,

  @Schema(description = "Type of identity", example = "MOBILE")
  val identityType: String,

  @Schema(description = "Identity ", example = "+1234567890")
  val identityValue: String,

  @Schema(description = "The id of the user who updated the contact identity", example = "JD000001")
  val updatedBy: String,

  @Schema(description = "The timestamp of when the contact identity was changed", example = "2024-01-01T00:00:00Z")
  val updatedTime: LocalDateTime,
)
