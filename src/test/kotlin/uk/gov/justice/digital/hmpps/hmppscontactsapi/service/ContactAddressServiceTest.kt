package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
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

class ContactAddressServiceTest {
  private val contactRepository: ContactRepository = mock()
  private val contactAddressRepository: ContactAddressRepository = mock()
  private val contactAddressService = ContactAddressService(contactRepository, contactAddressRepository)

  @Nested
  inner class ContactAddressServiceTests {
    private val contactId: Long = 1L
    private val contactAddressId: Long = 2L

    @Test
    fun `should get a contact address by id`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(contactEntity(contactId)))
      whenever(contactAddressRepository.findById(contactAddressId)).thenReturn(Optional.of(contactAddressEntity()))

      val contactAddress = contactAddressService.get(contactId, contactAddressId)

      with(contactAddress) {
        assertThat(addressType).isEqualTo("HOME")
        assertThat(primaryAddress).isTrue()
        assertThat(flat).isEqualTo("1B")
        assertThat(property).isEqualTo("Mason House")
        assertThat(street).isEqualTo("Main Street")
        assertThat(postcode).isEqualTo("LS13 4KD")
      }

      verify(contactRepository).findById(contactId)
      verify(contactAddressRepository).findById(contactAddressId)
    }

    @Test
    fun `should fail to get a contact address by id`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(contactEntity(contactId)))
      whenever(contactAddressRepository.findById(contactAddressId)).thenReturn(Optional.empty())

      assertThrows<EntityNotFoundException> {
        contactAddressService.get(contactId, contactAddressId)
      }

      verify(contactRepository).findById(contactId)
      verify(contactAddressRepository).findById(contactAddressId)
    }

    @Test
    fun `should fail to get an address if the contact is not present`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.empty())
      whenever(contactAddressRepository.findById(contactAddressId)).thenReturn(Optional.of(contactAddressEntity()))

      assertThrows<EntityNotFoundException> {
        contactAddressService.get(contactId, contactAddressId)
      }

      verify(contactRepository).findById(contactId)
      verify(contactAddressRepository, never()).findById(contactAddressId)
    }

    @Test
    fun `should create a contact address`() {
      val request = createContactAddressRequest()
      val contactAddressEntity = request.toEntity(contactId, contactAddressId)

      whenever(contactRepository.findById(contactId))
        .thenReturn(Optional.of(contactEntity()))

      whenever(contactAddressRepository.saveAndFlush(any()))
        .thenReturn(contactAddressEntity)

      val contactAddress = contactAddressService.create(contactId, request)

      val addressCaptor = argumentCaptor<ContactAddressEntity>()

      verify(contactRepository).findById(contactId)
      verify(contactAddressRepository).saveAndFlush(addressCaptor.capture())

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
        assertThat(createdTime).isAfterOrEqualTo(LocalDateTime.now().minusMinutes(1))
      }

      verify(contactRepository).findById(contactId)
    }

    @Test
    fun `should fail to create a contact address when the contact is not present`() {
      val request = createContactAddressRequest()

      whenever(contactRepository.findById(contactId)).thenReturn(Optional.empty())

      whenever(contactAddressRepository.saveAndFlush(request.toEntity(contactId, contactAddressId)))
        .thenReturn(request.toEntity(contactId, contactAddressId))

      assertThrows<EntityNotFoundException> {
        contactAddressService.create(contactId, request)
      }

      verify(contactRepository).findById(contactId)
      verifyNoInteractions(contactAddressRepository)
    }

    @Test
    fun `should delete contact address by id`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(contactEntity(contactId)))
      whenever(contactAddressRepository.findById(contactAddressId)).thenReturn(Optional.of(contactAddressEntity()))
      whenever(contactAddressRepository.deleteById(contactAddressId)).then {}

      contactAddressService.delete(contactId, contactAddressId)

      verify(contactRepository).findById(contactId)
      verify(contactAddressRepository).findById(contactAddressId)
      verify(contactAddressRepository).deleteById(contactAddressId)
    }

    @Test
    fun `should fail to delete contact address when the id is not found`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(contactEntity(contactId)))
      whenever(contactAddressRepository.findById(contactAddressId)).thenReturn(Optional.empty())

      assertThrows<EntityNotFoundException> {
        contactAddressService.delete(contactId, contactAddressId)
      }

      verify(contactRepository).findById(contactId)
      verify(contactAddressRepository, never()).deleteById(contactAddressId)
    }

    @Test
    fun `should fail to delete contact address when the contact is not found`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.empty())
      whenever(contactAddressRepository.findById(contactAddressId)).thenReturn(Optional.of(contactAddressEntity()))

      assertThrows<EntityNotFoundException> {
        contactAddressService.delete(contactId, contactAddressId)
      }

      verify(contactRepository).findById(contactId)
      verify(contactAddressRepository, never()).deleteById(contactAddressId)
    }

    @Test
    fun `should update a contact address by id`() {
      val request = updateContactAddressRequest()

      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(contactEntity()))

      whenever(contactAddressRepository.findById(contactAddressId))
        .thenReturn(Optional.of(request.toEntity(contactId, contactAddressId)))

      whenever(contactAddressRepository.saveAndFlush(any()))
        .thenReturn(request.toEntity(contactId, contactAddressId))

      val updated = contactAddressService.update(contactId, contactAddressId, request)

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
        assertThat(amendedTime).isAfterOrEqualTo(LocalDateTime.now().minusMinutes(1))
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
        assertThat(updatedBy).isEqualTo(request.updatedBy)
        assertThat(updatedTime).isAfterOrEqualTo(LocalDateTime.now().minusMinutes(1))
      }
    }

    @Test
    fun `should fail to update a contact address when the contact is not found`() {
      val updateRequest = updateContactAddressRequest()
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.empty())

      assertThrows<EntityNotFoundException> {
        contactAddressService.update(contactId, contactAddressId, updateRequest)
      }

      verify(contactRepository).findById(contactId)
      verifyNoInteractions(contactAddressRepository)
    }

    @Test
    fun `should fail to update a contact address when the address is not found`() {
      val updateRequest = updateContactAddressRequest()

      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(contactEntity(contactId)))
      whenever(contactAddressRepository.findById(contactAddressId)).thenReturn(Optional.empty())

      assertThrows<EntityNotFoundException> {
        contactAddressService.update(contactId, contactAddressId, updateRequest)
      }

      verify(contactRepository).findById(contactId)
      verify(contactAddressRepository).findById(contactAddressId)
      verify(contactAddressRepository, never()).saveAndFlush(any())
    }
  }

  private fun updateContactAddressRequest() =
    UpdateContactAddressRequest(
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
    )

  private fun createContactAddressRequest() =
    CreateContactAddressRequest(
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
      middleNames = null,
      lastName = "Smith",
      dateOfBirth = null,
      estimatedIsOverEighteen = EstimatedIsOverEighteen.NO,
      isDeceased = false,
      deceasedDate = null,
      createdBy = "TEST",
    )

  private fun contactAddressEntity() =
    ContactAddressEntity(
      contactAddressId = 1L,
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

  private fun CreateContactAddressRequest.toEntity(contactId: Long, contactAddressId: Long = 0) =
    ContactAddressEntity(
      contactAddressId = contactAddressId,
      contactId = contactId,
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

  private fun UpdateContactAddressRequest.toEntity(contactId: Long, contactAddressId: Long = 1L): ContactAddressEntity {
    val updatedBy = this.updatedBy

    return ContactAddressEntity(
      contactAddressId = contactAddressId,
      contactId = contactId,
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
      it.amendedTime = LocalDateTime.now()
    }
  }
}
