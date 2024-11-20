package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactRestrictionEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.SyncContactRestriction

fun ContactRestrictionEntity.toModel(): SyncContactRestriction {
  return SyncContactRestriction(
    contactRestrictionId = this.contactRestrictionId,
    contactId = this.contactId!!,
    restrictionType = this.restrictionType,
    startDate = this.startDate,
    expiryDate = this.expiryDate,
    comments = this.comments,
    staffUsername = this.staffUsername,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
    amendedBy = this.amendedBy,
    amendedTime = this.amendedTime,
  )
}

fun List<ContactRestrictionEntity>.toModel() = map { it.toModel() }

fun SyncCreateContactRestrictionRequest.toEntity() = ContactRestrictionEntity(
  contactRestrictionId = 0L,
  contactId = contactId,
  restrictionType = restrictionType,
  startDate = startDate,
  expiryDate = expiryDate,
  comments = comments,
  staffUsername = staffUsername,
  createdBy = createdBy,
  createdTime = createdTime,
)
