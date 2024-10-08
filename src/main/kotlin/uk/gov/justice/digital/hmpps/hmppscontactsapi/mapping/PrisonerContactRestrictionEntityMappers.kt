package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactRestrictionEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.PrisonerContactRestriction

fun CreatePrisonerContactRestrictionRequest.toEntity(): PrisonerContactRestrictionEntity {
  return PrisonerContactRestrictionEntity(
    prisonerContactRestrictionId = 0L,
    prisonerContactId = this.contactId,
    restrictionType = this.restrictionType,
    startDate = this.startDate,
    expiryDate = this.expiryDate,
    comments = this.comments,
    authorisedBy = this.authorisedBy,
    authorisedTime = this.authorisedTime,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
  )
}

fun PrisonerContactRestrictionEntity.toResponse(): PrisonerContactRestriction {
  return PrisonerContactRestriction(
    prisonerContactRestrictionId = this.prisonerContactRestrictionId,
    contactId = this.prisonerContactId,
    restrictionType = this.restrictionType,
    startDate = this.startDate,
    expiryDate = this.expiryDate,
    comments = this.comments,
    authorisedBy = this.authorisedBy,
    authorisedTime = this.authorisedTime,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
    amendedBy = this.amendedBy,
    amendedTime = this.amendedTime,
  )
}
