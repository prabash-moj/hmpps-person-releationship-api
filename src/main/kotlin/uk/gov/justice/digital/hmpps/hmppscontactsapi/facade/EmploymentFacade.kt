package uk.gov.justice.digital.hmpps.hmppscontactsapi.facade

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.PatchEmploymentsRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.EmploymentDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.Source
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.EmploymentService

@Service
class EmploymentFacade(
  private val employmentService: EmploymentService,
  private val outboundEventsService: OutboundEventsService,
) {

  fun patchEmployments(contactId: Long, request: PatchEmploymentsRequest): List<EmploymentDetails> {
    return employmentService.patchEmployments(contactId, request).also { result ->
      result.createdIds.onEach { outboundEventsService.send(OutboundEvent.EMPLOYMENT_CREATED, it, contactId = contactId, source = Source.DPS) }
      result.updatedIds.onEach { outboundEventsService.send(OutboundEvent.EMPLOYMENT_UPDATED, it, contactId = contactId, source = Source.DPS) }
      result.deletedIds.onEach { outboundEventsService.send(OutboundEvent.EMPLOYMENT_DELETED, it, contactId = contactId, source = Source.DPS) }
    }.employmentsAfterUpdate
  }
}
