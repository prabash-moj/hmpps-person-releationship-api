package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Contact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.ContactService
import java.net.URI
import java.time.LocalDateTime

class ContactControllerTest {

  private val contactService: ContactService = mock()
  private val controller = ContactController(contactService)

  @Nested
  inner class CreateContact {
    @Test
    fun `should create a contact successfully`() {
      val request = CreateContactRequest(
        lastName = "last",
        firstName = "first",
        createdBy = "created",
      )
      val expectedContact = Contact(
        id = 99,
        lastName = request.lastName,
        firstName = request.firstName,
        createdBy = request.createdBy,
        createdTime = LocalDateTime.now(),
      )
      whenever(contactService.createContact(request)).thenReturn(expectedContact)

      val response = controller.createContact(request)

      assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
      assertThat(response.body).isEqualTo(expectedContact)
      assertThat(response.headers.location).isEqualTo(URI.create("/contact/99"))
      verify(contactService).createContact(request)
    }

    @Test
    fun `should propagate exceptions creating a contact`() {
      val request = CreateContactRequest(
        lastName = "last",
        firstName = "first",
        createdBy = "created",
      )
      whenever(contactService.createContact(request)).thenThrow(RuntimeException("Bang!"))

      assertThrows<RuntimeException>("Bang!") {
        controller.createContact(request)
      }
    }
  }

  @Nested
  inner class GetContact {
    private val id = 123456L
    private val contact = Contact(
      id = id,
      lastName = "last",
      firstName = "first",
      createdBy = "user",
      createdTime = LocalDateTime.now(),
    )

    @Test
    fun `should get a contact successfully`() {
      whenever(contactService.getContact(id)).thenReturn(contact)

      val response = controller.getContact(id)

      assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
      assertThat(response.body).isEqualTo(contact)
      verify(contactService).getContact(id)
    }

    @Test
    fun `should return 404 if contact not found`() {
      whenever(contactService.getContact(id)).thenReturn(null)

      val response = controller.getContact(id)

      assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
      verify(contactService).getContact(id)
    }

    @Test
    fun `should propagate exceptions getting a contact`() {
      whenever(contactService.getContact(id)).thenThrow(RuntimeException("Bang!"))

      assertThrows<RuntimeException>("Bang!") {
        controller.getContact(id)
      }
    }
  }
}
