package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactRestrictionEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.SyncPrisonerContactRestriction

fun SyncCreatePrisonerContactRestrictionRequest.toEntity(): PrisonerContactRestrictionEntity {
  return PrisonerContactRestrictionEntity(
    prisonerContactRestrictionId = 0L,
    prisonerContactId = this.prisonerContactId,
    restrictionType = this.restrictionType,
    startDate = this.startDate,
    expiryDate = this.expiryDate,
    comments = this.comments,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
  )
}

fun PrisonerContactRestrictionEntity.toResponse(
  contactId: Long,
  prisonerNumber: String,
): SyncPrisonerContactRestriction {
  return SyncPrisonerContactRestriction(
    prisonerContactRestrictionId = this.prisonerContactRestrictionId,
    prisonerContactId = this.prisonerContactId,
    contactId = contactId,
    prisonerNumber = prisonerNumber,
    restrictionType = this.restrictionType,
    startDate = this.startDate,
    expiryDate = this.expiryDate,
    comments = this.comments,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
    updatedBy = this.updatedBy,
    updatedTime = this.updatedTime,
  )
}
