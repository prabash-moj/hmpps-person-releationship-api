package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactPhoneDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactPhoneEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreatePhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactPhoneDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactPhoneDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactPhoneRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import java.time.LocalDateTime
import java.util.*

class ContactPhoneServiceTest {

  private val contactRepository: ContactRepository = mock()
  private val contactPhoneRepository: ContactPhoneRepository = mock()
  private val contactPhoneDetailsRepository: ContactPhoneDetailsRepository = mock()
  private val referenceCodeService: ReferenceCodeService = mock()
  private val service = ContactPhoneService(contactRepository, contactPhoneRepository, contactPhoneDetailsRepository, referenceCodeService)

  private val contactId = 99L
  private val aContact = ContactEntity(
    contactId = contactId,
    title = "Mr",
    lastName = "last",
    middleNames = "middle",
    firstName = "first",
    dateOfBirth = null,
    estimatedIsOverEighteen = EstimatedIsOverEighteen.YES,
    isDeceased = false,
    deceasedDate = null,
    createdBy = "user",
    createdTime = LocalDateTime.now(),
  )

  @Nested
  inner class CreatePhone {
    private val request = CreatePhoneRequest(
      "MOB",
      "+447777777777",
      "0123",
      "USER1",
    )

    @Test
    fun `should throw EntityNotFoundException creating phone if contact doesn't exist`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.empty())

      val exception = assertThrows<EntityNotFoundException> {
        service.create(contactId, request)
      }
      assertThat(exception.message).isEqualTo("Contact (99) not found")
    }

    @Test
    fun `should throw ValidationException creating phone if phone type doesn't exist`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(aContact))
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("PHONE_TYPE", "FOO")).thenReturn(null)

      val exception = assertThrows<ValidationException> {
        service.create(contactId, request.copy(phoneType = "FOO"))
      }
      assertThat(exception.message).isEqualTo("Unsupported phone type (FOO)")
    }

    @ParameterizedTest
    @CsvSource(
      "!",
      "\"",
      "£",
      "$",
      "%",
      "^",
      "&",
      "*",
      "_",
      "-",
      "=",
      // + not allowed unless at start
      "0+",
      ":",
      ";",
      "[",
      "]",
      "{",
      "}",
      "@",
      "#",
      "~",
      "/",
      "\\",
      "'",
      quoteCharacter = 'X',
    )
    fun `should throw ValidationException creating phone if phone contains invalid chars`(phoneNumber: String) {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(aContact))
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("PHONE_TYPE", "MOB")).thenReturn(
        ReferenceCode(
          0,
          "PHONE_TYPE",
          "MOB",
          "Mobile",
          90,
        ),
      )

      val exception = assertThrows<ValidationException> {
        service.create(contactId, request.copy(phoneNumber = phoneNumber))
      }
      assertThat(exception.message).isEqualTo("Phone number invalid, it can only contain numbers, () and whitespace with an optional + at the start")
    }

    @Test
    fun `should return a phone details including the reference data after creating a contact successfully`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(aContact))
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("PHONE_TYPE", "MOB")).thenReturn(
        ReferenceCode(
          0,
          "PHONE_TYPE",
          "MOB",
          "Mobile",
          90,
        ),
      )
      whenever(contactPhoneRepository.saveAndFlush(any())).thenAnswer { i ->
        (i.arguments[0] as ContactPhoneEntity).copy(
          contactPhoneId = 9999,
        )
      }

      val created = service.create(contactId, request)
      assertThat(created.createdTime).isNotNull()
      assertThat(created).isEqualTo(
        ContactPhoneDetails(
          contactPhoneId = 9999,
          contactId = contactId,
          phoneType = "MOB",
          phoneTypeDescription = "Mobile",
          phoneNumber = "+447777777777",
          extNumber = "0123",
          createdBy = "USER1",
          createdTime = created.createdTime,
          amendedBy = null,
          amendedTime = null,
        ),
      )
    }
  }

  @Nested
  inner class GetPhone {
    private val createdTime = LocalDateTime.now()
    private val phoneEntity = ContactPhoneDetailsEntity(
      contactPhoneId = 99,
      contactId = contactId,
      phoneType = "MOB",
      phoneTypeDescription = "Mobile",
      phoneNumber = "07777777777",
      extNumber = null,
      createdBy = "USER1",
      createdTime = createdTime,
      amendedBy = null,
      amendedTime = null,
    )

    @Test
    fun `get phone if found by ids`() {
      whenever(contactPhoneDetailsRepository.findByContactIdAndContactPhoneId(contactId, 99)).thenReturn(phoneEntity)

      val returnedPhone = service.get(contactId, 99)

      assertThat(returnedPhone).isEqualTo(
        ContactPhoneDetails(
          contactPhoneId = 99,
          contactId = contactId,
          phoneType = "MOB",
          phoneTypeDescription = "Mobile",
          phoneNumber = "07777777777",
          extNumber = null,
          createdBy = "USER1",
          createdTime = createdTime,
          amendedBy = null,
          amendedTime = null,
        ),
      )
    }

    @Test
    fun `return null if phone not found`() {
      whenever(contactPhoneDetailsRepository.findByContactIdAndContactPhoneId(contactId, 99)).thenReturn(null)

      assertThat(service.get(contactId, 99)).isNull()
    }
  }
}
