package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactPhoneNumberDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreatePhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.ContactPhoneService

class ContactPhoneControllerTest {

  private val service: ContactPhoneService = mock()
  private val controller = ContactPhoneController(service)

  @Test
  fun `should return 201 with created phone number if created successfully`() {
    val createdPhone = createContactPhoneNumberDetails(id = 99, contactId = 1)
    val request = CreatePhoneRequest(
      "MOBILE",
      "+07777777777",
      null,
      "JAMES",
    )
    whenever(service.create(1, request)).thenReturn(createdPhone)

    val response = controller.create(1, request)

    assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    assertThat(response.body).isEqualTo(createdPhone)
    verify(service).create(1, request)
  }

  @Test
  fun `should propagate exceptions if create fails`() {
    val request = CreatePhoneRequest(
      "MOBILE",
      "+07777777777",
      null,
      "JAMES",
    )
    val expected = EntityNotFoundException("Couldn't find contact")
    whenever(service.create(1, request)).thenThrow(expected)

    val exception = assertThrows<EntityNotFoundException> {
      controller.create(1, request)
    }

    assertThat(exception).isEqualTo(expected)
    verify(service).create(1, request)
  }
}
