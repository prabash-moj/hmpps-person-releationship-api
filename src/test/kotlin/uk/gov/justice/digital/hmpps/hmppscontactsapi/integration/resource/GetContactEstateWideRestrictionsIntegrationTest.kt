package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import java.time.LocalDate

class GetContactEstateWideRestrictionsIntegrationTest : H2IntegrationTestBase() {

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.get()
      .uri("/contact/1/estate-wide-restrictions")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.get()
      .uri("/contact/1/estate-wide-restrictions")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    webTestClient.get()
      .uri("/contact/1/estate-wide-restrictions")
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return not found if no contact found`() {
    webTestClient.get()
      .uri("/contact/-1/estate-wide-restrictions")
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `should return all estate wide restrictions for a contact`() {
    val restrictions = testAPIClient.getContactEstateWideRestrictions(3)
    assertThat(restrictions).hasSize(2)
    with(restrictions[0]) {
      assertThat(contactRestrictionId).isNotNull()
      assertThat(contactId).isEqualTo(3)
      assertThat(restrictionType).isEqualTo("CCTV")
      assertThat(restrictionTypeDescription).isEqualTo("CCTV")
      assertThat(startDate).isEqualTo(LocalDate.of(2000, 11, 21))
      assertThat(expiryDate).isEqualTo(LocalDate.of(2001, 11, 21))
      assertThat(comments).isEqualTo("N/A")
    }
    with(restrictions[1]) {
      assertThat(contactRestrictionId).isNotNull()
      assertThat(contactId).isEqualTo(3)
      assertThat(restrictionType).isEqualTo("BAN")
      assertThat(restrictionTypeDescription).isEqualTo("Banned")
      assertThat(startDate).isNull()
      assertThat(expiryDate).isNull()
      assertThat(comments).isNull()
    }
  }

  @Test
  fun `should return empty list if no restrictions for a contact`() {
    val createdContact = testAPIClient.createAContact(CreateContactRequest(firstName = "First", lastName = "Last", createdBy = "USER1"))
    val restrictions = testAPIClient.getContactEstateWideRestrictions(createdContact.id)
    assertThat(restrictions).isEmpty()
  }
}
