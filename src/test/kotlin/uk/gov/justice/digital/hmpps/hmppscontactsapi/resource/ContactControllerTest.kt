package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.openapitools.jackson.nullable.JsonNullable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactAddressDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactEmailDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactIdentityDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactPhoneNumberDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.AddContactRelationshipRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactRelationship
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactSearchRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.patch.PatchContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.patch.PatchContactResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactSearchResultItem
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.ContactService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.patch.ContactPatchFacade
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime

class ContactControllerTest {

  private val contactService: ContactService = mock()
  private val contactPatchFacade: ContactPatchFacade = mock()
  private val controller = ContactController(contactService, contactPatchFacade)

  @Nested
  inner class CreateContact {
    @Test
    fun `should create a contact successfully`() {
      val request = CreateContactRequest(
        lastName = "last",
        firstName = "first",
        createdBy = "created",
      )
      val expectedContact = ContactDetails(
        id = 99,
        lastName = request.lastName,
        firstName = request.firstName,
        estimatedIsOverEighteen = EstimatedIsOverEighteen.DO_NOT_KNOW,
        isDeceased = false,
        deceasedDate = null,
        languageCode = null,
        languageDescription = null,
        interpreterRequired = false,
        addresses = listOf(createContactAddressDetails()),
        phoneNumbers = listOf(createContactPhoneNumberDetails()),
        emailAddresses = listOf(createContactEmailDetails()),
        identities = listOf(createContactIdentityDetails()),
        domesticStatusCode = "S",
        domesticStatusDescription = "Single",
        gender = null,
        genderDescription = null,
        createdBy = request.createdBy,
        createdTime = LocalDateTime.now(),
      )
      whenever(contactService.createContact(request)).thenReturn(expectedContact)

      val response = controller.createContact(request)

      assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
      assertThat(response.body).isEqualTo(expectedContact)
      assertThat(response.headers.location).isEqualTo(URI.create("/contact/99"))
      verify(contactService).createContact(request)
    }

    @Test
    fun `should propagate exceptions creating a contact`() {
      val request = CreateContactRequest(
        lastName = "last",
        firstName = "first",
        createdBy = "created",
      )
      whenever(contactService.createContact(request)).thenThrow(RuntimeException("Bang!"))

      assertThrows<RuntimeException>("Bang!") {
        controller.createContact(request)
      }
    }
  }

  @Nested
  inner class GetContact {
    private val id = 123456L
    private val contact = ContactDetails(
      id = id,
      lastName = "last",
      firstName = "first",
      estimatedIsOverEighteen = EstimatedIsOverEighteen.DO_NOT_KNOW,
      isDeceased = false,
      deceasedDate = null,
      languageCode = null,
      languageDescription = null,
      interpreterRequired = false,
      addresses = listOf(createContactAddressDetails()),
      phoneNumbers = listOf(createContactPhoneNumberDetails()),
      emailAddresses = listOf(createContactEmailDetails()),
      identities = listOf(createContactIdentityDetails()),
      domesticStatusCode = null,
      domesticStatusDescription = null,
      gender = null,
      genderDescription = null,
      createdBy = "user",
      createdTime = LocalDateTime.now(),
    )

    @Test
    fun `should get a contact successfully`() {
      whenever(contactService.getContact(id)).thenReturn(contact)

      val response = controller.getContact(id)

      assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
      assertThat(response.body).isEqualTo(contact)
      verify(contactService).getContact(id)
    }

    @Test
    fun `should return 404 if contact not found`() {
      whenever(contactService.getContact(id)).thenReturn(null)

      val response = controller.getContact(id)

      assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
      verify(contactService).getContact(id)
    }

    @Test
    fun `should propagate exceptions getting a contact`() {
      whenever(contactService.getContact(id)).thenThrow(RuntimeException("Bang!"))

      assertThrows<RuntimeException>("Bang!") {
        controller.getContact(id)
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
    private val request = AddContactRelationshipRequest(relationship, "USER")

    @Test
    fun `should create a contact relationship successfully`() {
      doNothing().whenever(contactService).addContactRelationship(id, request)

      controller.addContactRelationship(id, request)

      verify(contactService).addContactRelationship(id, request)
    }

    @Test
    fun `should propagate exceptions getting a contact`() {
      whenever(contactService.addContactRelationship(id, request)).thenThrow(RuntimeException("Bang!"))

      assertThrows<RuntimeException>("Bang!") {
        controller.addContactRelationship(id, request)
      }
    }
  }

  @Nested
  inner class SearchContact {

    @Test
    fun `test searchContacts with surname ,forename ,middle and date of birth`() {
      // Given
      val pageable = PageRequest.of(0, 10)
      val contactEntities = listOf(
        getContact(),
      )
      val pageContacts = PageImpl(contactEntities, pageable, contactEntities.size.toLong())

      // When
      whenever(
        contactService.searchContacts(
          pageable,
          ContactSearchRequest("last", "first", "middle", LocalDate.of(1980, 1, 1)),
        ),
      ).thenReturn(pageContacts)

      // Act
      val result: Page<ContactSearchResultItem> = controller.searchContacts(pageable, ContactSearchRequest("last", "first", "middle", LocalDate.of(1980, 1, 1)))

      // Then
      assertNotNull(result)
      assertThat(result.totalElements).isEqualTo(1)
      assertThat(result.content[0].lastName).isEqualTo("last")
      assertThat(result.content[0].firstName).isEqualTo("first")
      assertThat(result.content[0].mailFlag).isEqualTo(true)
      assertThat(result.content[0].noFixedAddress).isEqualTo(true)
    }
  }

  @Nested
  inner class PatchContact {
    private val id = 123456L
    private val contact = patchContactResponse(id)

    @Test
    fun `should patch a contact successfully`() {
      val request = patchContactRequest()
      whenever(contactPatchFacade.patch(id, request)).thenReturn(contact)

      val result = controller.patchContact(id, request)

      assertThat(result).isEqualTo(contact)
    }

    @Test
    fun `should return 404 if contact not found`() {
      val request = patchContactRequest()
      whenever(contactPatchFacade.patch(id, request)).thenReturn(null)

      val response = controller.patchContact(id, request)

      assertThat(response).isEqualTo(null)
    }

    @Test
    fun `should propagate exceptions getting a contact`() {
      val request = patchContactRequest()
      whenever(contactPatchFacade.patch(id, request)).thenThrow(RuntimeException("Bang!"))

      assertThrows<RuntimeException>("Bang!") {
        controller.patchContact(id, request)
      }
    }

    private fun patchContactRequest() = PatchContactRequest(
      languageCode = JsonNullable.of("ENG"),
      updatedBy = "system",
    )

    private fun patchContactResponse(id: Long) = PatchContactResponse(
      id = id,
      title = "MR",
      lastName = "Doe",
      firstName = "John",
      middleNames = "William",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      estimatedIsOverEighteen = EstimatedIsOverEighteen.YES,
      placeOfBirth = "London",
      active = true,
      suspended = false,
      isStaff = false,
      deceasedFlag = false,
      deceasedDate = null,
      coronerNumber = null,
      gender = "Male",
      domesticStatus = "Single",
      languageCode = "EN",
      nationalityCode = "GB",
      interpreterRequired = false,
      createdBy = "JD000001",
      createdTime = LocalDateTime.now(),
      amendedBy = "UPDATE",
      amendedTime = LocalDateTime.now(),
    )
  }

  private fun getContact() = ContactSearchResultItem(
    id = 1L,
    lastName = "last",
    firstName = "first",
    middleNames = "first",
    dateOfBirth = LocalDate.of(1980, 2, 1),
    createdBy = "user",
    createdTime = LocalDateTime.now(),
    flat = "user",
    street = "user",
    area = "user",
    postCode = "user",
    mailFlag = true,
    noFixedAddress = true,
    comments = "Some comments",
  )
}
