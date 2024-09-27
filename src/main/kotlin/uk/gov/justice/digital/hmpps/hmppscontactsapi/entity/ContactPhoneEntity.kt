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

  val contactId: Long? = null,

  val phoneType: String,

  val phoneNumber: String? = null,

  val extNumber: String? = null,

  val primaryPhone: Boolean = false,

  val createdBy: String,

  @CreationTimestamp
  val createdTime: LocalDateTime = now(),
) {
  var amendedBy: String? = null
  var amendedTime: LocalDateTime? = null
}
