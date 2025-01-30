package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.CountyEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.County

fun CountyEntity.toModel(): County = County(
  countyId = this.countyId,
  nomisCode = this.nomisCode,
  nomisDescription = this.nomisDescription,
  displaySequence = this.displaySequence,
)

fun List<CountyEntity>.toModel() = map { it.toModel() }
