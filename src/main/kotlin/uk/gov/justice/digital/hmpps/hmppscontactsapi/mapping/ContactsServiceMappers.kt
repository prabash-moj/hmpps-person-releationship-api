package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactWithAddressEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactRelationship
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactSearchResultItem
import java.time.LocalDate
import java.time.LocalDateTime

fun ContactWithAddressEntity.toModel() = ContactSearchResultItem(
  id = this.contactId,
  lastName = this.lastName,
  firstName = this.firstName,
  middleNames = this.middleNames,
  dateOfBirth = this.dateOfBirth,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  flat = this.flat,
  property = this.property,
  street = this.street,
  area = this.area,
  cityCode = this.cityCode,
  cityDescription = this.cityDescription,
  countyCode = this.countyCode,
  countyDescription = this.countyDescription,
  postCode = this.postCode,
  countryCode = this.countryCode,
  countryDescription = this.countryDescription,
  mailFlag = this.mailFlag,
  startDate = this.startDate,
  endDate = this.endDate,
  noFixedAddress = this.noFixedAddress,
  comments = this.comments,
)

fun PageImpl<ContactWithAddressEntity>.toModel(): Page<ContactSearchResultItem> = map { it.toModel() }

fun ContactRelationship.toEntity(
  contactId: Long,
  createdBy: String,
) = newPrisonerContact(
  contactId,
  this.prisonerNumber,
  "SOCIAL",
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
    middleNames = this.middleNames,
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
  middleNames: String?,
  dateOfBirth: LocalDate?,
  estimatedIsOverEighteen: EstimatedIsOverEighteen?,
  createdBy: String,
): ContactEntity {
  return ContactEntity(
    0,
    title,
    firstName,
    lastName,
    middleNames,
    dateOfBirth,
    estimatedIsOverEighteen,
    false,
    null,
    createdBy,
    LocalDateTime.now(),
  )
}

private fun newPrisonerContact(
  contactId: Long,
  prisonerNumber: String,
  contactType: String,
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
    contactType,
    relationshipType,
    nextOfKin,
    emergencyContact,
    comments,
    createdBy,
    LocalDateTime.now(),
  )
}
