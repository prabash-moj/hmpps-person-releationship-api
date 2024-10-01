package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactSummary

class GetPrisonerContactsIntegrationTest : IntegrationTestBase() {
  companion object {
    private const val GET_PRISONER_CONTACT = "/prisoner/A4385DZ/contact"
  }

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.get()
      .uri("/prisoner-contacts/prisoner/P001")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.get()
      .uri("/prisoner/P001/contact")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    webTestClient.get()
      .uri("/prisoner/P001/contact")
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return not found if no prisoner found`() {
    stubPrisonSearchWithNotFoundResponse("A4385DZ")

    webTestClient.get()
      .uri("/prisoner/A4385DZ/contact")
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `should return OK`() {
    stubPrisonSearchWithResponse("A4385DZ")

    val contacts = webTestClient.getContacts(GET_PRISONER_CONTACT)

    assertThat(contacts).hasSize(3)

    val contact = contacts.first()
    assertThat(contact.surname).isEqualTo("Last")
    assertThat(contact.cityCode).isEqualTo("25343")
    assertThat(contact.cityDescription).isEqualTo("Sheffield")
    assertThat(contact.countyCode).isEqualTo("S.YORKSHIRE")
    assertThat(contact.countyDescription).isEqualTo("South Yorkshire")
    assertThat(contact.countryCode).isEqualTo("ENG")
    assertThat(contact.countryDescription).isEqualTo("England")

    val minimal = contacts.find { it.surname == "Minimal" } ?: fail("Couldn't find 'Minimal' contact")
    assertThat(minimal.surname).isEqualTo("Minimal")
    assertThat(minimal.cityCode).isEqualTo("")
    assertThat(minimal.cityDescription).isEqualTo("")
    assertThat(minimal.countyCode).isEqualTo("")
    assertThat(minimal.countyDescription).isEqualTo("")
    assertThat(minimal.countryCode).isEqualTo("")
    assertThat(minimal.countryDescription).isEqualTo("")
  }

  private fun WebTestClient.getContacts(URL: String): MutableList<PrisonerContactSummary> =
    get()
      .uri(URL)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(PrisonerContactSummary::class.java)
      .returnResult().responseBody!!
}
