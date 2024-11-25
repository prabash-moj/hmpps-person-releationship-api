package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource.sync

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncUpdatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.SyncPrisonerContactRestriction
import java.time.LocalDate
import java.time.LocalDateTime

class SyncPrisonerContactRestrictionEntityIntegrationTest : H2IntegrationTestBase() {

  @Nested
  inner class PrisonerContactRestrictionEntitySyncTests {

    @Test
    fun `Sync endpoints should return unauthorized if no token provided`() {
      webTestClient.get()
        .uri("/sync/prisoner-contact-restriction/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.put()
        .uri("/sync/prisoner-contact-restriction")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(createPrisonerContactRestrictionRequest())
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.post()
        .uri("/sync/prisoner-contact-restriction/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updatePrisonerContactRestrictionRequest())
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.delete()
        .uri("/sync/prisoner-contact-restriction/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `Sync endpoints should return forbidden without an authorised role on the token`() {
      webTestClient.get()
        .uri("/sync/prisoner-contact-restriction/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.post()
        .uri("/sync/prisoner-contact-restriction")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(createPrisonerContactRestrictionRequest())
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.put()
        .uri("/sync/prisoner-contact-restriction/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updatePrisonerContactRestrictionRequest())
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.delete()
        .uri("/sync/prisoner-contact-restriction/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should get an existing prisoner contact restriction`() {
      // From base data
      val contactId = 1L
      val prisonerContactRestriction = webTestClient.get()
        .uri("/sync/prisoner-contact-restriction/{id}", contactId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncPrisonerContactRestriction::class.java)
        .returnResult().responseBody!!

      with(prisonerContactRestriction) {
        assertThat(prisonerContactRestrictionId).isEqualTo(1L)
        assertThat(contactId).isEqualTo(1L)
        assertThat(restrictionType).isEqualTo("PREINF")
        assertThat(startDate).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(expiryDate).isEqualTo(LocalDate.of(2024, 12, 31))
        assertThat(comments).isEqualTo("Restriction due to ongoing investigation")
        assertThat(createdBy).isEqualTo("admin")
        assertThat(createdTime).isEqualTo(LocalDateTime.of(2024, 10, 1, 12, 0, 0))
        assertThat(updatedBy).isEqualTo("editor")
        assertThat(updatedTime).isEqualTo(LocalDateTime.of(2024, 10, 2, 15, 30, 0))
      }
    }

    @Test
    fun `should create a new prisoner contact restriction`() {
      val prisonerContactRestriction = webTestClient.post()
        .uri("/sync/prisoner-contact-restriction")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(createPrisonerContactRestrictionRequest())
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncPrisonerContactRestriction::class.java)
        .returnResult().responseBody!!

      // The created is returned
      with(prisonerContactRestriction) {
        assertThat(prisonerContactRestrictionId).isGreaterThan(0)
        assertThat(contactId).isEqualTo(1L)
        assertThat(restrictionType).isEqualTo("PREINF")
        assertThat(startDate).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(expiryDate).isEqualTo(LocalDate.of(2024, 12, 31))
        assertThat(comments).isEqualTo("Restriction due to ongoing investigation")
        assertThat(createdBy).isEqualTo("admin")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isNull()
        assertThat(updatedTime).isNull()
      }
    }

    @Test
    fun `should create and then update a prisoner contact restriction`() {
      val prisonerContactRestriction = webTestClient.post()
        .uri("/sync/prisoner-contact-restriction")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(createPrisonerContactRestrictionRequest())
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncPrisonerContactRestriction::class.java)
        .returnResult().responseBody!!

      with(prisonerContactRestriction) {
        assertThat(prisonerContactRestrictionId).isGreaterThan(3L)
        assertThat(contactId).isEqualTo(1L)
        assertThat(restrictionType).isEqualTo("PREINF")
        assertThat(startDate).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(expiryDate).isEqualTo(LocalDate.of(2024, 12, 31))
        assertThat(comments).isEqualTo("Restriction due to ongoing investigation")
        assertThat(createdBy).isEqualTo("admin")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isNull()
        assertThat(updatedTime).isNull()
      }

      val updatedPrisonerContactRestriction = webTestClient.put()
        .uri("/sync/prisoner-contact-restriction/{id}", prisonerContactRestriction.prisonerContactRestrictionId)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(updatePrisonerContactRestrictionRequest())
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncPrisonerContactRestriction::class.java)
        .returnResult().responseBody!!

      // Check the updated copy
      with(updatedPrisonerContactRestriction) {
        assertThat(prisonerContactRestrictionId).isGreaterThan(3L)
        assertThat(contactId).isEqualTo(1L)
        assertThat(restrictionType).isEqualTo("PREINF")
        assertThat(startDate).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(expiryDate).isEqualTo(LocalDate.of(2024, 12, 31))
        assertThat(comments).isEqualTo("Restriction due to ongoing investigation")
        assertThat(createdBy).isEqualTo("admin")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isEqualTo("UpdatedUser")
        assertThat(updatedTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }
    }

    @Test
    fun `should delete an existing prisoner contact restriction`() {
      val prisonerContactRestriction = webTestClient.post()
        .uri("/sync/prisoner-contact-restriction")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(createPrisonerContactRestrictionRequest())
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncPrisonerContactRestriction::class.java)
        .returnResult().responseBody!!

      webTestClient.delete()
        .uri("/sync/prisoner-contact-restriction/{id}", prisonerContactRestriction.prisonerContactRestrictionId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/sync/prisoner-contact-restriction/{id}", prisonerContactRestriction.prisonerContactRestrictionId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    private fun updatePrisonerContactRestrictionRequest() =
      SyncUpdatePrisonerContactRestrictionRequest(
        contactId = 1L,
        restrictionType = "PREINF",
        startDate = LocalDate.of(2024, 1, 1),
        expiryDate = LocalDate.of(2024, 12, 31),
        comments = "Restriction due to ongoing investigation",
        staffUsername = "UpdatedUser",
        updatedBy = "UpdatedUser",
        updatedTime = LocalDateTime.now(),
      )

    private fun createPrisonerContactRestrictionRequest() =
      SyncCreatePrisonerContactRestrictionRequest(
        contactId = 1L,
        restrictionType = "PREINF",
        startDate = LocalDate.of(2024, 1, 1),
        expiryDate = LocalDate.of(2024, 12, 31),
        comments = "Restriction due to ongoing investigation",
        staffUsername = "admin",
        createdBy = "admin",
        createdTime = LocalDateTime.now(),
      )
  }
}
