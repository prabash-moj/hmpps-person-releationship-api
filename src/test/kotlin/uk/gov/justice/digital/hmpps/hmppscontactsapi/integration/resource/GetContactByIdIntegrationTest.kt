package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Contact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import java.time.LocalDate

class GetContactByIdIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var contactRepository: ContactRepository

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.get()
      .uri("/contact/123456")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.get()
      .uri("/contact/123456")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    webTestClient.get()
      .uri("/contact/123456")
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return 404 if the contact doesn't exist`() {
    webTestClient.get()
      .uri("/contact/123456")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `should get the contact with all fields`() {
    val contact = webTestClient.get()
      .uri("/contact/1")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(Contact::class.java)
      .returnResult().responseBody!!

    with(contact) {
      assertThat(id).isEqualTo(1)
      assertThat(title).isEqualTo("Mr")
      assertThat(lastName).isEqualTo("Last")
      assertThat(firstName).isEqualTo("Jack")
      assertThat(middleName).isEqualTo("Middle")
      assertThat(dateOfBirth).isEqualTo(LocalDate.of(2000, 11, 21))
      assertThat(createdBy).isEqualTo("TIM")
      assertThat(createdTime).isNotNull()
    }
  }
}
