package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.CountryEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Country

fun CountryEntity.toModel(): Country {
  return Country(
    countryId = this.countryId,
    nomisCode = this.nomisCode,
    nomisDescription = this.nomisDescription,
    isoNumeric = this.isoNumeric,
    isoAlpha2 = this.isoAlpha2,
    isoAlpha3 = this.isoAlpha3,
    isoCountryDesc = this.isoCountryDesc,
    displaySequence = this.displaySequence,
  )
}

fun List<CountryEntity>.toModel() = map { it.toModel() }
