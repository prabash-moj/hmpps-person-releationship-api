package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource.sync

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.UpdateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.Contact
import java.time.LocalDate
import java.time.LocalDateTime

class SyncContactIntegrationTest : IntegrationTestBase() {

  @Nested
  inner class ContactSyncTests {

    @Test
    fun `Sync endpoints should return unauthorized if no token provided`() {
      webTestClient.get()
        .uri("/sync/contact/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.put()
        .uri("/sync/contact")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(createContactRequest())
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.post()
        .uri("/sync/contact/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateContactRequest())
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.delete()
        .uri("/sync/contact/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `Sync endpoints should return forbidden without an authorised role on the token`() {
      webTestClient.get()
        .uri("/sync/contact/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.post()
        .uri("/sync/contact")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(createContactRequest())
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.put()
        .uri("/sync/contact/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateContactRequest())
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.delete()
        .uri("/sync/contact/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should get an existing contact`() {
      // From base data
      val contactId = 15L
      val contact = webTestClient.get()
        .uri("/sync/contact/{contactId}", contactId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(Contact::class.java)
        .returnResult().responseBody!!

      with(contact) {
        assertThat(id).isEqualTo(15)
        assertThat(title).isEqualTo("MRS")
        assertThat(firstName).isEqualTo("Carl")
        assertThat(lastName).isEqualTo("Fifteen")
        assertThat(middleName).isEqualTo("Middle")
        assertThat(dateOfBirth).isEqualTo(LocalDate.of(2000, 11, 26))
        assertThat(estimatedIsOverEighteen).isEqualTo(EstimatedIsOverEighteen.DO_NOT_KNOW)
        assertThat(placeOfBirth).isEqualTo("London")
        assertThat(active).isFalse()
        assertThat(suspended).isFalse
        assertThat(staffFlag).isFalse
        assertThat(deceasedFlag).isFalse
        assertThat(deceasedDate).isEqualTo("2024-01-26")
        assertThat(coronerNumber).isNull()
        assertThat(gender).isEqualTo("Female")
        assertThat(domesticStatus).isEqualTo("S")
        assertThat(languageCode).isEqualTo("ENG")
        assertThat(nationalityCode).isNull()
        assertThat(interpreterRequired).isFalse
        assertThat(createdBy).isEqualTo("TIM")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(amendedBy).isNull()
        assertThat(amendedTime).isNull()
      }
    }

    @Test
    fun `should create a new contact`() {
      val contact = webTestClient.post()
        .uri("/sync/contact")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(createContactRequest())
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(Contact::class.java)
        .returnResult().responseBody!!

      // The created is returned
      with(contact) {
        assertThat(id).isGreaterThan(19)
        assertThat(title).isEqualTo("Mr")
        assertThat(firstName).isEqualTo("John")
        assertThat(lastName).isEqualTo("Doe")
        assertThat(middleName).isEqualTo("William")
        assertThat(dateOfBirth).isEqualTo(LocalDate.of(1980, 1, 1))
        assertThat(estimatedIsOverEighteen).isEqualTo(EstimatedIsOverEighteen.YES)
        assertThat(placeOfBirth).isEqualTo("London")
        assertThat(active).isTrue
        assertThat(suspended).isFalse
        assertThat(staffFlag).isFalse
        assertThat(deceasedFlag).isFalse
        assertThat(deceasedDate).isNull()
        assertThat(coronerNumber).isNull()
        assertThat(gender).isEqualTo("Male")
        assertThat(domesticStatus).isEqualTo("Single")
        assertThat(languageCode).isEqualTo("EN")
        assertThat(nationalityCode).isEqualTo("GB")
        assertThat(interpreterRequired).isFalse
        assertThat(createdBy).isEqualTo("JD000001")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }
    }

    @Test
    fun `should create and then update a contact`() {
      val contact = webTestClient.post()
        .uri("/sync/contact")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(createContactRequest())
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(Contact::class.java)
        .returnResult().responseBody!!

      with(contact) {
        assertThat(id).isGreaterThan(19)
        assertThat(title).isEqualTo("Mr")
        assertThat(firstName).isEqualTo("John")
        assertThat(lastName).isEqualTo("Doe")
        assertThat(middleName).isEqualTo("William")
        assertThat(active).isTrue()
        assertThat(createdBy).isEqualTo("JD000001")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }

      val updatedContact = webTestClient.put()
        .uri("/sync/contact/{contactId}", contact.id)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(updateContactRequest())
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(Contact::class.java)
        .returnResult().responseBody!!

      // Check the updated copy
      with(updatedContact) {
        assertThat(id).isGreaterThan(19)
        assertThat(title).isEqualTo("Mr")
        assertThat(firstName).isEqualTo("John")
        assertThat(lastName).isEqualTo("Doe")
        assertThat(middleName).isEqualTo("William")
        assertThat(dateOfBirth).isEqualTo(LocalDate.of(1980, 1, 1))
        assertThat(estimatedIsOverEighteen).isEqualTo(EstimatedIsOverEighteen.YES)
        assertThat(createdBy).isEqualTo("JD000001")
        assertThat(placeOfBirth).isEqualTo("Birmingham")
        assertThat(active).isTrue
        assertThat(suspended).isFalse
        assertThat(staffFlag).isFalse
        assertThat(deceasedFlag).isFalse
        assertThat(deceasedDate).isNull()
        assertThat(coronerNumber).isNull()
        assertThat(gender).isEqualTo("Male")
        assertThat(domesticStatus).isEqualTo("Single")
        assertThat(languageCode).isEqualTo("EN")
        assertThat(nationalityCode).isEqualTo("GB")
        assertThat(interpreterRequired).isTrue()
        assertThat(amendedBy).isEqualTo("UPDATE")
        assertThat(amendedTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }
    }

    @Test
    fun `should delete an existing contact`() {
      val contact = webTestClient.post()
        .uri("/sync/contact")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .bodyValue(createContactRequest())
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(Contact::class.java)
        .returnResult().responseBody!!

      webTestClient.delete()
        .uri("/sync/contact/{contactId}", contact.id)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/sync/contact/{contactId}", contact.id)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    private fun updateContactRequest() =
      UpdateContactRequest(
        title = "Mr",
        firstName = "John",
        lastName = "Doe",
        middleName = "William",
        dateOfBirth = LocalDate.of(1980, 1, 1),
        estimatedIsOverEighteen = EstimatedIsOverEighteen.YES,
        placeOfBirth = "Birmingham",
        active = true,
        suspended = false,
        staffFlag = false,
        deceasedFlag = false,
        deceasedDate = null,
        coronerNumber = null,
        gender = "Male",
        domesticStatus = "Single",
        languageCode = "EN",
        nationalityCode = "GB",
        interpreterRequired = true,
        updatedBy = "UPDATE",
        updatedTime = LocalDateTime.now(),
      )

    private fun createContactRequest() =
      CreateContactRequest(
        firstName = "John",
        title = "Mr",
        lastName = "Doe",
        middleName = "William",
        dateOfBirth = LocalDate.of(1980, 1, 1),
        estimatedIsOverEighteen = EstimatedIsOverEighteen.YES,
        createdBy = "JD000001",
        placeOfBirth = "London",
        active = true,
        suspended = false,
        staffFlag = false,
        deceasedFlag = false,
        deceasedDate = null,
        coronerNumber = null,
        gender = "Male",
        domesticStatus = "Single",
        languageCode = "EN",
        nationalityCode = "GB",
        interpreterRequired = false,
      )
  }
}
