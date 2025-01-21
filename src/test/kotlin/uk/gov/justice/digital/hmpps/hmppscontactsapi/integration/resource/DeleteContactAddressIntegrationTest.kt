package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.PostgresIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.ContactAddressInfo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.PersonReference
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.Source
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

class DeleteContactAddressIntegrationTest : PostgresIntegrationTestBase() {
  private var savedContactId = 0L
  private var savedContactAddressId = 0L

  @BeforeEach
  fun initialiseData() {
    savedContactId = testAPIClient.createAContact(
      CreateContactRequest(
        lastName = "address",
        firstName = "has",
        createdBy = "created",
      ),
    ).id

    savedContactAddressId = testAPIClient.createAContactAddress(
      savedContactId,
      CreateContactAddressRequest(
        primaryAddress = false,
        addressType = "HOME",
        flat = "1A",
        property = "27",
        street = "Acacia Avenue",
        area = "Hoggs Bottom",
        postcode = "HB10 2NB",
        createdBy = "created",
      ),
    ).contactAddressId
  }

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.delete()
      .uri("/contact/$savedContactId/address/$savedContactAddressId")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.delete()
      .uri("/contact/$savedContactId/address/$savedContactAddressId")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    webTestClient.delete()
      .uri("/contact/$savedContactId/address/$savedContactAddressId")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should not delete the address if the contact is not found`() {
    val errors = webTestClient.delete()
      .uri("/contact/-321/address/$savedContactAddressId")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isNotFound
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody!!

    assertThat(errors.userMessage).isEqualTo("Entity not found : Contact (-321) not found")

    stubEvents.assertHasNoEvents(
      event = OutboundEvent.CONTACT_ADDRESS_DELETED,
      additionalInfo = ContactAddressInfo(savedContactAddressId, Source.DPS),
    )
  }

  @Test
  fun `should not update the address if the address is not found`() {
    val errors = webTestClient.delete()
      .uri("/contact/$savedContactId/address/-99")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isNotFound
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody!!

    assertThat(errors.userMessage).isEqualTo("Entity not found : Contact address (-99) not found")

    stubEvents.assertHasNoEvents(
      event = OutboundEvent.CONTACT_ADDRESS_DELETED,
      additionalInfo = ContactAddressInfo(-99, Source.DPS),
    )
  }

  @ParameterizedTest
  @ValueSource(strings = ["ROLE_CONTACTS_ADMIN", "ROLE_CONTACTS__RW"])
  fun `should delete the contact address`(role: String) {
    webTestClient.delete()
      .uri("/contact/$savedContactId/address/$savedContactAddressId")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf(role)))
      .exchange()
      .expectStatus()
      .isNoContent

    webTestClient.get()
      .uri("/contact/$savedContactId/address/$savedContactAddressId")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isNotFound

    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_ADDRESS_DELETED,
      additionalInfo = ContactAddressInfo(savedContactAddressId, Source.DPS),
      personReference = PersonReference(dpsContactId = savedContactId),
    )
  }
}
