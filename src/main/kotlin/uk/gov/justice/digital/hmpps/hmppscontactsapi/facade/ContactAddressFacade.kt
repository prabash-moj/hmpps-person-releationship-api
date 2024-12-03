package uk.gov.justice.digital.hmpps.hmppscontactsapi.facade

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactAddressResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.ContactAddressService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsService

@Service
class ContactAddressFacade(
  private val contactAddressService: ContactAddressService,
  private val outboundEventsService: OutboundEventsService,
) {

  fun create(contactId: Long, request: CreateContactAddressRequest): ContactAddressResponse {
    return contactAddressService.create(contactId, request).also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.CONTACT_ADDRESS_CREATED,
        identifier = it.contactAddressId,
        contactId = contactId,
      )
    }
  }

  fun update(contactId: Long, contactAddressId: Long, request: UpdateContactAddressRequest): ContactAddressResponse {
    return contactAddressService.update(contactId, contactAddressId, request).also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.CONTACT_ADDRESS_UPDATED,
        identifier = it.contactAddressId,
        contactId = contactId,
      )
    }
  }

  fun delete(contactId: Long, contactAddressId: Long) {
    contactAddressService.delete(contactId, contactAddressId).also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.CONTACT_ADDRESS_DELETED,
        identifier = it.contactAddressId,
        contactId = contactId,
      )
    }
  }

  fun get(contactId: Long, contactAddressId: Long) = contactAddressService.get(contactId, contactAddressId)
}
