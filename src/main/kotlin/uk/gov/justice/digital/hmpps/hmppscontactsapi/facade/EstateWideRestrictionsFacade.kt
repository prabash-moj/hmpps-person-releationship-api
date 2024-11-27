package uk.gov.justice.digital.hmpps.hmppscontactsapi.facade

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.RestrictionsService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsService

@Service
class EstateWideRestrictionsFacade(
  private val restrictionsService: RestrictionsService,
  private val outboundEventsService: OutboundEventsService,
) {

  fun getEstateWideRestrictionsForContact(contactId: Long): List<ContactRestrictionDetails> {
    return restrictionsService.getEstateWideRestrictionsForContact(contactId)
  }

  fun createEstateWideRestriction(
    contactId: Long,
    request: CreateContactRestrictionRequest,
  ): ContactRestrictionDetails {
    return restrictionsService.createEstateWideRestriction(contactId, request).also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.CONTACT_RESTRICTION_CREATED,
        identifier = it.contactRestrictionId,
        contactId = contactId,
      )
    }
  }

  fun updateEstateWideRestriction(
    contactId: Long,
    contactRestrictionId: Long,
    request: UpdateContactRestrictionRequest,
  ): ContactRestrictionDetails {
    return restrictionsService.updateEstateWideRestriction(contactId, contactRestrictionId, request).also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.CONTACT_RESTRICTION_UPDATED,
        identifier = contactRestrictionId,
        contactId = contactId,
      )
    }
  }
}
