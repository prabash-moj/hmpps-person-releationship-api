package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.ContactAddress
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Contact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactAddressRepository
import java.time.LocalDateTime

class SyncEndpointsIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var contactAddressRepository: ContactAddressRepository

  @Nested
  inner class ContactAddressSyncTests {
    private var contactId = 0L

    @BeforeEach
    fun initialiseData() {
      // Create a new contact for each test and expose its ID
      val request = aMinimalCreateContactRequest()
      val contactReturnedOnCreate = testAPIClient.createAContact(request)
      assertContactsAreEqualExcludingTimestamps(contactReturnedOnCreate, request)
      assertThat(contactReturnedOnCreate).isEqualTo(testAPIClient.getContact(contactReturnedOnCreate.id))
      contactId = contactReturnedOnCreate.id
    }

    @Test
    fun `Sync endpoints should return unauthorized if no token provided`() {
      webTestClient.get()
        .uri("/sync/contact-address/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.put()
        .uri("/sync/contact-address")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(createContactAddressRequest(contactId))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.post()
        .uri("/sync/contact-address/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateContactAddressRequest(contactId))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.delete()
        .uri("/sync/contact-address/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `Sync endpoints should return forbidden without an authorised role on the token`() {
      webTestClient.get()
        .uri("/sync/contact-address/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.put()
        .uri("/sync/contact-address")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(createContactAddressRequest(contactId))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.post()
        .uri("/sync/contact-address/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateContactAddressRequest(contactId))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.delete()
        .uri("/sync/contact-address/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should get an existing contact address`() {
      // From base data
      val contactAddressId = 1L
      val contactAddress = webTestClient.get()
        .uri("/sync/contact-address/{contactAddressId}", contactAddressId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ContactAddress::class.java)
        .returnResult().responseBody!!

      with(contactAddress) {
        assertThat(addressType).isEqualTo("HOME")
        assertThat(property).isEqualTo("24")
        assertThat(street).isEqualTo("Acacia Avenue")
        assertThat(primaryAddress).isTrue()
      }
    }

    @Test
    fun `should create a new contact address`() {
      val contactAddress = webTestClient.put()
        .uri("/sync/contact-address")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(createContactAddressRequest(contactId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ContactAddress::class.java)
        .returnResult().responseBody!!

      // The created address is returned
      with(contactAddress) {
        assertThat(flat).isEqualTo("1B")
        assertThat(addressType).isEqualTo("HOME")
        assertThat(amendedBy).isNullOrEmpty()
        assertThat(amendedTime).isNull()
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }
    }

    @Test
    fun `should create and then update a contact address`() {
      val contactAddress = webTestClient.put()
        .uri("/sync/contact-address")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(createContactAddressRequest(contactId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ContactAddress::class.java)
        .returnResult().responseBody!!

      with(contactAddress) {
        assertThat(addressType).isEqualTo("HOME")
        assertThat(flat).isEqualTo("1B")
        assertThat(property).isEqualTo("13")
        assertThat(street).isEqualTo("Main Street")
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }

      val updatedAddress = webTestClient.post()
        .uri("/sync/contact-address/{contactAddressId}", contactAddress.contactAddressId)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(updateContactAddressRequest(contactId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ContactAddress::class.java)
        .returnResult().responseBody!!

      // Check the updated copy
      with(updatedAddress) {
        assertThat(flat).isEqualTo("2B")
        assertThat(addressType).isEqualTo("WORK")
        assertThat(amendedBy).isEqualTo("UPDATE")
        assertThat(amendedTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isNotNull()
      }
    }

    @Test
    fun `should delete an existing contact address`() {
      val beforeCount = contactAddressRepository.count()

      webTestClient.delete()
        .uri("/sync/contact-address/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk

      val afterCount = contactAddressRepository.count()
      assertThat(beforeCount).isEqualTo((afterCount + 1))
    }

    private fun assertContactsAreEqualExcludingTimestamps(contact: Contact, request: CreateContactRequest) {
      with(contact) {
        assertThat(title).isEqualTo(request.title)
        assertThat(lastName).isEqualTo(request.lastName)
        assertThat(firstName).isEqualTo(request.firstName)
        assertThat(middleName).isEqualTo(request.middleName)
        assertThat(dateOfBirth).isEqualTo(request.dateOfBirth)
        if (request.estimatedIsOverEighteen != null) {
          assertThat(estimatedIsOverEighteen).isEqualTo(request.estimatedIsOverEighteen)
        }
        assertThat(createdBy).isEqualTo(request.createdBy)
      }
    }

    private fun updateContactAddressRequest(contactId: Long) =
      UpdateContactAddressRequest(
        contactId = contactId,
        addressType = "WORK",
        primaryAddress = false,
        flat = "2B",
        property = "14",
        street = "Main Street",
        area = "Dodworth",
        cityCode = "CVNTRY",
        countyCode = "WARWKS",
        postcode = "CV4 9NJ",
        countryCode = "UK",
        updatedBy = "UPDATE",
        updatedTime = LocalDateTime.now(),
      )

    private fun createContactAddressRequest(contactId: Long) =
      CreateContactAddressRequest(
        contactId = contactId,
        addressType = "HOME",
        primaryAddress = false,
        flat = "1B",
        property = "13",
        street = "Main Street",
        area = "Dodworth",
        cityCode = "CVNTRY",
        countyCode = "WARWKS",
        postcode = "CV4 9NJ",
        countryCode = "UK",
        createdBy = "CREATE",
      )
  }

  private fun aMinimalCreateContactRequest() = CreateContactRequest(
    lastName = "last",
    firstName = "first",
    createdBy = "created",
  )
}
