package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource.sync

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateContactPhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncUpdateContactPhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.SyncContactPhone
import java.time.LocalDateTime

class SyncContactPhoneIntegrationTest : H2IntegrationTestBase() {

  @Nested
  inner class SyncContactPhoneSyncTests {
    private var savedContactId = 0L

    @BeforeEach
    fun initialiseData() {
      savedContactId = testAPIClient.createAContact(aMinimalCreateContactRequest()).id
    }

    @Test
    fun `Sync endpoints should return unauthorized if no token provided`() {
      webTestClient.get()
        .uri("/sync/contact-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.put()
        .uri("/sync/contact-phone")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(createContactPhoneRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.post()
        .uri("/sync/contact-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateContactPhoneRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.delete()
        .uri("/sync/contact-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `Sync endpoints should return forbidden without an authorised role on the token`() {
      webTestClient.get()
        .uri("/sync/contact-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.post()
        .uri("/sync/contact-phone")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(createContactPhoneRequest(savedContactId))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.put()
        .uri("/sync/contact-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateContactPhoneRequest(savedContactId))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.delete()
        .uri("/sync/contact-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should get an existing contact phone`() {
      // From base data
      val contactPhoneId = 3L
      val contactPhone = webTestClient.get()
        .uri("/sync/contact-phone/{contactPhoneId}", contactPhoneId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncContactPhone::class.java)
        .returnResult().responseBody!!

      with(contactPhone) {
        assertThat(phoneType).isEqualTo("MOB")
        assertThat(phoneNumber).isEqualTo("07878 222222")
      }
    }

    @Test
    fun `should create a new contact phone`() {
      val contactPhone = webTestClient.post()
        .uri("/sync/contact-phone")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(createContactPhoneRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncContactPhone::class.java)
        .returnResult().responseBody!!

      // The created phone is returned
      with(contactPhone) {
        assertThat(contactPhoneId).isGreaterThan(10L)
        assertThat(contactId).isEqualTo(savedContactId)
        assertThat(phoneType).isEqualTo("Mobile")
        assertThat(phoneNumber).isEqualTo("555-1234")
        assertThat(extNumber).isEqualTo("101")
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }
    }

    @Test
    fun `should create and then update a contact phone`() {
      val contactPhone = webTestClient.post()
        .uri("/sync/contact-phone")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(createContactPhoneRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncContactPhone::class.java)
        .returnResult().responseBody!!

      with(contactPhone) {
        assertThat(phoneType).isEqualTo("Mobile")
        assertThat(phoneNumber).isEqualTo("555-1234")
        assertThat(extNumber).isEqualTo("101")
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }

      val updatedPhone = webTestClient.put()
        .uri("/sync/contact-phone/{contactPhoneId}", contactPhone.contactPhoneId)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(updateContactPhoneRequest(savedContactId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncContactPhone::class.java)
        .returnResult().responseBody!!

      // Check the updated copy
      with(updatedPhone) {
        assertThat(contactPhoneId).isGreaterThan(10)
        assertThat(contactId).isEqualTo(savedContactId)
        assertThat(phoneType).isEqualTo("Mobile")
        assertThat(phoneNumber).isEqualTo("555-1234")
        assertThat(amendedBy).isEqualTo("UPDATE")
        assertThat(amendedTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isNotNull()
      }
    }

    @Test
    fun `should delete an existing contact phone`() {
      val contactPhoneId = 11L

      webTestClient.delete()
        .uri("/sync/contact-phone/{contactPhoneId}", contactPhoneId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/sync/contact-phone/{contactPhoneId}", contactPhoneId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    private fun updateContactPhoneRequest(contactId: Long) =
      SyncUpdateContactPhoneRequest(
        contactId = contactId,
        phoneType = "Mobile",
        phoneNumber = "555-1234",
        extNumber = "101",
        updatedBy = "UPDATE",
        updatedTime = LocalDateTime.now(),
      )

    private fun createContactPhoneRequest(contactId: Long) =
      SyncCreateContactPhoneRequest(
        contactId = contactId,
        phoneType = "Mobile",
        phoneNumber = "555-1234",
        extNumber = "101",
        createdBy = "CREATE",
      )
  }

  private fun aMinimalCreateContactRequest() = CreateContactRequest(
    lastName = "last",
    firstName = "first",
    createdBy = "created",
  )
}
