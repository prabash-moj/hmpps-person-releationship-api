package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "prisoner_contact_restriction")
data class PrisonerContactRestrictionEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val prisonerContactRestrictionId: Long,

  val prisonerContactId: Long,

  val restrictionType: String? = null,

  val startDate: LocalDate? = null,

  val expiryDate: LocalDate? = null,

  val comments: String? = null,

  val authorisedBy: String? = null,

  val authorisedTime: LocalDateTime? = null,

  val createdBy: String? = null,

  val createdTime: LocalDateTime? = null,
) {

  var amendedBy: String? = null

  var amendedTime: LocalDateTime? = null
}
