package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "country_reference")
data class CountryEntity(
  @Id
  val countryId: Long,

  val nomisCode: String,

  val nomisDescription: String,

  val isoNumeric: Int,

  val isoAlpha2: String,

  val isoAlpha3: String,

  val isoCountryDesc: String,

  val displaySequence: Int,
)
