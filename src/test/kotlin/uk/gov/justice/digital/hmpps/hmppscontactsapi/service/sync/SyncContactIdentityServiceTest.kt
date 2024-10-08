package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactIdentityEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreateContactIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.UpdateContactIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactIdentityRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import java.time.LocalDateTime
import java.util.*

class SyncContactIdentityServiceTest {
  private val contactRepository: ContactRepository = mock()
  private val contactIdentityRepository: ContactIdentityRepository = mock()
  private val syncService = SyncContactIdentityService(contactRepository, contactIdentityRepository)

  @Nested
  inner class ContactIdentityTests {
    @Test
    fun `should get a contact identity by ID`() {
      whenever(contactIdentityRepository.findById(1L)).thenReturn(Optional.of(contactIdentityEntity()))
      val contactIdentity = syncService.getContactIdentityById(1L)
      with(contactIdentity) {
        assertThat(identityType).isEqualTo("PASSPORT")
      }
      verify(contactIdentityRepository).findById(1L)
    }

    @Test
    fun `should fail to get a contact identity by ID when not found`() {
      whenever(contactIdentityRepository.findById(1L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.getContactIdentityById(1L)
      }
      verify(contactIdentityRepository).findById(1L)
    }

    @Test
    fun `should create a contact identity`() {
      val request = createContactIdentityRequest()
      whenever(contactRepository.findById(1L)).thenReturn(Optional.of(contactEntity()))
      whenever(contactIdentityRepository.saveAndFlush(request.toEntity())).thenReturn(request.toEntity())

      val contactIdentity = syncService.createContactIdentity(request)
      val identityCaptor = argumentCaptor<ContactIdentityEntity>()

      verify(contactIdentityRepository).saveAndFlush(identityCaptor.capture())

      // Checks the entity saved
      with(identityCaptor.firstValue) {
        assertThat(identityType).isEqualTo(request.identityType)
        assertThat(createdBy).isEqualTo(request.createdBy)
      }

      // Checks the model response
      with(contactIdentity) {
        assertThat(contactIdentityId).isEqualTo(0L)
        assertThat(identityType).isEqualTo(request.identityType)
        assertThat(createdBy).isEqualTo(request.createdBy)
      }

      verify(contactRepository).findById(1L)
    }

    @Test
    fun `should fail to create a contact identity when the contact ID is not present`() {
      val request = createContactIdentityRequest()
      whenever(contactRepository.findById(1L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.createContactIdentity(request)
      }
      verifyNoInteractions(contactIdentityRepository)
    }

    @Test
    fun `should delete contact identity by ID`() {
      whenever(contactIdentityRepository.findById(1L)).thenReturn(Optional.of(contactIdentityEntity()))
      syncService.deleteContactIdentity(1L)
      verify(contactIdentityRepository).deleteById(1L)
    }

    @Test
    fun `should fail to delete contact identity by ID when not found`() {
      whenever(contactIdentityRepository.findById(1L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.deleteContactIdentity(1L)
      }
      verify(contactIdentityRepository).findById(1L)
    }

    @Test
    fun `should update a contact identity by ID`() {
      val request = updateContactIdentityRequest()
      whenever(contactRepository.findById(1L)).thenReturn(Optional.of(contactEntity()))
      whenever(contactIdentityRepository.findById(1L)).thenReturn(Optional.of(request.toEntity()))
      whenever(contactIdentityRepository.saveAndFlush(any())).thenReturn(request.toEntity())

      val updated = syncService.updateContactIdentity(1L, request)

      val identityCaptor = argumentCaptor<ContactIdentityEntity>()

      verify(contactIdentityRepository).saveAndFlush(identityCaptor.capture())

      // Checks the entity saved
      with(identityCaptor.firstValue) {
        assertThat(identityType).isEqualTo(request.identityType)
        assertThat(amendedBy).isEqualTo(request.updatedBy)
        assertThat(amendedTime).isEqualTo(request.updatedTime)
      }

      // Checks the model returned
      with(updated) {
        assertThat(identityType).isEqualTo(request.identityType)
        assertThat(amendedBy).isEqualTo(request.updatedBy)
        assertThat(amendedTime).isEqualTo(request.updatedTime)
      }
    }

    @Test
    fun `should fail to update a contact identity by ID when contact is not found`() {
      val updateRequest = updateContactIdentityRequest()
      whenever(contactRepository.findById(1L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.updateContactIdentity(1L, updateRequest)
      }
      verifyNoInteractions(contactIdentityRepository)
    }

    @Test
    fun `should fail to update a contact identity when contact identity is not found`() {
      val updateRequest = updateContactIdentityRequest()
      whenever(contactRepository.findById(1L)).thenReturn(Optional.of(contactEntity(1L)))
      whenever(contactIdentityRepository.findById(updateRequest.contactId)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.updateContactIdentity(1L, updateRequest)
      }
      verify(contactRepository).findById(updateRequest.contactId)
      verify(contactIdentityRepository).findById(1L)
    }
  }

  private fun updateContactIdentityRequest(contactId: Long = 1L) =
    UpdateContactIdentityRequest(
      contactId = contactId,
      identityType = "PASSPORT",
      identityValue = "PP87878787878",
      updatedBy = "TEST",
      updatedTime = LocalDateTime.now(),
    )

  private fun createContactIdentityRequest() =
    CreateContactIdentityRequest(
      contactId = 1L,
      identityType = "PASSPORT",
      identityValue = "PP87878787878",
      createdBy = "TEST",
    )

  private fun contactEntity(contactId: Long = 1L) =
    ContactEntity(
      contactId = contactId,
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
    )

  private fun contactIdentityEntity() =
    ContactIdentityEntity(
      contactIdentityId = 1L,
      contactId = 1L,
      identityType = "PASSPORT",
      identityValue = "PP87878787878",
      createdBy = "TEST",
    )

  private fun UpdateContactIdentityRequest.toEntity(contactIdentityId: Long = 1L): ContactIdentityEntity {
    val updatedBy = this.updatedBy
    val updatedTime = this.updatedTime

    return ContactIdentityEntity(
      contactIdentityId = contactIdentityId,
      contactId = this.contactId,
      identityType = this.identityType,
      identityValue = this.identityValue,
      createdBy = "TEST",
    ).also {
      it.amendedBy = updatedBy
      it.amendedTime = updatedTime
    }
  }
}
