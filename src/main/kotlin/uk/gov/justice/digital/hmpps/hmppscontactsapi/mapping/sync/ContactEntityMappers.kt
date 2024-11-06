package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.Contact

fun ContactEntity.mapEntityToSyncResponse(): Contact {
  return Contact(
    id = this.contactId,
    title = this.title,
    firstName = this.firstName,
    lastName = this.lastName,
    middleName = this.middleNames,
    dateOfBirth = this.dateOfBirth,
    estimatedIsOverEighteen = this.estimatedIsOverEighteen,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
    placeOfBirth = this.placeOfBirth,
    active = this.active,
    suspended = this.suspended,
    isStaff = this.staffFlag,
    remitter = this.remitterFlag,
    deceasedFlag = this.isDeceased,
    deceasedDate = this.deceasedDate,
    coronerNumber = this.coronerNumber,
    gender = this.gender,
    domesticStatus = this.domesticStatus,
    languageCode = this.languageCode,
    nationalityCode = this.nationalityCode,
    interpreterRequired = this.interpreterRequired,
    amendedBy = this.amendedBy,
    amendedTime = this.amendedTime,
  )
}

fun CreateContactRequest.mapSyncRequestToEntity() = ContactEntity(
  contactId = 0L,
  title = this.title,
  firstName = this.firstName,
  lastName = this.lastName,
  middleNames = this.middleName,
  dateOfBirth = this.dateOfBirth,
  estimatedIsOverEighteen = this.estimatedIsOverEighteen,
  createdBy = this.createdBy,
  isDeceased = this.deceasedFlag!!,
  deceasedDate = this.deceasedDate,
  createdTime = this.createdTime,
  placeOfBirth = this.placeOfBirth,
  active = this.active,
  suspended = this.suspended,
  staffFlag = this.isStaff,
  remitterFlag = this.remitter,
  coronerNumber = this.coronerNumber,
  gender = this.gender,
  domesticStatus = this.domesticStatus,
  languageCode = this.languageCode,
  nationalityCode = this.nationalityCode,
  interpreterRequired = this.interpreterRequired ?: false,
  amendedBy = null,
  amendedTime = null,
)
