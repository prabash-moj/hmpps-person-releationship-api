package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "organisation_type")
data class OrganisationTypeEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val organisationTypeId: Long,

  val organisationId: Long,

  val organisationType: String,

  val createdBy: String,

  @CreationTimestamp
  val createdTime: LocalDateTime,

  val updatedBy: String?,

  val updatedTime: LocalDateTime?,
)
