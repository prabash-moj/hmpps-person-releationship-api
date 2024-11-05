package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "Request to create a new contact restriction ")
data class CreateContactRestrictionRequest(
  @Schema(description = "Unique identifier for the contact", example = "123")
  val contactId: Long,

  @Schema(description = "Type of restriction", example = "MOBILE")
  val restrictionType: String,

  @Schema(description = "Restriction start date", example = "2024-01-01")
  val startDate: LocalDate? = null,

  @Schema(description = "Restriction end date ", example = "2024-01-01")
  val expiryDate: LocalDate? = null,

  @Schema(description = "Comments for the restriction ", example = "N/A")
  val comments: String? = null,

  @Schema(description = "Staff username who entered the restriction", example = "X999A")
  val staffUsername: String,

  @Schema(description = "User who created the entry", example = "admin")
  val createdBy: String,

  @Schema(description = "The timestamp of when the restriction was created", example = "2024-01-01T00:00:00Z")
  val createdTime: LocalDateTime = LocalDateTime.now(),
)
