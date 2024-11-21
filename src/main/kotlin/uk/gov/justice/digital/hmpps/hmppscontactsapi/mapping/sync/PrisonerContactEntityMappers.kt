package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreatePrisonerContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.SyncPrisonerContact

fun SyncCreatePrisonerContactRequest.toEntity(): PrisonerContactEntity {
  return PrisonerContactEntity(
    prisonerContactId = 0L,
    contactId = this.contactId,
    prisonerNumber = this.prisonerNumber,
    contactType = this.contactType,
    relationshipType = this.relationshipType,
    nextOfKin = this.nextOfKin,
    emergencyContact = this.emergencyContact,
    comments = this.comments,
    active = this.active ?: true,
    approvedVisitor = this.approvedVisitor ?: false,
    currentTerm = this.currentTerm ?: true,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
  ).also {
    it.approvedBy = this.approvedBy
    it.approvedTime = this.approvedTime
    it.expiryDate = this.expiryDate
    it.createdAtPrison = this.createdAtPrison
  }
}

fun PrisonerContactEntity.toResponse(): SyncPrisonerContact {
  return SyncPrisonerContact(
    id = this.prisonerContactId,
    contactId = this.contactId,
    prisonerNumber = this.prisonerNumber,
    contactType = this.contactType,
    relationshipType = this.relationshipType,
    nextOfKin = this.nextOfKin,
    emergencyContact = this.emergencyContact,
    comments = this.comments,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
    active = this.active,
    approvedVisitor = this.approvedVisitor,
    currentTerm = this.currentTerm,
    approvedBy = this.approvedBy,
    approvedTime = this.approvedTime,
    expiryDate = this.expiryDate,
    updatedTime = this.amendedTime,
    updatedBy = this.amendedBy,
    createdAtPrison = this.createdAtPrison,
  )
}
