package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactWithAddressEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactRelationship
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Contact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactSearchResultItem
import java.time.LocalDate
import java.time.LocalDateTime

fun ContactEntity.toModel() = Contact(
  id = this.contactId,
  title = this.title,
  lastName = this.lastName,
  firstName = this.firstName,
  middleName = this.middleName,
  dateOfBirth = this.dateOfBirth,
  estimatedIsOverEighteen = this.estimatedIsOverEighteen,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
)

fun Page<ContactEntity>.toModel(): Page<Contact> = map { it.toModel() }

fun ContactWithAddressEntity.toModel() = ContactSearchResultItem(
  id = this.contactId,
  lastName = this.lastName,
  firstName = this.firstName,
  middleName = this.middleName,
  dateOfBirth = this.dateOfBirth,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  flat = this.flat,
  property = this.property,
  street = this.street,
  area = this.area,
  cityCode = this.cityCode,
  countyCode = this.countyCode,
  postCode = this.postCode,
  countryCode = this.countryCode,
)

fun PageImpl<ContactWithAddressEntity>.toModel(): Page<ContactSearchResultItem> = map { it.toModel() }

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
    estimatedIsOverEighteen = mapIsOverEighteen(this),
    createdBy = this.createdBy,
  )

private fun mapIsOverEighteen(entity: CreateContactRequest): EstimatedIsOverEighteen? =
  if (entity.dateOfBirth != null) null else entity.estimatedIsOverEighteen

private fun newContact(
  title: String?,
  firstName: String,
  lastName: String,
  middleName: String?,
  dateOfBirth: LocalDate?,
  estimatedIsOverEighteen: EstimatedIsOverEighteen?,
  createdBy: String,
): ContactEntity {
  return ContactEntity(
    0,
    title,
    firstName,
    lastName,
    middleName,
    dateOfBirth,
    estimatedIsOverEighteen,
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
