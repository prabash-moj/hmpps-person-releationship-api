package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Describes the details of a reference code")
data class ReferenceCode(

  @Schema(description = "An internally-generated unique identifier for this reference code.", example = "12345")
  val referenceCodeId: Long,

  @Schema(description = "The group name for related reference codes.", example = "PHONE_TYPE")
  val groupCode: String,

  @Schema(description = "The code for this reference data", example = "MOBILE")
  val code: String,

  @Schema(description = "A fuller description of the reference code", example = "Mobile")
  val description: String?,
)
