package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class ContactServiceTest {

  private val contactRepository: ContactRepository = mock()
  private val service = ContactService(contactRepository)

  @Nested
  inner class CreateContact {
    @Test
    fun `should create a contact successfully`() {
      val request = CreateContactRequest(
        title = "mr",
        lastName = "last",
        firstName = "first",
        middleName = "middle",
        dateOfBirth = LocalDate.of(1982, 6, 15),
        createdBy = "created",
      )
      whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

      val createdContact = service.createContact(request)

      val contactCaptor = argumentCaptor<ContactEntity>()
      verify(contactRepository).saveAndFlush(contactCaptor.capture())
      with(contactCaptor.firstValue) {
        assertThat(title).isEqualTo(request.title)
        assertThat(lastName).isEqualTo(request.lastName)
        assertThat(firstName).isEqualTo(request.firstName)
        assertThat(middleName).isEqualTo(request.middleName)
        assertThat(dateOfBirth).isEqualTo(request.dateOfBirth)
        assertThat(createdBy).isEqualTo(request.createdBy)
        assertThat(createdTime).isNotNull()
      }
      with(createdContact) {
        assertThat(title).isEqualTo(request.title)
        assertThat(lastName).isEqualTo(request.lastName)
        assertThat(firstName).isEqualTo(request.firstName)
        assertThat(middleName).isEqualTo(request.middleName)
        assertThat(dateOfBirth).isEqualTo(request.dateOfBirth)
        assertThat(createdBy).isEqualTo(request.createdBy)
        assertThat(createdTime).isNotNull()
      }
    }

    @Test
    fun `should propagate exceptions creating a contact`() {
      val request = CreateContactRequest(
        lastName = "last",
        firstName = "first",
        createdBy = "created",
      )
      whenever(contactRepository.saveAndFlush(any())).thenThrow(RuntimeException("Bang!"))

      assertThrows<RuntimeException>("Bang!") {
        service.createContact(request)
      }
    }
  }

  @Nested
  inner class GetContact {
    private val id = 123456L
    private val entity = ContactEntity(
      contactId = id,
      title = "Mr",
      lastName = "last",
      middleName = "middle",
      firstName = "first",
      dateOfBirth = LocalDate.of(1982, 6, 15),
      createdBy = "user",
      createdTime = LocalDateTime.now(),
    )

    @Test
    fun `should get a contact successfully`() {
      whenever(contactRepository.findById(id)).thenReturn(Optional.of(entity))
      val contact = service.getContact(id)
      assertNotNull(contact)
      with(contact!!) {
        assertThat(id).isEqualTo(entity.contactId)
        assertThat(title).isEqualTo(entity.title)
        assertThat(lastName).isEqualTo(entity.lastName)
        assertThat(firstName).isEqualTo(entity.firstName)
        assertThat(middleName).isEqualTo(entity.middleName)
        assertThat(dateOfBirth).isEqualTo(entity.dateOfBirth)
        assertThat(createdBy).isEqualTo(entity.createdBy)
        assertThat(createdTime).isEqualTo(entity.createdTime)
      }
    }

    @Test
    fun `should not blow up if contact not found`() {
      whenever(contactRepository.findById(id)).thenReturn(Optional.empty())
      val contact = service.getContact(id)
      assertNull(contact)
    }

    @Test
    fun `should propagate exceptions getting a contact`() {
      whenever(contactRepository.findById(id)).thenThrow(RuntimeException("Bang!"))

      assertThrows<RuntimeException>("Bang!") {
        service.getContact(id)
      }
    }
  }
}
