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
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.patch.PatchContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Language
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import java.lang.Boolean.TRUE
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class ContactPatchServiceTest {

  private val contactId = 1L
  private val domesticStatusCode = "P"
  private val originalContact = createDummyContactEntity()

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

    assertThat(updatedContact.title).isEqualTo(originalContact.title)
    assertThat(updatedContact.firstName).isEqualTo(originalContact.firstName)
    assertThat(updatedContact.lastName).isEqualTo(originalContact.lastName)
    assertThat(updatedContact.middleNames).isEqualTo(originalContact.middleNames)
    assertThat(updatedContact.dateOfBirth).isEqualTo(originalContact.dateOfBirth)
    assertThat(updatedContact.placeOfBirth).isEqualTo(originalContact.placeOfBirth)
    assertThat(updatedContact.active).isEqualTo(originalContact.active)
    assertThat(updatedContact.suspended).isEqualTo(originalContact.suspended)
    assertThat(updatedContact.staffFlag).isEqualTo(originalContact.staffFlag)
    assertThat(updatedContact.coronerNumber).isEqualTo(originalContact.coronerNumber)
    assertThat(updatedContact.gender).isEqualTo(originalContact.gender)
    assertThat(updatedContact.nationalityCode).isEqualTo(originalContact.nationalityCode)
    assertThat(updatedContact.interpreterRequired).isEqualTo(originalContact.interpreterRequired)
    assertThat(updatedContact.domesticStatus).isEqualTo(originalContact.domesticStatus)
    assertThat(updatedContact.amendedTime).isAfter(originalContact.amendedTime)
    assertThat(updatedContact.languageCode).isEqualTo(originalContact.languageCode)
    // patched fields
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

      // patched fields
      assertThat(updatedContact.languageCode).isEqualTo(null)
      assertThat(updatedContact.amendedBy).isEqualTo(patchRequest.updatedBy)
    }

    @Test
    fun `should patch without validating a null language code `() {
      val existingContact = createDummyContactEntity()

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
      val existingContact = createDummyContactEntity()

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
      val originalContact = createDummyContactEntity(languageCode = null)

      val patchRequest = PatchContactRequest(
        languageCode = JsonNullable.of("FRE-FRA"),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()
      whenever(languageService.getLanguageByNomisCode(any())).thenReturn(
        Language(1, "FRE-FRA", "French", "Foo", "Bar", "X", 99),
      )

      val updatedContact = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())
      verify(languageService, times(1)).getLanguageByNomisCode("FRE-FRA")

      // patched fields
      assertThat(updatedContact.languageCode).isEqualTo("FRE-FRA")
      assertThat(updatedContact.amendedBy).isEqualTo(patchRequest.updatedBy)
    }

    @Test
    fun `should patch when language code is valid`() {
      val existingContact = createDummyContactEntity()

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

      // Assert updating entity

      assertThat(updatingEntity.title).isEqualTo(existingContact.title)
      assertThat(updatingEntity.firstName).isEqualTo(existingContact.firstName)
      assertThat(updatingEntity.lastName).isEqualTo(existingContact.lastName)
      assertThat(updatingEntity.middleNames).isEqualTo(existingContact.middleNames)
      assertThat(updatingEntity.dateOfBirth).isEqualTo(existingContact.dateOfBirth)
      assertThat(updatingEntity.placeOfBirth).isEqualTo(existingContact.placeOfBirth)
      assertThat(updatingEntity.active).isEqualTo(existingContact.active)
      assertThat(updatingEntity.suspended).isEqualTo(existingContact.suspended)
      assertThat(updatingEntity.staffFlag).isEqualTo(existingContact.staffFlag)
      assertThat(updatingEntity.coronerNumber).isEqualTo(existingContact.coronerNumber)
      assertThat(updatingEntity.gender).isEqualTo(existingContact.gender)
      assertThat(updatingEntity.nationalityCode).isEqualTo(existingContact.nationalityCode)
      assertThat(updatingEntity.domesticStatus).isEqualTo(existingContact.domesticStatus)
      assertThat(updatingEntity.amendedTime).isAfter(existingContact.amendedTime)
      assertThat(updatingEntity.interpreterRequired).isEqualTo(existingContact.interpreterRequired)
      // patched fields
      assertThat(updatingEntity.languageCode).isEqualTo(patchRequest.languageCode.get())
      assertThat(updatingEntity.amendedBy).isEqualTo(patchRequest.updatedBy)

      // Assert response

      assertThat(response.title).isEqualTo(existingContact.title)
      assertThat(response.firstName).isEqualTo(existingContact.firstName)
      assertThat(response.lastName).isEqualTo(existingContact.lastName)
      assertThat(response.middleNames).isEqualTo(existingContact.middleNames)
      assertThat(response.dateOfBirth).isEqualTo(existingContact.dateOfBirth)
      assertThat(response.placeOfBirth).isEqualTo(existingContact.placeOfBirth)
      assertThat(response.active).isEqualTo(existingContact.active)
      assertThat(response.suspended).isEqualTo(existingContact.suspended)
      assertThat(response.staffFlag).isEqualTo(existingContact.staffFlag)
      assertThat(response.coronerNumber).isEqualTo(existingContact.coronerNumber)
      assertThat(response.gender).isEqualTo(existingContact.gender)
      assertThat(response.nationalityCode).isEqualTo(existingContact.nationalityCode)
      assertThat(response.domesticStatus).isEqualTo(existingContact.domesticStatus)
      assertThat(response.amendedTime).isAfter(existingContact.amendedTime)
      assertThat(response.interpreterRequired).isEqualTo(existingContact.interpreterRequired)
      // patched fields
      assertThat(response.languageCode).isEqualTo(patchRequest.languageCode.get())
      assertThat(response.amendedBy).isEqualTo(patchRequest.updatedBy)
    }
  }

  @Nested
  inner class InterpreterRequired {

    @Test
    fun `should patch when interpreter required is valid`() {
      val existingContact = createDummyContactEntity()

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

      // Assert updating entity

      assertThat(updatingEntity.title).isEqualTo(existingContact.title)
      assertThat(updatingEntity.firstName).isEqualTo(existingContact.firstName)
      assertThat(updatingEntity.lastName).isEqualTo(existingContact.lastName)
      assertThat(updatingEntity.middleNames).isEqualTo(existingContact.middleNames)
      assertThat(updatingEntity.dateOfBirth).isEqualTo(existingContact.dateOfBirth)
      assertThat(updatingEntity.placeOfBirth).isEqualTo(existingContact.placeOfBirth)
      assertThat(updatingEntity.active).isEqualTo(existingContact.active)
      assertThat(updatingEntity.suspended).isEqualTo(existingContact.suspended)
      assertThat(updatingEntity.staffFlag).isEqualTo(existingContact.staffFlag)
      assertThat(updatingEntity.coronerNumber).isEqualTo(existingContact.coronerNumber)
      assertThat(updatingEntity.gender).isEqualTo(existingContact.gender)
      assertThat(updatingEntity.nationalityCode).isEqualTo(existingContact.nationalityCode)
      assertThat(updatingEntity.domesticStatus).isEqualTo(existingContact.domesticStatus)
      assertThat(updatingEntity.amendedTime).isAfter(existingContact.amendedTime)
      assertThat(updatingEntity.languageCode).isEqualTo(existingContact.languageCode)
      // patched fields
      assertThat(updatingEntity.interpreterRequired).isEqualTo(patchRequest.interpreterRequired.get())
      assertThat(updatingEntity.amendedBy).isEqualTo(patchRequest.updatedBy)

      // Assert response

      assertThat(response.title).isEqualTo(existingContact.title)
      assertThat(response.firstName).isEqualTo(existingContact.firstName)
      assertThat(response.lastName).isEqualTo(existingContact.lastName)
      assertThat(response.middleNames).isEqualTo(existingContact.middleNames)
      assertThat(response.dateOfBirth).isEqualTo(existingContact.dateOfBirth)
      assertThat(response.placeOfBirth).isEqualTo(existingContact.placeOfBirth)
      assertThat(response.active).isEqualTo(existingContact.active)
      assertThat(response.suspended).isEqualTo(existingContact.suspended)
      assertThat(response.staffFlag).isEqualTo(existingContact.staffFlag)
      assertThat(response.coronerNumber).isEqualTo(existingContact.coronerNumber)
      assertThat(response.gender).isEqualTo(existingContact.gender)
      assertThat(response.nationalityCode).isEqualTo(existingContact.nationalityCode)
      assertThat(response.domesticStatus).isEqualTo(existingContact.domesticStatus)
      assertThat(response.amendedTime).isAfter(existingContact.amendedTime)
      assertThat(response.languageCode).isEqualTo(existingContact.languageCode)
      // patched fields
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

      // patched fields
      assertThat(updatedContact.domesticStatus).isEqualTo(null)
      assertThat(updatedContact.amendedBy).isEqualTo(patchRequest.updatedBy)
    }

    @Test
    fun `should patch without validating a null domestic status code `() {
      val existingContact = createDummyContactEntity()

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
      val existingContact = createDummyContactEntity()

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
      val originalContact = createDummyContactEntity(domesticStatus = null)

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
        ),
      )

      val updatedContact = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())
      verify(referenceCodeService, times(1)).getReferenceDataByGroupAndCode("DOMESTIC_STS", domesticStatusCode)

      // patched fields
      assertThat(updatedContact.domesticStatus).isEqualTo(domesticStatusCode)
      assertThat(updatedContact.amendedBy).isEqualTo(patchRequest.updatedBy)
    }

    @Test
    fun `should patch when domestic status code is valid`() {
      val existingContact = createDummyContactEntity()

      val patchRequest = PatchContactRequest(
        domesticStatus = JsonNullable.of(domesticStatusCode),
        updatedBy = "Modifier",
      )

      whenContactExists()
      whenUpdateIsSuccessful()
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("DOMESTIC_STS", domesticStatusCode)).thenReturn(
        ReferenceCode(1, "DOMESTIC_STS", "P", "Single", 1),
      )

      val response = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())
      verify(referenceCodeService).getReferenceDataByGroupAndCode("DOMESTIC_STS", domesticStatusCode)

      val updatingEntity = contactCaptor.firstValue

      // Assert updating entity

      assertThat(updatingEntity.title).isEqualTo(existingContact.title)
      assertThat(updatingEntity.firstName).isEqualTo(existingContact.firstName)
      assertThat(updatingEntity.lastName).isEqualTo(existingContact.lastName)
      assertThat(updatingEntity.middleNames).isEqualTo(existingContact.middleNames)
      assertThat(updatingEntity.dateOfBirth).isEqualTo(existingContact.dateOfBirth)
      assertThat(updatingEntity.placeOfBirth).isEqualTo(existingContact.placeOfBirth)
      assertThat(updatingEntity.active).isEqualTo(existingContact.active)
      assertThat(updatingEntity.suspended).isEqualTo(existingContact.suspended)
      assertThat(updatingEntity.staffFlag).isEqualTo(existingContact.staffFlag)
      assertThat(updatingEntity.coronerNumber).isEqualTo(existingContact.coronerNumber)
      assertThat(updatingEntity.gender).isEqualTo(existingContact.gender)
      assertThat(updatingEntity.nationalityCode).isEqualTo(existingContact.nationalityCode)
      assertThat(updatingEntity.amendedTime).isAfter(existingContact.amendedTime)
      assertThat(updatingEntity.languageCode).isEqualTo(existingContact.languageCode)
      assertThat(updatingEntity.interpreterRequired).isEqualTo(existingContact.interpreterRequired)
      // patched fields
      assertThat(updatingEntity.domesticStatus).isEqualTo(patchRequest.domesticStatus.get())
      assertThat(updatingEntity.amendedBy).isEqualTo(patchRequest.updatedBy)

      // Assert response

      assertThat(response.title).isEqualTo(existingContact.title)
      assertThat(response.firstName).isEqualTo(existingContact.firstName)
      assertThat(response.lastName).isEqualTo(existingContact.lastName)
      assertThat(response.middleNames).isEqualTo(existingContact.middleNames)
      assertThat(response.dateOfBirth).isEqualTo(existingContact.dateOfBirth)
      assertThat(response.placeOfBirth).isEqualTo(existingContact.placeOfBirth)
      assertThat(response.active).isEqualTo(existingContact.active)
      assertThat(response.suspended).isEqualTo(existingContact.suspended)
      assertThat(response.staffFlag).isEqualTo(existingContact.staffFlag)
      assertThat(response.coronerNumber).isEqualTo(existingContact.coronerNumber)
      assertThat(response.gender).isEqualTo(existingContact.gender)
      assertThat(response.nationalityCode).isEqualTo(existingContact.nationalityCode)
      assertThat(response.amendedTime).isAfter(existingContact.amendedTime)
      assertThat(response.languageCode).isEqualTo(existingContact.languageCode)
      assertThat(response.interpreterRequired).isEqualTo(existingContact.interpreterRequired)
      // patched fields
      assertThat(response.domesticStatus).isEqualTo(patchRequest.domesticStatus.get())
      assertThat(response.amendedBy).isEqualTo(patchRequest.updatedBy)
    }
  }

  private fun whenUpdateIsSuccessful() {
    whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }
  }

  private fun whenContactExists() {
    whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(originalContact))
  }
}

private fun createDummyContactEntity(languageCode: String? = "FRE-FRA", domesticStatus: String? = "M") = ContactEntity(
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
).also {
  it.placeOfBirth = "London"
  it.active = true
  it.suspended = false
  it.staffFlag = false
  it.coronerNumber = null
  it.gender = "M"
  it.domesticStatus = domesticStatus
  it.languageCode = languageCode
  it.nationalityCode = "GB"
  it.interpreterRequired = false
  it.amendedBy = "admin"
  it.amendedTime = LocalDateTime.of(2024, 1, 22, 0, 0, 0)
}
