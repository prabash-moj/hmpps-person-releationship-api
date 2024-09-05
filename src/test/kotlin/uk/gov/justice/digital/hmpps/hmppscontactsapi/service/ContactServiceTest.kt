package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.IsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class ContactServiceTest {

  private val contactRepository: ContactRepository = mock()
  private val clock =
    Clock.fixed(ZonedDateTime.of(2000, 1, 1, 10, 30, 0, 0, ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"))
  private val service = ContactService(contactRepository, clock)

  @Nested
  inner class CreateContact {
    @Test
    fun `should create a contact with a date of birth successfully`() {
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

    @TestFactory
    fun `should create a contact without a date of birth successfully`() = listOf(
      IsOverEighteen.YES to true,
      IsOverEighteen.NO to false,
      IsOverEighteen.DO_NOT_KNOW to null,
    ).map { (requestIsOverEighteen, expectedIsOverEighteen) ->
      DynamicTest.dynamicTest("when is over eighteen is $requestIsOverEighteen then expected is $expectedIsOverEighteen") {
        reset(contactRepository)
        val request = CreateContactRequest(
          title = "mr",
          lastName = "last",
          firstName = "first",
          middleName = "middle",
          dateOfBirth = null,
          requestIsOverEighteen,
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
          assertNull(dateOfBirth)
          assertThat(isOverEighteen).isEqualTo(expectedIsOverEighteen)
          assertThat(createdBy).isEqualTo(request.createdBy)
          assertThat(createdTime).isNotNull()
        }
        with(createdContact) {
          assertThat(title).isEqualTo(request.title)
          assertThat(lastName).isEqualTo(request.lastName)
          assertThat(firstName).isEqualTo(request.firstName)
          assertThat(middleName).isEqualTo(request.middleName)
          assertNull(dateOfBirth)
          assertThat(isOverEighteen).isEqualTo(requestIsOverEighteen)
          assertThat(createdBy).isEqualTo(request.createdBy)
          assertThat(createdTime).isNotNull()
        }
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

    @TestFactory
    fun `should get a contact with a dob and is over 18 calculated given today is 1st January 2000`() = listOf(
      LocalDate.of(1981, 12, 31) to IsOverEighteen.YES,
      LocalDate.of(1982, 1, 1) to IsOverEighteen.YES,
      LocalDate.of(1982, 1, 2) to IsOverEighteen.NO,
    ).map { (dob, expectedIsOverEighteen) ->
      DynamicTest.dynamicTest("when dob is $dob then expected is $expectedIsOverEighteen") {
        val entity = ContactEntity(
          contactId = id,
          title = "Mr",
          lastName = "last",
          middleName = "middle",
          firstName = "first",
          dateOfBirth = dob,
          isOverEighteen = null,
          createdBy = "user",
          createdTime = LocalDateTime.now(),
        )
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
          assertThat(isOverEighteen).isEqualTo(expectedIsOverEighteen)
          assertThat(createdBy).isEqualTo(entity.createdBy)
          assertThat(createdTime).isEqualTo(entity.createdTime)
        }
      }
    }

    @TestFactory
    fun `should get a contact without dob successfully`() = listOf(
      true to IsOverEighteen.YES,
      false to IsOverEighteen.NO,
      null to IsOverEighteen.DO_NOT_KNOW,
    ).map { (storedIsOverEighteen, expectedIsOverEighteen) ->
      DynamicTest.dynamicTest("when stored is $storedIsOverEighteen then expected is $expectedIsOverEighteen") {
        val entity = ContactEntity(
          contactId = id,
          title = "Mr",
          lastName = "last",
          middleName = "middle",
          firstName = "first",
          dateOfBirth = null,
          isOverEighteen = storedIsOverEighteen,
          createdBy = "user",
          createdTime = LocalDateTime.now(),
        )
        whenever(contactRepository.findById(id)).thenReturn(Optional.of(entity))
        val contact = service.getContact(id)
        assertNotNull(contact)
        with(contact!!) {
          assertThat(id).isEqualTo(entity.contactId)
          assertThat(title).isEqualTo(entity.title)
          assertThat(lastName).isEqualTo(entity.lastName)
          assertThat(firstName).isEqualTo(entity.firstName)
          assertThat(middleName).isEqualTo(entity.middleName)
          assertThat(dateOfBirth).isNull()
          assertThat(isOverEighteen).isEqualTo(expectedIsOverEighteen)
          assertThat(createdBy).isEqualTo(entity.createdBy)
          assertThat(createdTime).isEqualTo(entity.createdTime)
        }
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
