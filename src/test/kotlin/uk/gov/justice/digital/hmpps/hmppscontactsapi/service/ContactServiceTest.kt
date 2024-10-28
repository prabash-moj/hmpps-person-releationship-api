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
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactAddressPhoneEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactWithAddressEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactAddressDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactEmailDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactIdentityDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactPhoneDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.AddContactRelationshipRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactRelationship
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactSearchRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactSearchResultItem
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Language
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactAddressDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactAddressPhoneRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactEmailDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactIdentityDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactPhoneDetailsRepository
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
  private val contactAddressDetailsRepository: ContactAddressDetailsRepository = mock()
  private val contactPhoneDetailsRepository: ContactPhoneDetailsRepository = mock()
  private val contactAddressPhoneRepository: ContactAddressPhoneRepository = mock()
  private val contactEmailDetailsRepository: ContactEmailDetailsRepository = mock()
  private val contactIdentityDetailsRepository: ContactIdentityDetailsRepository = mock()
  private val languageService: LanguageService = mock()
  private val referenceCodeService: ReferenceCodeService = mock()
  private val service = ContactService(
    contactRepository,
    prisonerContactRepository,
    prisonerService,
    contactSearchRepository,
    contactAddressDetailsRepository,
    contactPhoneDetailsRepository,
    contactAddressPhoneRepository,
    contactEmailDetailsRepository,
    contactIdentityDetailsRepository,
    languageService,
    referenceCodeService,
  )

  private val aContactAddressDetailsEntity = createContactAddressDetailsEntity()

  @Nested
  inner class CreateContact {
    @Test
    fun `should create a contact with a date of birth successfully`() {
      val request = CreateContactRequest(
        title = "mr",
        lastName = "last",
        firstName = "first",
        middleNames = "middle",
        dateOfBirth = LocalDate.of(1982, 6, 15),
        createdBy = "created",
      )
      whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }
      whenever(contactAddressDetailsRepository.findByContactId(any())).thenReturn(listOf(aContactAddressDetailsEntity))

      val createdContact = service.createContact(request)

      val contactCaptor = argumentCaptor<ContactEntity>()
      verify(contactRepository).saveAndFlush(contactCaptor.capture())
      with(contactCaptor.firstValue) {
        assertThat(title).isEqualTo(request.title)
        assertThat(lastName).isEqualTo(request.lastName)
        assertThat(firstName).isEqualTo(request.firstName)
        assertThat(middleNames).isEqualTo(request.middleNames)
        assertThat(dateOfBirth).isEqualTo(request.dateOfBirth)
        assertThat(createdBy).isEqualTo(request.createdBy)
        assertThat(createdTime).isNotNull()
      }
      with(createdContact) {
        assertThat(title).isEqualTo(request.title)
        assertThat(lastName).isEqualTo(request.lastName)
        assertThat(firstName).isEqualTo(request.firstName)
        assertThat(middleNames).isEqualTo(request.middleNames)
        assertThat(dateOfBirth).isEqualTo(request.dateOfBirth)
        assertThat(createdBy).isEqualTo(request.createdBy)
        assertThat(createdTime).isNotNull()
        assertThat(addresses).isEqualTo(listOf(aContactAddressDetailsEntity.toModel(emptyList())))
      }
    }

    @Test
    fun `should create a contact without a date of birth successfully`() {
      val request = CreateContactRequest(
        title = "mr",
        lastName = "last",
        firstName = "first",
        middleNames = "middle",
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
        assertThat(middleNames).isEqualTo(request.middleNames)
        assertNull(dateOfBirth)
        assertThat(estimatedIsOverEighteen).isEqualTo(request.estimatedIsOverEighteen)
        assertThat(createdBy).isEqualTo(request.createdBy)
        assertThat(createdTime).isNotNull()
      }
      with(createdContact) {
        assertThat(title).isEqualTo(request.title)
        assertThat(lastName).isEqualTo(request.lastName)
        assertThat(firstName).isEqualTo(request.firstName)
        assertThat(middleNames).isEqualTo(request.middleNames)
        assertNull(dateOfBirth)
        assertThat(estimatedIsOverEighteen).isEqualTo(estimatedIsOverEighteen)
        assertThat(createdBy).isEqualTo(request.createdBy)
        assertThat(createdTime).isNotNull()
      }
    }

    @Test
    fun `should create a contact returns existing value for the staff flag`() {
      val request = CreateContactRequest(
        title = "mr",
        lastName = "last",
        firstName = "first",
        middleNames = "middle",
        dateOfBirth = null,
        createdBy = "created",
      )
      whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

      val createdContact = service.createContact(request)

      val contactCaptor = argumentCaptor<ContactEntity>()
      verify(contactRepository).saveAndFlush(contactCaptor.capture())
      with(contactCaptor.firstValue) {
        assertThat(staffFlag).isFalse()
        assertThat(createdTime).isNotNull()
      }
      with(createdContact) {
        assertThat(title).isEqualTo(request.title)
        assertThat(isStaff).isFalse()
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
    private val contactId = 123456L

    @ParameterizedTest
    @EnumSource(EstimatedIsOverEighteen::class)
    fun `should get a contact without dob successfully`(estimatedIsOverEighteen: EstimatedIsOverEighteen) {
      whenever(contactAddressDetailsRepository.findByContactId(contactId)).thenReturn(
        listOf(
          aContactAddressDetailsEntity,
        ),
      )

      val entity = createContactEntity(estimatedIsOverEighteen)
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(entity))
      val contact = service.getContact(contactId)
      assertNotNull(contact)
      with(contact!!) {
        assertThat(id).isEqualTo(entity.contactId)
        assertThat(title).isEqualTo(entity.title)
        assertThat(lastName).isEqualTo(entity.lastName)
        assertThat(firstName).isEqualTo(entity.firstName)
        assertThat(middleNames).isEqualTo(entity.middleNames)
        assertThat(dateOfBirth).isNull()
        assertThat(estimatedIsOverEighteen).isEqualTo(entity.estimatedIsOverEighteen)
        assertThat(createdBy).isEqualTo(entity.createdBy)
        assertThat(createdTime).isEqualTo(entity.createdTime)
        assertThat(addresses).isEqualTo(listOf(aContactAddressDetailsEntity.toModel(emptyList())))
      }
    }

    @Test
    fun `should get a contact with phone numbers including those attached to addresses`() {
      val aGeneralPhoneNumber = createContactPhoneDetailsEntity(id = 1, contactId = contactId)
      val aPhoneAttachedToAddress1 = createContactPhoneDetailsEntity(id = 2, contactId = contactId)
      val aPhoneAttachedToAddress2 = createContactPhoneDetailsEntity(id = 3, contactId = contactId)
      val anotherPhoneAttachedToAddress1 = createContactPhoneDetailsEntity(id = 4, contactId = contactId)
      val address1 = createContactAddressDetailsEntity(id = 1, contactId = contactId)
      val address2 = createContactAddressDetailsEntity(id = 2, contactId = contactId)

      whenever(contactAddressDetailsRepository.findByContactId(contactId)).thenReturn(listOf(address1, address2))
      whenever(contactPhoneDetailsRepository.findByContactId(contactId)).thenReturn(
        listOf(
          aGeneralPhoneNumber,
          aPhoneAttachedToAddress1,
          aPhoneAttachedToAddress2,
          anotherPhoneAttachedToAddress1,
        ),
      )
      whenever(contactAddressPhoneRepository.findByContactId(contactId)).thenReturn(
        listOf(
          ContactAddressPhoneEntity(1, contactId, 1, 2),
          ContactAddressPhoneEntity(1, contactId, 1, 4),
          ContactAddressPhoneEntity(1, contactId, 2, 3),
        ),
      )

      val entity = createContactEntity()
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(entity))

      val contact = service.getContact(contactId)
      assertNotNull(contact)
      with(contact!!) {
        assertThat(id).isEqualTo(entity.contactId)

        assertThat(phoneNumbers).hasSize(4)
        assertThat(phoneNumbers[0].contactPhoneId).isEqualTo(1)
        assertThat(phoneNumbers[1].contactPhoneId).isEqualTo(2)
        assertThat(phoneNumbers[2].contactPhoneId).isEqualTo(3)
        assertThat(phoneNumbers[3].contactPhoneId).isEqualTo(4)

        assertThat(addresses).hasSize(2)
        assertThat(addresses[0].contactAddressId).isEqualTo(1)
        assertThat(addresses[0].phoneNumbers).hasSize(2)
        assertThat(addresses[0].phoneNumbers[0].contactPhoneId).isEqualTo(2)
        assertThat(addresses[0].phoneNumbers[1].contactPhoneId).isEqualTo(4)
        assertThat(addresses[1].phoneNumbers).hasSize(1)
        assertThat(addresses[1].contactAddressId).isEqualTo(2)
        assertThat(addresses[1].phoneNumbers[0].contactPhoneId).isEqualTo(3)
      }
    }

    @Test
    fun `should get a contact with email addresses`() {
      val emailAddressEntity1 = createContactEmailDetailsEntity(id = 1)
      val emailAddressEntity2 = createContactEmailDetailsEntity(id = 2)

      whenever(contactEmailDetailsRepository.findByContactId(contactId)).thenReturn(listOf(emailAddressEntity1, emailAddressEntity2))

      val entity = createContactEntity()
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(entity))

      val contact = service.getContact(contactId)
      assertNotNull(contact)
      with(contact!!) {
        assertThat(id).isEqualTo(entity.contactId)

        assertThat(emailAddresses).hasSize(2)
        assertThat(emailAddresses[0].contactEmailId).isEqualTo(1)
        assertThat(emailAddresses[1].contactEmailId).isEqualTo(2)
      }
    }

    @Test
    fun `should get a contact with identities`() {
      val identityEntity1 = createContactIdentityDetailsEntity(id = 1)
      val identityEntity2 = createContactIdentityDetailsEntity(id = 2)

      whenever(contactIdentityDetailsRepository.findByContactId(contactId)).thenReturn(listOf(identityEntity1, identityEntity2))

      val entity = createContactEntity()
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(entity))

      val contact = service.getContact(contactId)
      assertNotNull(contact)
      with(contact!!) {
        assertThat(id).isEqualTo(entity.contactId)

        assertThat(identities).hasSize(2)
        assertThat(identities[0].contactIdentityId).isEqualTo(1)
        assertThat(identities[1].contactIdentityId).isEqualTo(2)
      }
    }

    @Test
    fun `should get a contact with language code`() {
      whenever(languageService.getLanguageByNomisCode("FRE-FRA")).thenReturn(
        Language(1, "FRE-FRA", "French", "Foo", "Bar", "X", 99),
      )

      val entity = createContactEntity()
      entity.languageCode = "FRE-FRA"
      entity.interpreterRequired = true
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(entity))

      val contact = service.getContact(contactId)
      assertNotNull(contact)
      with(contact!!) {
        assertThat(id).isEqualTo(entity.contactId)
        assertThat(languageCode).isEqualTo("FRE-FRA")
        assertThat(languageDescription).isEqualTo("French")
        assertThat(interpreterRequired).isTrue()
      }
    }

    @Test
    fun `should get a contact if language code null and not lookup the null`() {
      val entity = createContactEntity()
      entity.languageCode = null
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(entity))

      val contact = service.getContact(contactId)
      assertNotNull(contact)
      with(contact!!) {
        assertThat(id).isEqualTo(entity.contactId)
        assertThat(languageCode).isNull()
        assertThat(languageDescription).isNull()
      }
      verify(languageService, never()).getLanguageByNomisCode(any())
    }

    @Test
    fun `should get a contact with domestic status`() {
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("DOMESTIC_STS", "S")).thenReturn(
        ReferenceCode(1, "DOMESTIC_STS", "S", "Single", 1),
      )

      val entity = createContactEntity()
      entity.domesticStatus = "S"
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(entity))

      val contact = service.getContact(contactId)
      assertNotNull(contact)
      with(contact!!) {
        assertThat(id).isEqualTo(entity.contactId)
        assertThat(domesticStatusCode).isEqualTo("S")
        assertThat(domesticStatusDescription).isEqualTo("Single")
      }
    }

    @Test
    fun `should get a contact with no domestic status and not look it up`() {
      val entity = createContactEntity()
      entity.domesticStatus = null
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(entity))

      val contact = service.getContact(contactId)
      assertNotNull(contact)
      with(contact!!) {
        assertThat(id).isEqualTo(entity.contactId)
        assertThat(domesticStatusCode).isNull()
        assertThat(domesticStatusDescription).isNull()
      }
      verify(referenceCodeService, never()).getReferenceDataByGroupAndCode(any(), any())
    }

    @Test
    fun `should get a contact with staff flag`() {
      val entity = createContactEntity().also {
        it.staffFlag = true
      }
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(entity))

      val contact = service.getContact(contactId)
      assertNotNull(contact)
      with(contact!!) {
        assertThat(id).isEqualTo(entity.contactId)
        assertThat(isStaff).isTrue()
      }
    }

    @Test
    fun `should not blow up if contact not found`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.empty())
      val contact = service.getContact(contactId)
      assertNull(contact)
    }

    @Test
    fun `should propagate exceptions getting a contact`() {
      whenever(contactRepository.findById(contactId)).thenThrow(RuntimeException("Bang!"))

      assertThrows<RuntimeException>("Bang!") {
        service.getContact(contactId)
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
      middleNames = null,
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
      whenever(prisonerService.getPrisoner(any())).thenReturn(
        Prisoner(
          request.relationship.prisonerNumber,
          prisonId = "MDI",
        ),
      )
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
      whenever(prisonerService.getPrisoner(any())).thenReturn(
        Prisoner(
          request.relationship.prisonerNumber,
          prisonId = "MDI",
        ),
      )
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
      middleNames = "middle",
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

  private fun createContactEntity(
    estimatedIsOverEighteen: EstimatedIsOverEighteen = EstimatedIsOverEighteen.DO_NOT_KNOW,
  ) = ContactEntity(
    contactId = 123456L,
    title = "Mr",
    lastName = "last",
    middleNames = "middle",
    firstName = "first",
    dateOfBirth = null,
    estimatedIsOverEighteen = estimatedIsOverEighteen,
    isDeceased = false,
    deceasedDate = null,
    createdBy = "user",
    createdTime = LocalDateTime.now(),
  )
}
