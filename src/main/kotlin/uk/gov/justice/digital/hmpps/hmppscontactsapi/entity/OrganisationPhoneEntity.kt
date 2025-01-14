package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "organisation_phone")
data class OrganisationPhoneEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val organisationPhoneId: Long,

  val organisationId: Long,

  val phoneType: String,

  val phoneNumber: String,

  val extNumber: String?,

  val createdBy: String,

  @CreationTimestamp
  val createdTime: LocalDateTime,

  val updatedBy: String?,

  val updatedTime: LocalDateTime?,
)
