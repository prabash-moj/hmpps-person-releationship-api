package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.manage.users.User
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearchapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.ContactRestrictionInfo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.PersonReference
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.Source
import java.time.LocalDate

class CreateContactRestrictionIntegrationTest : H2IntegrationTestBase() {
  private var savedContactId = 0L

  @BeforeEach
  fun initialiseData() {
    savedContactId = testAPIClient.createAContact(
      CreateContactRequest(
        lastName = "last",
        firstName = "first",
        createdBy = "created",
      ),
    ).id
    stubGetUserByUsername(User("created", "Created User"))
  }

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.post()
      .uri("/contact/$savedContactId/restriction")
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
      .uri("/contact/$savedContactId/restriction")
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
      .uri("/contact/$savedContactId/restriction")
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
      .uri("/contact/$savedContactId/restriction")
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

  @ParameterizedTest
  @MethodSource("allFieldConstraintViolations")
  fun `should enforce field constraints`(expectedMessage: String, request: CreateContactRestrictionRequest) {
    val errors = webTestClient.post()
      .uri("/contact/$savedContactId/restriction")
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
  }

  @Test
  fun `should not create the restriction if the contact is not found`() {
    val request = aMinimalRequest()

    val errors = webTestClient.post()
      .uri("/contact/-321/restriction")
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

    assertThat(errors.userMessage).isEqualTo("Entity not found : Contact (-321) could not be found")
  }

  @Test
  fun `should not create the restriction if the type is not valid`() {
    val request = aMinimalRequest().copy(restrictionType = "FOO")

    val errors = webTestClient.post()
      .uri("/contact/$savedContactId/restriction")
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
  }

  @Test
  fun `should create the restriction with minimal fields`() {
    val request = aMinimalRequest()

    val created = testAPIClient.createContactGlobalRestriction(savedContactId, request)

    with(created) {
      assertThat(contactRestrictionId).isGreaterThan(0)
      assertThat(contactId).isEqualTo(savedContactId)
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
      event = OutboundEvent.CONTACT_RESTRICTION_CREATED,
      additionalInfo = ContactRestrictionInfo(created.contactRestrictionId, Source.DPS),
      personReference = PersonReference(dpsContactId = created.contactId),
    )
  }

  @Test
  fun `should create the restriction with all fields`() {
    val request = CreateContactRestrictionRequest(
      restrictionType = "BAN",
      startDate = LocalDate.of(2020, 1, 1),
      expiryDate = LocalDate.of(2021, 2, 2),
      comments = "Some comments",
      createdBy = "created",
    )

    val created = testAPIClient.createContactGlobalRestriction(savedContactId, request)

    with(created) {
      assertThat(contactRestrictionId).isGreaterThan(0)
      assertThat(contactId).isEqualTo(savedContactId)
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
      event = OutboundEvent.CONTACT_RESTRICTION_CREATED,
      additionalInfo = ContactRestrictionInfo(created.contactRestrictionId, Source.DPS),
      personReference = PersonReference(dpsContactId = created.contactId),
    )
  }

  companion object {
    @JvmStatic
    fun allFieldConstraintViolations(): List<Arguments> {
      return listOf(
        Arguments.of("comments must be <= 240 characters", aMinimalRequest().copy(comments = "".padStart(241))),
        Arguments.of(
          "createdBy must be <= 100 characters",
          aMinimalRequest().copy(createdBy = "".padStart(101)),
        ),
      )
    }

    private fun aMinimalRequest() = CreateContactRestrictionRequest(
      restrictionType = "BAN",
      startDate = LocalDate.of(2020, 1, 1),
      expiryDate = null,
      comments = null,
      createdBy = "created",
    )
  }
}
