package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.OrganisationWebAddressEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.OrganisationWebAddressDetails

fun OrganisationWebAddressEntity.toModel(): OrganisationWebAddressDetails = OrganisationWebAddressDetails(
  organisationWebAddressId = this.organisationWebAddressId,
  organisationId = this.organisationId,
  webAddress = this.webAddress,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  updatedBy = this.updatedBy,
  updatedTime = this.updatedTime,
)

fun List<OrganisationWebAddressEntity>.toModel() = map { it.toModel() }
