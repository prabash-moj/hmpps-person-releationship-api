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
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactAddressEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactAddressRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import java.time.LocalDateTime
import java.util.Optional

class SyncServiceTest {
  private val contactRepository: ContactRepository = mock()
  private val contactAddressRepository: ContactAddressRepository = mock()
  private val syncService = SyncService(contactRepository, contactAddressRepository)

  @Nested
  inner class ContactAddressTests {
    @Test
    fun `should get a contact address by ID`() {
      whenever(contactAddressRepository.findById(1L)).thenReturn(Optional.of(contactAddressEntity(1L)))
      val contactAddress = syncService.getContactAddressById(1L)
      with(contactAddress) {
        assertThat(addressType).isEqualTo("HOME")
        assertThat(primaryAddress).isTrue()
        assertThat(flat).isEqualTo("1B")
        assertThat(property).isEqualTo("Mason House")
        assertThat(street).isEqualTo("Main Street")
        assertThat(postcode).isEqualTo("LS13 4KD")
      }
      verify(contactAddressRepository).findById(1L)
    }

    @Test
    fun `should fail to get a contact address by ID when not found`() {
      whenever(contactAddressRepository.findById(1L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.getContactAddressById(1L)
      }
      verify(contactAddressRepository).findById(1L)
    }

    @Test
    fun `should create a contact address`() {
      val request = createContactAddressRequest(1L)
      whenever(contactRepository.findById(1L)).thenReturn(Optional.of(contactEntity()))
      whenever(contactAddressRepository.saveAndFlush(request.toEntity())).thenReturn(request.toEntity())

      val contactAddress = syncService.createContactAddress(request)
      val addressCaptor = argumentCaptor<ContactAddressEntity>()

      verify(contactAddressRepository).saveAndFlush(addressCaptor.capture())

      // Checks the entity saved
      with(addressCaptor.firstValue) {
        assertThat(addressType).isEqualTo(request.addressType)
        assertThat(primaryAddress).isEqualTo(request.primaryAddress)
        assertThat(property).isEqualTo(request.property)
        assertThat(street).isEqualTo(request.street)
        assertThat(area).isEqualTo(request.area)
        assertThat(cityCode).isEqualTo(request.cityCode)
        assertThat(countyCode).isEqualTo(request.countyCode)
        assertThat(countryCode).isEqualTo(request.countryCode)
        assertThat(postCode).isEqualTo(request.postcode)
      }

      // Checks the model response
      with(contactAddress) {
        assertThat(contactAddressId).isEqualTo(0L)
        assertThat(addressType).isEqualTo(request.addressType)
        assertThat(primaryAddress).isEqualTo(request.primaryAddress)
        assertThat(property).isEqualTo(request.property)
        assertThat(street).isEqualTo(request.street)
        assertThat(area).isEqualTo(request.area)
        assertThat(cityCode).isEqualTo(request.cityCode)
        assertThat(countyCode).isEqualTo(request.countyCode)
        assertThat(countyCode).isEqualTo(request.countyCode)
        assertThat(postcode).isEqualTo(request.postcode)
        assertThat(createdBy).isEqualTo(request.createdBy)
        assertThat(createdTime).isAfterOrEqualTo(request.createdTime)
      }

      verify(contactRepository).findById(1L)
    }

    @Test
    fun `should fail to create a contact address when the contact ID is not present`() {
      val request = createContactAddressRequest(1L)
      whenever(contactRepository.findById(1L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.createContactAddress(request)
      }
      verifyNoInteractions(contactAddressRepository)
    }

    @Test
    fun `should delete contact address by ID`() {
      whenever(contactAddressRepository.findById(1L)).thenReturn(Optional.of(contactAddressEntity(1L)))
      syncService.deleteContactAddressById(1L)
      verify(contactAddressRepository).deleteById(1L)
    }

    @Test
    fun `should fail to delete contact address by ID when not found`() {
      whenever(contactAddressRepository.findById(1L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.deleteContactAddressById(1L)
      }
      verify(contactAddressRepository).findById(1L)
    }

    @Test
    fun `should update a contact address by ID`() {
      val request = updateContactAddressRequest()
      whenever(contactRepository.findById(1L)).thenReturn(Optional.of(contactEntity()))
      whenever(contactAddressRepository.findById(1L)).thenReturn(Optional.of(request.toEntity()))
      whenever(contactAddressRepository.saveAndFlush(any())).thenReturn(request.toEntity())

      val updated = syncService.updateContactAddress(1L, request)

      val addressCaptor = argumentCaptor<ContactAddressEntity>()

      verify(contactAddressRepository).saveAndFlush(addressCaptor.capture())

      // Checks the entity saved
      with(addressCaptor.firstValue) {
        assertThat(addressType).isEqualTo(request.addressType)
        assertThat(primaryAddress).isEqualTo(request.primaryAddress)
        assertThat(property).isEqualTo(request.property)
        assertThat(street).isEqualTo(request.street)
        assertThat(area).isEqualTo(request.area)
        assertThat(cityCode).isEqualTo(request.cityCode)
        assertThat(countyCode).isEqualTo(request.countyCode)
        assertThat(countryCode).isEqualTo(request.countryCode)
        assertThat(postCode).isEqualTo(request.postcode)
        assertThat(amendedBy).isEqualTo(request.updatedBy)
        assertThat(amendedTime).isEqualTo(request.updatedTime)
      }

      // Checks the model returned
      with(updated) {
        assertThat(addressType).isEqualTo(request.addressType)
        assertThat(primaryAddress).isEqualTo(request.primaryAddress)
        assertThat(property).isEqualTo(request.property)
        assertThat(street).isEqualTo(request.street)
        assertThat(area).isEqualTo(request.area)
        assertThat(cityCode).isEqualTo(request.cityCode)
        assertThat(countyCode).isEqualTo(request.countyCode)
        assertThat(countyCode).isEqualTo(request.countyCode)
        assertThat(postcode).isEqualTo(request.postcode)
        assertThat(amendedBy).isEqualTo(request.updatedBy)
        assertThat(amendedTime).isEqualTo(request.updatedTime)
      }
    }

    @Test
    fun `should fail to update a contact address by ID when contact is not found`() {
      val updateRequest = updateContactAddressRequest()
      whenever(contactRepository.findById(1L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.updateContactAddress(1L, updateRequest)
      }
      verifyNoInteractions(contactAddressRepository)
    }

    @Test
    fun `should fail to update a contact address when contact address is not found`() {
      val updateRequest = updateContactAddressRequest()
      whenever(contactRepository.findById(1L)).thenReturn(Optional.of(contactEntity(1L)))
      whenever(contactAddressRepository.findById(updateRequest.contactId)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.updateContactAddress(1L, updateRequest)
      }
      verify(contactRepository).findById(updateRequest.contactId)
      verify(contactAddressRepository).findById(1L)
    }
  }

  private fun updateContactAddressRequest(contactId: Long = 1L) =
    UpdateContactAddressRequest(
      contactId = contactId,
      addressType = "HOME",
      primaryAddress = true,
      property = "13",
      street = "Main Street",
      area = "Dodworth",
      cityCode = "CVNTRY",
      countyCode = "WARWKS",
      postcode = "CV4 9NJ",
      countryCode = "UK",
      updatedBy = "TEST",
      updatedTime = LocalDateTime.now(),
    )

  private fun createContactAddressRequest(contactId: Long = 1L) =
    CreateContactAddressRequest(
      contactId = contactId,
      addressType = "HOME",
      primaryAddress = true,
      property = "13",
      street = "Main Street",
      area = "Dodworth",
      cityCode = "CVNTRY",
      countyCode = "WARWKS",
      postcode = "CV4 9NJ",
      countryCode = "UK",
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

  private fun contactAddressEntity(contactAddressId: Long) =
    ContactAddressEntity(
      contactAddressId = contactAddressId,
      contactId = 1L,
      addressType = "HOME",
      primaryAddress = true,
      flat = "1B",
      property = "Mason House",
      street = "Main Street",
      area = "Howarth",
      cityCode = "LEEDS",
      countyCode = "YORKS",
      postCode = "LS13 4KD",
      countryCode = "UK",
      createdBy = "TEST",
    )

  private fun CreateContactAddressRequest.toEntity(contactAddressId: Long = 0) =
    ContactAddressEntity(
      contactAddressId = contactAddressId,
      contactId = this.contactId,
      addressType = this.addressType,
      primaryAddress = this.primaryAddress,
      flat = this.flat,
      property = this.property,
      street = this.street,
      area = this.area,
      cityCode = this.cityCode,
      countyCode = this.countyCode,
      postCode = this.postcode,
      countryCode = this.countryCode,
      createdBy = this.createdBy,
    )

  private fun UpdateContactAddressRequest.toEntity(contactAddressId: Long = 1L): ContactAddressEntity {
    val updatedBy = this.updatedBy
    val updatedTime = this.updatedTime

    return ContactAddressEntity(
      contactAddressId = contactAddressId,
      contactId = this.contactId,
      addressType = this.addressType,
      primaryAddress = this.primaryAddress,
      flat = this.flat,
      property = this.property,
      street = this.street,
      area = this.area,
      cityCode = this.cityCode,
      countyCode = this.countyCode,
      postCode = this.postcode,
      countryCode = this.countryCode,
      createdBy = "TEST",
    ).also {
      it.amendedBy = updatedBy
      it.amendedTime = updatedTime
    }
  }
}
