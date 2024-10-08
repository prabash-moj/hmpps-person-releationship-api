package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.mapSyncRequestToEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.UpdateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class SyncContactServiceTest {
  private val contactRepository: ContactRepository = mock()
  private val syncService = SyncContactService(contactRepository)

  @Nested
  inner class ContactTests {
    @Test
    fun `should get a contact by ID`() {
      whenever(contactRepository.findById(1L)).thenReturn(Optional.of(contactEntity()))
      val contact = syncService.getContactById(1L)
      with(contact) {
        assertThat(title).isEqualTo("Mr")
        assertThat(firstName).isEqualTo("John")
        assertThat(middleName).isNull()
        assertThat(lastName).isEqualTo("Smith")
        assertThat(createdBy).isEqualTo("TEST")
        assertThat(contactTypeCode).isEqualTo("PERSON")
        assertThat(placeOfBirth).isEqualTo("London")
        assertThat(gender).isEqualTo("Male")
        assertThat(maritalStatus).isEqualTo("Single")
        assertThat(languageCode).isEqualTo("EN")
        assertThat(nationalityCode).isEqualTo("GB")
        assertThat(comments).isEqualTo("Special requirements for contact.")
        assertThat(dateOfBirth).isNull()
        assertThat(estimatedIsOverEighteen).isEqualTo(EstimatedIsOverEighteen.NO)
        assertThat(deceasedDate).isNull()
        assertThat(coronerNumber).isNull()
        assertThat(active).isTrue()
        assertThat(suspended).isFalse()
        assertThat(staffFlag).isFalse()
        assertThat(deceasedFlag).isFalse()
        assertThat(interpreterRequired).isFalse()
      }
      verify(contactRepository).findById(1L)
    }

    @Test
    fun `should fail to get a contact by ID when not found`() {
      whenever(contactRepository.findById(1L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.getContactById(1L)
      }
      verify(contactRepository).findById(1L)
    }

    @Test
    fun `should create a contact`() {
      val request = createContactRequest()
      whenever(contactRepository.saveAndFlush(request.mapSyncRequestToEntity())).thenReturn(request.mapSyncRequestToEntity())

      val contact = syncService.createContact(request)
      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())

      // Checks the entity saved
      with(contactCaptor.firstValue) {
        assertThat(title).isEqualTo(request.title)
        assertThat(lastName).isEqualTo(request.lastName)
        assertThat(active).isEqualTo(request.active)
        assertThat(createdBy).isEqualTo(request.createdBy)
      }

      // Checks the model response
      with(contact) {
        assertThat(id).isEqualTo(0L)
        assertThat(title).isEqualTo(request.title)
        assertThat(lastName).isEqualTo(request.lastName)
        assertThat(active).isEqualTo(request.active)
        assertThat(createdBy).isEqualTo(request.createdBy)
      }
    }

    @Test
    fun `should delete contact by ID`() {
      whenever(contactRepository.findById(1L)).thenReturn(Optional.of(contactEntity()))
      syncService.deleteContact(1L)
      verify(contactRepository).deleteById(1L)
    }

    @Test
    fun `should fail to delete contact by ID when not found`() {
      whenever(contactRepository.findById(1L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.deleteContact(1L)
      }
      verify(contactRepository).findById(1L)
    }

    @Test
    fun `should update a contact by ID`() {
      val request = updateContactRequest()
      whenever(contactRepository.findById(1L)).thenReturn(Optional.of(contactEntity()))
      whenever(contactRepository.findById(1L)).thenReturn(Optional.of(request.toEntity()))
      whenever(contactRepository.saveAndFlush(any())).thenReturn(request.toEntity())

      val updated = syncService.updateContact(1L, request)

      val contactCaptor = argumentCaptor<ContactEntity>()

      verify(contactRepository).saveAndFlush(contactCaptor.capture())

      // Checks the entity saved
      with(contactCaptor.firstValue) {
        assertThat(title).isEqualTo(request.title)
        assertThat(lastName).isEqualTo(request.lastName)
        assertThat(active).isEqualTo(request.active)
        assertThat(amendedBy).isEqualTo(request.updatedBy)
        assertThat(amendedTime).isEqualTo(request.updatedTime)
      }

      // Checks the model returned
      with(updated) {
        assertThat(title).isEqualTo(request.title)
        assertThat(lastName).isEqualTo(request.lastName)
        assertThat(active).isEqualTo(request.active)
        assertThat(amendedBy).isEqualTo(request.updatedBy)
        assertThat(amendedTime).isEqualTo(request.updatedTime)
      }
    }

    @Test
    fun `should fail to update a contact when contact is not found`() {
      val updateRequest = updateContactRequest()
      whenever(contactRepository.findById(1L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.updateContact(1L, updateRequest)
      }
      verify(contactRepository).findById(1L)
    }
  }

  private fun updateContactRequest(contactId: Long = 1L) =
    UpdateContactRequest(
      title = "Mr",
      firstName = "John",
      lastName = "Doe",
      middleName = "William",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      estimatedIsOverEighteen = EstimatedIsOverEighteen.YES,
      contactTypeCode = "PERSON",
      placeOfBirth = "London",
      active = true,
      suspended = false,
      staffFlag = false,
      deceasedFlag = false,
      deceasedDate = null,
      coronerNumber = null,
      gender = "Male",
      maritalStatus = "Single",
      languageCode = "EN",
      nationalityCode = "GB",
      interpreterRequired = false,
      comments = "Special requirements for contact.",
      updatedBy = "Admin",
      updatedTime = LocalDateTime.now(),
    )

  private fun createContactRequest() =
    CreateContactRequest(
      title = "Mr",
      firstName = "John",
      lastName = "Doe",
      middleName = "William",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      estimatedIsOverEighteen = EstimatedIsOverEighteen.YES,
      createdBy = "JD000001",
      createdTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0),
      contactTypeCode = "PERSON",
      placeOfBirth = "London",
      active = true,
      suspended = false,
      staffFlag = false,
      deceasedFlag = false,
      deceasedDate = null,
      coronerNumber = null,
      gender = "Male",
      maritalStatus = "Single",
      languageCode = "EN",
      nationalityCode = "GB",
      interpreterRequired = false,
      comments = "Special requirements for contact.",
    )

  private fun contactEntity() =
    ContactEntity(
      contactId = 1L,
      title = "Mr",
      firstName = "John",
      middleName = null,
      lastName = "Smith",
      dateOfBirth = null,
      estimatedIsOverEighteen = EstimatedIsOverEighteen.NO,
      isDeceased = false,
      deceasedDate = null,
      createdBy = "TEST",
      createdTime = LocalDateTime.now(),
    ).also {
      it.contactTypeCode = "PERSON"
      it.placeOfBirth = "London"
      it.active = true
      it.suspended = false
      it.staffFlag = false
      it.coronerNumber = null
      it.gender = "Male"
      it.maritalStatus = "Single"
      it.languageCode = "EN"
      it.nationalityCode = "GB"
      it.interpreterRequired = false
      it.comments = "Special requirements for contact."
    }

  private fun UpdateContactRequest.toEntity(contactId: Long = 1L): ContactEntity {
    val updatedBy = this.updatedBy
    val updatedTime = this.updatedTime

    return ContactEntity(
      contactId = contactId,
      title = this.title,
      firstName = this.firstName,
      lastName = this.lastName,
      middleName = this.middleName,
      dateOfBirth = this.dateOfBirth,
      estimatedIsOverEighteen = this.estimatedIsOverEighteen,
      isDeceased = false,
      deceasedDate = null,
      createdBy = "Admin",
    ).also {
      it.contactTypeCode = this.contactTypeCode
      it.placeOfBirth = this.placeOfBirth
      it.active = this.active
      it.suspended = this.suspended
      it.staffFlag = this.staffFlag
      it.coronerNumber = this.coronerNumber
      it.gender = this.gender
      it.maritalStatus = this.maritalStatus
      it.languageCode = this.languageCode
      it.nationalityCode = this.nationalityCode
      it.interpreterRequired = this.interpreterRequired ?: false
      it.comments = this.comments
      it.amendedBy = updatedBy
      it.amendedTime = updatedTime
    }
  }
}
