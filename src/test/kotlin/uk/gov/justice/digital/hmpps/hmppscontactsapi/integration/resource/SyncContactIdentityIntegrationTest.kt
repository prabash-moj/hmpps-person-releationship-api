package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Contact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactIdentity
import java.time.LocalDateTime

class SyncContactIdentityIntegrationTest : IntegrationTestBase() {

  @Nested
  inner class ContactIdentitySyncTests {
    private var savedContactId = 0L

    @BeforeEach
    fun initialiseData() {
      val request = aMinimalCreateContactRequest()
      val contactReturnedOnCreate = testAPIClient.createAContact(request)
      assertContactsAreEqualExcludingTimestamps(contactReturnedOnCreate, request)
      assertThat(contactReturnedOnCreate).isEqualTo(testAPIClient.getContact(contactReturnedOnCreate.id))
      savedContactId = contactReturnedOnCreate.id
    }

    @Test
    fun `Sync endpoints should return unauthorized if no token provided`() {
      webTestClient.get()
        .uri("/sync/contact-identity/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.put()
        .uri("/sync/contact-identity")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(createContactIdentityRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.post()
        .uri("/sync/contact-identity/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateContactIdentityRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.delete()
        .uri("/sync/contact-identity/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `Sync endpoints should return forbidden without an authorised role on the token`() {
      webTestClient.get()
        .uri("/sync/contact-identity/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.put()
        .uri("/sync/contact-identity")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(createContactIdentityRequest(savedContactId))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.post()
        .uri("/sync/contact-identity/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateContactIdentityRequest(savedContactId))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.delete()
        .uri("/sync/contact-identity/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should get an existing contact identity`() {
      // From base data
      val contactIdentityId = 2L
      val contactIdentity = webTestClient.get()
        .uri("/sync/contact-identity/{contactIdentityId}", contactIdentityId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ContactIdentity::class.java)
        .returnResult().responseBody!!

      with(contactIdentity) {
        assertThat(identityType).isEqualTo("PASSPORT")
      }
    }

    @Test
    fun `should create a new contact identity`() {
      val contactIdentity = webTestClient.put()
        .uri("/sync/contact-identity")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(createContactIdentityRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ContactIdentity::class.java)
        .returnResult().responseBody!!

      // The created identity is returned
      with(contactIdentity) {
        assertThat(contactIdentityId).isGreaterThan(3)
        assertThat(contactId).isEqualTo(savedContactId)
        assertThat(identityType).isEqualTo("PASSPORT")
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }
    }

    @Test
    fun `should create and then update a contact identity`() {
      val contactIdentity = webTestClient.put()
        .uri("/sync/contact-identity")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(createContactIdentityRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ContactIdentity::class.java)
        .returnResult().responseBody!!

      with(contactIdentity) {
        assertThat(identityType).isEqualTo("PASSPORT")
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }

      val updatedIdentity = webTestClient.post()
        .uri("/sync/contact-identity/{contactIdentityId}", contactIdentity.contactIdentityId)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(updateContactIdentityRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ContactIdentity::class.java)
        .returnResult().responseBody!!

      // Check the updated copy
      with(updatedIdentity) {
        assertThat(contactIdentityId).isGreaterThan(4)
        assertThat(contactId).isEqualTo(savedContactId)
        assertThat(identityType).isEqualTo("PASSPORT")
        assertThat(amendedBy).isEqualTo("UPDATE")
        assertThat(amendedTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isNotNull()
      }
    }

    @Test
    fun `should delete an existing contact identity`() {
      val contactIdentityId = 3L

      webTestClient.delete()
        .uri("/sync/contact-identity/{contactIdentityId}", contactIdentityId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/sync/contact-identity/{contactIdentityId}", contactIdentityId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isNotFound
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

    private fun updateContactIdentityRequest(contactId: Long) =
      UpdateContactIdentityRequest(
        contactId = contactId,
        identityType = "PASSPORT",
        identityValue = "PP87878787878",
        updatedBy = "UPDATE",
        updatedTime = LocalDateTime.now(),
      )

    private fun createContactIdentityRequest(contactId: Long) =
      CreateContactIdentityRequest(
        contactId = contactId,
        identityType = "PASSPORT",
        identityValue = "PP87878787878",
        createdBy = "CREATE",
      )
  }

  private fun aMinimalCreateContactRequest() = CreateContactRequest(
    lastName = "last",
    firstName = "first",
    createdBy = "created",
  )
}
