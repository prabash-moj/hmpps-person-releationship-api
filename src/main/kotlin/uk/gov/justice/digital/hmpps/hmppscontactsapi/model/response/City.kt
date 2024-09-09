package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "City reference entity")
data class City(

  @Schema(description = "Unique identifier of the city", example = "1", nullable = true)
  val cityId: Long,

  @Schema(description = "Nomis code of the city", example = "GBR")
  val nomisCode: String,

  @Schema(description = "Nomis description of the city", example = "United Kingdom")
  val nomisDescription: String,

  @Schema(description = "Display sequence for the city", example = "1")
  val displaySequence: Int,
)
