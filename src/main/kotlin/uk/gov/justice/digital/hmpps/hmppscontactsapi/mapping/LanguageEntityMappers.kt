package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.LanguageEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Language

fun LanguageEntity.toModel(): Language = Language(
  languageId = this.languageId,
  nomisCode = this.nomisCode,
  nomisDescription = this.nomisDescription,
  isoAlpha2 = this.isoAlpha2,
  isoAlpha3 = this.isoAlpha3,
  isoLanguageDesc = this.isoLanguageDesc,
  displaySequence = this.displaySequence,
)

fun List<LanguageEntity>.toModel() = map { it.toModel() }
