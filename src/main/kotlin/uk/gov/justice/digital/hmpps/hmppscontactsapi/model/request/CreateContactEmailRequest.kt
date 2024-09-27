package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Request to create a new contact email address")
data class CreateContactEmailRequest(
  @Schema(description = "Unique identifier for the contact", example = "123")
  val contactId: Long,

  @Schema(description = "Type of email", example = "MOBILE")
  val emailType: String,

  @Schema(description = "Email address", example = "+1234567890")
  val emailAddress: String,

  @Schema(description = "Indicates if this is the primary email address", example = "true")
  val primaryEmail: Boolean,

  @Schema(description = "User who created the entry", example = "admin")
  val createdBy: String,

  @Schema(description = "The timestamp of when the email was created", example = "2024-01-01T00:00:00Z")
  val createdTime: LocalDateTime = LocalDateTime.now(),
)
