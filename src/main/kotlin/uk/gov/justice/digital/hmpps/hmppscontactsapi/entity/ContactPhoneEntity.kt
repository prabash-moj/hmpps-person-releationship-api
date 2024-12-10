package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.time.LocalDateTime.now

@Entity
@Table(name = "contact_phone")
data class ContactPhoneEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val contactPhoneId: Long,

  val contactId: Long,

  val phoneType: String,

  val phoneNumber: String,

  val extNumber: String? = null,

  val createdBy: String,

  @CreationTimestamp
  val createdTime: LocalDateTime = now(),

  val updatedBy: String? = null,

  val updatedTime: LocalDateTime? = null,
)
