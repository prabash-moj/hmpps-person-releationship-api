package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppscontactsapi.facade.ContactIdentityFacade
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactIdentityDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactIdentityDetails
import java.time.LocalDateTime

class ContactIdentityControllerTest {

  private val facade: ContactIdentityFacade = mock()
  private val controller = ContactIdentityController(facade)

  @Nested
  inner class CreateContactIdentity {
    @Test
    fun `should return 201 with created identity if created successfully`() {
      val createdIdentity = createContactIdentityDetails(id = 99, contactId = 1)
      val request = CreateIdentityRequest(
        identityType = "DRIVING_LIC",
        identityValue = "DL123456789",
        createdBy = "created",
      )
      whenever(facade.create(1, request)).thenReturn(createdIdentity)

      val response = controller.create(1, request)

      assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
      assertThat(response.body).isEqualTo(createdIdentity)
      verify(facade).create(1, request)
    }

    @Test
    fun `should propagate exceptions if create fails`() {
      val request = CreateIdentityRequest(
        identityType = "DRIVING_LIC",
        identityValue = "DL123456789",
        createdBy = "created",
      )
      val expected = EntityNotFoundException("Couldn't find contact")
      whenever(facade.create(1, request)).thenThrow(expected)

      val exception = assertThrows<EntityNotFoundException> {
        controller.create(1, request)
      }

      assertThat(exception).isEqualTo(expected)
      verify(facade).create(1, request)
    }
  }

  @Nested
  inner class GetIdentity {
    private val identity = ContactIdentityDetails(
      contactIdentityId = 99,
      contactId = 11,
      identityType = "DRIVING_LIC",
      identityTypeDescription = "Driving licence",
      identityValue = "DL123456789",
      issuingAuthority = null,
      createdBy = "USER1",
      createdTime = LocalDateTime.now(),
      amendedBy = null,
      amendedTime = null,
    )

    @Test
    fun `get identity if found by ids`() {
      whenever(facade.get(11, 99)).thenReturn(identity)

      val returnedIdentity = facade.get(11, 99)

      assertThat(returnedIdentity).isEqualTo(identity)
    }

    @Test
    fun `propagate exception getting identity`() {
      val expected = EntityNotFoundException("Bang!")
      whenever(facade.get(11, 99)).thenThrow(expected)
      val exception = assertThrows<EntityNotFoundException> {
        controller.get(11, 99)
      }
      assertThat(exception).isEqualTo(expected)
    }
  }

  @Nested
  inner class DeleteContactIdentity {
    @Test
    fun `should return 204 if deleted successfully`() {
      whenever(facade.delete(1, 2)).then { }

      val response = controller.delete(1, 2)

      assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
      verify(facade).delete(1, 2)
    }

    @Test
    fun `should propagate exceptions if delete fails`() {
      val expected = EntityNotFoundException("Couldn't find contact")
      whenever(facade.delete(1, 2)).thenThrow(expected)

      val exception = assertThrows<EntityNotFoundException> {
        controller.delete(1, 2)
      }

      assertThat(exception).isEqualTo(expected)
      verify(facade).delete(1, 2)
    }
  }
}
