package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource.sync

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.PostgresIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncUpdateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.SyncContactRestriction
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.ContactRestrictionInfo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.PersonReference
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.Source
import java.time.LocalDate
import java.time.LocalDateTime

class SyncContactRestrictionIntegrationTest : PostgresIntegrationTestBase() {

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

    @ParameterizedTest
    @ValueSource(strings = ["ROLE_CONTACTS_ADMIN", "ROLE_CONTACTS__R", "ROLE_CONTACTS__RW"])
    fun `Sync endpoints should return forbidden without an authorised role on the token`(role: String) {
      webTestClient.get()
        .uri("/sync/contact-restriction/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf(role)))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.post()
        .uri("/sync/contact-restriction")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(createContactRestrictionRequest(savedContactId))
        .headers(setAuthorisation(roles = listOf(role)))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.put()
        .uri("/sync/contact-restriction/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateContactRestrictionRequest(savedContactId))
        .headers(setAuthorisation(roles = listOf(role)))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.delete()
        .uri("/sync/contact-restriction/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf(role)))
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
        .expectBody(SyncContactRestriction::class.java)
        .returnResult().responseBody!!

      with(contactRestriction) {
        assertThat(restrictionType).isEqualTo("BAN")
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
        .expectBody(SyncContactRestriction::class.java)
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
      stubEvents.assertHasEvent(
        event = OutboundEvent.CONTACT_RESTRICTION_CREATED,
        additionalInfo = ContactRestrictionInfo(contactRestriction.contactRestrictionId, Source.NOMIS),
        personReference = PersonReference(dpsContactId = contactRestriction.contactId),
      )
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
        .expectBody(SyncContactRestriction::class.java)
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
        .expectBody(SyncContactRestriction::class.java)
        .returnResult().responseBody!!

      // Check the updated copy
      with(updatedRestriction) {
        assertThat(contactRestrictionId).isGreaterThan(0)
        assertThat(contactId).isEqualTo(savedContactId)
        assertThat(restrictionType).isEqualTo("RESTRICTION")
        assertThat(startDate).isEqualTo(LocalDate.of(1982, 6, 15))
        assertThat(expiryDate).isEqualTo(LocalDate.of(1988, 6, 15))
        assertThat(comments).isEqualTo("N/A")
        assertThat(updatedBy).isEqualTo("UPDATE")
        assertThat(updatedTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isNotNull()
      }
      stubEvents.assertHasEvent(
        event = OutboundEvent.CONTACT_RESTRICTION_UPDATED,
        additionalInfo = ContactRestrictionInfo(updatedRestriction.contactRestrictionId, Source.NOMIS),
        personReference = PersonReference(dpsContactId = updatedRestriction.contactId),
      )
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
      stubEvents.assertHasEvent(
        event = OutboundEvent.CONTACT_RESTRICTION_DELETED,
        additionalInfo = ContactRestrictionInfo(contactRestrictionId, Source.NOMIS),
        personReference = PersonReference(dpsContactId = 3),
      )
    }

    private fun updateContactRestrictionRequest(contactId: Long) = SyncUpdateContactRestrictionRequest(
      contactId = contactId,
      restrictionType = "RESTRICTION",
      startDate = LocalDate.of(1982, 6, 15),
      expiryDate = LocalDate.of(1988, 6, 15),
      comments = "N/A",
      updatedBy = "UPDATE",
      updatedTime = LocalDateTime.now(),
    )

    private fun createContactRestrictionRequest(contactId: Long) = SyncCreateContactRestrictionRequest(
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
