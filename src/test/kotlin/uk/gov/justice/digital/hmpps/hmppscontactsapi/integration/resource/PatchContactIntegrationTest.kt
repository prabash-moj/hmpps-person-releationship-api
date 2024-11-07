package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.openapitools.jackson.nullable.JsonNullable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearchapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.H2IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.patch.PatchContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.patch.PatchContactResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.ContactInfo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import java.time.LocalDate

class PatchContactIntegrationTest : H2IntegrationTestBase() {

  private val contactId = 21L
  private val updatedByUser = "JD000001"

  @Autowired
  lateinit var contactRepository: ContactRepository

  @Nested
  inner class ErrorScenarios {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.patch()
        .uri("/contact/123456")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(aPatchContactRequest())
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.patch()
        .uri("/contact/123456")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(aPatchContactRequest())
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.patch()
        .uri("/contact/123456")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(aPatchContactRequest())
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun ` should return bad request when request is empty`() {
      webTestClient.patch()
        .uri("/contact/$contactId")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .bodyValue(
          """{
                  }""",
        )
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ErrorResponse::class.java)
        .returnResult().responseBody!!
    }

    private fun aPatchContactRequest() = PatchContactRequest(
      languageCode = JsonNullable.of("BEN"),
      updatedBy = updatedByUser,
    )
  }

  @Nested
  inner class LanguageCode {

    @Test
    fun `should not patch the language code when not provided`() {
      resetLanguageCode()

      val req = PatchContactRequest(
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.languageCode).isEqualTo("ENG")
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactId))
    }

    @Test
    fun `should successfully patch the language code with null value`() {
      resetLanguageCode()

      val req = PatchContactRequest(
        languageCode = JsonNullable.of(null),
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.languageCode).isEqualTo(null)
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactId))
    }

    @Test
    fun `should successfully patch the language code with a value`() {
      resetLanguageCode()

      val req = PatchContactRequest(
        languageCode = JsonNullable.of("FRE-FRA"),
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.languageCode).isEqualTo("FRE-FRA")
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactId))
    }

    private fun resetLanguageCode() {
      val req = PatchContactRequest(
        languageCode = JsonNullable.of("ENG"),
        updatedBy = updatedByUser,
      )
      val res =
        testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.languageCode).isEqualTo("ENG")
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.reset()
    }
  }

  @Nested
  inner class InterpreterRequired {

    @Test
    fun `should successfully patch the interpreter required with true`() {
      resetInterpreterRequired(false)

      val req = PatchContactRequest(
        interpreterRequired = JsonNullable.of(true),
        updatedBy = updatedByUser,
      )
      val res =
        testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.interpreterRequired).isEqualTo(true)
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactId))
    }

    @Test
    fun `should not patch the interpreter required when not provided`() {
      resetInterpreterRequired(true)

      val req = PatchContactRequest(
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.interpreterRequired).isEqualTo(true)
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactId))
    }

    @Test
    fun `should not patch the interpreter required with null value`() {
      resetInterpreterRequired(true)

      val req = PatchContactRequest(
        interpreterRequired = JsonNullable.of(null),
        updatedBy = updatedByUser,
      )
      val uri = UriComponentsBuilder.fromPath("/contact/$contactId")
        .build()
        .toUri()

      val errors = testAPIClient.getBadResponseErrorsWithPatch(req, uri)

      assertThat(errors.userMessage).isEqualTo("Validation failure: Unsupported interpreter required type null.")
      stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactId))
    }

    private fun resetInterpreterRequired(resetValue: Boolean) {
      val req = PatchContactRequest(
        interpreterRequired = JsonNullable.of(resetValue),
        updatedBy = updatedByUser,
      )
      val res =
        testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.interpreterRequired).isEqualTo(resetValue)
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.reset()
    }
  }

  @Nested
  inner class DomesticStatus {

    @Test
    fun `should not patch the domestic status code when not provided`() {
      resetDomesticStatus()

      val req = PatchContactRequest(
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.domesticStatus).isEqualTo("P")
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactId))
    }

    @Test
    fun `should successfully patch the domestic status code with null value`() {
      resetDomesticStatus()

      val req = PatchContactRequest(
        domesticStatus = JsonNullable.of(null),
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.domesticStatus).isEqualTo(null)
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactId))
    }

    @Test
    fun `should successfully patch the domestic status code with a value`() {
      resetDomesticStatus()

      val req = PatchContactRequest(
        domesticStatus = JsonNullable.of("M"),
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.domesticStatus).isEqualTo("M")
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactId))
    }

    private fun resetDomesticStatus() {
      val req = PatchContactRequest(
        domesticStatus = JsonNullable.of("P"),
        updatedBy = updatedByUser,
      )
      val res =
        testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.domesticStatus).isEqualTo("P")
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.reset()
    }
  }

  @Nested
  inner class StaffFlag {

    @Test
    fun `should successfully patch the staff flag with true`() {
      resetStaffFlag(false)

      val req = PatchContactRequest(
        isStaff = JsonNullable.of(true),
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.isStaff).isEqualTo(true)
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactId))
    }

    @Test
    fun `should not patch the staff flag when not provided`() {
      resetStaffFlag(true)

      val req = PatchContactRequest(
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.isStaff).isEqualTo(true)
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactId))
    }

    @Test
    fun `should not patch the staff flag with null value`() {
      resetStaffFlag(true)

      val req = PatchContactRequest(
        isStaff = JsonNullable.of(null),
        updatedBy = updatedByUser,
      )
      val uri = UriComponentsBuilder.fromPath("/contact/$contactId")
        .build()
        .toUri()

      val errors = testAPIClient.getBadResponseErrorsWithPatch(req, uri)

      assertThat(errors.userMessage).isEqualTo("Validation failure: Unsupported staff flag value null.")
      stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactId))
    }

    private fun resetStaffFlag(resetValue: Boolean) {
      val req = PatchContactRequest(
        isStaff = JsonNullable.of(resetValue),
        updatedBy = updatedByUser,
      )
      val res =
        testAPIClient.patchAContact(req, "/contact/$contactId")

      assertThat(res.isStaff).isEqualTo(resetValue)
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.reset()
    }
  }

  @Nested
  inner class DateOfBirth {
    private var contactIdThatHasDOB = 0L

    @BeforeEach
    fun createContactWithDob() {
      contactIdThatHasDOB = testAPIClient.createAContact(
        CreateContactRequest(
          lastName = "Date of birth",
          firstName = "Has",
          dateOfBirth = LocalDate.of(1982, 6, 15),
          createdBy = "created",
        ),
      ).id
    }

    @Test
    fun `should not patch the date of birth when not provided`() {
      val req = PatchContactRequest(
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactIdThatHasDOB")

      assertThat(res.dateOfBirth).isEqualTo(LocalDate.of(1982, 6, 15))
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactIdThatHasDOB))
    }

    @Test
    fun `should successfully patch the date of birth with null value`() {
      val req = PatchContactRequest(
        dateOfBirth = JsonNullable.of(null),
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactIdThatHasDOB")

      assertThat(res.dateOfBirth).isNull()
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactIdThatHasDOB))
    }

    @Test
    fun `should successfully patch the date of birth with a value`() {
      val req = PatchContactRequest(
        dateOfBirth = JsonNullable.of(LocalDate.of(2000, 12, 25)),
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactIdThatHasDOB")

      assertThat(res.dateOfBirth).isEqualTo(LocalDate.of(2000, 12, 25))
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactIdThatHasDOB))
    }
  }

  @Nested
  inner class PatchEstimatedIsOverEighteen {
    private var contactIdThatHasEstimatedDOB = 0L

    @BeforeEach
    fun createContactWithDob() {
      contactIdThatHasEstimatedDOB = testAPIClient.createAContact(
        CreateContactRequest(
          lastName = "Date of birth",
          firstName = "Has",
          dateOfBirth = null,
          estimatedIsOverEighteen = EstimatedIsOverEighteen.YES,
          createdBy = "created",
        ),
      ).id
    }

    @Test
    fun `should not patch estimated is over eighteen when not provided`() {
      val req = PatchContactRequest(
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactIdThatHasEstimatedDOB")

      assertThat(res.estimatedIsOverEighteen).isEqualTo(EstimatedIsOverEighteen.YES)
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactIdThatHasEstimatedDOB))
    }

    @Test
    fun `should successfully patch estimated is over eighteen with null value`() {
      val req = PatchContactRequest(
        estimatedIsOverEighteen = JsonNullable.of(null),
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactIdThatHasEstimatedDOB")

      assertThat(res.estimatedIsOverEighteen).isEqualTo(null)
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactIdThatHasEstimatedDOB))
    }

    @Test
    fun `should successfully patch estimated is over eighteen with a value`() {
      val req = PatchContactRequest(
        estimatedIsOverEighteen = JsonNullable.of(EstimatedIsOverEighteen.DO_NOT_KNOW),
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactIdThatHasEstimatedDOB")

      assertThat(res.estimatedIsOverEighteen).isEqualTo(EstimatedIsOverEighteen.DO_NOT_KNOW)
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactIdThatHasEstimatedDOB))
    }
  }

  @Nested
  inner class PatchNames {
    private var contactThatHasAllNameFields = 0L

    @BeforeEach
    fun createContactWithDob() {
      contactThatHasAllNameFields = testAPIClient.createAContact(
        CreateContactRequest(
          lastName = "Last",
          firstName = "First",
          middleNames = "Middle Names",
          title = "MR",
          createdBy = "created",
        ),
      ).id
    }

    @Test
    fun `should not patch names when not provided`() {
      val req = PatchContactRequest(
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactThatHasAllNameFields")

      assertThat(res.firstName).isEqualTo("First")
      assertThat(res.lastName).isEqualTo("Last")
      assertThat(res.middleNames).isEqualTo("Middle Names")
      assertThat(res.title).isEqualTo("MR")
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactThatHasAllNameFields))
    }

    @Test
    fun `should not patch first or last names`() {
      val res = webTestClient.patch()
        .uri("/contact/$contactThatHasAllNameFields")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
        .bodyValue(
          """
          { "firstName": "update first", "lastName": "update last", "middleNames": "update middle", "title": "MRS", "updatedBy": "$updatedByUser" }
          """.trimIndent(),
        )
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(PatchContactResponse::class.java)
        .returnResult().responseBody!!

      assertThat(res.firstName).isEqualTo("First")
      assertThat(res.lastName).isEqualTo("Last")
      assertThat(res.middleNames).isEqualTo("update middle")
      assertThat(res.title).isEqualTo("MRS")
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactThatHasAllNameFields))
    }

    @Test
    fun `should successfully patch middle name and title with null values`() {
      val req = PatchContactRequest(
        title = JsonNullable.of(null),
        middleNames = JsonNullable.of(null),
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactThatHasAllNameFields")

      assertThat(res.firstName).isEqualTo("First")
      assertThat(res.lastName).isEqualTo("Last")
      assertThat(res.middleNames).isNull()
      assertThat(res.title).isNull()
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactThatHasAllNameFields))
    }

    @Test
    fun `should successfully patch middle name and title with a value`() {
      val req = PatchContactRequest(
        title = JsonNullable.of("MRS"),
        middleNames = JsonNullable.of("Updated Middle"),
        updatedBy = updatedByUser,
      )
      val res = testAPIClient.patchAContact(req, "/contact/$contactThatHasAllNameFields")

      assertThat(res.firstName).isEqualTo("First")
      assertThat(res.lastName).isEqualTo("Last")
      assertThat(res.middleNames).isEqualTo("Updated Middle")
      assertThat(res.title).isEqualTo("MRS")
      assertThat(res.amendedBy).isEqualTo(updatedByUser)
      stubEvents.assertHasEvent(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactThatHasAllNameFields))
    }

    @Test
    fun `should not be able to patch to an invalid title value`() {
      val req = PatchContactRequest(
        title = JsonNullable.of("FOO"),
        updatedBy = updatedByUser,
      )

      val uri = UriComponentsBuilder.fromPath("/contact/$contactThatHasAllNameFields")
        .build()
        .toUri()
      val errors = testAPIClient.getBadResponseErrorsWithPatch(req, uri)
      assertThat(errors.userMessage).isEqualTo("Validation failure: Reference code with groupCode TITLE and code 'FOO' not found.")
      stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactThatHasAllNameFields))
    }

    @Test
    fun `should not be able to patch middle name when it's too long`() {
      val req = PatchContactRequest(
        middleNames = JsonNullable.of("".padEnd(36, 'X')),
        updatedBy = updatedByUser,
      )

      val uri = UriComponentsBuilder.fromPath("/contact/$contactThatHasAllNameFields")
        .build()
        .toUri()
      val errors = testAPIClient.getBadResponseErrorsWithPatch(req, uri)
      assertThat(errors.userMessage).isEqualTo("Validation failure(s): middleNames must be <= 35 characters")
      stubEvents.assertHasNoEvents(OutboundEvent.CONTACT_AMENDED, ContactInfo(contactThatHasAllNameFields))
    }
  }
}
