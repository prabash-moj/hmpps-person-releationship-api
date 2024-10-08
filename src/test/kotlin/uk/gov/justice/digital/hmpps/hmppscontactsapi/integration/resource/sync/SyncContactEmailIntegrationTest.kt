package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource.sync

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreateContactEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.UpdateContactEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.ContactEmail
import java.time.LocalDateTime

class SyncContactEmailIntegrationTest : IntegrationTestBase() {

  @Nested
  inner class ContactEmailSyncTests {
    private var savedContactId = 0L

    @BeforeEach
    fun initialiseData() {
      savedContactId = testAPIClient.createAContact(aMinimalCreateContactRequest()).id
    }

    @Test
    fun `Sync endpoints should return unauthorized if no token provided`() {
      webTestClient.get()
        .uri("/sync/contact-email/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.put()
        .uri("/sync/contact-email")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(createContactEmailRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.post()
        .uri("/sync/contact-email/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateContactEmailRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.delete()
        .uri("/sync/contact-email/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `Sync endpoints should return forbidden without an authorised role on the token`() {
      webTestClient.get()
        .uri("/sync/contact-email/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.post()
        .uri("/sync/contact-email")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(createContactEmailRequest(savedContactId))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.put()
        .uri("/sync/contact-email/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateContactEmailRequest(savedContactId))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.delete()
        .uri("/sync/contact-email/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should get an existing contact email`() {
      // From base data
      val contactEmailId = 2L
      val contactEmail = webTestClient.get()
        .uri("/sync/contact-email/{contactEmailId}", contactEmailId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ContactEmail::class.java)
        .returnResult().responseBody!!

      with(contactEmail) {
        assertThat(emailType).isEqualTo("PERSONAL")
        assertThat(emailAddress).isEqualTo("miss.last@example.com")
        assertThat(primaryEmail).isTrue()
      }
    }

    @Test
    fun `should create a new contact email`() {
      val contactEmail = webTestClient.post()
        .uri("/sync/contact-email")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(createContactEmailRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ContactEmail::class.java)
        .returnResult().responseBody!!

      // The created email is returned
      with(contactEmail) {
        assertThat(contactEmailId).isGreaterThan(3)
        assertThat(contactId).isEqualTo(savedContactId)
        assertThat(emailType).isEqualTo("Personal")
        assertThat(emailAddress).isEqualTo("test@test.co.uk")
        assertThat(primaryEmail).isTrue()
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }
    }

    @Test
    fun `should create and then update a contact email`() {
      val contactEmail = webTestClient.post()
        .uri("/sync/contact-email")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(createContactEmailRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ContactEmail::class.java)
        .returnResult().responseBody!!

      with(contactEmail) {
        assertThat(emailType).isEqualTo("Personal")
        assertThat(emailAddress).isEqualTo("test@test.co.uk")
        assertThat(primaryEmail).isTrue()
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }

      val updatedEmail = webTestClient.put()
        .uri("/sync/contact-email/{contactEmailId}", contactEmail.contactEmailId)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(updateContactEmailRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ContactEmail::class.java)
        .returnResult().responseBody!!

      // Check the updated copy
      with(updatedEmail) {
        assertThat(contactEmailId).isGreaterThan(4)
        assertThat(contactId).isEqualTo(savedContactId)
        assertThat(emailType).isEqualTo("Personal")
        assertThat(emailAddress).isEqualTo("test@test.co.uk")
        assertThat(amendedBy).isEqualTo("UPDATE")
        assertThat(amendedTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isNotNull()
      }
    }

    @Test
    fun `should delete an existing contact email`() {
      val contactEmailId = 3L

      webTestClient.delete()
        .uri("/sync/contact-email/{contactEmailId}", contactEmailId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/sync/contact-email/{contactEmailId}", contactEmailId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    private fun updateContactEmailRequest(contactId: Long) =
      UpdateContactEmailRequest(
        contactId = contactId,
        emailType = "Personal",
        emailAddress = "test@test.co.uk",
        primaryEmail = true,
        updatedBy = "UPDATE",
        updatedTime = LocalDateTime.now(),
      )

    private fun createContactEmailRequest(contactId: Long) =
      CreateContactEmailRequest(
        contactId = contactId,
        emailType = "Personal",
        emailAddress = "test@test.co.uk",
        primaryEmail = true,
        createdBy = "CREATE",
      )
  }

  private fun aMinimalCreateContactRequest() = CreateContactRequest(
    lastName = "last",
    firstName = "first",
    createdBy = "created",
  )
}
