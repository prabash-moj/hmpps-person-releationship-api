package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactRestrictionEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.SyncPrisonerContactRestriction

fun SyncCreatePrisonerContactRestrictionRequest.toEntity(): PrisonerContactRestrictionEntity {
  return PrisonerContactRestrictionEntity(
    prisonerContactRestrictionId = 0L,
    prisonerContactId = this.contactId,
    restrictionType = this.restrictionType,
    startDate = this.startDate,
    expiryDate = this.expiryDate,
    comments = this.comments,
    staffUsername = this.staffUsername,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
  )
}

fun PrisonerContactRestrictionEntity.toResponse(): SyncPrisonerContactRestriction {
  return SyncPrisonerContactRestriction(
    prisonerContactRestrictionId = this.prisonerContactRestrictionId,
    contactId = this.prisonerContactId,
    restrictionType = this.restrictionType,
    startDate = this.startDate,
    expiryDate = this.expiryDate,
    comments = this.comments,
    staffUsername = this.staffUsername,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
    updatedBy = this.amendedBy,
    updatedTime = this.amendedTime,
  )
}
