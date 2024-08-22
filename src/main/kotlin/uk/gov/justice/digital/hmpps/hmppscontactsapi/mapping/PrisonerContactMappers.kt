package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactSummaryEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactSummary

fun PrisonerContactSummaryEntity.toModel(): PrisonerContactSummary {
  return PrisonerContactSummary(
    prisonerContactId = this.prisonerContactId,
    contactId = this.contactId,
    prisonerNumber = this.prisonerNumber,
    surname = this.lastName,
    forename = this.firstName,
    middleName = this.middleName,
    dateOfBirth = this.dateOfBirth!!,
    relationshipCode = this.relationshipType,
    relationshipDescription = this.relationshipDescription ?: "",
    flat = this.flat ?: "",
    property = this.property ?: "",
    street = this.street ?: "",
    area = this.area ?: "",
    cityCode = this.cityCode ?: "",
    countyCode = this.countyCode ?: "",
    postCode = this.postCode ?: "",
    countryCode = this.countryCode ?: "",
    approvedVisitor = this.approvedVisitor,
    nextOfKin = this.nextOfKin,
    emergencyContact = this.emergencyContact,
    awareOfCharges = this.awareOfCharges,
    comments = this.comments ?: "",
  )
}

fun List<PrisonerContactSummaryEntity>.toModel() = map { it.toModel() }
