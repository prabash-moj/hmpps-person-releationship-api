package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactSummaryEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactSummary

fun PrisonerContactSummaryEntity.toModel(): PrisonerContactSummary {
  return PrisonerContactSummary(
    prisonerContactId = this.prisonerContactId,
    contactId = this.contactId,
    prisonerNumber = this.prisonerNumber,
    lastName = this.lastName,
    firstName = this.firstName,
    middleNames = this.middleNames,
    dateOfBirth = this.dateOfBirth,
    relationshipToPrisoner = this.relationshipToPrisoner,
    relationshipDescription = this.relationshipDescription ?: "",
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
    primaryAddress = this.primaryAddress,
    mailAddress = this.mailFlag,
    phoneType = this.phoneType,
    phoneTypeDescription = this.phoneTypeDescription,
    phoneNumber = this.phoneNumber,
    extNumber = this.extNumber,
    approvedVisitor = this.approvedVisitor,
    nextOfKin = this.nextOfKin,
    emergencyContact = this.emergencyContact,
    isRelationshipActive = this.active,
    currentTerm = this.currentTerm,
    comments = this.comments,
  )
}
