package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.OrganisationEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Organisation

fun OrganisationEntity.toModel(): Organisation {
  return Organisation(
    organisationId = this.organisationId!!,
    organisationName = this.organisationName,
    programmeNumber = this.programmeNumber,
    vatNumber = this.vatNumber,
    caseloadId = this.caseloadId,
    comments = this.comments,
    active = this.active,
    deactivatedDate = this.deactivatedDate,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
    updatedBy = this.updatedBy,
    updatedTime = this.updatedTime,
  )
}

fun List<OrganisationEntity>.toModel() = map { it.toModel() }
