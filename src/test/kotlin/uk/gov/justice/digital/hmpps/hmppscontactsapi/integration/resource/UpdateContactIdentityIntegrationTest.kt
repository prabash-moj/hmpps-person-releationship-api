package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearchapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.ContactIdentityInfo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.PersonReference
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.Source

class UpdateContactIdentityIntegrationTest : H2IntegrationTestBase() {
  private var savedContactId = 0L
  private var savedContactIdentityId = 0L

  @BeforeEach
  fun initialiseData() {
    savedContactId = testAPIClient.createAContact(
      CreateContactRequest(
        lastName = "identity",
        firstName = "has",
        createdBy = "created",
      ),
    ).id
    savedContactIdentityId = testAPIClient.createAContactIdentity(
      savedContactId,
      CreateIdentityRequest(
        identityType = "DL",
        identityValue = "DL123456789",
        issuingAuthority = "DVLA",
        createdBy = "created",
      ),
    ).contactIdentityId
  }

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.put()
      .uri("/contact/$savedContactId/identity/$savedContactIdentityId")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(aMinimalRequest())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.put()
      .uri("/contact/$savedContactId/identity/$savedContactIdentityId")
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
    webTestClient.put()
      .uri("/contact/$savedContactId/identity/$savedContactIdentityId")
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
      "identityType must not be null;{\"identityType\": null, \"identityValue\": \"0123456789\", \"updatedBy\": \"created\"}",
      "identityType must not be null;{\"identityValue\": \"0123456789\", \"updatedBy\": \"created\"}",
      "identityValue must not be null;{\"identityType\": \"DL\", \"identityValue\": null, \"updatedBy\": \"created\"}",
      "identityValue must not be null;{\"identityType\": \"DL\", \"updatedBy\": \"created\"}",
      "updatedBy must not be null;{\"identityType\": \"DL\", \"identityValue\": \"0123456789\", \"updatedBy\": null}",
      "updatedBy must not be null;{\"identityType\": \"DL\", \"identityValue\": \"0123456789\"}",
    ],
    delimiter = ';',
  )
  fun `should return bad request if required fields are null`(expectedMessage: String, json: String) {
    val errors = webTestClient.put()
      .uri("/contact/$savedContactId/identity/$savedContactIdentityId")
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
    stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_IDENTITY_AMENDED, ContactIdentityInfo(savedContactIdentityId))
  }

  @ParameterizedTest
  @MethodSource("allFieldConstraintViolations")
  fun `should enforce field constraints`(expectedMessage: String, request: UpdateIdentityRequest) {
    val errors = webTestClient.put()
      .uri("/contact/$savedContactId/identity/$savedContactIdentityId")
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
    stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_IDENTITY_AMENDED, ContactIdentityInfo(savedContactIdentityId))
  }

  @Test
  fun `should not update the identity if the type is unknown`() {
    val request = UpdateIdentityRequest(
      identityType = "MACRO",
      identityValue = "DL123456789",
      updatedBy = "amended",
    )

    val errors = webTestClient.put()
      .uri("/contact/$savedContactId/identity/$savedContactIdentityId")
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

    assertThat(errors.userMessage).isEqualTo("Validation failure: Unsupported identity type (MACRO)")
    stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_IDENTITY_AMENDED, ContactIdentityInfo(savedContactIdentityId))
  }

  @Test
  fun `should not update the identity if the type is no longer active`() {
    val request = UpdateIdentityRequest(
      identityType = "NHS",
      identityValue = "Is active is false",
      updatedBy = "amended",
    )

    val errors = webTestClient.put()
      .uri("/contact/$savedContactId/identity/$savedContactIdentityId")
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

    assertThat(errors.userMessage).isEqualTo("Validation failure: Identity type (NHS) is no longer supported for creating or updating identities")
    stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_IDENTITY_AMENDED, ContactIdentityInfo(savedContactIdentityId))
  }

  @Test
  fun `should not update the identity if the contact is not found`() {
    val request = aMinimalRequest()

    val errors = webTestClient.put()
      .uri("/contact/-321/identity/$savedContactIdentityId")
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

    assertThat(errors.userMessage).isEqualTo("Entity not found : Contact (-321) not found")
    stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_IDENTITY_AMENDED, ContactIdentityInfo(savedContactIdentityId))
  }

  @Test
  fun `should not update the identity if the identity is not found`() {
    val request = aMinimalRequest()

    val errors = webTestClient.put()
      .uri("/contact/$savedContactId/identity/-99")
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

    assertThat(errors.userMessage).isEqualTo("Entity not found : Contact identity (-99) not found")
    stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_IDENTITY_AMENDED, ContactIdentityInfo(-99))
  }

  @Test
  fun `should update the identity with minimal fields`() {
    val request = UpdateIdentityRequest(
      identityType = "PASS",
      identityValue = "P978654312",
      issuingAuthority = null,
      updatedBy = "amended",
    )
    val updated = testAPIClient.updateAContactIdentity(savedContactId, savedContactIdentityId, request)

    with(updated) {
      assertThat(identityType).isEqualTo(request.identityType)
      assertThat(identityValue).isEqualTo(request.identityValue)
      assertThat(issuingAuthority).isNull()
      assertThat(createdBy).isEqualTo("created")
      assertThat(createdTime).isNotNull()
      assertThat(updatedBy).isEqualTo("amended")
      assertThat(updatedTime).isNotNull()
    }

    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_IDENTITY_AMENDED,
      additionalInfo = ContactIdentityInfo(savedContactIdentityId, Source.DPS),
      personReference = PersonReference(dpsContactId = savedContactId),
    )
  }

  @Test
  fun `should update the identity with all fields`() {
    val request = UpdateIdentityRequest(
      identityType = "PASS",
      identityValue = "P978654312",
      issuingAuthority = "Passport office",
      updatedBy = "amended",
    )

    val updated = testAPIClient.updateAContactIdentity(savedContactId, savedContactIdentityId, request)

    with(updated) {
      assertThat(identityType).isEqualTo(request.identityType)
      assertThat(identityValue).isEqualTo(request.identityValue)
      assertThat(issuingAuthority).isEqualTo(request.issuingAuthority)
      assertThat(createdBy).isEqualTo("created")
      assertThat(createdTime).isNotNull()
      assertThat(updatedBy).isEqualTo("amended")
      assertThat(updatedTime).isNotNull()
    }

    stubEvents.assertHasEvent(
      event = OutboundEvent.CONTACT_IDENTITY_AMENDED,
      additionalInfo = ContactIdentityInfo(savedContactIdentityId, Source.DPS),
      personReference = PersonReference(dpsContactId = savedContactId),
    )
  }

  companion object {
    @JvmStatic
    fun allFieldConstraintViolations(): List<Arguments> {
      return listOf(
        Arguments.of("identityType must be <= 20 characters", aMinimalRequest().copy(identityType = "".padStart(21))),
        Arguments.of("identityValue must be <= 20 characters", aMinimalRequest().copy(identityValue = "".padStart(21))),
        Arguments.of(
          "issuingAuthority must be <= 40 characters",
          aMinimalRequest().copy(issuingAuthority = "".padStart(41)),
        ),
        Arguments.of(
          "updatedBy must be <= 100 characters",
          aMinimalRequest().copy(updatedBy = "".padStart(101)),
        ),
      )
    }

    private fun aMinimalRequest() = UpdateIdentityRequest(
      identityType = "DL",
      identityValue = "DL123456789",
      updatedBy = "amended",
    )
  }
}
