package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.CityEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.City

fun CityEntity.toModel(): City {
  return City(
    cityId = this.cityId,
    nomisCode = this.nomisCode,
    nomisDescription = this.nomisDescription,
    displaySequence = this.displaySequence,
  )
}

fun List<CityEntity>.toModel() = map { it.toModel() }
