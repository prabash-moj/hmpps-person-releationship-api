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
    estimatedIsOverEighteen = this.estimatedIsOverEighteen,
    relationshipCode = this.relationshipType,
    relationshipDescription = this.relationshipDescription ?: "",
    flat = this.flat ?: "",
    property = this.property ?: "",
    street = this.street ?: "",
    area = this.area ?: "",
    cityCode = this.cityCode ?: "",
    cityDescription = this.cityDescription ?: "",
    countyCode = this.countyCode ?: "",
    countyDescription = this.countyDescription ?: "",
    postCode = this.postCode ?: "",
    countryCode = this.countryCode ?: "",
    countryDescription = this.countryDescription ?: "",
    approvedVisitor = this.approvedVisitor,
    nextOfKin = this.nextOfKin,
    emergencyContact = this.emergencyContact,
    awareOfCharges = this.awareOfCharges,
    comments = this.comments,
  )
}
