package uk.gov.justice.digital.hmpps.hmppscontactsapi.facade

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactIdentityDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.ContactIdentityService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsService

class ContactIdentityFacadeTest {

  private val identityService: ContactIdentityService = mock()
  private val eventsService: OutboundEventsService = mock()
  private val facade = ContactIdentityFacade(identityService, eventsService)

  private val contactId = 11L
  private val contactIdentityId = 99L
  private val contactIdentityDetails = createContactIdentityDetails(id = contactIdentityId, contactId = contactId)

  @Test
  fun `should send event if create success`() {
    whenever(identityService.create(any(), any())).thenReturn(contactIdentityDetails)
    whenever(eventsService.send(any(), any())).then {}
    val request = CreateIdentityRequest(
      identityType = "DRIVING_LIC",
      identityValue = "DL123456789",
      createdBy = "created",
    )

    val result = facade.create(contactId, request)

    assertThat(result).isEqualTo(contactIdentityDetails)
    verify(identityService).create(contactId, request)
    verify(eventsService).send(OutboundEvent.CONTACT_IDENTITY_CREATED, contactIdentityId)
  }

  @Test
  fun `should not send event if create throws exception and propagate the exception`() {
    val expectedException = RuntimeException("Bang!")
    whenever(identityService.create(any(), any())).thenThrow(expectedException)
    whenever(eventsService.send(any(), any())).then {}
    val request = CreateIdentityRequest(
      identityType = "DRIVING_LIC",
      identityValue = "DL123456789",
      createdBy = "created",
    )

    val exception = assertThrows<RuntimeException> {
      facade.create(contactId, request)
    }

    assertThat(exception).isEqualTo(exception)
    verify(identityService).create(contactId, request)
    verify(eventsService, never()).send(any(), any())
  }

  @Test
  fun `should not send no event on get`() {
    whenever(identityService.get(any(), any())).thenReturn(contactIdentityDetails)

    val result = facade.get(contactId, contactIdentityId)

    assertThat(result).isEqualTo(contactIdentityDetails)
    verify(identityService).get(contactId, contactIdentityId)
    verify(eventsService, never()).send(any(), any())
  }
}
