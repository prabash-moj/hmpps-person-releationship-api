package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.SyncContact

fun ContactEntity.mapEntityToSyncResponse(): SyncContact {
  return SyncContact(
    id = this.contactId,
    title = this.title,
    firstName = this.firstName,
    lastName = this.lastName,
    middleName = this.middleNames,
    dateOfBirth = this.dateOfBirth,
    estimatedIsOverEighteen = this.estimatedIsOverEighteen,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
    isStaff = this.staffFlag,
    remitter = this.remitterFlag,
    deceasedFlag = this.isDeceased,
    deceasedDate = this.deceasedDate,
    gender = this.gender,
    domesticStatus = this.domesticStatus,
    languageCode = this.languageCode,
    interpreterRequired = this.interpreterRequired,
    updatedBy = this.amendedBy,
    updatedTime = this.amendedTime,
  )
}

fun SyncCreateContactRequest.mapSyncRequestToEntity() = ContactEntity(
  contactId = this.personId,
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
  staffFlag = this.isStaff,
  remitterFlag = this.remitter,
  gender = this.gender,
  domesticStatus = this.domesticStatus,
  languageCode = this.languageCode,
  interpreterRequired = this.interpreterRequired ?: false,
  amendedBy = null,
  amendedTime = null,
)
