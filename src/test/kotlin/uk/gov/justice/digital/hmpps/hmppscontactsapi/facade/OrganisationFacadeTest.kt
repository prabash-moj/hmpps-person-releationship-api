package uk.gov.justice.digital.hmpps.hmppscontactsapi.facade

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateOrganisationRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.OrganisationDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.OrganisationService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsService
import java.time.LocalDate
import java.time.LocalDateTime

class OrganisationFacadeTest {

  private val organisationService: OrganisationService = mock()
  private val outboundEventsService: OutboundEventsService = mock()
  private val facade = OrganisationFacade(organisationService, outboundEventsService)

  @Test
  fun `should not send event on get`() {
    val organisationId = 1L
    val deactivatedDate = LocalDate.now()
    val createdTime = LocalDateTime.now().minusMinutes(20)
    val updatedTime = LocalDateTime.now().plusMinutes(20)
    val organisation = OrganisationDetails(
      organisationId = 1L,
      organisationName = "Name",
      programmeNumber = "P1",
      vatNumber = "V1",
      caseloadId = "C1",
      comments = "C2",
      active = false,
      deactivatedDate = deactivatedDate,
      organisationTypes = emptyList(),
      phoneNumbers = emptyList(),
      emailAddresses = emptyList(),
      webAddresses = emptyList(),
      addresses = emptyList(),
      createdBy = "Created by",
      createdTime = createdTime,
      updatedBy = "U1",
      updatedTime = updatedTime,
    )
    whenever(organisationService.getOrganisationById(organisationId)).thenReturn(organisation)

    val result = facade.getOrganisationById(organisationId)

    assertThat(result).isEqualTo(organisation)
    verify(organisationService).getOrganisationById(organisationId)
  }

  @Test
  fun `should create organisation and send outbound event`() {
    // Given
    val request = createTestOrganisationRequest()
    val expectedOrganisation = OrganisationDetails(
      organisationId = 1L,
      organisationName = request.organisationName,
      programmeNumber = request.programmeNumber,
      vatNumber = request.vatNumber,
      caseloadId = request.caseloadId,
      comments = request.comments,
      active = request.active,
      deactivatedDate = request.deactivatedDate,
      organisationTypes = emptyList(),
      phoneNumbers = emptyList(),
      emailAddresses = emptyList(),
      webAddresses = emptyList(),
      addresses = emptyList(),
      createdBy = "test-user",
      createdTime = LocalDateTime.now(),
      updatedBy = "test-user",
      updatedTime = LocalDateTime.now(),
    )

    whenever(organisationService.create(request)).thenReturn(expectedOrganisation)

    // When
    val result = facade.create(request)

    // Then
    assertThat(result).isEqualTo(expectedOrganisation)
    verify(organisationService).create(request)
    verify(outboundEventsService).send(
      outboundEvent = OutboundEvent.ORGANISATION_CREATED,
      identifier = expectedOrganisation.organisationId,
    )
  }

  @Test
  fun `should propagate exception when organisation creation fails`() {
    // Given
    val request = createTestOrganisationRequest()
    whenever(organisationService.create(request)).thenThrow(RuntimeException("Creation failed"))

    // When/Then
    assertThrows<RuntimeException>("Creation failed") {
      facade.create(request)
    }

    verify(organisationService).create(request)
    verifyNoInteractions(outboundEventsService)
  }

  companion object {
    fun createTestOrganisationRequest(
      organisationName: String = "Test Organisation",
      programmeNumber: String = "P123",
      vatNumber: String = "GB123456789",
      caseloadId: String = "CASE001",
      comments: String? = "Test comments",
      active: Boolean = true,
      deactivatedDate: LocalDate? = LocalDate.now(),
      createdTime: LocalDateTime = LocalDateTime.now().minusMinutes(20),
      updatedBy: String? = "Admin",
      createdBy: String = "User",
      updatedTime: LocalDateTime? = LocalDateTime.now().minusMinutes(20),
    ) = CreateOrganisationRequest(
      organisationName = organisationName,
      programmeNumber = programmeNumber,
      vatNumber = vatNumber,
      caseloadId = caseloadId,
      comments = comments,
      active = active,
      deactivatedDate = deactivatedDate,
      createdBy = createdBy,
      createdTime = createdTime,
      updatedBy = updatedBy,
      updatedTime = updatedTime,
    )
  }
}
