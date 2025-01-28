package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request to create a new employment with an employer and whether be active or inactive")
data class PatchEmploymentsNewEmployment(
  @Schema(description = "The organisation id", example = "123456789", nullable = false, required = true)
  val organisationId: Long,
  @Schema(description = "Whether this is a current employment or not", nullable = false, required = true)
  val isActive: Boolean,
)
