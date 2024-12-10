package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "contact_employment")
data class ContactEmploymentEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val contactEmploymentId: Long,

  val contactId: Long? = null,

  val corporateId: Long? = null,

  val corporateName: String? = null,

  val active: Boolean = true,

  val createdBy: String,

  @CreationTimestamp
  val createdTime: LocalDateTime = LocalDateTime.now(),
) {
  var updatedBy: String? = null
  var updatedTime: LocalDateTime? = null
}
