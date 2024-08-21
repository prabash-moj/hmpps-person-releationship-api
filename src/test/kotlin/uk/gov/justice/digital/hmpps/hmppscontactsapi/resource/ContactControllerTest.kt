package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.ContactService

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

      val response = controller.createContact(request)

      assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
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
}
