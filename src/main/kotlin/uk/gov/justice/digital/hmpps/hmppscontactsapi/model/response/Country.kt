package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Country reference entity")
data class Country(

  @Schema(description = "Unique identifier of the country", example = "1", nullable = true)
  val countryId: Long,

  @Schema(description = "Nomis code of the country", example = "GBR")
  val nomisCode: String,

  @Schema(description = "Nomis description of the country", example = "United Kingdom")
  val nomisDescription: String,

  @Schema(description = "ISO numeric code of the country", example = "826")
  val isoNumeric: Int,

  @Schema(description = "ISO Alpha-2 code of the country", example = "GB")
  val isoAlpha2: String,

  @Schema(description = "ISO Alpha-3 code of the country", example = "GBR")
  val isoAlpha3: String,

  @Schema(description = "ISO country description", example = "United Kingdom of Great Britain and Northern Ireland")
  val isoCountryDesc: String,

  @Schema(description = "Display sequence for the country", example = "1")
  val displaySequence: Int,
)
