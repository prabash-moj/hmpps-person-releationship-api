package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.prisoner
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.PostgresIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.AddContactRelationshipRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactRelationship
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.LinkedPrisonerDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.LinkedPrisonerRelationshipDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRelationshipDetails

class GetContactLinkedPrisonerIntegrationTest : PostgresIntegrationTestBase() {
  private var savedContactId = 0L

  val prisoner1 = prisoner(
    prisonerNumber = "A1234BC",
    prisonId = "MDI",
    firstName = "Joe",
    middleNames = "Middle",
    lastName = "Bloggs",
  )

  val prisoner2 = prisoner(
    prisonerNumber = "X9876YZ",
    prisonId = "BXI",
    firstName = "Barney",
    middleNames = null,
    lastName = "Rubble",
  )

  @BeforeEach
  fun initialiseData() {
    savedContactId = testAPIClient.createAContact(
      CreateContactRequest(
        lastName = "Contact",
        firstName = "Linked",
        createdBy = "created",
      ),
    ).id
  }

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.get()
      .uri("/contact/1/linked-prisoners")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.get()
      .uri("/contact/1/linked-prisoners")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    webTestClient.get()
      .uri("/contact/1/linked-prisoners")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should get linked prisoners`() {
    stubPrisonerSearch(prisoner1)
    stubPrisonerSearch(prisoner2)

    val prisoner1FriendRelationship = addRelationship(prisoner1, "FRI")
    val prisoner1OtherRelationship = addRelationship(prisoner1, "OTHER")
    val prisoner2FatherRelationship = addRelationship(prisoner2, "FA")

    val linkedPrisoners = testAPIClient.getLinkedPrisoners(savedContactId)
    assertThat(linkedPrisoners).isEqualTo(
      listOf(
        LinkedPrisonerDetails(
          prisonerNumber = prisoner2.prisonerNumber,
          firstName = prisoner2.firstName,
          middleNames = prisoner2.middleNames,
          lastName = prisoner2.lastName,
          relationships = listOf(
            LinkedPrisonerRelationshipDetails(
              prisonerContactId = prisoner2FatherRelationship.prisonerContactId,
              relationshipType = "S",
              relationshipTypeDescription = "Social/Family",
              relationshipToPrisoner = "FA",
              relationshipToPrisonerDescription = "Father",
            ),
          ),
        ),
        LinkedPrisonerDetails(
          prisonerNumber = prisoner1.prisonerNumber,
          firstName = prisoner1.firstName,
          middleNames = prisoner1.middleNames,
          lastName = prisoner1.lastName,
          relationships = listOf(
            LinkedPrisonerRelationshipDetails(
              prisonerContactId = prisoner1OtherRelationship.prisonerContactId,
              relationshipType = "S",
              relationshipTypeDescription = "Social/Family",
              relationshipToPrisoner = "OTHER",
              relationshipToPrisonerDescription = "Other - Social",
            ),
            LinkedPrisonerRelationshipDetails(
              prisonerContactId = prisoner1FriendRelationship.prisonerContactId,
              relationshipType = "S",
              relationshipTypeDescription = "Social/Family",
              relationshipToPrisoner = "FRI",
              relationshipToPrisonerDescription = "Friend",
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `should return other linked prisoners even if one is missing in prisoner search`() {
    stubPrisonerSearch(prisoner1)
    stubPrisonerSearch(prisoner2)

    val prisoner1OtherRelationship = addRelationship(prisoner1, "OTHER")
    addRelationship(prisoner2, "FA")

    stubPrisonSearchWithNotFoundResponse(prisoner2.prisonerNumber)

    val linkedPrisoners = testAPIClient.getLinkedPrisoners(savedContactId)
    assertThat(linkedPrisoners).isEqualTo(
      listOf(
        LinkedPrisonerDetails(
          prisonerNumber = prisoner1.prisonerNumber,
          firstName = prisoner1.firstName,
          middleNames = prisoner1.middleNames,
          lastName = prisoner1.lastName,
          relationships = listOf(
            LinkedPrisonerRelationshipDetails(
              prisonerContactId = prisoner1OtherRelationship.prisonerContactId,
              relationshipType = "S",
              relationshipTypeDescription = "Social/Family",
              relationshipToPrisoner = "OTHER",
              relationshipToPrisonerDescription = "Other - Social",
            ),
          ),
        ),
      ),
    )
  }

  private fun addRelationship(prisoner: Prisoner, relationshipCode: String): PrisonerContactRelationshipDetails {
    return testAPIClient.addAContactRelationship(
      AddContactRelationshipRequest(
        contactId = savedContactId,
        ContactRelationship(
          prisonerNumber = prisoner.prisonerNumber,
          relationshipToPrisoner = relationshipCode,
          isNextOfKin = true,
          relationshipType = "S",
          isEmergencyContact = true,
        ),
        createdBy = "created",
      ),
    )
  }
}
