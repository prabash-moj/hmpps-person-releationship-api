package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "city_reference")
data class CityEntity(
  @Id
  val cityId: Long,

  val nomisCode: String,

  val nomisDescription: String,

  val displaySequence: Int,
)
