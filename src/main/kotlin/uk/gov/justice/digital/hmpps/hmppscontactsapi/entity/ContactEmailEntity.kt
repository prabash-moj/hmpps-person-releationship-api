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
@Table(name = "contact_email")
data class ContactEmailEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val contactEmailId: Long,

  val contactId: Long,

  val emailAddress: String,

  val createdBy: String,

  @CreationTimestamp
  val createdTime: LocalDateTime = now(),

  val amendedBy: String? = null,

  val amendedTime: LocalDateTime? = null,
)
