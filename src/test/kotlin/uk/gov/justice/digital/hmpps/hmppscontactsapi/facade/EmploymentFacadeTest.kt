package uk.gov.justice.digital.hmpps.hmppscontactsapi.facade

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createEmploymentDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.internal.PatchEmploymentResult
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.PatchEmploymentsRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.Source
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.EmploymentService

class EmploymentFacadeTest {

  private val employmentService: EmploymentService = mock()
  private val outboundEventsService: OutboundEventsService = mock()
  private val facade = EmploymentFacade(employmentService, outboundEventsService)

  @Test
  fun `patch contact should send updates for all ids created updated or deleted`() {
    val expectedEmploymentDetails = listOf(createEmploymentDetails())
    val contactId = 123L
    val request = PatchEmploymentsRequest(emptyList(), emptyList(), emptyList(), "USER")

    whenever(employmentService.patchEmployments(contactId, request)).thenReturn(
      PatchEmploymentResult(
        createdIds = listOf(1, 2),
        updatedIds = listOf(3, 4),
        deletedIds = listOf(5, 6),
        employmentsAfterUpdate = expectedEmploymentDetails,
      ),
    )

    val result = facade.patchEmployments(contactId, request)

    assertThat(result).isEqualTo(expectedEmploymentDetails)
    verify(outboundEventsService).send(
      outboundEvent = OutboundEvent.EMPLOYMENT_CREATED,
      identifier = 1,
      contactId = contactId,
      noms = "",
      source = Source.DPS,
    )
    verify(outboundEventsService).send(
      outboundEvent = OutboundEvent.EMPLOYMENT_CREATED,
      identifier = 2,
      contactId = contactId,
      noms = "",
      source = Source.DPS,
    )

    verify(outboundEventsService).send(
      outboundEvent = OutboundEvent.EMPLOYMENT_UPDATED,
      identifier = 3,
      contactId = contactId,
      noms = "",
      source = Source.DPS,
    )
    verify(outboundEventsService).send(
      outboundEvent = OutboundEvent.EMPLOYMENT_UPDATED,
      identifier = 4,
      contactId = contactId,
      noms = "",
      source = Source.DPS,
    )

    verify(outboundEventsService).send(
      outboundEvent = OutboundEvent.EMPLOYMENT_DELETED,
      identifier = 5,
      contactId = contactId,
      noms = "",
      source = Source.DPS,
    )
    verify(outboundEventsService).send(
      outboundEvent = OutboundEvent.EMPLOYMENT_DELETED,
      identifier = 6,
      contactId = contactId,
      noms = "",
      source = Source.DPS,
    )
  }
}
