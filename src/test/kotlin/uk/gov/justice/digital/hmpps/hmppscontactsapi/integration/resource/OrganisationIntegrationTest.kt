package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.PostgresIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateOrganisationRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Organisation
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.LocalDateTime

class OrganisationIntegrationTest : PostgresIntegrationTestBase() {

  @Nested
  inner class GetOrganisationByOrganisationId {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("$GET_ORGANISATION_DATA/001")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("$GET_ORGANISATION_DATA/001")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("$GET_ORGANISATION_DATA/001")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @ParameterizedTest
    @ValueSource(strings = ["ROLE_CONTACTS_ADMIN", "ROLE_CONTACTS__R", "ROLE_CONTACTS__RW"])
    fun `should return not found if no organisation found`(role: String) {
      webTestClient.get()
        .uri("$GET_ORGANISATION_DATA/9999")
        .headers(setAuthorisation(roles = listOf(role)))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Test
    fun `should return organisation data when using a valid organisation id`() {
      val request = createValidOrganisationRequest()

      val response = webTestClient.post()
        .uri(GET_ORGANISATION_DATA)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS__RW")))
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isCreated
        .expectBody(Organisation::class.java)
        .returnResult()
        .responseBody

      val organisation = testAPIClient.getOrganisation(response.organisationId)

      with(organisation) {
        assertThat(organisationName).isEqualTo(request.organisationName)
        assertThat(programmeNumber).isEqualTo(request.programmeNumber)
        assertThat(vatNumber).isEqualTo(request.vatNumber)
        assertThat(caseloadId).isEqualTo(request.caseloadId)
        assertThat(comments).isEqualTo(request.comments)
        assertThat(active).isEqualTo(request.active)
        assertThat(deactivatedDate).isEqualTo(request.deactivatedDate)
        assertThat(createdBy).isEqualTo(request.createdBy)
        assertThat(createdTime).isNotNull()
        assertThat(organisationId).isEqualTo(response.organisationId)
      }
    }
  }

  @Nested
  inner class CreateOrganisation {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.post()
        .uri(GET_ORGANISATION_DATA)
        .bodyValue(createValidOrganisationRequest())
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.post()
        .uri(GET_ORGANISATION_DATA)
        .headers(setAuthorisation())
        .bodyValue(createValidOrganisationRequest())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.post()
        .uri(GET_ORGANISATION_DATA)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(createValidOrganisationRequest())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return bad request when organisation name exceeds 40 characters`() {
      val request = createValidOrganisationRequest().copy(
        organisationName = "A".repeat(41),
      )

      val errors = webTestClient.post()
        .uri(GET_ORGANISATION_DATA)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS__RW")))
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ErrorResponse::class.java)
        .returnResult().responseBody!!

      assertThat(errors.userMessage).isEqualTo("Validation failure(s): organisationName must be <= 40 characters")
    }

    @Test
    fun `should return bad request when programme number exceeds 40 characters`() {
      val request = createValidOrganisationRequest().copy(
        programmeNumber = "A".repeat(41),
      )

      val errors = webTestClient.post()
        .uri(GET_ORGANISATION_DATA)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS__RW")))
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ErrorResponse::class.java)
        .returnResult().responseBody!!

      assertThat(errors.userMessage).isEqualTo("Validation failure(s): programmeNumber must be <= 40 characters")
    }

    @Test
    fun `should return bad request when VAT number exceeds 12 characters`() {
      val request = createValidOrganisationRequest().copy(
        vatNumber = "A".repeat(13),
      )

      val errors = webTestClient.post()
        .uri(GET_ORGANISATION_DATA)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS__RW")))
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ErrorResponse::class.java)
        .returnResult().responseBody!!

      assertThat(errors.userMessage).isEqualTo("Validation failure(s): vatNumber must be <= 12 characters")
    }

    @Test
    fun `should return bad request when caseload ID exceeds 6 characters`() {
      val request = createValidOrganisationRequest().copy(
        caseloadId = "A".repeat(7),
      )

      val errors = webTestClient.post()
        .uri(GET_ORGANISATION_DATA)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS__RW")))
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ErrorResponse::class.java)
        .returnResult().responseBody!!

      assertThat(errors.userMessage).isEqualTo("Validation failure(s): caseloadId must be <= 6 characters")
    }

    @Test
    fun `should return bad request when comments exceed 240 characters`() {
      val request = createValidOrganisationRequest().copy(
        comments = "A".repeat(241),
      )

      val errors = webTestClient.post()
        .uri(GET_ORGANISATION_DATA)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS__RW")))
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ErrorResponse::class.java)
        .returnResult().responseBody!!

      assertThat(errors.userMessage).isEqualTo("Validation failure(s): comments must be <= 240 characters")
    }

    @Test
    fun `should return bad request when required fields are missing`() {
      val request = mapOf(
        "active" to true,
        "createdTime" to LocalDateTime.now(),
      )

      webTestClient.post()
        .uri(GET_ORGANISATION_DATA)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS__RW")))
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isBadRequest
    }

    @Test
    fun `should create organisation successfully with valid data`() {
      val request = createValidOrganisationRequest()

      val response = webTestClient.post()
        .uri(GET_ORGANISATION_DATA)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS__RW")))
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isCreated
        .expectBody(Organisation::class.java)
        .returnResult()
        .responseBody

      assertThat(response).isNotNull
      with(response!!) {
        assertThat(organisationName).isEqualTo(request.organisationName)
        assertThat(programmeNumber).isEqualTo(request.programmeNumber)
        assertThat(vatNumber).isEqualTo(request.vatNumber)
        assertThat(caseloadId).isEqualTo(request.caseloadId)
        assertThat(comments).isEqualTo(request.comments)
        assertThat(active).isEqualTo(request.active)
        assertThat(deactivatedDate).isEqualTo(request.deactivatedDate)
        assertThat(createdBy).isEqualTo(request.createdBy)
        assertThat(createdTime).isNotNull()
        assertThat(organisationId).isNotNull()
      }
    }
  }

  companion object {

    private const val GET_ORGANISATION_DATA = "/organisation"

    fun createValidOrganisationRequest() = CreateOrganisationRequest(
      organisationName = "Test Organisation",
      programmeNumber = "TEST01",
      vatNumber = "GB123456789",
      caseloadId = "TEST1",
      comments = "Test comments",
      active = true,
      deactivatedDate = null,
      createdBy = "test-user",
      createdTime = LocalDateTime.now(),
      updatedBy = null,
      updatedTime = null,
    )
  }
}
