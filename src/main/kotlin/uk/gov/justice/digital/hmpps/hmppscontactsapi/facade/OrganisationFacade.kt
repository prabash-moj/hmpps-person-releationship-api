package uk.gov.justice.digital.hmpps.hmppscontactsapi.facade

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateOrganisationRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Organisation
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.OrganisationService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsService

@Service
class OrganisationFacade(
  private val organisationService: OrganisationService,
  private val outboundEventsService: OutboundEventsService,
) {

  fun create(request: CreateOrganisationRequest): Organisation {
    return organisationService.create(request).also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_CREATED,
        identifier = it.organisationId,
      )
    }
  }

  fun getOrganisationById(organisationId: Long): Organisation {
    return organisationService.getOrganisationById(organisationId)
  }
}
