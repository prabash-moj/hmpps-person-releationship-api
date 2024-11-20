package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.openapitools.jackson.nullable.JsonNullable
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.PatchContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Language
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PatchContactResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import java.lang.Boolean.TRUE
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class ContactPatchServiceTest {

  private val contactId = 1L
  private val domesticStatusCode = "P"
  private var originalContact = createDummyContactEntity()
  private val countryCode = "FRE-FRA"

  private val languageService: LanguageService = mock()
  private val contactRepository: ContactRepository = mock()
  private val referenceCodeService: ReferenceCodeService = mock()

  private val service = ContactPatchService(contactRepository, languageService, referenceCodeService)

  @Test
  fun `should throw EntityNotFoundException when contact does not exist`() {
    val patchRequest = PatchContactRequest(
      languageCode = JsonNullable.of("ENG"),
      updatedBy = "system",
    )

    whenever(contactRepository.findById(contactId)).thenReturn(Optional.empty())

    assertThrows<EntityNotFoundException> {
      service.patch(contactId, patchRequest)
    }
  }

  @Test
  fun `should patch when only the updated by field is provided`() {
    val patchRequest = PatchContactRequest(
      updatedBy = "Modifier",
    )

    whenContactExists()
    whenUpdateIsSuccessful()

    val updatedContact = service.patch(contactId, patchRequest)

    val contactCaptor = argumentCaptor<ContactEntity>()

    verify(contactRepository).saveAndFlush(contactCaptor.capture())
    verify(languageService, never()).getLanguageByNomisCode(any())

    assertUnchangedFields(updatedContact)

    assertThat(updatedContact.amendedBy).isEqualTo(patchRequest.updatedBy)
  }

  @Nested
  inner class LanguageCode {

    @Test
    fun `should patch when language code is null`() {
      val patchRequest = PatchContactRequest(
        languageCode = JsonNullable.of(null),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      val updatedContact = service.patch(contactId, patchRequest)

      assertThat(updatedContact.languageCode).isEqualTo(null)
      assertThat(updatedContact.amendedBy).isEqualTo(patchRequest.updatedBy)
    }

    @Test
    fun `should patch without validating a null language code `() {
      val patchRequest = PatchContactRequest(
        languageCode = JsonNullable.of(null),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      service.patch(contactId, patchRequest)

      verify(languageService, never()).getLanguageByNomisCode(any())
      verify(contactRepository, times(1)).saveAndFlush(any())
    }

    @Test
    fun `should patch without validating undefined language code`() {
      val patchRequest = PatchContactRequest(
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      service.patch(contactId, patchRequest)

      verify(languageService, never()).getLanguageByNomisCode(any())
      verify(contactRepository, times(1)).saveAndFlush(any())
    }

    @Test
    fun `should patch language code when existing value is null`() {
      originalContact = createDummyContactEntity(languageCode = null)

      val patchRequest = PatchContactRequest(
        languageCode = JsonNullable.of(countryCode),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()
      whenever(languageService.getLanguageByNomisCode(any())).thenReturn(
        Language(1, countryCode, "French", "Foo", "Bar", "X", 99),
      )

      val updatedContact = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())
      verify(languageService, times(1)).getLanguageByNomisCode(countryCode)

      assertThat(updatedContact.languageCode).isEqualTo(countryCode)
      assertThat(updatedContact.amendedBy).isEqualTo(patchRequest.updatedBy)
    }

    @Test
    fun `should patch when language code is valid`() {
      val patchRequest = PatchContactRequest(
        languageCode = JsonNullable.of("FR"),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      val response = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())
      verify(languageService).getLanguageByNomisCode("FR")

      val updatingEntity = contactCaptor.firstValue

      assertThat(updatingEntity.languageCode).isEqualTo(patchRequest.languageCode.get())
      assertThat(updatingEntity.amendedBy).isEqualTo(patchRequest.updatedBy)

      assertThat(response.languageCode).isEqualTo(patchRequest.languageCode.get())
      assertThat(response.amendedBy).isEqualTo(patchRequest.updatedBy)
    }
  }

  @Nested
  inner class InterpreterRequired {

    @Test
    fun `should patch when interpreter required is valid`() {
      val patchRequest = PatchContactRequest(
        interpreterRequired = JsonNullable.of(TRUE),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      val response = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())

      val updatingEntity = contactCaptor.firstValue

      assertThat(updatingEntity.interpreterRequired).isEqualTo(patchRequest.interpreterRequired.get())
      assertThat(updatingEntity.amendedBy).isEqualTo(patchRequest.updatedBy)

      assertThat(response.interpreterRequired).isEqualTo(patchRequest.interpreterRequired.get())
      assertThat(response.amendedBy).isEqualTo(patchRequest.updatedBy)
    }

    @Test
    fun `should throw validation error when interpreter required is null`() {
      val patchRequest = PatchContactRequest(
        interpreterRequired = JsonNullable.of(null),
        updatedBy = "Modifier",
      )

      whenContactExists()

      val exception = assertThrows<ValidationException> {
        service.patch(contactId, patchRequest)
      }
      assertThat(exception.message).isEqualTo("Unsupported interpreter required type null.")
    }
  }

  @Nested
  inner class DomesticStatusCode {

    @Test
    fun `should patch when domestic status code is null`() {
      val patchRequest = PatchContactRequest(
        domesticStatus = JsonNullable.of(null),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      val updatedContact = service.patch(contactId, patchRequest)

      assertThat(updatedContact.domesticStatus).isEqualTo(null)
      assertThat(updatedContact.amendedBy).isEqualTo(patchRequest.updatedBy)
    }

    @Test
    fun `should patch without validating a null domestic status code `() {
      val patchRequest = PatchContactRequest(
        domesticStatus = JsonNullable.of(null),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      service.patch(contactId, patchRequest)

      verify(referenceCodeService, never()).getReferenceDataByGroupAndCode(any(), any())
      verify(contactRepository, times(1)).saveAndFlush(any())
    }

    @Test
    fun `should patch without validating undefined domestic status code`() {
      val patchRequest = PatchContactRequest(
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      service.patch(contactId, patchRequest)

      verify(referenceCodeService, never()).getReferenceDataByGroupAndCode(any(), any())
      verify(contactRepository, times(1)).saveAndFlush(any())
    }

    @Test
    fun `should patch domestic status code when existing value is null`() {
      originalContact = createDummyContactEntity(domesticStatus = null)

      val patchRequest = PatchContactRequest(
        domesticStatus = JsonNullable.of(domesticStatusCode),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()
      whenever(referenceCodeService.getReferenceDataByGroupAndCode(any(), any())).thenReturn(
        ReferenceCode(
          0,
          "PHONE_TYPE",
          "MOB",
          "Mobile",
          90,
          true,
        ),
      )

      val updatedContact = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())
      verify(referenceCodeService, times(1)).getReferenceDataByGroupAndCode("DOMESTIC_STS", domesticStatusCode)

      assertThat(updatedContact.domesticStatus).isEqualTo(domesticStatusCode)
      assertThat(updatedContact.amendedBy).isEqualTo(patchRequest.updatedBy)
    }

    @Test
    fun `should patch when domestic status code is valid`() {
      val patchRequest = PatchContactRequest(
        domesticStatus = JsonNullable.of(domesticStatusCode),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("DOMESTIC_STS", domesticStatusCode)).thenReturn(
        ReferenceCode(1, "DOMESTIC_STS", "P", "Single", 1, true),
      )

      val response = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())
      verify(referenceCodeService).getReferenceDataByGroupAndCode("DOMESTIC_STS", domesticStatusCode)

      val updatingEntity = contactCaptor.firstValue

      assertThat(updatingEntity.domesticStatus).isEqualTo(patchRequest.domesticStatus.get())
      assertThat(updatingEntity.amendedBy).isEqualTo(patchRequest.updatedBy)

      assertThat(response.domesticStatus).isEqualTo(patchRequest.domesticStatus.get())
      assertThat(response.amendedBy).isEqualTo(patchRequest.updatedBy)
    }
  }

  @Nested
  inner class Staff {

    @Test
    fun `should patch when is staff flag is valid`() {
      val patchRequest = PatchContactRequest(
        isStaff = JsonNullable.of(true),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      val response = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())

      val updatingEntity = contactCaptor.firstValue

      assertThat(updatingEntity.staffFlag).isEqualTo(patchRequest.isStaff.get())
      assertThat(updatingEntity.amendedBy).isEqualTo(patchRequest.updatedBy)

      assertThat(response.isStaff).isEqualTo(patchRequest.isStaff.get())
      assertThat(response.amendedBy).isEqualTo(patchRequest.updatedBy)
    }

    @Test
    fun `should throw validation error when is staff flag is null`() {
      val patchRequest = PatchContactRequest(
        isStaff = JsonNullable.of(null),
        updatedBy = "Modifier",
      )

      whenContactExists()

      val exception = assertThrows<ValidationException> {
        service.patch(contactId, patchRequest)
      }
      assertThat(exception.message).isEqualTo("Unsupported staff flag value null.")
    }

    @Test
    fun `should patch staff flag is undefined`() {
      val patchRequest = PatchContactRequest(
        isStaff = JsonNullable.undefined(),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      service.patch(contactId, patchRequest)

      verify(contactRepository, times(1)).saveAndFlush(any())
    }
  }

  @Nested
  inner class Title {

    @Test
    fun `should patch when title is valid`() {
      whenever(referenceCodeService.getReferenceDataByGroupAndCode(any(), any())).thenReturn(
        ReferenceCode(
          0,
          "TITLE",
          "MRS",
          "Mrs",
          0,
          true,
        ),
      )

      val patchRequest = PatchContactRequest(
        title = JsonNullable.of("MRS"),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      val response = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())

      val updatingEntity = contactCaptor.firstValue

      assertThat(updatingEntity.title).isEqualTo("MRS")
      assertThat(updatingEntity.amendedBy).isEqualTo("Modifier")

      assertThat(response.title).isEqualTo("MRS")
      assertThat(response.amendedBy).isEqualTo("Modifier")
    }

    @Test
    fun `should patch when title is null`() {
      val patchRequest = PatchContactRequest(
        title = JsonNullable.of(null),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      val response = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())

      val updatingEntity = contactCaptor.firstValue

      assertThat(updatingEntity.title).isNull()
      assertThat(updatingEntity.amendedBy).isEqualTo("Modifier")

      assertThat(response.title).isNull()
      assertThat(response.amendedBy).isEqualTo("Modifier")
      verify(referenceCodeService, never()).getReferenceDataByGroupAndCode(any(), any())
    }

    @Test
    fun `should not patch title if it is invalid code`() {
      whenever(referenceCodeService.getReferenceDataByGroupAndCode(any(), any())).thenReturn(null)

      val patchRequest = PatchContactRequest(
        title = JsonNullable.of("FOO"),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      val exception = assertThrows<ValidationException> {
        service.patch(contactId, patchRequest)
      }
      assertThat(exception.message).isEqualTo("Reference code with groupCode TITLE and code 'FOO' not found.")
      verify(contactRepository, never()).saveAndFlush(any())
    }
  }

  @Nested
  inner class MiddleNames {

    @Test
    fun `should patch when middle names is valid`() {
      val patchRequest = PatchContactRequest(
        middleNames = JsonNullable.of("Some Middle Names Updated"),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      val response = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())

      val updatingEntity = contactCaptor.firstValue

      assertThat(updatingEntity.middleNames).isEqualTo("Some Middle Names Updated")
      assertThat(updatingEntity.amendedBy).isEqualTo("Modifier")

      assertThat(response.middleNames).isEqualTo("Some Middle Names Updated")
      assertThat(response.amendedBy).isEqualTo("Modifier")
    }

    @Test
    fun `should patch when middle names is null`() {
      val patchRequest = PatchContactRequest(
        middleNames = JsonNullable.of(null),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      val response = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())

      val updatingEntity = contactCaptor.firstValue

      assertThat(updatingEntity.middleNames).isNull()
      assertThat(updatingEntity.amendedBy).isEqualTo("Modifier")

      assertThat(response.middleNames).isNull()
      assertThat(response.amendedBy).isEqualTo("Modifier")
      verify(referenceCodeService, never()).getReferenceDataByGroupAndCode(any(), any())
    }
  }

  @Nested
  inner class DateOfBirth {

    @Test
    fun `should patch when date of birth is null`() {
      originalContact = createDummyContactEntity().copy(dateOfBirth = LocalDate.of(1982, 6, 15))
      val patchRequest = PatchContactRequest(
        dateOfBirth = JsonNullable.of(null),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      val updatedContact = service.patch(contactId, patchRequest)

      assertThat(updatedContact.dateOfBirth).isNull()
      assertThat(updatedContact.amendedBy).isEqualTo("Modifier")
    }

    @Test
    fun `should patch when date of birth provided`() {
      originalContact = createDummyContactEntity().copy(dateOfBirth = LocalDate.of(1982, 6, 15))

      val patchRequest = PatchContactRequest(
        dateOfBirth = JsonNullable.of(LocalDate.of(2000, 12, 25)),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      val updatedContact = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())

      assertThat(updatedContact.dateOfBirth).isEqualTo(LocalDate.of(2000, 12, 25))
      assertThat(updatedContact.amendedBy).isEqualTo("Modifier")
    }
  }

  @Nested
  inner class PatchEstimatedIsOverEighteen {

    @Test
    fun `should patch when estimated is over eighteen is null`() {
      originalContact = createDummyContactEntity().copy(estimatedIsOverEighteen = EstimatedIsOverEighteen.YES)

      val patchRequest = PatchContactRequest(
        estimatedIsOverEighteen = JsonNullable.of(null),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      val updatedContact = service.patch(contactId, patchRequest)

      assertThat(updatedContact.estimatedIsOverEighteen).isEqualTo(null)
      assertThat(updatedContact.amendedBy).isEqualTo("Modifier")
    }

    @Test
    fun `should patch when estimated is over eighteen provided`() {
      originalContact = createDummyContactEntity().copy(estimatedIsOverEighteen = EstimatedIsOverEighteen.YES)

      val patchRequest = PatchContactRequest(
        estimatedIsOverEighteen = JsonNullable.of(EstimatedIsOverEighteen.DO_NOT_KNOW),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      val updatedContact = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())

      assertThat(updatedContact.estimatedIsOverEighteen).isEqualTo(EstimatedIsOverEighteen.DO_NOT_KNOW)
      assertThat(updatedContact.amendedBy).isEqualTo("Modifier")
    }
  }

  private fun whenUpdateIsSuccessful() {
    whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }
  }

  private fun whenContactExists() {
    whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(originalContact))
  }

  private fun createDummyContactEntity(languageCode: String? = countryCode, domesticStatus: String? = "M") = ContactEntity(
    contactId = 1L,
    title = "Mr.",
    firstName = "John",
    lastName = "Doe",
    middleNames = "A",
    dateOfBirth = LocalDate.of(1990, 1, 1),
    isDeceased = false,
    deceasedDate = null,
    estimatedIsOverEighteen = EstimatedIsOverEighteen.DO_NOT_KNOW,
    createdBy = "Admin",
    createdTime = LocalDateTime.of(2024, 1, 22, 0, 0, 0),
    staffFlag = false,
    gender = "M",
    domesticStatus = domesticStatus,
    languageCode = languageCode,
    interpreterRequired = false,
    amendedBy = "admin",
    amendedTime = LocalDateTime.of(2024, 1, 22, 0, 0, 0),
  )

  private fun assertUnchangedFields(updatedContact: PatchContactResponse) {
    assertThat(updatedContact.title).isEqualTo(originalContact.title)
    assertThat(updatedContact.firstName).isEqualTo(originalContact.firstName)
    assertThat(updatedContact.lastName).isEqualTo(originalContact.lastName)
    assertThat(updatedContact.middleNames).isEqualTo(originalContact.middleNames)
    assertThat(updatedContact.dateOfBirth).isEqualTo(originalContact.dateOfBirth)
    assertThat(updatedContact.isStaff).isEqualTo(originalContact.staffFlag)
    assertThat(updatedContact.gender).isEqualTo(originalContact.gender)
    assertThat(updatedContact.interpreterRequired).isEqualTo(originalContact.interpreterRequired)
    assertThat(updatedContact.domesticStatus).isEqualTo(originalContact.domesticStatus)
    assertThat(updatedContact.amendedTime).isAfter(originalContact.amendedTime)
    assertThat(updatedContact.languageCode).isEqualTo(originalContact.languageCode)
  }

  @Nested
  inner class Gender {

    @Test
    fun `should patch when gender is null`() {
      originalContact = createDummyContactEntity().copy(gender = "NS")

      val patchRequest = PatchContactRequest(
        gender = JsonNullable.of(null),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      val updatedContact = service.patch(contactId, patchRequest)

      assertThat(updatedContact.gender).isEqualTo(null)
      assertThat(updatedContact.amendedBy).isEqualTo(patchRequest.updatedBy)
    }

    @Test
    fun `should patch without validating undefined gender`() {
      originalContact = createDummyContactEntity().copy(gender = "NS")

      val patchRequest = PatchContactRequest(
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()

      val patched = service.patch(contactId, patchRequest)

      assertThat(patched.gender).isEqualTo("NS")
      verify(referenceCodeService, never()).getReferenceDataByGroupAndCode(any(), any())
      verify(contactRepository, times(1)).saveAndFlush(any())
    }

    @Test
    fun `should patch gender with a valid code`() {
      originalContact = createDummyContactEntity().copy(gender = null)

      val patchRequest = PatchContactRequest(
        gender = JsonNullable.of("NS"),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("GENDER", "NS")).thenReturn(
        ReferenceCode(1, "GENDER", "NS", "Not specified", 99, true),
      )

      val updatedContact = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())
      verify(referenceCodeService, times(1)).getReferenceDataByGroupAndCode("GENDER", "NS")

      assertThat(updatedContact.gender).isEqualTo("NS")
      assertThat(updatedContact.amendedBy).isEqualTo(patchRequest.updatedBy)
    }

    @Test
    fun `should reject gender when code is inactive`() {
      originalContact = createDummyContactEntity().copy(gender = null)

      val patchRequest = PatchContactRequest(
        gender = JsonNullable.of("NS"),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("GENDER", "NS")).thenReturn(ReferenceCode(1, "GENDER", "NS", "Not specified", 99, false))

      val exception = assertThrows<ValidationException> {
        service.patch(contactId, patchRequest)
      }
      assertThat(exception.message).isEqualTo("Reference code with groupCode GENDER and code 'NS' is not active and is no longer supported.")
    }

    @Test
    fun `should reject gender when code is not found`() {
      originalContact = createDummyContactEntity().copy(gender = null)

      val patchRequest = PatchContactRequest(
        gender = JsonNullable.of("NS"),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("GENDER", "NS")).thenReturn(null)

      val exception = assertThrows<ValidationException> {
        service.patch(contactId, patchRequest)
      }
      assertThat(exception.message).isEqualTo("Reference code with groupCode GENDER and code 'NS' not found.")
    }
  }
}
