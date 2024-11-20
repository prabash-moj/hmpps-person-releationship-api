package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "Restriction related to a contact")
data class SyncContactRestriction(
  @Schema(description = "Unique identifier for the contact restriction", example = "1")
  val contactRestrictionId: Long,

  @Schema(description = "Unique identifier for the contact", example = "123")
  val contactId: Long,

  @Schema(description = "Type of restriction", example = "MOBILE")
  val restrictionType: String,

  @Schema(description = "Restriction created date", example = "2024-01-01")
  val startDate: LocalDate? = null,

  @Schema(description = "Restriction end date ", example = "2024-01-01")
  val expiryDate: LocalDate? = null,

  @Schema(description = "Comments for the restriction ", example = "N/A")
  val comments: String? = null,

  @Schema(description = "Entered staff username", example = "N/A")
  val staffUsername: String? = null,

  @Schema(description = "User who created the entry", example = "admin")
  val createdBy: String,

  @Schema(description = "Timestamp when the entry was created", example = "2023-09-23T10:15:30")
  val createdTime: LocalDateTime,

  @Schema(description = "User who amended the entry", example = "admin2")
  val amendedBy: String?,

  @Schema(description = "Timestamp when the entry was amended", example = "2023-09-24T12:00:00")
  val amendedTime: LocalDateTime?,
)
