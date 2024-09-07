package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactSummaryEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.IsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactSummary
import java.time.LocalDate

fun PrisonerContactSummaryEntity.toModel(): PrisonerContactSummary {
  return PrisonerContactSummary(
    prisonerContactId = this.prisonerContactId,
    contactId = this.contactId,
    prisonerNumber = this.prisonerNumber,
    surname = this.lastName,
    forename = this.firstName,
    middleName = this.middleName,
    dateOfBirth = this.dateOfBirth,
    isOverEighteen = this.mapIsOverEighteen(),
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

fun PrisonerContactSummaryEntity.mapIsOverEighteen(): IsOverEighteen {
  return if (this.dateOfBirth != null) {
    if (!this.dateOfBirth.isAfter(LocalDate.now().minusYears(18))) {
      IsOverEighteen.YES
    } else {
      IsOverEighteen.NO
    }
  } else {
    when (this.isOverEighteen) {
      true -> IsOverEighteen.YES
      false -> IsOverEighteen.NO
      else -> {
        IsOverEighteen.DO_NOT_KNOW
      }
    }
  }
}
