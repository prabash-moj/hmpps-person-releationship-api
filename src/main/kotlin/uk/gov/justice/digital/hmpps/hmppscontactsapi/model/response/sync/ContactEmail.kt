package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Email related to a contact")
data class ContactEmail(
  @Schema(description = "Unique identifier for the contact email", example = "1")
  val contactEmailId: Long,

  @Schema(description = "Unique identifier for the contact", example = "123")
  val contactId: Long,

  @Schema(description = "Type of email", example = "WORK")
  val emailType: String,

  @Schema(description = "Email address", example = "work@example.com")
  val emailAddress: String,

  @Schema(description = "Indicates if this is the primary Email address", example = "true")
  val primaryEmail: Boolean,

  @Schema(description = "User who created the entry", example = "admin")
  val createdBy: String,

  @Schema(description = "Timestamp when the entry was created", example = "2023-09-23T10:15:30")
  val createdTime: LocalDateTime,

  @Schema(description = "User who amended the entry", example = "admin2")
  val amendedBy: String?,

  @Schema(description = "Timestamp when the entry was amended", example = "2023-09-24T12:00:00")
  val amendedTime: LocalDateTime?,
)
