package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.Immutable
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
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

  val middleNames: String?,

  val lastName: String,

  val dateOfBirth: LocalDate?,

  @Enumerated(EnumType.STRING)
  val estimatedIsOverEighteen: EstimatedIsOverEighteen?,

  val contactAddressId: Long?,

  val flat: String?,

  val property: String?,

  val street: String?,

  val area: String?,

  val cityCode: String?,

  val cityDescription: String?,

  val countyCode: String?,

  val countyDescription: String?,

  val postCode: String?,

  val countryCode: String?,

  val countryDescription: String?,

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
