package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Contact
import java.time.LocalDate

class CreateContactIntegrationTest : IntegrationTestBase() {

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.post()
      .uri("/contact")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalCreateContactRequest())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.post()
      .uri("/contact")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalCreateContactRequest())
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    webTestClient.post()
      .uri("/contact")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalCreateContactRequest())
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return bad request if required fields are null`() {
    webTestClient.post()
      .uri("/contact")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .bodyValue("{}")
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @Test
  fun `should create the contact with minimal fields`() {
    val request = aMinimalCreateContactRequest()

    val contactReturnedOnCreate = testAPIClient.createAContact(request)

    assertContactsAreEqualExcludingTimestamps(contactReturnedOnCreate, request)
    assertThat(contactReturnedOnCreate).isEqualTo(testAPIClient.getContact(contactReturnedOnCreate.id))
  }

  @Test
  fun `should create the contact with all fields`() {
    val request = CreateContactRequest(
      title = "mr",
      lastName = "last",
      firstName = "first",
      middleName = "middle",
      dateOfBirth = LocalDate.of(1982, 6, 15),
      createdBy = "created",
    )

    val contact = testAPIClient.createAContact(request)

    assertContactsAreEqualExcludingTimestamps(contact, request)
  }

  private fun assertContactsAreEqualExcludingTimestamps(contact: Contact, request: CreateContactRequest) {
    with(contact) {
      assertThat(title).isEqualTo(request.title)
      assertThat(lastName).isEqualTo(request.lastName)
      assertThat(firstName).isEqualTo(request.firstName)
      assertThat(middleName).isEqualTo(request.middleName)
      assertThat(dateOfBirth).isEqualTo(request.dateOfBirth)
      assertThat(createdBy).isEqualTo(request.createdBy)
    }
  }

  private fun aMinimalCreateContactRequest() = CreateContactRequest(
    lastName = "last",
    firstName = "first",
    createdBy = "created",
  )
}
