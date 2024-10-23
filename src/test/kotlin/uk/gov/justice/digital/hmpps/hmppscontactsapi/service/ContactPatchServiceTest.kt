package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
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
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.patch.PatchContactResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Language
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class ContactPatchServiceTest {
  private val languageService: LanguageService = mock()
  private val contactRepository: ContactRepository = mock()

  private val service = ContactPatchService(contactRepository, languageService)

  @Nested
  inner class PatchContact {

    @Test
    fun `should patch a contact when fields are provided`() {
      val contactId = 1L

      val existingContact = getDummyContactEntity("FRE-FRA")

      val patchRequest = patchContactRequest()

      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(existingContact))
      whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

      val response = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())
      verify(languageService).getLanguageByNomisCode("FR")

      assertThat(contactCaptor.firstValue)
        .usingRecursiveComparison()
        .ignoringFields("amendedTime")
        .isEqualTo(mapToEntity(patchRequest, existingContact))

      assertThat(response)
        .usingRecursiveComparison()
        .ignoringFields("amendedTime")
        .isEqualTo(mapToResponse(patchRequest, existingContact))
    }

    @Test
    fun `should patch a contact when language code fields is not provided`() {
      val contactId = 1L

      val originalContact = getDummyContactEntity("FRE-FRA")

      val patchRequest = PatchContactRequest(
        updatedBy = "Modifier",
      )

      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(originalContact))
      whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

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

    @Test
    fun `should patch a contact when language code fields is null`() {
      val contactId = 1L

      val originalContact = getDummyContactEntity("FRE-FRA")

      val patchRequest = PatchContactRequest(
        languageCode = JsonNullable.of(null),
        updatedBy = "Modifier",
      )

      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(originalContact))
      whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

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
      // patched fields
      assertThat(updatedContact.languageCode).isEqualTo(null)
      assertThat(updatedContact.amendedBy).isEqualTo(patchRequest.updatedBy)
    }

    @Test
    fun `should patch a contact when language code fields is set when existing value is null`() {
      val contactId = 1L

      val originalContact = getDummyContactEntity(null)

      val patchRequest = PatchContactRequest(
        languageCode = JsonNullable.of("FRE-FRA"),
        updatedBy = "Modifier",
      )

      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(originalContact))
      whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }
      whenever(languageService.getLanguageByNomisCode(any())).thenReturn(
        Language(1, "FRE-FRA", "French", "Foo", "Bar", "X", 99),
      )

      val updatedContact = service.patch(contactId, patchRequest)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())
      verify(languageService, times(1)).getLanguageByNomisCode("FRE-FRA")

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
      // patched fields
      assertThat(updatedContact.languageCode).isEqualTo("FRE-FRA")
      assertThat(updatedContact.amendedBy).isEqualTo(patchRequest.updatedBy)
    }

    @Test
    fun `should not validate language code if it is not available in the request`() {
      val contactId = 1L

      val existingContact = getDummyContactEntity("FRE-FRA")

      val patchRequest = PatchContactRequest(
        languageCode = JsonNullable.of(null),
        updatedBy = "Modifier",
      )

      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(existingContact))
      whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

      service.patch(contactId, patchRequest)

      verify(languageService, never()).getLanguageByNomisCode(any())
      verify(contactRepository, times(1)).saveAndFlush(any())
    }

    @Test
    fun `should throw EntityNotFoundException if contact does not exist`() {
      val contactId = 1L
      val patchRequest = PatchContactRequest(
        languageCode = JsonNullable.of("ENG"),
        updatedBy = "system",
      )

      whenever(contactRepository.findById(contactId)).thenReturn(Optional.empty())

      assertThrows<EntityNotFoundException> {
        service.patch(contactId, patchRequest)
      }
    }
  }

  private fun mapToResponse(request: PatchContactRequest, existingContact: ContactEntity): PatchContactResponse {
    return PatchContactResponse(
      id = 1L,
      title = existingContact.title,
      firstName = existingContact.firstName,
      lastName = existingContact.lastName,
      middleNames = existingContact.middleNames,
      dateOfBirth = existingContact.dateOfBirth,
      estimatedIsOverEighteen = existingContact.estimatedIsOverEighteen,
      createdBy = "Admin",
      createdTime = LocalDateTime.of(2024, 1, 22, 0, 0, 0),
      placeOfBirth = existingContact.placeOfBirth,
      active = existingContact.active,
      suspended = existingContact.suspended,
      staffFlag = existingContact.staffFlag,
      deceasedFlag = existingContact.isDeceased,
      deceasedDate = existingContact.deceasedDate,
      coronerNumber = existingContact.coronerNumber,
      gender = existingContact.gender,
      domesticStatus = existingContact.domesticStatus,
      languageCode = request.languageCode.get(),
      nationalityCode = existingContact.nationalityCode,
      interpreterRequired = existingContact.interpreterRequired,
      amendedBy = request.updatedBy,
      amendedTime = LocalDateTime.now(),
    )
  }

  private fun getDummyContactEntity(languageCode: String?): ContactEntity {
    val existingContact = ContactEntity(
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
      it.domesticStatus = "Single"
      it.languageCode = languageCode
      it.nationalityCode = "GB"
      it.interpreterRequired = false
      it.amendedBy = "admin"
      it.amendedTime = LocalDateTime.of(2024, 1, 22, 0, 0, 0)
    }
    return existingContact
  }

  private fun mapToEntity(request: PatchContactRequest, existingContact: ContactEntity): ContactEntity {
    return ContactEntity(
      contactId = 1L,
      title = existingContact.title,
      firstName = existingContact.firstName,
      lastName = existingContact.lastName,
      middleNames = existingContact.middleNames,
      dateOfBirth = existingContact.dateOfBirth,
      estimatedIsOverEighteen = existingContact.estimatedIsOverEighteen,
      isDeceased = existingContact.isDeceased,
      deceasedDate = existingContact.deceasedDate,
      createdBy = "Admin",
      createdTime = LocalDateTime.of(2024, 1, 22, 0, 0, 0),
    ).also {
      it.placeOfBirth = existingContact.placeOfBirth
      it.active = existingContact.active
      it.suspended = existingContact.suspended
      it.staffFlag = existingContact.staffFlag
      it.coronerNumber = existingContact.coronerNumber
      it.gender = existingContact.gender
      it.domesticStatus = existingContact.domesticStatus
      it.nationalityCode = existingContact.nationalityCode
      it.interpreterRequired = existingContact.interpreterRequired
      it.amendedTime = LocalDateTime.now()
      it.languageCode = request.languageCode.get()
      it.amendedBy = request.updatedBy
    }
  }

  private fun patchContactRequest(): PatchContactRequest {
    val patchRequest = PatchContactRequest(
      languageCode = JsonNullable.of("FR"),
      updatedBy = "Modifier",
    )
    return patchRequest
  }
}
