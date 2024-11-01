package uk.gov.justice.digital.hmpps.hmppscontactsapi.facade

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactIdentityDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.ContactIdentityService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsService

@Service
class ContactIdentityFacade(
  private val contactIdentityService: ContactIdentityService,
  private val outboundEventsService: OutboundEventsService,
) {

  fun create(contactId: Long, request: CreateIdentityRequest): ContactIdentityDetails {
    return contactIdentityService.create(contactId, request).also {
      outboundEventsService.send(OutboundEvent.CONTACT_IDENTITY_CREATED, it.contactIdentityId)
    }
  }

  fun get(contactId: Long, contactIdentityId: Long): ContactIdentityDetails {
    return contactIdentityService.get(contactId, contactIdentityId) ?: throw EntityNotFoundException("Contact identity with id ($contactIdentityId) not found for contact ($contactId)")
  }

  fun delete(contactId: Long, contactIdentityId: Long) {
    contactIdentityService.delete(contactId, contactIdentityId).also {
      outboundEventsService.send(OutboundEvent.CONTACT_IDENTITY_DELETED, contactIdentityId)
    }
  }
}
