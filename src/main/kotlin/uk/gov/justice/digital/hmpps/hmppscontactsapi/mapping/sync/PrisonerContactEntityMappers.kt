package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreatePrisonerContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.PrisonerContact

fun CreatePrisonerContactRequest.toEntity(): PrisonerContactEntity {
  return PrisonerContactEntity(
    prisonerContactId = 0L,
    contactId = this.contactId,
    prisonerNumber = this.prisonerNumber,
    relationshipType = this.relationshipType,
    nextOfKin = this.nextOfKin,
    emergencyContact = this.emergencyContact,
    comments = this.comments,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
  ).also {
    it.active = this.active
    it.approvedVisitor = this.approvedVisitor
    it.awareOfCharges = this.awareOfCharges
    it.canBeContacted = this.canBeContacted
    it.approvedBy = this.approvedBy
    it.approvedTime = this.approvedTime
    it.expiryDate = this.expiryDate
    it.createdAtPrison = this.createdAtPrison
  }
}

fun PrisonerContactEntity.toResponse(): PrisonerContact {
  return PrisonerContact(
    id = this.prisonerContactId,
    contactId = this.contactId,
    prisonerNumber = this.prisonerNumber,
    relationshipType = this.relationshipType,
    nextOfKin = this.nextOfKin,
    emergencyContact = this.emergencyContact,
    comments = this.comments,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
    active = this.active,
    approvedVisitor = this.approvedVisitor,
    awareOfCharges = this.awareOfCharges,
    canBeContacted = this.canBeContacted,
    approvedBy = this.approvedBy,
    approvedTime = this.approvedTime,
    expiryDate = this.expiryDate,
    amendedTime = this.amendedTime,
    amendedBy = this.amendedBy,
    createdAtPrison = this.createdAtPrison,
  )
}
