package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "prisoner_contact")
data class PrisonerContactEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val prisonerContactId: Long,

  @Column(name = "contact_id")
  val contactId: Long,

  @Column(name = "prisoner_number")
  val prisonerNumber: String,

  @Column(name = "contact_type")
  val contactType: String,

  @Column(name = "relationship_type")
  val relationshipType: String,

  @Column(name = "next_of_kin")
  val nextOfKin: Boolean,

  @Column(name = "emergency_contact")
  val emergencyContact: Boolean,

  @Column(name = "comments")
  val comments: String?,

  @Column(updatable = false, name = "created_by")
  val createdBy: String,

  @Column(updatable = false, name = "created_time")
  @CreationTimestamp
  val createdTime: LocalDateTime,
) {
  var active: Boolean? = true

  var approvedVisitor: Boolean? = false

  var awareOfCharges: Boolean? = false

  var canBeContacted: Boolean? = false

  var approvedBy: String? = null

  var approvedTime: LocalDateTime? = null

  var expiryDate: LocalDate? = null

  var createdAtPrison: String? = null

  var amendedBy: String? = null

  var amendedTime: LocalDateTime? = null
}
