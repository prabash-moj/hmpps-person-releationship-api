package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.patch

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PatchContactResponse

fun ContactEntity.mapToResponse(): PatchContactResponse {
  return PatchContactResponse(
    id = this.id(),
    title = this.title,
    firstName = this.firstName,
    lastName = this.lastName,
    middleNames = this.middleNames,
    dateOfBirth = this.dateOfBirth,
    estimatedIsOverEighteen = this.estimatedIsOverEighteen,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
    isStaff = this.staffFlag,
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
