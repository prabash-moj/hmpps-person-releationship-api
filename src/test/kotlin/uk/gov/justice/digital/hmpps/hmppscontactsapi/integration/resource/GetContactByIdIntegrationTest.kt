package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import java.time.LocalDate
import java.time.LocalDateTime

class GetContactByIdIntegrationTest : IntegrationTestBase() {

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.get()
      .uri("/contact/123456")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.get()
      .uri("/contact/123456")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    webTestClient.get()
      .uri("/contact/123456")
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return 404 if the contact doesn't exist`() {
    webTestClient.get()
      .uri("/contact/123456")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `should get the contact with all fields`() {
    val contact = testAPIClient.getContact(1)

    with(contact) {
      assertThat(id).isEqualTo(1)
      assertThat(title).isEqualTo("MR")
      assertThat(lastName).isEqualTo("Last")
      assertThat(firstName).isEqualTo("Jack")
      assertThat(middleName).isEqualTo("Middle")
      assertThat(dateOfBirth).isEqualTo(LocalDate.of(2000, 11, 21))
      assertThat(estimatedIsOverEighteen).isNull()
      assertThat(isDeceased).isFalse()
      assertThat(deceasedDate).isNull()
      assertThat(createdBy).isEqualTo("TIM")
      assertThat(createdTime).isNotNull()
      assertThat(contact.addresses).hasSize(2)
    }
  }

  @Test
  fun `should get the contact with addresses`() {
    val contact = testAPIClient.getContact(1)

    with(contact) {
      assertThat(id).isEqualTo(1)
      assertThat(id).isEqualTo(1)

      with(contact.addresses[0]) {
        assertThat(contactAddressId).isEqualTo(1)
        assertThat(contactId).isEqualTo(1)
        assertThat(addressType).isEqualTo("HOME")
        assertThat(addressTypeDescription).isEqualTo("Home address")
        assertThat(primaryAddress).isEqualTo(true)
        assertThat(flat).isNull()
        assertThat(property).isEqualTo("24")
        assertThat(street).isEqualTo("Acacia Avenue")
        assertThat(area).isEqualTo("Bunting")
        assertThat(cityCode).isEqualTo("25343")
        assertThat(cityDescription).isEqualTo("Sheffield")
        assertThat(countyCode).isEqualTo("S.YORKSHIRE")
        assertThat(countyDescription).isEqualTo("South Yorkshire")
        assertThat(postcode).isEqualTo("S2 3LK")
        assertThat(countryCode).isEqualTo("ENG")
        assertThat(countryDescription).isEqualTo("England")
        assertThat(mailFlag).isFalse()
        assertThat(noFixedAddress).isFalse()
        assertThat(createdBy).isEqualTo("TIM")
        assertThat(createdTime).isNotNull()
      }
      with(contact.addresses[1]) {
        assertThat(contactAddressId).isEqualTo(2)
        assertThat(contactId).isEqualTo(1)
        assertThat(addressType).isEqualTo("WORK")
        assertThat(addressTypeDescription).isEqualTo("Work address")
        assertThat(primaryAddress).isEqualTo(false)
        assertThat(flat).isEqualTo("Flat 1")
        assertThat(verified).isTrue()
        assertThat(verifiedBy).isEqualTo("BOB")
        assertThat(verifiedTime).isEqualTo(LocalDateTime.of(2020, 1, 1, 10, 30, 0))
        assertThat(mailFlag).isTrue()
        assertThat(noFixedAddress).isTrue()
        assertThat(startDate).isEqualTo(LocalDate.of(2020, 1, 2))
        assertThat(endDate).isEqualTo(LocalDate.of(2029, 3, 4))
      }
    }
  }

  @Test
  fun `should get the contact with phone numbers`() {
    val contact = testAPIClient.getContact(1)

    with(contact) {
      assertThat(id).isEqualTo(1)
      assertThat(contact.phoneNumbers).hasSize(2)
      with(contact.phoneNumbers[0]) {
        assertThat(contactPhoneId).isEqualTo(1)
        assertThat(contactId).isEqualTo(1)
        assertThat(phoneType).isEqualTo("MOBILE")
        assertThat(phoneTypeDescription).isEqualTo("Mobile phone")
        assertThat(phoneNumber).isEqualTo("07878 111111")
        assertThat(extNumber).isNull()
        assertThat(primaryPhone).isTrue()
        assertThat(createdBy).isEqualTo("TIM")
      }
      with(contact.phoneNumbers[1]) {
        assertThat(contactPhoneId).isEqualTo(2)
        assertThat(contactId).isEqualTo(1)
        assertThat(phoneType).isEqualTo("HOME")
        assertThat(phoneTypeDescription).isEqualTo("Home phone")
        assertThat(phoneNumber).isEqualTo("01111 777777")
        assertThat(extNumber).isEqualTo("+0123")
        assertThat(primaryPhone).isFalse()
        assertThat(createdBy).isEqualTo("JAMES")
      }

      assertThat(contact.addresses).hasSize(2)
      assertThat(contact.addresses[0].phoneNumbers).hasSize(1)
      assertThat(contact.addresses[0].phoneNumbers[0].contactPhoneId).isEqualTo(2)
      assertThat(contact.addresses[1].phoneNumbers).isEmpty()
    }
  }

  @Test
  fun `should get the contact with emails`() {
    val contact = testAPIClient.getContact(3)

    with(contact) {
      assertThat(id).isEqualTo(3)
      assertThat(contact.emailAddresses).hasSize(2)
      with(contact.emailAddresses[0]) {
        assertThat(contactEmailId).isEqualTo(3)
        assertThat(contactId).isEqualTo(3)
        assertThat(emailType).isEqualTo("PERSONAL")
        assertThat(emailTypeDescription).isEqualTo("Personal email")
        assertThat(emailAddress).isEqualTo("mrs.last@example.com")
        assertThat(primaryEmail).isFalse()
        assertThat(createdBy).isEqualTo("TIM")
      }
      with(contact.emailAddresses[1]) {
        assertThat(contactEmailId).isEqualTo(4)
        assertThat(contactId).isEqualTo(3)
        assertThat(emailType).isEqualTo("WORK")
        assertThat(emailTypeDescription).isEqualTo("Work email")
        assertThat(emailAddress).isEqualTo("work@example.com")
        assertThat(primaryEmail).isTrue()
        assertThat(createdBy).isEqualTo("JAMES")
      }
    }
  }

  @Test
  fun `should get deceased contacts`() {
    val contact = testAPIClient.getContact(19)

    with(contact) {
      assertThat(id).isEqualTo(19)
      assertThat(title).isNull()
      assertThat(lastName).isEqualTo("Dead")
      assertThat(firstName).isEqualTo("Currently")
      assertThat(middleName).isNull()
      assertThat(dateOfBirth).isEqualTo(LocalDate.of(1980, 1, 1))
      assertThat(estimatedIsOverEighteen).isNull()
      assertThat(isDeceased).isTrue()
      assertThat(deceasedDate).isEqualTo(LocalDate.of(2000, 1, 1))
      assertThat(createdBy).isEqualTo("TIM")
      assertThat(createdTime).isNotNull()
    }
  }

  @ParameterizedTest
  @EnumSource(EstimatedIsOverEighteen::class)
  fun `should return is over eighteen when DOB is not known`(estimatedIsOverEighteen: EstimatedIsOverEighteen) {
    val createdContactId = testAPIClient.createAContact(
      CreateContactRequest(
        firstName = "First",
        lastName = "Last",
        dateOfBirth = null,
        estimatedIsOverEighteen = estimatedIsOverEighteen,
        createdBy = "USER1",
      ),
    ).id
    val contact = testAPIClient.getContact(createdContactId)
    assertThat(contact.estimatedIsOverEighteen).isEqualTo(estimatedIsOverEighteen)
  }
}
