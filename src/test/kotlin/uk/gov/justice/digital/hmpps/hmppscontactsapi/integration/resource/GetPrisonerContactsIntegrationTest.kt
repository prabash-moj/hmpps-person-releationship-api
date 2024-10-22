package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.helper.TestAPIClient.PrisonerContactSummaryResponse

class GetPrisonerContactsIntegrationTest : H2IntegrationTestBase() {
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

    val contacts = webTestClient.get()
      .uri(GET_PRISONER_CONTACT)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(PrisonerContactSummaryResponse::class.java)
      .returnResult().responseBody!!

    assertThat(contacts.content).hasSize(3)

    val contact = contacts.content.first()
    assertThat(contact.lastName).isEqualTo("Last")
    assertThat(contact.cityCode).isEqualTo("25343")
    assertThat(contact.cityDescription).isEqualTo("Sheffield")
    assertThat(contact.countyCode).isEqualTo("S.YORKSHIRE")
    assertThat(contact.countyDescription).isEqualTo("South Yorkshire")
    assertThat(contact.countryCode).isEqualTo("ENG")
    assertThat(contact.countryDescription).isEqualTo("England")

    val minimal = contacts.content.find { it.firstName == "Minimal" } ?: fail("Couldn't find 'Minimal' contact")
    assertThat(minimal.firstName).isEqualTo("Minimal")
    assertThat(minimal.cityCode).isEqualTo("")
    assertThat(minimal.cityDescription).isEqualTo("")
    assertThat(minimal.countyCode).isEqualTo("")
    assertThat(minimal.countyDescription).isEqualTo("")
    assertThat(minimal.countryCode).isEqualTo("")
    assertThat(minimal.countryDescription).isEqualTo("")
  }

  @Test
  fun `should return phone numbers with latest first`() {
    stubPrisonSearchWithResponse("A1234BB")

    val contacts = webTestClient.get()
      .uri("/prisoner/A1234BB/contact")
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(PrisonerContactSummaryResponse::class.java)
      .returnResult().responseBody!!

    assertThat(contacts.content).hasSize(1)

    val contact = contacts.content.first()
    assertThat(contact.contactId).isEqualTo(1)
    assertThat(contact.phoneType).isEqualTo("HOME")
    assertThat(contact.phoneTypeDescription).isEqualTo("Home phone")
    assertThat(contact.phoneNumber).isEqualTo("01111 777777")
    assertThat(contact.extNumber).isEqualTo("+0123")
  }

  @Test
  fun `should return results for the correct page`() {
    stubPrisonSearchWithResponse("A4385DZ")

    val firstPage = webTestClient.get()
      .uri("$GET_PRISONER_CONTACT?size=2&page=0")
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(PrisonerContactSummaryResponse::class.java)
      .returnResult().responseBody!!

    assertThat(firstPage.content).hasSize(2)
    assertThat(firstPage.totalPages).isEqualTo(2)
    assertThat(firstPage.totalElements).isEqualTo(3)
    assertThat(firstPage.number).isEqualTo(0)

    assertThat(firstPage.content[0].contactId).isEqualTo(1)
    assertThat(firstPage.content[1].contactId).isEqualTo(10)

    val contacts = webTestClient.get()
      .uri("$GET_PRISONER_CONTACT?size=2&page=1")
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(PrisonerContactSummaryResponse::class.java)
      .returnResult().responseBody!!

    assertThat(contacts.content).hasSize(1)
    assertThat(contacts.totalPages).isEqualTo(2)
    assertThat(contacts.totalElements).isEqualTo(3)
    assertThat(contacts.number).isEqualTo(1)

    assertThat(contacts.content[0].contactId).isEqualTo(18)
  }

  @Test
  fun `should return sorted correctly`() {
    stubPrisonSearchWithResponse("A4385DZ")

    val firstPage = webTestClient.get()
      .uri("$GET_PRISONER_CONTACT?sort=lastName")
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(PrisonerContactSummaryResponse::class.java)
      .returnResult().responseBody!!

    assertThat(firstPage.content).hasSize(3)
    assertThat(firstPage.totalPages).isEqualTo(1)
    assertThat(firstPage.totalElements).isEqualTo(3)
    assertThat(firstPage.number).isEqualTo(0)

    assertThat(firstPage.content[0].lastName).isEqualTo("Address")
    assertThat(firstPage.content[1].lastName).isEqualTo("Last")
    assertThat(firstPage.content[2].lastName).isEqualTo("Ten")
  }
}
