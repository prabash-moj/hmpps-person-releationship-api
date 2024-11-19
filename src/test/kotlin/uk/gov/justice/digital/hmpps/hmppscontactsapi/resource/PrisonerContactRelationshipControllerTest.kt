package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRelationshipDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.PrisonerContactRelationshipService

class PrisonerContactRelationshipControllerTest {

  private val prisonerContactRelationshipService: PrisonerContactRelationshipService = mock()

  private val prisonerContactRelationshipController = PrisonerContactRelationshipController(prisonerContactRelationshipService)

  @Test
  fun `should return prisoner contact relationship when found`() {
    val prisonerContactId = 1L
    val mockPrisonerContactRelationship = getMockPrisonerContactRelationship()
    whenever(prisonerContactRelationshipService.getById(prisonerContactId)).thenReturn(mockPrisonerContactRelationship)

    val response: PrisonerContactRelationshipDetails = prisonerContactRelationshipController.getPrisonerContactById(prisonerContactId)

    assertThat(response).isEqualTo(mockPrisonerContactRelationship)
    verify(prisonerContactRelationshipService).getById(prisonerContactId)
  }

  @Test
  fun `should throw exception when relationship is not found`() {
    val prisonContactId = 999L
    whenever(prisonerContactRelationshipService.getById(prisonContactId)).thenThrow(EntityNotFoundException::class.java)

    assertThrows<EntityNotFoundException> {
      prisonerContactRelationshipController.getPrisonerContactById(prisonContactId)
    }
    verify(prisonerContactRelationshipService, times(1)).getById(prisonContactId)
  }

  private fun getMockPrisonerContactRelationship() = PrisonerContactRelationshipDetails(
    prisonerContactId = 1,
    relationshipCode = "FRI",
    relationshipDescription = "Friend",
    nextOfKin = false,
    emergencyContact = true,
    isRelationshipActive = true,
    comments = "Close family friend",
  )
}
