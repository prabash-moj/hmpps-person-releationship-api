package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearchapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.ContactEmailInfo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent

class DeleteContactEmailIntegrationTest : H2IntegrationTestBase() {
  private var savedContactId = 0L
  private var savedContactEmailId = 0L

  @BeforeEach
  fun initialiseData() {
    savedContactId = testAPIClient.createAContact(
      CreateContactRequest(
        lastName = "email",
        firstName = "has",
        createdBy = "created",
      ),
    ).id
    savedContactEmailId = testAPIClient.createAContactEmail(
      savedContactId,
      CreateEmailRequest(
        emailAddress = "test@example.com",
        createdBy = "created",
      ),
    ).contactEmailId
  }

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.delete()
      .uri("/contact/$savedContactId/email/$savedContactEmailId")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.delete()
      .uri("/contact/$savedContactId/email/$savedContactEmailId")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    webTestClient.delete()
      .uri("/contact/$savedContactId/email/$savedContactEmailId")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should not delete the email if the contact is not found`() {
    val errors = webTestClient.delete()
      .uri("/contact/-321/email/$savedContactEmailId")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isNotFound
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody!!

    assertThat(errors.userMessage).isEqualTo("Entity not found : Contact (-321) not found")
    stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_EMAIL_DELETED, ContactEmailInfo(savedContactEmailId))
  }

  @Test
  fun `should not update the email if the email is not found`() {
    val errors = webTestClient.delete()
      .uri("/contact/$savedContactId/email/-99")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isNotFound
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody!!

    assertThat(errors.userMessage).isEqualTo("Entity not found : Contact email (-99) not found")
    stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_EMAIL_DELETED, ContactEmailInfo(-99))
  }

  @Test
  fun `should delete the contacts email`() {
    webTestClient.delete()
      .uri("/contact/$savedContactId/email/$savedContactEmailId")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isNoContent

    webTestClient.get()
      .uri("/contact/$savedContactId/email/$savedContactEmailId")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isNotFound

    stubEvents.assertHasEvent(OutboundEvent.CONTACT_EMAIL_DELETED, ContactEmailInfo(savedContactEmailId))
  }
}
