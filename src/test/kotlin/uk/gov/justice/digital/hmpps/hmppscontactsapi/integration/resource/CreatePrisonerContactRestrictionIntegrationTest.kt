package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.manage.users.User
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.PostgresIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactRelationship
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.PersonReference
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.PrisonerContactRestrictionInfo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.Source
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.LocalDate

class CreatePrisonerContactRestrictionIntegrationTest : PostgresIntegrationTestBase() {
  private var savedPrisonerContactId = 0L
  private var savedContactId = 0L
  private val prisonerNumberCreatedAgainst = "A1234AA"

  @BeforeEach
  fun initialiseData() {
    stubPrisonSearchWithResponse(prisonerNumberCreatedAgainst)
    val created = testAPIClient.createAContactWithARelationship(
      CreateContactRequest(
        lastName = "last",
        firstName = "first",
        relationship = ContactRelationship(
          prisonerNumber = prisonerNumberCreatedAgainst,
          relationshipType = "S",
          relationshipToPrisoner = "FRI",
          isNextOfKin = true,
          isEmergencyContact = true,
          comments = "Some comments",
        ),
        createdBy = "created",
      ),
    )
    savedPrisonerContactId = created.createdRelationship!!.prisonerContactId
    savedContactId = created.createdContact.id
  }

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.post()
      .uri("/prisoner-contact/$savedPrisonerContactId/restriction")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalRequest())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.post()
      .uri("/prisoner-contact/$savedPrisonerContactId/restriction")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalRequest())
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    webTestClient.post()
      .uri("/prisoner-contact/$savedPrisonerContactId/restriction")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalRequest())
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "restrictionType must not be null;{\"restrictionType\": null, \"startDate\": \"2020-01-01\", \"createdBy\": \"created\"}",
      "restrictionType must not be null;{\"startDate\": \"2020-01-01\", \"createdBy\": \"created\"}",
      "startDate must not be null;{\"restrictionType\": \"BAN\", \"startDate\": null, \"createdBy\": \"created\"}",
      "startDate must not be null;{\"restrictionType\": \"BAN\", \"createdBy\": \"created\"}",
      "createdBy must not be null;{\"restrictionType\": \"BAN\", \"startDate\": \"2020-01-01\", \"createdBy\": null}",
      "createdBy must not be null;{\"restrictionType\": \"BAN\", \"startDate\": \"2020-01-01\"}",
    ],
    delimiter = ';',
  )
  fun `should return bad request if required fields are null`(expectedMessage: String, json: String) {
    val errors = webTestClient.post()
      .uri("/prisoner-contact/$savedPrisonerContactId/restriction")
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
    stubEvents.assertHasNoEvents(event = OutboundEvent.PRISONER_CONTACT_RESTRICTION_CREATED)
  }

  @ParameterizedTest
  @MethodSource("allFieldConstraintViolations")
  fun `should enforce field constraints`(expectedMessage: String, request: CreatePrisonerContactRestrictionRequest) {
    val errors = webTestClient.post()
      .uri("/prisoner-contact/$savedPrisonerContactId/restriction")
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

    assertThat(errors.userMessage).isEqualTo("Validation failure(s): $expectedMessage")
    stubEvents.assertHasNoEvents(event = OutboundEvent.PRISONER_CONTACT_RESTRICTION_CREATED)
  }

  @Test
  fun `should not create the restriction if the prisoner contact is not found`() {
    val request = aMinimalRequest()

    val errors = webTestClient.post()
      .uri("/prisoner-contact/-321/restriction")
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

    assertThat(errors.userMessage).isEqualTo("Entity not found : Prisoner contact (-321) could not be found")
    stubEvents.assertHasNoEvents(event = OutboundEvent.PRISONER_CONTACT_RESTRICTION_CREATED)
  }

  @Test
  fun `should not create the restriction if the type is not valid`() {
    val request = aMinimalRequest().copy(restrictionType = "FOO")

    val errors = webTestClient.post()
      .uri("/prisoner-contact/$savedPrisonerContactId/restriction")
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

    assertThat(errors.userMessage).isEqualTo("Validation failure: Unsupported restriction type (FOO)")
    stubEvents.assertHasNoEvents(event = OutboundEvent.PRISONER_CONTACT_RESTRICTION_CREATED)
  }

  @Test
  fun `should create the restriction with minimal fields`() {
    stubGetUserByUsername(User("created", "Created User"))
    val request = CreatePrisonerContactRestrictionRequest(
      restrictionType = "BAN",
      startDate = LocalDate.of(2020, 1, 1),
      expiryDate = null,
      comments = null,
      createdBy = "created",
    )

    val created = testAPIClient.createPrisonerContactRestriction(savedPrisonerContactId, request)

    with(created) {
      assertThat(prisonerContactRestrictionId).isGreaterThan(0)
      assertThat(prisonerContactId).isEqualTo(savedPrisonerContactId)
      assertThat(contactId).isEqualTo(savedContactId)
      assertThat(prisonerNumber).isEqualTo(prisonerNumberCreatedAgainst)
      assertThat(restrictionType).isEqualTo(request.restrictionType)
      assertThat(startDate).isEqualTo(request.startDate)
      assertThat(expiryDate).isNull()
      assertThat(comments).isNull()
      assertThat(enteredByUsername).isEqualTo("created")
      assertThat(enteredByDisplayName).isEqualTo("Created User")
      assertThat(createdBy).isEqualTo(request.createdBy)
      assertThat(createdTime).isNotNull()
    }

    stubEvents.assertHasEvent(
      event = OutboundEvent.PRISONER_CONTACT_RESTRICTION_CREATED,
      additionalInfo = PrisonerContactRestrictionInfo(created.prisonerContactRestrictionId, Source.DPS),
      personReference = PersonReference(dpsContactId = savedContactId, nomsNumber = prisonerNumberCreatedAgainst),
    )
  }

  @ParameterizedTest
  @ValueSource(strings = ["ROLE_CONTACTS_ADMIN", "ROLE_CONTACTS__RW"])
  fun `should create the restriction with all fields`(role: String) {
    stubGetUserByUsername(User("created", "Created User"))
    val request = CreatePrisonerContactRestrictionRequest(
      restrictionType = "BAN",
      startDate = LocalDate.of(2020, 1, 1),
      expiryDate = LocalDate.of(2021, 2, 2),
      comments = "Some comments",
      createdBy = "created",
    )

    val created = testAPIClient.createPrisonerContactRestriction(savedPrisonerContactId, request, role)

    with(created) {
      assertThat(prisonerContactRestrictionId).isGreaterThan(0)
      assertThat(prisonerContactId).isEqualTo(savedPrisonerContactId)
      assertThat(contactId).isEqualTo(savedContactId)
      assertThat(prisonerNumber).isEqualTo(prisonerNumberCreatedAgainst)
      assertThat(restrictionType).isEqualTo(request.restrictionType)
      assertThat(startDate).isEqualTo(request.startDate)
      assertThat(expiryDate).isEqualTo(request.expiryDate)
      assertThat(comments).isEqualTo(request.comments)
      assertThat(enteredByUsername).isEqualTo("created")
      assertThat(enteredByDisplayName).isEqualTo("Created User")
      assertThat(createdBy).isEqualTo(request.createdBy)
      assertThat(createdTime).isNotNull()
    }

    stubEvents.assertHasEvent(
      event = OutboundEvent.PRISONER_CONTACT_RESTRICTION_CREATED,
      additionalInfo = PrisonerContactRestrictionInfo(created.prisonerContactRestrictionId, Source.DPS),
      personReference = PersonReference(dpsContactId = savedContactId, nomsNumber = prisonerNumberCreatedAgainst),
    )
  }

  companion object {
    @JvmStatic
    fun allFieldConstraintViolations(): List<Arguments> = listOf(
      Arguments.of("comments must be <= 240 characters", aMinimalRequest().copy(comments = "".padStart(241))),
      Arguments.of(
        "createdBy must be <= 100 characters",
        aMinimalRequest().copy(createdBy = "".padStart(101)),
      ),
    )

    private fun aMinimalRequest() = CreatePrisonerContactRestrictionRequest(
      restrictionType = "BAN",
      startDate = LocalDate.of(2020, 1, 1),
      expiryDate = null,
      comments = null,
      createdBy = "created",
    )
  }
}
