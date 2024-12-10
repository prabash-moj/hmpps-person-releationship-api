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
@Table(name = "contact_identity")
data class ContactIdentityEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val contactIdentityId: Long,

  val contactId: Long,

  val identityType: String,

  val identityValue: String? = null,

  val issuingAuthority: String? = null,

  val createdBy: String,

  @CreationTimestamp
  val createdTime: LocalDateTime = now(),

  val updatedBy: String? = null,

  val updatedTime: LocalDateTime? = null,
)
