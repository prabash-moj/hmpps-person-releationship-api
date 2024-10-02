package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactWithAddressEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.AddContactRelationshipRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactRelationship
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactSearchRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactSearchResultItem
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactSearchRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class ContactServiceTest {

  private val contactRepository: ContactRepository = mock()
  private val prisonerContactRepository: PrisonerContactRepository = mock()
  private val prisonerService: PrisonerService = mock()
  private val contactSearchRepository: ContactSearchRepository = mock()
  private val service = ContactService(
    contactRepository,
    prisonerContactRepository,
    prisonerService,
    contactSearchRepository,
  )

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

    @Test
    fun `should create a contact without a date of birth successfully`() {
      val request = CreateContactRequest(
        title = "mr",
        lastName = "last",
        firstName = "first",
        middleName = "middle",
        dateOfBirth = null,
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
        assertThat(estimatedIsOverEighteen).isEqualTo(request.estimatedIsOverEighteen)
        assertThat(createdBy).isEqualTo(request.createdBy)
        assertThat(createdTime).isNotNull()
      }
      with(createdContact) {
        assertThat(title).isEqualTo(request.title)
        assertThat(lastName).isEqualTo(request.lastName)
        assertThat(firstName).isEqualTo(request.firstName)
        assertThat(middleName).isEqualTo(request.middleName)
        assertNull(dateOfBirth)
        assertThat(estimatedIsOverEighteen).isEqualTo(estimatedIsOverEighteen)
        assertThat(createdBy).isEqualTo(request.createdBy)
        assertThat(createdTime).isNotNull()
      }
    }

    @Test
    fun `should create a contact with a relationship successfully`() {
      val relationshipRequest = ContactRelationship(
        prisonerNumber = "A1234BC",
        relationshipCode = "FRI",
        isNextOfKin = true,
        isEmergencyContact = true,
        comments = "some comments",
      )
      val request = CreateContactRequest(
        lastName = "last",
        firstName = "first",
        createdBy = "created",
        relationship = relationshipRequest,
      )
      whenever(prisonerService.getPrisoner(any())).thenReturn(
        Prisoner(
          relationshipRequest.prisonerNumber,
          prisonId = "MDI",
        ),
      )
      whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }
      whenever(prisonerContactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

      service.createContact(request)

      verify(contactRepository).saveAndFlush(any())
      val prisonerContactCaptor = argumentCaptor<PrisonerContactEntity>()
      verify(prisonerContactRepository).saveAndFlush(prisonerContactCaptor.capture())
      with(prisonerContactCaptor.firstValue) {
        assertThat(prisonerNumber).isEqualTo("A1234BC")
        assertThat(relationshipType).isEqualTo("FRI")
        assertThat(nextOfKin).isEqualTo(true)
        assertThat(emergencyContact).isEqualTo(true)
        assertThat(comments).isEqualTo("some comments")
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

    @Test
    fun `should propagate exceptions creating a contact relationship`() {
      val request = CreateContactRequest(
        lastName = "last",
        firstName = "first",
        createdBy = "created",
        relationship = ContactRelationship(
          prisonerNumber = "A1234BC",
          relationshipCode = "FRI",
          isNextOfKin = true,
          isEmergencyContact = true,
          comments = "some comments",
        ),
      )
      whenever(prisonerService.getPrisoner(any())).thenReturn(Prisoner("A1234BC", prisonId = "MDI"))
      whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }
      whenever(prisonerContactRepository.saveAndFlush(any())).thenThrow(RuntimeException("Bang!"))

      assertThrows<RuntimeException>("Bang!") {
        service.createContact(request)
      }

      verify(contactRepository).saveAndFlush(any())
      verify(prisonerContactRepository).saveAndFlush(any())
    }

    @Test
    fun `should throw EntityNotFoundException when prisoner can't be found and save nothing`() {
      val request = CreateContactRequest(
        lastName = "last",
        firstName = "first",
        createdBy = "created",
        relationship = ContactRelationship(
          prisonerNumber = "A1234BC",
          relationshipCode = "FRI",
          isNextOfKin = true,
          isEmergencyContact = true,
          comments = "some comments",
        ),
      )
      whenever(prisonerService.getPrisoner(any())).thenReturn(null)
      whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }
      whenever(prisonerContactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

      assertThrows<EntityNotFoundException>("Prisoner number A1234BC - not found") {
        service.createContact(request)
      }

      verify(contactRepository, never()).saveAndFlush(any())
      verify(prisonerContactRepository, never()).saveAndFlush(any())
    }
  }

  @Nested
  inner class GetContact {
    private val id = 123456L

    @ParameterizedTest
    @EnumSource(EstimatedIsOverEighteen::class)
    fun `should get a contact without dob successfully`(estimatedIsOverEighteen: EstimatedIsOverEighteen) {
      val entity = ContactEntity(
        contactId = id,
        title = "Mr",
        lastName = "last",
        middleName = "middle",
        firstName = "first",
        dateOfBirth = null,
        estimatedIsOverEighteen = estimatedIsOverEighteen,
        isDeceased = false,
        deceasedDate = null,
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
        assertThat(estimatedIsOverEighteen).isEqualTo(entity.estimatedIsOverEighteen)
        assertThat(createdBy).isEqualTo(entity.createdBy)
        assertThat(createdTime).isEqualTo(entity.createdTime)
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

  @Nested
  inner class AddContactRelationship {
    private val id = 123456L
    private val relationship = ContactRelationship(
      prisonerNumber = "A1234BC",
      relationshipCode = "MOT",
      isNextOfKin = true,
      isEmergencyContact = false,
      comments = "Foo",
    )
    private val request = AddContactRelationshipRequest(relationship, "RELATIONSHIP_USER")
    private val contact = ContactEntity(
      contactId = id,
      title = null,
      lastName = "last",
      middleName = null,
      firstName = "first",
      dateOfBirth = null,
      estimatedIsOverEighteen = EstimatedIsOverEighteen.DO_NOT_KNOW,
      isDeceased = false,
      deceasedDate = null,
      createdBy = "user",
      createdTime = LocalDateTime.now(),
    )

    @Test
    fun `should save the contact relationship`() {
      whenever(prisonerService.getPrisoner(any())).thenReturn(Prisoner(request.relationship.prisonerNumber, prisonId = "MDI"))
      whenever(contactRepository.findById(id)).thenReturn(Optional.of(contact))
      whenever(prisonerContactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

      service.addContactRelationship(id, request)

      val prisonerContactCaptor = argumentCaptor<PrisonerContactEntity>()
      verify(prisonerContactRepository).saveAndFlush(prisonerContactCaptor.capture())
      with(prisonerContactCaptor.firstValue) {
        assertThat(prisonerNumber).isEqualTo("A1234BC")
        assertThat(relationshipType).isEqualTo("MOT")
        assertThat(nextOfKin).isEqualTo(true)
        assertThat(emergencyContact).isEqualTo(false)
        assertThat(comments).isEqualTo("Foo")
        assertThat(createdBy).isEqualTo("RELATIONSHIP_USER")
      }
    }

    @Test
    fun `should blow up if prisoner not found`() {
      whenever(prisonerService.getPrisoner(any())).thenReturn(null)

      assertThrows<EntityNotFoundException>("Prisoner (A1234BC) could not be found") {
        service.addContactRelationship(id, request)
      }
    }

    @Test
    fun `should blow up if contact not found`() {
      whenever(contactRepository.findById(id)).thenReturn(Optional.empty())

      assertThrows<EntityNotFoundException>("Contact ($id) could not be found") {
        service.addContactRelationship(id, request)
      }
    }

    @Test
    fun `should propagate exceptions adding a relationship`() {
      whenever(prisonerService.getPrisoner(any())).thenReturn(Prisoner(request.relationship.prisonerNumber, prisonId = "MDI"))
      whenever(contactRepository.findById(id)).thenReturn(Optional.of(contact))
      whenever(prisonerContactRepository.saveAndFlush(any())).thenThrow(RuntimeException("Bang!"))

      assertThrows<RuntimeException>("Bang!") {
        service.addContactRelationship(id, request)
      }
    }
  }

  @Nested
  inner class SearchContact {

    @Test
    fun `test searchContacts with lastName , firstName , middleName and date of birth`() {
      // Given
      val pageable = PageRequest.of(0, 10)
      val contactWithAddressEntity = getContactWithAddressEntity()

      val results = listOf(contactWithAddressEntity)

      val pageContacts = PageImpl(results, pageable, results.size.toLong())

      // When
      whenever(
        contactSearchRepository.searchContacts(
          ContactSearchRequest("last", "first", "middle", LocalDate.of(1980, 1, 1)),
          pageable,
        ),
      ).thenReturn(pageContacts)

      // Act
      val result: Page<ContactSearchResultItem> = service.searchContacts(
        pageable,
        ContactSearchRequest("last", "first", "middle", LocalDate.of(1980, 1, 1)),
      )

      // Then
      assertNotNull(result)
      assertThat(result.totalElements).isEqualTo(1)
      assertThat(result.content[0].lastName).isEqualTo("last")
      assertThat(result.content[0].firstName).isEqualTo("first")
    }

    private fun getContactWithAddressEntity() = ContactWithAddressEntity(
      contactId = 1L,
      title = "Mr",
      lastName = "last",
      middleName = "middle",
      firstName = "first",
      dateOfBirth = LocalDate.of(1980, 2, 1),
      estimatedIsOverEighteen = EstimatedIsOverEighteen.DO_NOT_KNOW,
      contactAddressId = 1L,
      primaryAddress = true,
      verified = false,
      addressType = "HOME",
      flat = "Mr",
      property = "last",
      street = "middle",
      area = "first",
      cityCode = "",
      countyCode = "null",
      postCode = "user",
      countryCode = "user",
      createdBy = "TEST",
      createdTime = LocalDateTime.now(),
    )
  }
}
