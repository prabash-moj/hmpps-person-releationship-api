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
@Table(name = "contact_restriction")
data class ContactRestrictionEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val contactRestrictionId: Long,

  val contactId: Long? = null,

  val restrictionType: String,

  val startDate: LocalDate? = null,

  val expiryDate: LocalDate? = null,

  val comments: String? = null,

  val createdBy: String,

  @CreationTimestamp
  val createdTime: LocalDateTime = now(),
) {
  var amendedBy: String? = null

  var amendedTime: LocalDateTime? = null
}
