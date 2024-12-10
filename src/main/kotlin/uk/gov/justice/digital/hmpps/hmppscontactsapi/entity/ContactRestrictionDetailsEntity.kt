package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.now

@Entity
@Table(name = "v_contact_restriction_details")
data class ContactRestrictionDetailsEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val contactRestrictionId: Long,

  val contactId: Long,

  val restrictionType: String,

  val restrictionTypeDescription: String,

  val startDate: LocalDate?,

  val expiryDate: LocalDate?,

  val comments: String?,

  val createdBy: String,

  @CreationTimestamp
  val createdTime: LocalDateTime = now(),

  val updatedBy: String?,

  val updatedTime: LocalDateTime?,
)
