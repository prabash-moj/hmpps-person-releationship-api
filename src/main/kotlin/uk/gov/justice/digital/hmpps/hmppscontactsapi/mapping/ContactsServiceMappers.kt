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
  "S",
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
    contactId = 0,
    title = title,
    firstName = firstName,
    lastName = lastName,
    middleNames = middleNames,
    dateOfBirth = dateOfBirth,
    estimatedIsOverEighteen = estimatedIsOverEighteen,
    isDeceased = false,
    deceasedDate = null,
    createdBy = createdBy,
    createdTime = LocalDateTime.now(),
    placeOfBirth = null,
    active = false,
    suspended = false,
    staffFlag = false,
    remitterFlag = false,
    coronerNumber = null,
    gender = null,
    domesticStatus = null,
    languageCode = null,
    nationalityCode = null,
    interpreterRequired = false,
    amendedBy = null,
    amendedTime = null,
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
    contactId = contactId,
    prisonerNumber = prisonerNumber,
    contactType = contactType,
    relationshipType = relationshipType,
    nextOfKin = nextOfKin,
    emergencyContact = emergencyContact,
    comments = comments,
    createdBy = createdBy,
    createdTime = LocalDateTime.now(),
    active = true,
    approvedVisitor = false,
    currentTerm = true,
  )
}
