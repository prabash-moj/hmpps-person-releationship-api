package uk.gov.justice.digital.hmpps.hmppscontactsapi.facade

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreatePhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdatePhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactPhoneDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.ContactPhoneService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsService

@Service
class ContactPhoneFacade(
  private val contactPhoneService: ContactPhoneService,
  private val outboundEventsService: OutboundEventsService,
) {

  fun create(contactId: Long, request: CreatePhoneRequest): ContactPhoneDetails {
    return contactPhoneService.create(contactId, request).also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.CONTACT_PHONE_CREATED,
        identifier = it.contactPhoneId,
        contactId = contactId,
      )
    }
  }

  fun get(contactId: Long, contactPhoneId: Long): ContactPhoneDetails? {
    return contactPhoneService.get(contactId, contactPhoneId)
  }

  fun update(contactId: Long, contactPhoneId: Long, request: UpdatePhoneRequest): ContactPhoneDetails {
    return contactPhoneService.update(contactId, contactPhoneId, request).also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.CONTACT_PHONE_AMENDED,
        identifier = contactPhoneId,
        contactId = contactId,
      )
    }
  }

  fun delete(contactId: Long, contactPhoneId: Long) {
    contactPhoneService.delete(contactId, contactPhoneId).also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.CONTACT_PHONE_DELETED,
        identifier = contactPhoneId,
        contactId = contactId,
      )
    }
  }
}
