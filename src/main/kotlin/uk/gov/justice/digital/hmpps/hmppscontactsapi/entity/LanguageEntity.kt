package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "language_reference")
data class LanguageEntity(
  @Id
  val languageId: Long,

  val nomisCode: String,

  val nomisDescription: String,

  val isoAlpha2: String,

  val isoAlpha3: String,

  val isoLanguageDesc: String,

  val displaySequence: Int,
)
