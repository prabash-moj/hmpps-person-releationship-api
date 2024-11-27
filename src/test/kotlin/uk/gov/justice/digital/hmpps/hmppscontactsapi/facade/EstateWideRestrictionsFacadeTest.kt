package uk.gov.justice.digital.hmpps.hmppscontactsapi.facade

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.RestrictionsService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.Source
import java.time.LocalDate
import java.time.LocalDateTime

class EstateWideRestrictionsFacadeTest {

  private val restrictionService: RestrictionsService = mock()
  private val outboundEventsService: OutboundEventsService = mock()
  private val facade = EstateWideRestrictionsFacade(restrictionService, outboundEventsService)

  private val contactId: Long = 99
  private val contactRestrictionId: Long = 66

  @Test
  fun `should send created estate wide restriction event on success`() {
    val request = CreateContactRestrictionRequest(
      restrictionType = "BAN",
      startDate = LocalDate.of(2020, 1, 1),
      expiryDate = LocalDate.of(2022, 2, 2),
      comments = "Some comments",
      createdBy = "created",
    )

    val expected = ContactRestrictionDetails(
      contactRestrictionId = contactRestrictionId,
      contactId = contactId,
      restrictionType = "BAN",
      restrictionTypeDescription = "Banned",
      startDate = LocalDate.of(2020, 1, 1),
      expiryDate = LocalDate.of(2022, 2, 2),
      comments = "Some comments",
      createdBy = "created",
      createdTime = LocalDateTime.now(),
      updatedBy = null,
      updatedTime = null,
    )
    whenever(restrictionService.createEstateWideRestriction(contactId, request)).thenReturn(expected)

    val result = facade.createEstateWideRestriction(contactId, request)

    assertThat(result).isEqualTo(expected)
    verify(restrictionService).createEstateWideRestriction(contactId, request)
    verify(outboundEventsService).send(
      outboundEvent = OutboundEvent.CONTACT_RESTRICTION_CREATED,
      identifier = contactRestrictionId,
      contactId = contactId,
      noms = "",
      source = Source.DPS,
    )
  }

  @Test
  fun `should not send created estate wide restriction event on failure`() {
    val request = CreateContactRestrictionRequest(
      restrictionType = "BAN",
      startDate = LocalDate.of(2020, 1, 1),
      expiryDate = LocalDate.of(2022, 2, 2),
      comments = "Some comments",
      createdBy = "created",
    )

    val expected = RuntimeException("Bang!")
    whenever(restrictionService.createEstateWideRestriction(contactId, request)).thenThrow(expected)

    val result = assertThrows<RuntimeException> {
      facade.createEstateWideRestriction(contactId, request)
    }

    assertThat(result).isEqualTo(expected)
    verify(restrictionService).createEstateWideRestriction(contactId, request)
    verify(outboundEventsService, Mockito.never()).send(any(), any(), any(), any(), any())
  }

  @Test
  fun `should send updated estate wide restriction event on success`() {
    val request = UpdateContactRestrictionRequest(
      restrictionType = "BAN",
      startDate = LocalDate.of(2020, 1, 1),
      expiryDate = LocalDate.of(2022, 2, 2),
      comments = "Some comments",
      updatedBy = "updated",
    )

    val expected = ContactRestrictionDetails(
      contactRestrictionId = contactRestrictionId,
      contactId = contactId,
      restrictionType = "BAN",
      restrictionTypeDescription = "Banned",
      startDate = LocalDate.of(2020, 1, 1),
      expiryDate = LocalDate.of(2022, 2, 2),
      comments = "Some comments",
      createdBy = "created",
      createdTime = LocalDateTime.now(),
      updatedBy = "updated",
      updatedTime = LocalDateTime.now(),
    )
    whenever(restrictionService.updateEstateWideRestriction(contactId, contactRestrictionId, request)).thenReturn(expected)

    val result = facade.updateEstateWideRestriction(contactId, contactRestrictionId, request)

    assertThat(result).isEqualTo(expected)
    verify(restrictionService).updateEstateWideRestriction(contactId, contactRestrictionId, request)
    verify(outboundEventsService).send(
      outboundEvent = OutboundEvent.CONTACT_RESTRICTION_UPDATED,
      identifier = contactRestrictionId,
      contactId = contactId,
      noms = "",
      source = Source.DPS,
    )
  }

  @Test
  fun `should not send updated estate wide restriction event on failure`() {
    val request = UpdateContactRestrictionRequest(
      restrictionType = "BAN",
      startDate = LocalDate.of(2020, 1, 1),
      expiryDate = LocalDate.of(2022, 2, 2),
      comments = "Some comments",
      updatedBy = "updated",
    )

    val expected = RuntimeException("Bang!")
    whenever(restrictionService.updateEstateWideRestriction(contactId, contactRestrictionId, request)).thenThrow(expected)

    val result = assertThrows<RuntimeException> {
      facade.updateEstateWideRestriction(contactId, contactRestrictionId, request)
    }

    assertThat(result).isEqualTo(expected)
    verify(restrictionService).updateEstateWideRestriction(contactId, contactRestrictionId, request)
    verify(outboundEventsService, Mockito.never()).send(any(), any(), any(), any(), any())
  }
}
