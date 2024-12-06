package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
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
import org.openapitools.jackson.nullable.JsonNullable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactAddressPhoneEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactWithAddressEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactAddressDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactEmailEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactIdentityDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactPhoneDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.AddContactRelationshipRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactRelationship
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactSearchRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateRelationshipRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactSearchResultItem
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Language
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactAddressDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactAddressPhoneRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactEmailRepository
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
  private val contactEmailRepository: ContactEmailRepository = mock()
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
    contactEmailRepository,
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
      whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> (i.arguments[0] as ContactEntity).copy(contactId = 123) }
      whenever(contactAddressDetailsRepository.findByContactId(any())).thenReturn(listOf(aContactAddressDetailsEntity))

      val result = service.createContact(request)

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
      with(result) {
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
      whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> (i.arguments[0] as ContactEntity).copy(contactId = 123) }

      val result = service.createContact(request)

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
      with(result) {
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
      whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> (i.arguments[0] as ContactEntity).copy(contactId = 123) }

      val result = service.createContact(request)

      val contactCaptor = argumentCaptor<ContactEntity>()
      verify(contactRepository).saveAndFlush(contactCaptor.capture())
      with(contactCaptor.firstValue) {
        assertThat(staffFlag).isFalse()
        assertThat(createdTime).isNotNull()
      }
      assertThat(result.createdContact.isStaff).isFalse()
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
      whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> (i.arguments[0] as ContactEntity).copy(contactId = 123) }
      whenever(prisonerContactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("RELATIONSHIP", "FRI")).thenReturn(
        ReferenceCode(1, "RELATIONSHIP", "FRI", "Friend", 1, true),
      )

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
      whenever(contactRepository.saveAndFlush(any())).thenAnswer { i -> (i.arguments[0] as ContactEntity).copy(contactId = 123) }
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
          ContactAddressPhoneEntity(1, contactId, 1, 2, createdBy = "TEST"),
          ContactAddressPhoneEntity(1, contactId, 1, 4, createdBy = "TEST"),
          ContactAddressPhoneEntity(1, contactId, 2, 3, createdBy = "TEST"),
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
      val emailAddressEntity1 = createContactEmailEntity(id = 1)
      val emailAddressEntity2 = createContactEmailEntity(id = 2)

      whenever(contactEmailRepository.findByContactId(contactId)).thenReturn(listOf(emailAddressEntity1, emailAddressEntity2))

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

      val entity = createContactEntity().copy(
        languageCode = "FRE-FRA",
        interpreterRequired = true,
      )
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
      val entity = createContactEntity().copy(languageCode = null)
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
        ReferenceCode(1, "DOMESTIC_STS", "S", "Single", 1, true),
      )

      val entity = createContactEntity().copy(domesticStatus = "S")
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
      val entity = createContactEntity().copy(domesticStatus = null)
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
      val entity = createContactEntity().copy(staffFlag = true)
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
    private val contactId = 123456L
    private val relationship = ContactRelationship(
      prisonerNumber = "A1234BC",
      relationshipCode = "MOT",
      isNextOfKin = true,
      isEmergencyContact = false,
      comments = "Foo",
    )
    private val request = AddContactRelationshipRequest(contactId, relationship, "RELATIONSHIP_USER")
    private val contact = ContactEntity(
      contactId = contactId,
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
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(contact))
      whenever(prisonerContactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("RELATIONSHIP", "MOT")).thenReturn(
        ReferenceCode(1, "RELATIONSHIP", "MOT", "Mother", 1, true),
      )

      service.addContactRelationship(request)

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
        service.addContactRelationship(request)
      }
    }

    @Test
    fun `should blow up if contact not found`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.empty())

      assertThrows<EntityNotFoundException>("Contact ($contactId) could not be found") {
        service.addContactRelationship(request)
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
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(contact))
      whenever(prisonerContactRepository.saveAndFlush(any())).thenThrow(RuntimeException("Bang!"))

      assertThrows<RuntimeException>("Bang!") {
        service.addContactRelationship(request)
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

  @Nested
  inner class UpdateContactRelationship {
    private val contactId = 1L
    private val prisonerContactId = 2L

    private lateinit var prisonerContact: PrisonerContactEntity

    @BeforeEach
    fun before() {
      prisonerContact = createPrisonerContact(contactId)
    }

    @Nested
    inner class RelationshipType {

      @Test
      fun `should update the contact relationship type`() {
        val relationShipTypeCode = "FRI"
        val request = UpdateRelationshipRequest(
          relationshipCode = JsonNullable.of(relationShipTypeCode),
          updatedBy = "Admin",
        )
        mockBrotherRelationshipReferenceCode()
        whenever(referenceCodeService.getReferenceDataByGroupAndCode("RELATIONSHIP", relationShipTypeCode)).thenReturn(
          ReferenceCode(1, "RELATIONSHIP", "FRI", "Friend", 1, true),
        )

        whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(prisonerContact))
        whenever(prisonerContactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

        service.updateContactRelationship(prisonerContactId, request)

        val prisonerContactCaptor = argumentCaptor<PrisonerContactEntity>()
        verify(prisonerContactRepository).saveAndFlush(prisonerContactCaptor.capture())
        with(prisonerContactCaptor.firstValue) {
          // assert changed
          assertThat(relationshipType).isEqualTo("FRI")
          assertThat(amendedBy).isEqualTo("Admin")
          assertThat(amendedTime).isInThePast()
          // assert unchanged
          assertThat(nextOfKin).isTrue()
          assertThat(emergencyContact).isTrue()
          assertThat(active).isTrue()
          assertThat(comments).isEqualTo("Updated relationship type to Brother")

          assertUnchangedFields()
        }
      }

      @Test
      fun `should not update relationship type with null`() {
        val request = UpdateRelationshipRequest(
          relationshipCode = JsonNullable.of(null),
          updatedBy = "Admin",
        )

        whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(prisonerContact))
        whenever(prisonerContactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

        val exception = assertThrows<ValidationException> {
          service.updateContactRelationship(prisonerContactId, request)
        }
        assertThat(exception.message).isEqualTo("Unsupported relationship type null.")
      }

      @Test
      fun `should not update relationship type with invalid type`() {
        val request = UpdateRelationshipRequest(
          relationshipCode = JsonNullable.of("OOO"),
          updatedBy = "Admin",
        )

        whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(prisonerContact))
        whenever(prisonerContactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

        val exception = assertThrows<ValidationException> {
          service.updateContactRelationship(prisonerContactId, request)
        }
        assertThat(exception.message).isEqualTo("Reference code with groupCode RELATIONSHIP and code 'OOO' not found.")
      }
    }

    @Nested
    inner class NextOfKin {

      @Test
      fun `should update the next of kin`() {
        val request = UpdateRelationshipRequest(
          isNextOfKin = JsonNullable.of(false),
          updatedBy = "Admin",
        )
        mockBrotherRelationshipReferenceCode()
        whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(prisonerContact))
        whenever(prisonerContactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

        service.updateContactRelationship(prisonerContactId, request)

        val prisonerContactCaptor = argumentCaptor<PrisonerContactEntity>()
        verify(prisonerContactRepository).saveAndFlush(prisonerContactCaptor.capture())
        with(prisonerContactCaptor.firstValue) {
          // assert changed
          assertThat(nextOfKin).isFalse()
          assertThat(amendedBy).isEqualTo("Admin")
          assertThat(amendedTime).isInThePast()
          // assert unchanged
          assertThat(relationshipType).isEqualTo("BRO")
          assertThat(emergencyContact).isTrue()
          assertThat(active).isTrue()
          assertThat(comments).isEqualTo("Updated relationship type to Brother")

          assertUnchangedFields()
        }
      }

      @Test
      fun `should not update relationship next of kin with null`() {
        val request = UpdateRelationshipRequest(
          isNextOfKin = JsonNullable.of(null),
          updatedBy = "Admin",
        )

        whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(prisonerContact))
        whenever(prisonerContactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

        val exception = assertThrows<ValidationException> {
          service.updateContactRelationship(prisonerContactId, request)
        }
        assertThat(exception.message).isEqualTo("Unsupported next of kin null.")
      }
    }

    @Nested
    inner class EmergencyContactStatus {

      @Test
      fun `should update the emergency contact status`() {
        val request = UpdateRelationshipRequest(
          isEmergencyContact = JsonNullable.of(false),
          updatedBy = "Admin",
        )
        mockBrotherRelationshipReferenceCode()
        whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(prisonerContact))
        whenever(prisonerContactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

        service.updateContactRelationship(prisonerContactId, request)

        val prisonerContactCaptor = argumentCaptor<PrisonerContactEntity>()
        verify(prisonerContactRepository).saveAndFlush(prisonerContactCaptor.capture())
        with(prisonerContactCaptor.firstValue) {
          // assert changed
          assertThat(emergencyContact).isFalse()
          assertThat(amendedBy).isEqualTo("Admin")
          assertThat(amendedTime).isInThePast()
          // assert unchanged
          assertThat(relationshipType).isEqualTo("BRO")
          assertThat(nextOfKin).isTrue()
          assertThat(active).isTrue()
          assertThat(comments).isEqualTo("Updated relationship type to Brother")

          assertUnchangedFields()
        }
      }

      @Test
      fun `should not update relationship emergency contact with null`() {
        val request = UpdateRelationshipRequest(
          isEmergencyContact = JsonNullable.of(null),
          updatedBy = "Admin",
        )

        whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(prisonerContact))
        whenever(prisonerContactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

        val exception = assertThrows<ValidationException> {
          service.updateContactRelationship(prisonerContactId, request)
        }
        assertThat(exception.message).isEqualTo("Unsupported emergency contact null.")
      }
    }

    @Nested
    inner class RelationshipActiveStatus {

      @Test
      fun `should update the relationship active status`() {
        val request = UpdateRelationshipRequest(
          isRelationshipActive = JsonNullable.of(false),
          updatedBy = "Admin",
        )
        mockBrotherRelationshipReferenceCode()
        whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(prisonerContact))
        whenever(prisonerContactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

        service.updateContactRelationship(prisonerContactId, request)

        val prisonerContactCaptor = argumentCaptor<PrisonerContactEntity>()
        verify(prisonerContactRepository).saveAndFlush(prisonerContactCaptor.capture())
        with(prisonerContactCaptor.firstValue) {
          // assert changed
          assertThat(active).isFalse()
          assertThat(amendedBy).isEqualTo("Admin")
          assertThat(amendedTime).isInThePast()
          // assert unchanged
          assertThat(relationshipType).isEqualTo("BRO")
          assertThat(nextOfKin).isTrue()
          assertThat(emergencyContact).isTrue()
          assertThat(comments).isEqualTo("Updated relationship type to Brother")

          assertUnchangedFields()
        }
      }

      @Test
      fun `should not update relationship active status with null`() {
        val request = UpdateRelationshipRequest(
          isRelationshipActive = JsonNullable.of(null),
          updatedBy = "Admin",
        )

        whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(prisonerContact))
        whenever(prisonerContactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

        val exception = assertThrows<ValidationException> {
          service.updateContactRelationship(prisonerContactId, request)
        }
        assertThat(exception.message).isEqualTo("Unsupported relationship status null.")
      }
    }

    @Nested
    inner class RelationshipComment {

      @Test
      fun `should update the contact relationship comment`() {
        val relationShipTypeCode = "FRI"
        val request = UpdateRelationshipRequest(
          comments = JsonNullable.of("a comment"),
          updatedBy = "Admin",
        )
        mockBrotherRelationshipReferenceCode()
        whenever(referenceCodeService.getReferenceDataByGroupAndCode("RELATIONSHIP", relationShipTypeCode)).thenReturn(
          ReferenceCode(1, "RELATIONSHIP", "FRI", "Friend", 1, true),
        )

        whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(prisonerContact))
        whenever(prisonerContactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

        service.updateContactRelationship(prisonerContactId, request)

        val prisonerContactCaptor = argumentCaptor<PrisonerContactEntity>()
        verify(prisonerContactRepository).saveAndFlush(prisonerContactCaptor.capture())
        with(prisonerContactCaptor.firstValue) {
          // assert changed
          assertThat(comments).isEqualTo("a comment")
          assertThat(amendedBy).isEqualTo("Admin")
          assertThat(amendedTime).isInThePast()
          // assert unchanged
          assertThat(relationshipType).isEqualTo("BRO")
          assertThat(nextOfKin).isTrue()
          assertThat(emergencyContact).isTrue()
          assertThat(active).isTrue()

          assertUnchangedFields()
        }
      }

      @Test
      fun `should not update relationship comment with null`() {
        val request = UpdateRelationshipRequest(
          relationshipCode = JsonNullable.of(null),
          updatedBy = "Admin",
        )

        whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(prisonerContact))
        whenever(prisonerContactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

        val exception = assertThrows<ValidationException> {
          service.updateContactRelationship(prisonerContactId, request)
        }
        assertThat(exception.message).isEqualTo("Unsupported relationship type null.")
      }
    }

    @Test
    fun `should update when only updated by filed is provided`() {
      val request = UpdateRelationshipRequest(
        updatedBy = "Admin",
      )

      mockBrotherRelationshipReferenceCode()
      whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(prisonerContact))
      whenever(prisonerContactRepository.saveAndFlush(any())).thenAnswer { i -> i.arguments[0] }

      service.updateContactRelationship(prisonerContactId, request)

      val prisonerContactCaptor = argumentCaptor<PrisonerContactEntity>()
      verify(prisonerContactRepository).saveAndFlush(prisonerContactCaptor.capture())
      with(prisonerContactCaptor.firstValue) {
        // assert changed

        assertThat(amendedBy).isEqualTo("Admin")
        assertThat(amendedTime).isInThePast()
        // assert unchanged
        assertThat(nextOfKin).isTrue()
        assertThat(relationshipType).isEqualTo("BRO")
        assertThat(emergencyContact).isTrue()
        assertThat(active).isTrue()
        assertThat(comments).isEqualTo("Updated relationship type to Brother")

        assertUnchangedFields()
      }
    }

    @Test
    fun `should blow up if prisoner contact not found`() {
      val request = updateRelationshipRequest()
      whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.empty())

      val exception = assertThrows<EntityNotFoundException> {
        service.updateContactRelationship(prisonerContactId, request)
      }
      assertThat(exception.message).isEqualTo("Prisoner contact with prisoner contact ID 2 not found")
    }

    @Test
    fun `should propagate exceptions updating a prisoner contact relationship`() {
      val request = updateRelationshipRequest()
      whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(prisonerContact))
      whenever(prisonerContactRepository.saveAndFlush(any())).thenThrow(RuntimeException("Bang!"))

      assertThrows<RuntimeException>("Bang!") {
        service.updateContactRelationship(prisonerContactId, request)
      }
    }

    private fun createPrisonerContact(cId: Long): PrisonerContactEntity {
      return PrisonerContactEntity(
        prisonerContactId = prisonerContactId,
        contactId = cId,
        prisonerNumber = "A1234BC",
        contactType = "SOCIAL",
        relationshipType = "BRO",
        nextOfKin = true,
        emergencyContact = true,
        approvedVisitor = true,
        active = true,
        currentTerm = true,
        comments = "Updated relationship type to Brother",
        createdBy = "TEST",
        createdTime = LocalDateTime.now(),
      ).apply {
        approvedBy = "officer456"
        approvedTime = LocalDateTime.now()
        expiryDate = LocalDate.of(2025, 12, 31)
        createdAtPrison = "LONDON"
        amendedBy = "adminUser"
        amendedTime = LocalDateTime.now()
      }
    }

    private fun PrisonerContactEntity.assertUnchangedFields() {
      assertThat(prisonerNumber).isEqualTo("A1234BC")
      assertThat(contactType).isEqualTo("SOCIAL")
      assertThat(currentTerm).isTrue()
      assertThat(approvedVisitor).isTrue()
      assertThat(createdBy).isEqualTo("TEST")
      assertThat(createdTime).isInThePast()
      assertThat(approvedBy).isEqualTo("officer456")
      assertThat(approvedTime).isInThePast()
      assertThat(expiryDate).isEqualTo(LocalDate.of(2025, 12, 31))
      assertThat(createdAtPrison).isEqualTo("LONDON")
    }

    private fun updateRelationshipRequest(): UpdateRelationshipRequest {
      return UpdateRelationshipRequest(
        relationshipCode = JsonNullable.of("MOT"),
        isEmergencyContact = JsonNullable.of(true),
        isNextOfKin = JsonNullable.of(true),
        isRelationshipActive = JsonNullable.of(false),
        comments = JsonNullable.of("Foo"),
        updatedBy = "Admin",
      )
    }
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

  private fun mockBrotherRelationshipReferenceCode() {
    whenever(referenceCodeService.getReferenceDataByGroupAndCode("RELATIONSHIP", "BRO")).thenReturn(
      ReferenceCode(1, "RELATIONSHIP", "BRO", "Brother", 1, true),
    )
  }
}
