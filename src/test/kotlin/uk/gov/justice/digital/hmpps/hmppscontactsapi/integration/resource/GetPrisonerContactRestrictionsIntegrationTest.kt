package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactRelationship
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import java.time.LocalDate
import java.time.LocalDateTime

class GetPrisonerContactRestrictionsIntegrationTest : H2IntegrationTestBase() {

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.get()
      .uri("/prisoner-contact/1/restrictions")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.get()
      .uri("/prisoner-contact/1/restrictions")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    webTestClient.get()
      .uri("/prisoner-contact/1/restrictions")
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return not found if the prisoner contact relationship is not found`() {
    webTestClient.get()
      .uri("/prisoner-contact/-1/restrictions")
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `should return all relationship and estate wide restrictions for a contact`() {
    val restrictions = testAPIClient.getPrisonerContactRestrictions(10)

    assertThat(restrictions.contactEstateWideRestrictions).hasSize(2)
    with(restrictions.contactEstateWideRestrictions[0]) {
      assertThat(contactRestrictionId).isNotNull()
      assertThat(contactId).isEqualTo(3)
      assertThat(restrictionType).isEqualTo("CCTV")
      assertThat(restrictionTypeDescription).isEqualTo("CCTV")
      assertThat(startDate).isEqualTo(LocalDate.of(2000, 11, 21))
      assertThat(expiryDate).isEqualTo(LocalDate.of(2001, 11, 21))
      assertThat(comments).isEqualTo("N/A")
    }
    with(restrictions.contactEstateWideRestrictions[1]) {
      assertThat(contactRestrictionId).isNotNull()
      assertThat(contactId).isEqualTo(3)
      assertThat(restrictionType).isEqualTo("BAN")
      assertThat(restrictionTypeDescription).isEqualTo("Banned")
      assertThat(startDate).isNull()
      assertThat(expiryDate).isNull()
      assertThat(comments).isNull()
    }

    assertThat(restrictions.prisonerContactRestrictions).hasSize(2)
    with(restrictions.prisonerContactRestrictions[0]) {
      assertThat(prisonerContactRestrictionId).isNotNull()
      assertThat(contactId).isEqualTo(3L)
      assertThat(prisonerNumber).isEqualTo("G4793VF")
      assertThat(restrictionType).isEqualTo("PREINF")
      assertThat(restrictionTypeDescription).isEqualTo("Previous Info")
      assertThat(startDate).isEqualTo(LocalDate.of(2024, 1, 1))
      assertThat(expiryDate).isEqualTo(LocalDate.of(2024, 12, 31))
      assertThat(comments).isEqualTo("Restriction due to ongoing investigation")
      assertThat(createdBy).isEqualTo("admin")
      assertThat(createdTime).isEqualTo(LocalDateTime.of(2024, 10, 1, 12, 0, 0))
      assertThat(updatedBy).isEqualTo("editor")
      assertThat(updatedTime).isEqualTo(LocalDateTime.of(2024, 10, 2, 15, 30, 0))
    }
    with(restrictions.prisonerContactRestrictions[1]) {
      assertThat(prisonerContactRestrictionId).isNotNull()
      assertThat(contactId).isEqualTo(3L)
      assertThat(prisonerNumber).isEqualTo("G4793VF")
      assertThat(restrictionType).isEqualTo("BAN")
      assertThat(restrictionTypeDescription).isEqualTo("Banned")
      assertThat(startDate).isNull()
      assertThat(expiryDate).isNull()
      assertThat(comments).isNull()
      assertThat(createdBy).isEqualTo("officer")
      assertThat(createdTime).isEqualTo(LocalDateTime.of(2022, 8, 14, 11, 0, 0))
      assertThat(updatedBy).isNull()
      assertThat(updatedTime).isNull()
    }
  }

  @Test
  fun `should return empty list if no restrictions for a contact`() {
    val prisonerNumber = "G4793VF"
    stubPrisonSearchWithResponse(prisonerNumber)
    val created = testAPIClient.createAContactWithARelationship(
      CreateContactRequest(
        firstName = "First",
        lastName = "Last",
        createdBy = "USER1",
        relationship = ContactRelationship(
          prisonerNumber = prisonerNumber,
          relationshipCode = "FRI",
          isNextOfKin = false,
          isEmergencyContact = false,
          comments = null,
        ),
      ),
    )
    val restrictions = testAPIClient.getPrisonerContactRestrictions(created.createdRelationship!!.prisonerContactId)
    assertThat(restrictions.prisonerContactRestrictions).isEmpty()
    assertThat(restrictions.contactEstateWideRestrictions).isEmpty()
  }
}
