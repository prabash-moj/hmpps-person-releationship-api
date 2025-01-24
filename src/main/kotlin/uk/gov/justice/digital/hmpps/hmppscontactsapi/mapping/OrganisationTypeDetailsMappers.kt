package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.OrganisationTypeDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.OrganisationTypeDetails

fun OrganisationTypeDetailsEntity.toModel(): OrganisationTypeDetails = OrganisationTypeDetails(
  organisationId = this.id.organisationId,
  organisationType = this.id.organisationType,
  organisationTypeDescription = this.organisationTypeDescription,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  updatedBy = this.updatedBy,
  updatedTime = this.updatedTime,
)

fun List<OrganisationTypeDetailsEntity>.toModel(): List<OrganisationTypeDetails> = map { it.toModel() }
