package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import org.springframework.data.domain.Page
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.OrganisationSummaryEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.OrganisationSummary

fun OrganisationSummaryEntity.toModel(): OrganisationSummary = OrganisationSummary(
  organisationId = this.organisationId,
  organisationName = this.organisationName,
  organisationActive = this.organisationActive,
  flat = this.flat,
  property = this.property,
  street = this.street,
  area = this.area,
  cityCode = this.cityCode,
  cityDescription = this.cityDescription,
  countyCode = this.countyCode,
  countyDescription = this.countyDescription,
  postcode = this.postCode,
  countryCode = this.countryCode,
  countryDescription = this.countryDescription,
  businessPhoneNumber = this.businessPhoneNumber,
  businessPhoneNumberExtension = this.businessPhoneNumberExtension,
)

fun Page<OrganisationSummaryEntity>.toModel(): Page<OrganisationSummary> = map { it.toModel() }
