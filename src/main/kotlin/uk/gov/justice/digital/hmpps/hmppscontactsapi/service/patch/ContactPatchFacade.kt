package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.patch

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.patch.PatchContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.patch.PatchContactResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.ContactPatchService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsService

@Service
class ContactPatchFacade(
  private val outboundEventsService: OutboundEventsService,
  private val contactService: ContactPatchService,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  fun patch(id: Long, request: PatchContactRequest): PatchContactResponse {
    return contactService.patch(id, request)
      .also {
        logger.info("Send patch domain event to {} {} ", OutboundEvent.CONTACT_AMENDED, id)
        outboundEventsService.send(OutboundEvent.CONTACT_AMENDED, id)
      }
  }
}
