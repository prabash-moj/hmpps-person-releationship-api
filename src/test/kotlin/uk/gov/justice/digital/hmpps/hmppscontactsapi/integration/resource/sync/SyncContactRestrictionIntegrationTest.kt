package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource.sync

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.UpdateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.ContactRestriction
import java.time.LocalDate
import java.time.LocalDateTime

class SyncContactRestrictionIntegrationTest : IntegrationTestBase() {

  @Nested
  inner class ContactRestrictionSyncTests {
    private var savedContactId = 0L

    @BeforeEach
    fun initialiseData() {
      savedContactId = testAPIClient.createAContact(aMinimalCreateContactRequest()).id
    }

    @Test
    fun `Sync endpoints should return unauthorized if no token provided`() {
      webTestClient.get()
        .uri("/sync/contact-restriction/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.put()
        .uri("/sync/contact-restriction")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(createContactRestrictionRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.post()
        .uri("/sync/contact-restriction/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateContactRestrictionRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.delete()
        .uri("/sync/contact-restriction/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `Sync endpoints should return forbidden without an authorised role on the token`() {
      webTestClient.get()
        .uri("/sync/contact-restriction/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.post()
        .uri("/sync/contact-restriction")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(createContactRestrictionRequest(savedContactId))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.put()
        .uri("/sync/contact-restriction/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateContactRestrictionRequest(savedContactId))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.delete()
        .uri("/sync/contact-restriction/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should get an existing contact restriction`() {
      // From base data
      val contactRestrictionId = 2L
      val contactRestriction = webTestClient.get()
        .uri("/sync/contact-restriction/{contactRestrictionId}", contactRestrictionId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ContactRestriction::class.java)
        .returnResult().responseBody!!

      with(contactRestriction) {
        assertThat(restrictionType).isEqualTo("PUBLIC")
        assertThat(startDate).isEqualTo(LocalDate.of(2000, 11, 21))
        assertThat(expiryDate).isEqualTo(LocalDate.of(2005, 11, 21))
        assertThat(comments).isEqualTo("N/A")
      }
    }

    @Test
    fun `should create a new contact restriction`() {
      val contactRestriction = webTestClient.post()
        .uri("/sync/contact-restriction")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(createContactRestrictionRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ContactRestriction::class.java)
        .returnResult().responseBody!!

      // The created restriction is returned
      with(contactRestriction) {
        assertThat(contactRestrictionId).isGreaterThan(0)
        assertThat(contactId).isEqualTo(savedContactId)
        assertThat(restrictionType).isEqualTo("NEW")
        assertThat(startDate).isEqualTo(LocalDate.of(1982, 6, 15))
        assertThat(expiryDate).isEqualTo(LocalDate.of(2025, 6, 15))
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }
    }

    @Test
    fun `should create and then update a contact restriction`() {
      val contactRestriction = webTestClient.post()
        .uri("/sync/contact-restriction")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(createContactRestrictionRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ContactRestriction::class.java)
        .returnResult().responseBody!!

      with(contactRestriction) {
        assertThat(restrictionType).isEqualTo("NEW")
        assertThat(startDate).isEqualTo(LocalDate.of(1982, 6, 15))
        assertThat(expiryDate).isEqualTo(LocalDate.of(2025, 6, 15))
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }

      val updatedRestriction = webTestClient.put()
        .uri("/sync/contact-restriction/{contactRestrictionId}", contactRestriction.contactRestrictionId)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(updateContactRestrictionRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ContactRestriction::class.java)
        .returnResult().responseBody!!

      // Check the updated copy
      with(updatedRestriction) {
        assertThat(contactRestrictionId).isGreaterThan(0)
        assertThat(contactId).isEqualTo(savedContactId)
        assertThat(restrictionType).isEqualTo("RESTRICTION")
        assertThat(startDate).isEqualTo(LocalDate.of(1982, 6, 15))
        assertThat(expiryDate).isEqualTo(LocalDate.of(1928, 6, 15))
        assertThat(comments).isEqualTo("N/A")
        assertThat(amendedBy).isEqualTo("UPDATE")
        assertThat(amendedTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isNotNull()
      }
    }

    @Test
    fun `should delete an existing contact restriction`() {
      val contactRestrictionId = 3L

      webTestClient.delete()
        .uri("/sync/contact-restriction/{contactRestrictionId}", contactRestrictionId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/sync/contact-restriction/{contactRestrictionId}", contactRestrictionId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    private fun updateContactRestrictionRequest(contactId: Long) =
      UpdateContactRestrictionRequest(
        contactId = contactId,
        restrictionType = "RESTRICTION",
        startDate = LocalDate.of(1982, 6, 15),
        expiryDate = LocalDate.of(1928, 6, 15),
        comments = "N/A",
        updatedBy = "UPDATE",
        updatedTime = LocalDateTime.now(),
      )

    private fun createContactRestrictionRequest(contactId: Long) =
      CreateContactRestrictionRequest(
        contactId = contactId,
        restrictionType = "NEW",
        startDate = LocalDate.of(1982, 6, 15),
        expiryDate = LocalDate.of(2025, 6, 15),
        comments = "N/A",
        createdBy = "CREATE",
      )
  }

  private fun aMinimalCreateContactRequest() = CreateContactRequest(
    lastName = "last",
    firstName = "first",
    createdBy = "created",
  )
}
