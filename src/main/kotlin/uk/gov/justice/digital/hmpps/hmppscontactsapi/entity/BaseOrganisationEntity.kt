package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.now

@MappedSuperclass
abstract class BaseOrganisationEntity(

  open val organisationName: String,

  open val programmeNumber: String?,

  open val vatNumber: String?,

  open val caseloadId: String?,

  open val comments: String?,

  open val active: Boolean,

  open val deactivatedDate: LocalDate?,

  @Column(updatable = false)
  open val createdBy: String,

  @Column(updatable = false)
  @CreationTimestamp
  open val createdTime: LocalDateTime = now(),

  open val updatedBy: String?,

  open val updatedTime: LocalDateTime?,
) {
  abstract fun id(): Long
}
