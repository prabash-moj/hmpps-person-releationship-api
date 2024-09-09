package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "county_reference")
data class CountyEntity(
  @Id
  val countyId: Long,

  val nomisCode: String,

  val nomisDescription: String,

  val displaySequence: Int,
)
