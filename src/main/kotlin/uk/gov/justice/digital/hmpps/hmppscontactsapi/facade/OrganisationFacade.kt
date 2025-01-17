package uk.gov.justice.digital.hmpps.hmppscontactsapi.facade

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Organisation
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.OrganisationService

@Service
class OrganisationFacade(
  private val organisationService: OrganisationService,
) {

  fun getOrganisationById(organisationId: Long): Organisation {
    return organisationService.getOrganisationById(organisationId)
  }
}
