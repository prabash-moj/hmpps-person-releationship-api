package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "Response object with prisoner contact restriction details")
data class PrisonerContactRestriction(

  @Schema(description = "ID of the prisoner contact restriction to which the restriction applies", example = "232")
  val prisonerContactRestrictionId: Long,

  @Schema(description = "ID of the contact to which the restriction applies", example = "12345")
  val contactId: Long,

  @Schema(description = "Type of restriction applied", example = "NoContact", nullable = true)
  val restrictionType: String? = null,

  @Schema(description = "Start date of the restriction", example = "2024-01-01", nullable = true)
  val startDate: LocalDate? = null,

  @Schema(description = "Expiry date of the restriction, if applicable", example = "2024-12-31", nullable = true)
  val expiryDate: LocalDate? = null,

  @Schema(description = "Comments regarding the restriction", example = "Restriction applied due to safety concerns", nullable = true)
  val comments: String? = null,

  @Schema(description = "Person who authorized the restriction", example = "John Doe", nullable = true)
  val authorisedBy: String? = null,

  @Schema(description = "Time when the restriction was authorized", example = "2024-10-01T12:00:00Z", nullable = true)
  val authorisedTime: LocalDateTime? = null,

  @Schema(description = "User who created the restriction record", example = "admin", nullable = true)
  val createdBy: String? = null,

  @Schema(description = "Time when the restriction record was created", example = "2024-10-01T12:00:00Z", nullable = true)
  val createdTime: LocalDateTime? = null,

  @Schema(description = "User who last amended the restriction record", example = "editor", nullable = true)
  val amendedBy: String? = null,

  @Schema(description = "Time when the restriction record was last amended", example = "2024-10-02T15:30:00Z", nullable = true)
  val amendedTime: LocalDateTime? = null,
)
