package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.openapitools.jackson.nullable.JsonNullable
import uk.gov.justice.digital.hmpps.hmppscontactsapi.facade.ContactFacade
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createPrisonerContactRelationshipDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.AddContactRelationshipRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactRelationship
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateRelationshipRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRelationshipDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.PrisonerContactRelationshipService

class PrisonerContactControllerTest {

  private val prisonerContactRelationshipService: PrisonerContactRelationshipService = mock()
  private val contactFacade: ContactFacade = mock()

  private val controller = PrisonerContactController(prisonerContactRelationshipService, contactFacade)

  @Nested
  inner class GetPrisonerContactRelationship {
    @Test
    fun `should return prisoner contact relationship when found`() {
      val prisonerContactId = 1L
      val mockPrisonerContactRelationship = getMockPrisonerContactRelationship()
      whenever(prisonerContactRelationshipService.getById(prisonerContactId)).thenReturn(mockPrisonerContactRelationship)

      val response: PrisonerContactRelationshipDetails =
        controller.getPrisonerContactById(prisonerContactId)

      assertThat(response).isEqualTo(mockPrisonerContactRelationship)
      verify(prisonerContactRelationshipService).getById(prisonerContactId)
    }

    @Test
    fun `should throw exception when relationship is not found`() {
      val prisonContactId = 999L
      whenever(prisonerContactRelationshipService.getById(prisonContactId)).thenThrow(EntityNotFoundException::class.java)

      assertThrows<EntityNotFoundException> {
        controller.getPrisonerContactById(prisonContactId)
      }
      verify(prisonerContactRelationshipService, times(1)).getById(prisonContactId)
    }

    private fun getMockPrisonerContactRelationship() = PrisonerContactRelationshipDetails(
      prisonerContactId = 1,
      contactId = 2,
      prisonerNumber = "A1234BC",
      relationshipCode = "FRI",
      relationshipDescription = "Friend",
      nextOfKin = false,
      emergencyContact = true,
      isRelationshipActive = true,
      comments = "Close family friend",
    )
  }

  @Nested
  inner class PatchContactRelationship {
    private val prisonerContactId = 2L
    private val request = patchContactRelationshipRequest()

    @Test
    fun `should update a contact relationship successfully`() {
      doNothing().whenever(contactFacade).patchRelationship(prisonerContactId, request)

      controller.patchContactRelationship(prisonerContactId, request)

      Mockito.verify(contactFacade).patchRelationship(prisonerContactId, request)
    }

    @Test
    fun `should propagate exceptions patching a contact relationship`() {
      whenever(contactFacade.patchRelationship(prisonerContactId, request)).thenThrow(RuntimeException("Bang!"))

      assertThrows<RuntimeException>("Bang!") {
        controller.patchContactRelationship(prisonerContactId, request)
      }
    }

    private fun patchContactRelationshipRequest() = UpdateRelationshipRequest(
      relationshipCode = JsonNullable.of("ENG"),
      updatedBy = "system",
    )
  }

  @Nested
  inner class AddContactRelationship {
    private val contactId = 123456L
    private val relationship = ContactRelationship(
      prisonerNumber = "A1234BC",
      relationshipCode = "MOT",
      isNextOfKin = true,
      isEmergencyContact = false,
      comments = "Foo",
    )
    private val request = AddContactRelationshipRequest(contactId, relationship, "USER")

    @Test
    fun `should create a contact relationship successfully`() {
      val created = createPrisonerContactRelationshipDetails()
      whenever(contactFacade.addContactRelationship(request)).thenReturn(created)

      val result = controller.addContactRelationship(request)

      assertThat(result).isEqualTo(created)
      Mockito.verify(contactFacade).addContactRelationship(request)
    }

    @Test
    fun `should propagate exceptions getting a contact`() {
      whenever(contactFacade.addContactRelationship(request)).thenThrow(RuntimeException("Bang!"))

      assertThrows<RuntimeException>("Bang!") {
        controller.addContactRelationship(request)
      }
    }
  }
}
