package uk.gov.justice.digital.hmpps.hmppscontactsapi.facade

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.RestrictionsService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsService

@Service
class ContactGlobalRestrictionsFacade(
  private val restrictionsService: RestrictionsService,
  private val outboundEventsService: OutboundEventsService,
) {

  fun getGlobalRestrictionsForContact(contactId: Long): List<ContactRestrictionDetails> {
    return restrictionsService.getGlobalRestrictionsForContact(contactId)
  }

  fun createContactGlobalRestriction(
    contactId: Long,
    request: CreateContactRestrictionRequest,
  ): ContactRestrictionDetails {
    return restrictionsService.createContactGlobalRestriction(contactId, request).also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.CONTACT_RESTRICTION_CREATED,
        identifier = it.contactRestrictionId,
        contactId = contactId,
      )
    }
  }

  fun updateContactGlobalRestriction(
    contactId: Long,
    contactRestrictionId: Long,
    request: UpdateContactRestrictionRequest,
  ): ContactRestrictionDetails {
    return restrictionsService.updateContactGlobalRestriction(contactId, contactRestrictionId, request).also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.CONTACT_RESTRICTION_UPDATED,
        identifier = contactRestrictionId,
        contactId = contactId,
      )
    }
  }
}
