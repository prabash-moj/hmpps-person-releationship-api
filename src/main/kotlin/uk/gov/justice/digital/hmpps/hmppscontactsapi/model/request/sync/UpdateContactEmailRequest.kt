package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Request to update a new contact email address")
data class UpdateContactEmailRequest(
  @Schema(description = "Unique identifier for the contact", example = "123")
  val contactId: Long,

  @Schema(description = "Type of email", example = "MOBILE")
  val emailType: String,

  @Schema(description = "Email address", example = "+1234567890")
  val emailAddress: String,

  @Schema(description = "Indicates if this is the primary email address", example = "true")
  val primaryEmail: Boolean,

  @Schema(description = "The id of the user who updated the contact email", example = "JD000001")
  val updatedBy: String,

  @Schema(description = "The timestamp of when the contact email was changed", example = "2024-01-01T00:00:00Z")
  val updatedTime: LocalDateTime,
)
