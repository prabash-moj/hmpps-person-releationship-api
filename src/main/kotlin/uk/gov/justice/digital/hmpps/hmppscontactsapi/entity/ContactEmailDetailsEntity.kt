package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "v_contact_emails")
data class ContactEmailDetailsEntity(
  @Id
  val contactEmailId: Long,

  val contactId: Long,

  val emailType: String,

  val emailTypeDescription: String,

  val emailAddress: String,

  val primaryEmail: Boolean,

  val createdBy: String,

  val createdTime: LocalDateTime,

  val amendedBy: String?,

  val amendedTime: LocalDateTime?,
)
