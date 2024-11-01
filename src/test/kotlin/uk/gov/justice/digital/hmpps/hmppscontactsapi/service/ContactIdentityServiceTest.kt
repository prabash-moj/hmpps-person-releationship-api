package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactIdentityDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactIdentityEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactIdentityDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactIdentityDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactIdentityRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import java.time.LocalDateTime.now
import java.util.*

class ContactIdentityServiceTest {

  private val contactRepository: ContactRepository = mock()
  private val contactIdentityRepository: ContactIdentityRepository = mock()
  private val contactIdentityDetailsRepository: ContactIdentityDetailsRepository = mock()
  private val referenceCodeService: ReferenceCodeService = mock()
  private val service =
    ContactIdentityService(contactRepository, contactIdentityRepository, contactIdentityDetailsRepository, referenceCodeService)

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
    createdTime = now(),
  )

  @Nested
  inner class CreateIdentity {
    private val request = CreateIdentityRequest(
      identityType = "DRIVING_LIC",
      identityValue = "DL123456789",
      issuingAuthority = "DVLA",
      createdBy = "created",
    )

    @Test
    fun `should throw EntityNotFoundException creating identity if contact doesn't exist`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.empty())

      val exception = assertThrows<EntityNotFoundException> {
        service.create(contactId, request)
      }
      assertThat(exception.message).isEqualTo("Contact (99) not found")
    }

    @Test
    fun `should throw ValidationException creating identity if identity type doesn't exist`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(aContact))
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("ID_TYPE", "FOO")).thenReturn(null)

      val exception = assertThrows<ValidationException> {
        service.create(contactId, request.copy(identityType = "FOO"))
      }
      assertThat(exception.message).isEqualTo("Unsupported identity type (FOO)")
    }

    @Test
    fun `should return identity details including the reference data after creating successfully`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(aContact))
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("ID_TYPE", "DRIVING_LIC")).thenReturn(
        ReferenceCode(
          0,
          "ID_TYPE",
          "DRIVING_LIC",
          "Driving licence",
          90,
        ),
      )
      whenever(contactIdentityRepository.saveAndFlush(any())).thenAnswer { i ->
        (i.arguments[0] as ContactIdentityEntity).copy(
          contactIdentityId = 9999,
        )
      }

      val created = service.create(contactId, request)
      assertThat(created.createdTime).isNotNull()
      assertThat(created).isEqualTo(
        ContactIdentityDetails(
          contactIdentityId = 9999,
          contactId = contactId,
          identityType = "DRIVING_LIC",
          identityTypeDescription = "Driving licence",
          identityValue = "DL123456789",
          issuingAuthority = "DVLA",
          createdBy = "created",
          createdTime = created.createdTime,
          amendedBy = null,
          amendedTime = null,
        ),
      )
    }
  }

  @Nested
  inner class GetIdentity {
    private val createdTime = now()
    private val entity = ContactIdentityDetailsEntity(
      contactIdentityId = 99,
      contactId = contactId,
      identityType = "DRIVING_LIC",
      identityTypeDescription = "Driving licence",
      identityValue = "DL123456789",
      issuingAuthority = "DVLA",
      createdBy = "USER1",
      createdTime = createdTime,
      amendedBy = null,
      amendedTime = null,
    )

    @Test
    fun `get identity if found by ids`() {
      whenever(contactIdentityDetailsRepository.findByContactIdAndContactIdentityId(contactId, 99)).thenReturn(entity)

      val returned = service.get(contactId, 99)

      assertThat(returned).isEqualTo(
        ContactIdentityDetails(
          contactIdentityId = 99,
          contactId = contactId,
          identityType = "DRIVING_LIC",
          identityTypeDescription = "Driving licence",
          identityValue = "DL123456789",
          issuingAuthority = "DVLA",
          createdBy = "USER1",
          createdTime = createdTime,
          amendedBy = null,
          amendedTime = null,
        ),
      )
    }

    @Test
    fun `return null if not found`() {
      whenever(contactIdentityDetailsRepository.findByContactIdAndContactIdentityId(contactId, 99)).thenReturn(null)

      assertThat(service.get(contactId, 99)).isNull()
    }
  }
}
