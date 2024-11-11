package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.patch

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.AddContactRelationshipRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactSearchRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.patch.PatchContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.patch.PatchContactResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactSearchResultItem
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.ContactPatchService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.ContactService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsService

@Service
class ContactFacade(
  private val outboundEventsService: OutboundEventsService,
  private val contactPatchService: ContactPatchService,
  private val contactService: ContactService,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  fun createContact(request: CreateContactRequest): ContactDetails {
    return contactService.createContact(request)
      .also { creationResult ->
        outboundEventsService.send(OutboundEvent.CONTACT_CREATED, creationResult.createdContact.id)
        creationResult.createdRelationship?.let { outboundEventsService.send(OutboundEvent.PRISONER_CONTACT_CREATED, it) }
      }
      .createdContact
  }

  fun addContactRelationship(contactId: Long, request: AddContactRelationshipRequest) {
    val prisonerContactId = contactService.addContactRelationship(contactId, request)
    outboundEventsService.send(OutboundEvent.PRISONER_CONTACT_CREATED, prisonerContactId)
  }

  fun patch(id: Long, request: PatchContactRequest): PatchContactResponse {
    return contactPatchService.patch(id, request)
      .also {
        logger.info("Send patch domain event to {} {} ", OutboundEvent.CONTACT_AMENDED, id)
        outboundEventsService.send(OutboundEvent.CONTACT_AMENDED, id)
      }
  }

  fun getContact(id: Long): ContactDetails? {
    return contactService.getContact(id)
  }

  fun searchContacts(pageable: Pageable, request: ContactSearchRequest): Page<ContactSearchResultItem> {
    return contactService.searchContacts(pageable, request)
  }
}
