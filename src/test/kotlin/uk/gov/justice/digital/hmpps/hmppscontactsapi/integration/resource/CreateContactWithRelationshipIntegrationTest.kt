package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearchapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactRelationship
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRelationshipDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.ContactInfo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.PersonReference
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.PrisonerContactInfo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.Source

class CreateContactWithRelationshipIntegrationTest : H2IntegrationTestBase() {

  @Autowired
  protected lateinit var contactRepository: ContactRepository

  @ParameterizedTest
  @CsvSource(
    value = [
      "relationship.prisonerNumber must not be null;{ \"relationshipCode\": \"FRI\", \"isNextOfKin\": false, \"isEmergencyContact\": false }",
      "relationship.relationshipCode must not be null;{ \"prisonerNumber\": \"A1234BC\", \"isNextOfKin\": false, \"isEmergencyContact\": false }",
      "relationship.isNextOfKin is invalid;{ \"prisonerNumber\": \"A1234BC\", \"relationshipCode\": \"FRI\", \"isEmergencyContact\": false }",
      "relationship.isEmergencyContact is invalid;{ \"prisonerNumber\": \"A1234BC\", \"relationshipCode\": \"FRI\", \"isNextOfKin\": false }",
    ],
    delimiter = ';',
  )
  fun `should return bad request if required fields are null`(expectedMessage: String, relationShipJson: String) {
    val json =
      "{\"firstName\": \"first\", \"lastName\": \"last\", \"createdBy\": \"created\", \"relationship\": $relationShipJson}"
    val errors = webTestClient.post()
      .uri("/contact")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .bodyValue(json)
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody!!

    assertThat(errors.userMessage).isEqualTo("Validation failure: $expectedMessage")
  }

  @Test
  fun `should return a 404 if the prisoner can not be found`() {
    val prisonerNumber = "A1234AB"
    stubPrisonSearchWithNotFoundResponse(prisonerNumber)
    val request = CreateContactRequest(
      lastName = RandomStringUtils.random(35),
      firstName = "a new guy",
      createdBy = "created",
      relationship = ContactRelationship(
        prisonerNumber = prisonerNumber,
        relationshipCode = "FRI",
        isNextOfKin = false,
        isEmergencyContact = false,
        comments = null,
      ),
    )

    val errors = webTestClient.post()
      .uri("/contact")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isNotFound
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody!!

    assertThat(errors.userMessage).isEqualTo("Entity not found : Prisoner (A1234AB) could not be found")
  }

  @Test
  fun `should reject the contact relationship if it's not a supported relationship type`() {
    val prisonerNumber = "A1234AB"
    stubPrisonSearchWithResponse(prisonerNumber)
    val requestedRelationship = ContactRelationship(
      prisonerNumber = prisonerNumber,
      relationshipCode = "FOO",
      isNextOfKin = false,
      isEmergencyContact = false,
      comments = null,
    )
    val request = CreateContactRequest(
      lastName = RandomStringUtils.random(35),
      firstName = "a new guy",
      createdBy = "created",
      relationship = requestedRelationship,
    )

    val errors = webTestClient.post()
      .uri("/contact")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody!!

    assertThat(errors.userMessage).isEqualTo("Validation failure: Reference code with groupCode RELATIONSHIP and code 'FOO' not found.")
    stubEvents.assertHasNoEvents(event = OutboundEvent.CONTACT_CREATED)
    stubEvents.assertHasNoEvents(event = OutboundEvent.PRISONER_CONTACT_CREATED)

    // Check that we do not create a contact without the relationship
    val createdContact = contactRepository.findAll().find { it.lastName == request.lastName }
    assertThat(createdContact).isNull()
  }

  @Test
  fun `should create the contact relationship with minimal fields`() {
    val prisonerNumber = "A1234AB"
    stubPrisonSearchWithResponse(prisonerNumber)
    val requestedRelationship = ContactRelationship(
      prisonerNumber = prisonerNumber,
      relationshipCode = "FRI",
      isNextOfKin = false,
      isEmergencyContact = false,
      comments = null,
    )
    val request = CreateContactRequest(
      lastName = RandomStringUtils.random(35),
      firstName = "a new guy",
      createdBy = "created",
      relationship = requestedRelationship,
    )

    val created = testAPIClient.createAContactWithARelationship(request)

    assertThat(created.createdContact.lastName).isEqualTo(request.lastName)
    asserPrisonerContactEquals(created.createdRelationship!!, requestedRelationship)

    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_CREATED,
      additionalInfo = ContactInfo(created.createdContact.id, Source.DPS),
      personReference = PersonReference(dpsContactId = created.createdContact.id),
    )

    stubEvents.assertHasEvent(
      event = OutboundEvent.PRISONER_CONTACT_CREATED,
      additionalInfo = PrisonerContactInfo(created.createdRelationship!!.prisonerContactId, Source.DPS),
      personReference = PersonReference(dpsContactId = created.createdContact.id, nomsNumber = request.relationship!!.prisonerNumber),
    )
  }

  @Test
  fun `should create the contact relationship with all fields`() {
    val prisonerNumber = "A1234AA"
    stubPrisonSearchWithResponse(prisonerNumber)
    val requestedRelationship = ContactRelationship(
      prisonerNumber = prisonerNumber,
      relationshipCode = "FRI",
      isNextOfKin = true,
      isEmergencyContact = true,
      comments = "Some comments",
    )
    val request = CreateContactRequest(
      lastName = RandomStringUtils.random(35),
      firstName = "a new guy",
      createdBy = "created",
      relationship = requestedRelationship,
    )

    val created = testAPIClient.createAContactWithARelationship(request)

    assertThat(created.createdContact.lastName).isEqualTo(request.lastName)
    asserPrisonerContactEquals(created.createdRelationship!!, requestedRelationship)

    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_CREATED,
      additionalInfo = ContactInfo(created.createdContact.id, Source.DPS),
      personReference = PersonReference(dpsContactId = created.createdContact.id),
    )

    stubEvents.assertHasEvent(
      event = OutboundEvent.PRISONER_CONTACT_CREATED,
      additionalInfo = PrisonerContactInfo(created.createdRelationship!!.prisonerContactId, Source.DPS),
      personReference = PersonReference(dpsContactId = created.createdContact.id, nomsNumber = request.relationship!!.prisonerNumber),
    )
  }

  private fun asserPrisonerContactEquals(
    prisonerContact: PrisonerContactRelationshipDetails,
    relationship: ContactRelationship,
  ) {
    with(prisonerContact) {
      assertThat(relationshipCode).isEqualTo(relationship.relationshipCode)
      assertThat(nextOfKin).isEqualTo(relationship.isNextOfKin)
      assertThat(emergencyContact).isEqualTo(relationship.isEmergencyContact)
      assertThat(comments).isEqualTo(relationship.comments)
    }
  }
}
