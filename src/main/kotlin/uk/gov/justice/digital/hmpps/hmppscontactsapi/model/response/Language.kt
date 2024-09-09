package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Language reference entity")
data class Language(

  @Schema(description = "Unique identifier of the language", example = "1", nullable = true)
  val languageId: Long,

  @Schema(description = "Nomis code of the language", example = "GBR")
  val nomisCode: String,

  @Schema(description = "Nomis description of the language", example = "United Kingdom")
  val nomisDescription: String,

  @Schema(description = "ISO Alpha-2 code of the language", example = "GB")
  val isoAlpha2: String,

  @Schema(description = "ISO Alpha-3 code of the language", example = "GBR")
  val isoAlpha3: String,

  @Schema(description = "ISO language description", example = "United Kingdom of Great Britain and Northern Ireland")
  val isoLanguageDesc: String,

  @Schema(description = "Display sequence for the language", example = "1")
  val displaySequence: Int,
)
