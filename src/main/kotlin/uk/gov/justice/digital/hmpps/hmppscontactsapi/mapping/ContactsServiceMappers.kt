package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactAddressEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactRelationship
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.IsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Contact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactSearch
import java.time.LocalDate
import java.time.LocalDateTime

fun ContactEntity.toModel() = Contact(
  id = this.contactId,
  title = this.title,
  lastName = this.lastName,
  firstName = this.firstName,
  middleName = this.middleName,
  dateOfBirth = this.dateOfBirth,
  isOverEighteen = mapIsOverEighteen(this),
  createdBy = this.createdBy,
  createdTime = this.createdTime,
)

fun Page<ContactEntity>.toModel(): Page<Contact> = map { it.toModel() }

fun Array<Any>.toModel(): ContactSearch {
  val contact = this[0] as ContactEntity
  val contactAddress = this[1] as ContactAddressEntity
  return ContactSearch(
    id = contact.contactId,

    // Personal details
    lastName = contact.lastName,
    firstName = contact.firstName,
    middleName = contact.middleName,
    dateOfBirth = contact.dateOfBirth,

    // Created details
    createdBy = contact.createdBy,
    createdTime = contact.createdTime,

    // Address details
    flat = contactAddress.flat,
    property = contactAddress.property,
    street = contactAddress.street,
    area = contactAddress.area,
    cityCode = contactAddress.cityCode,
    countyCode = contactAddress.countyCode,
    postCode = contactAddress.postCode,
    countryCode = contactAddress.countryCode,
  )
}

fun PageImpl<Array<Any>>.toModel(): Page<ContactSearch> = map { it.toModel() }

fun ContactRelationship.toEntity(
  contactId: Long,
  createdBy: String,
) = newPrisonerContact(
  contactId,
  this.prisonerNumber,
  this.relationshipCode,
  this.isNextOfKin,
  this.isEmergencyContact,
  this.comments,
  createdBy,
)

fun CreateContactRequest.toModel() =
  newContact(
    title = this.title,
    lastName = this.lastName,
    firstName = this.firstName,
    middleName = this.middleName,
    dateOfBirth = this.dateOfBirth,
    isOverEighteen = mapIsOverEighteen(this),
    createdBy = this.createdBy,
  )

private fun mapIsOverEighteen(entity: ContactEntity): IsOverEighteen {
  return if (entity.dateOfBirth != null) {
    if (isOver18(entity.dateOfBirth)) {
      IsOverEighteen.YES
    } else {
      IsOverEighteen.NO
    }
  } else {
    when (entity.isOverEighteen) {
      true -> IsOverEighteen.YES
      false -> IsOverEighteen.NO
      null -> IsOverEighteen.DO_NOT_KNOW
    }
  }
}

private fun isOver18(dateOfBirth: LocalDate) = !dateOfBirth.isAfter(LocalDate.now().minusYears(18))

private fun mapIsOverEighteen(request: CreateContactRequest): Boolean? {
  return if (request.dateOfBirth != null) {
    null
  } else {
    when (request.isOverEighteen) {
      IsOverEighteen.YES -> true
      IsOverEighteen.NO -> false
      IsOverEighteen.DO_NOT_KNOW -> null
      null -> null
    }
  }
}

private fun newContact(
  title: String?,
  firstName: String,
  lastName: String,
  middleName: String?,
  dateOfBirth: LocalDate?,
  isOverEighteen: Boolean?,
  createdBy: String,
): ContactEntity {
  return ContactEntity(
    0,
    title,
    firstName,
    lastName,
    middleName,
    dateOfBirth,
    isOverEighteen,
    createdBy,
    LocalDateTime.now(),
  )
}

private fun newPrisonerContact(
  contactId: Long,
  prisonerNumber: String,
  relationshipType: String,
  nextOfKin: Boolean,
  emergencyContact: Boolean,
  comments: String?,
  createdBy: String,
): PrisonerContactEntity {
  return PrisonerContactEntity(
    0,
    contactId,
    prisonerNumber,
    relationshipType,
    nextOfKin,
    emergencyContact,
    comments,
    createdBy,
    LocalDateTime.now(),
  )
}
