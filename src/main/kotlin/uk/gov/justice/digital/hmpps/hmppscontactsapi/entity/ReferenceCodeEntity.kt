package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "reference_codes")
data class ReferenceCodeEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val referenceCodeId: Long = 0,

  val groupCode: String,

  val code: String,

  val description: String,

  val createdBy: String,

  val createdTime: LocalDateTime = LocalDateTime.now(),
)
