package uk.gov.justice.digital.hmpps.hmppscontactsapi.facade

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateOrganisationRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.OrganisationSearchRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.OrganisationDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.OrganisationSummary
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.OrganisationService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsService

@Service
class OrganisationFacade(
  private val organisationService: OrganisationService,
  private val outboundEventsService: OutboundEventsService,
) {

  fun create(request: CreateOrganisationRequest): OrganisationDetails {
    return organisationService.create(request).also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_CREATED,
        identifier = it.organisationId,
      )
    }
  }

  fun getOrganisationById(organisationId: Long): OrganisationDetails {
    return organisationService.getOrganisationById(organisationId)
  }

  fun search(request: OrganisationSearchRequest, pageable: Pageable): Page<OrganisationSummary> =
    organisationService.search(request, pageable)
}
