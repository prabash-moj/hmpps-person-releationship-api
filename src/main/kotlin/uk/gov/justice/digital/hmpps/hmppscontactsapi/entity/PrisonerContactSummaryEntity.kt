package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.Immutable
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "v_prisoner_contacts")
data class PrisonerContactSummaryEntity(
  @Id
  val prisonerContactId: Long,

  val contactId: Long,

  val title: String?,

  val firstName: String,

  val middleName: String?,

  val lastName: String,

  val dateOfBirth: LocalDate?,

  val contactAddressId: Long?,

  val flat: String?,

  val property: String?,

  val street: String?,

  val area: String?,

  val cityCode: String?,

  val countyCode: String?,

  val postCode: String?,

  val countryCode: String?,

  val contactPhoneId: Long?,

  val phoneType: String?,

  val phoneTypeDescription: String?,

  val phoneNumber: String?,

  val contactEmailId: Long?,

  val emailType: String?,

  val emailTypeDescription: String?,

  val emailAddress: String?,

  val prisonerNumber: String,

  val relationshipType: String,

  val relationshipDescription: String?,

  val active: Boolean,

  val canBeContacted: Boolean,

  val approvedVisitor: Boolean,

  val awareOfCharges: Boolean,

  val nextOfKin: Boolean,

  val emergencyContact: Boolean,

  val comments: String?,
)
