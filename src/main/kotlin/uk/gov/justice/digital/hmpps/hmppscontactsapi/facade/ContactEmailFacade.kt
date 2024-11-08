package uk.gov.justice.digital.hmpps.hmppscontactsapi.facade

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactEmailDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.ContactEmailService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsService

@Service
class ContactEmailFacade(
  private val contactEmailService: ContactEmailService,
  private val outboundEventsService: OutboundEventsService,
) {

  fun create(contactId: Long, request: CreateEmailRequest): ContactEmailDetails {
    return contactEmailService.create(contactId, request).also {
      outboundEventsService.send(OutboundEvent.CONTACT_EMAIL_CREATED, it.contactEmailId)
    }
  }

  fun update(contactId: Long, contactEmailId: Long, request: UpdateEmailRequest): ContactEmailDetails {
    return contactEmailService.update(contactId, contactEmailId, request).also {
      outboundEventsService.send(OutboundEvent.CONTACT_EMAIL_AMENDED, contactEmailId)
    }
  }

  fun get(contactId: Long, contactEmailId: Long): ContactEmailDetails {
    return contactEmailService.get(contactId, contactEmailId) ?: throw EntityNotFoundException("Contact email with id ($contactEmailId) not found for contact ($contactId)")
  }

  fun delete(contactId: Long, contactEmailId: Long) {
    contactEmailService.delete(contactId, contactEmailId).also {
      outboundEventsService.send(OutboundEvent.CONTACT_EMAIL_DELETED, contactEmailId)
    }
  }
}
