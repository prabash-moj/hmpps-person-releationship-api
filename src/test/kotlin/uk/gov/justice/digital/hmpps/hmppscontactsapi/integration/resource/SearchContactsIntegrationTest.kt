package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.IntegrationTestBase
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val CONTACT_SEARCH_URL = UriComponentsBuilder.fromPath("contact/search")
  .queryParam("lastName", "Last")
  .queryParam("firstName", "Jack")
  .queryParam("middleName", "Middle")
  .queryParam("dateOfBirth", "21/11/2000")
  .build()
  .toUri()

class SearchContactsIntegrationTest : IntegrationTestBase() {

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.get()
      .uri(CONTACT_SEARCH_URL.toString())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.get()
      .uri(CONTACT_SEARCH_URL.toString())
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    webTestClient.get()
      .uri(CONTACT_SEARCH_URL.toString())
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return empty list if the contact doesn't exist`() {
    val url = UriComponentsBuilder.fromPath("contact/search")
      .queryParam("lastName", "NEW")
      .queryParam("firstName", "NEW")
      .queryParam("middleName", "Middle")
      .queryParam("dateOfBirth", "21/11/2000")
      .build()
      .toUri()

    val body = testAPIClient.getSearchContactResults(url)

    with(body!!) {
      assertThat(content).isEmpty()
      assertThat(totalElements).isEqualTo(0)
      assertThat(totalPages).isEqualTo(0)
    }
  }

  @Test
  fun `should return validation errors for date of birth when the date is in the future and special characters on name fields `() {
    val futureTime = getFormattedDateOneDayInFuture()

    val uri = UriComponentsBuilder.fromPath("contact/search")
      .queryParam("lastName", "NEW$")
      .queryParam("firstName", "NEW$")
      .queryParam("middleName", "Middle$")
      .queryParam("dateOfBirth", futureTime)
      .queryParam("page", 0)
      .queryParam("size", 10)
      .queryParam("sort", "lastName,asc")
      .queryParam("sort", "firstName,desc")
      .build()
      .toUri()

    val errors = testAPIClient.getBadResponseErrors(uri)

    assertThat(errors.userMessage).isEqualTo(
      "Validation failure(s): " +
        "Special characters are not allowed for First Name.\n" +
        "Special characters are not allowed for Last Name.\n" +
        "Special characters are not allowed for Middle Name.\n" +
        "The date of birth must be in the past",
    )
  }

  @Test
  fun `should return contacts when first, middle names and date of birth is not in request parameters`() {
    val url = UriComponentsBuilder.fromPath("contact/search")
      .queryParam("lastName", "Twelve")
      .build()
      .toUri()

    val body = testAPIClient.getSearchContactResults(url)

    with(body!!) {
      assertThat(content).isNotEmpty()
      assertThat(content.size).isEqualTo(1)
      assertThat(totalElements).isEqualTo(1)

      assertThat(totalPages).isEqualTo(1)

      val contact = content.first()
      assertThat(contact.id).isEqualTo(12)
      assertThat(contact.firstName).isEqualTo("Jane")
      assertThat(contact.lastName).isEqualTo("Twelve")
      assertThat(contact.middleName).isEqualTo("Middle")
      assertThat(contact.dateOfBirth).isEqualTo("2000-11-26")
      assertThat(contact.createdBy).isEqualTo("TIM")
      assertThat(contact.createdTime).isInThePast()
      assertThat(contact.flat).isEqualTo("Flat 3b")
      assertThat(contact.property).isEqualTo("42")
      assertThat(contact.street).isEqualTo("Acacia Avenue")
      assertThat(contact.area).isEqualTo("Bunting")
      assertThat(contact.cityCode).isEqualTo("SHEF")
      assertThat(contact.countyCode).isEqualTo("SYORKS")
      assertThat(contact.postCode).isEqualTo("S2 3LK")
      assertThat(contact.countryCode).isEqualTo("UK")
      assertThat(contact.mailFlag).isFalse()
      assertThat(contact.noFixedAddress).isFalse()
      assertThat(contact.startDate).isNull()
      assertThat(contact.startDate).isNull()
    }
  }

  @Test
  fun `should get the contact with when search by first, middle, last and date of birth`() {
    val body = testAPIClient.getSearchContactResults(CONTACT_SEARCH_URL)

    with(body!!) {
      assertThat(content).isNotEmpty()
      assertThat(content.size).isEqualTo(1)
      assertThat(totalElements).isEqualTo(1)
      assertThat(pageable.pageNumber).isEqualTo(0)
      assertThat(pageable.pageSize).isEqualTo(10)
      assertThat(pageable.sort.sorted).isEqualTo(true)
      assertThat(sort.sorted).isEqualTo(true)
      assertThat(first).isEqualTo(true)
      assertThat(size).isEqualTo(10)
      assertThat(number).isEqualTo(0)
      assertThat(totalPages).isEqualTo(1)

      val contact = content.first()
      assertThat(contact.id).isEqualTo(1)
      assertThat(contact.firstName).isEqualTo("Jack")
      assertThat(contact.lastName).isEqualTo("Last")
      assertThat(contact.middleName).isEqualTo("Middle")
      assertThat(contact.dateOfBirth).isEqualTo("2000-11-21")
      assertThat(contact.createdBy).isEqualTo("TIM")
      assertThat(contact.createdTime).isInThePast()
      assertThat(contact.property).isEqualTo("24")
      assertThat(contact.street).isEqualTo("Acacia Avenue")
      assertThat(contact.area).isEqualTo("Bunting")
      assertThat(contact.cityCode).isEqualTo("SHEF")
      assertThat(contact.countyCode).isEqualTo("SYORKS")
      assertThat(contact.postCode).isEqualTo("S2 3LK")
      assertThat(contact.countryCode).isEqualTo("UK")
      assertThat(contact.mailFlag).isFalse()
      assertThat(contact.noFixedAddress).isFalse()
      assertThat(contact.startDate).isNull()
      assertThat(contact.startDate).isNull()
    }
  }

  @Test
  fun `should get the contacts when searched by first name and last name with partial match`() {
    val uri = UriComponentsBuilder.fromPath("contact/search")
      .queryParam("lastName", "Las")
      .queryParam("firstName", "ck")
      .build()
      .toUri()

    val body = testAPIClient.getSearchContactResults(uri)

    with(body!!) {
      assertThat(content).isNotEmpty()
      assertThat(content.size).isEqualTo(1)
      assertThat(totalElements).isEqualTo(1)

      assertThat(totalPages).isEqualTo(1)

      val contact = content.first()
      assertThat(contact.id).isEqualTo(1)
      assertThat(contact.firstName).isEqualTo("Jack")
      assertThat(contact.lastName).isEqualTo("Last")
      assertThat(contact.middleName).isEqualTo("Middle")
      assertThat(contact.dateOfBirth).isEqualTo("2000-11-21")
      assertThat(contact.createdBy).isEqualTo("TIM")
      assertThat(contact.createdTime).isInThePast()
      assertThat(contact.property).isEqualTo("24")
      assertThat(contact.street).isEqualTo("Acacia Avenue")
      assertThat(contact.area).isEqualTo("Bunting")
      assertThat(contact.cityCode).isEqualTo("SHEF")
      assertThat(contact.countyCode).isEqualTo("SYORKS")
      assertThat(contact.postCode).isEqualTo("S2 3LK")
      assertThat(contact.countryCode).isEqualTo("UK")
      assertThat(contact.mailFlag).isFalse()
      assertThat(contact.noFixedAddress).isFalse()
      assertThat(contact.startDate).isNull()
      assertThat(contact.startDate).isNull()
    }
  }

  @Test
  fun `should get the contacts with no addresses associated with them when searched by last name `() {
    val uri = UriComponentsBuilder.fromPath("contact/search")
      .queryParam("lastName", "NoAddress")
      .build()
      .toUri()

    val body = testAPIClient.getSearchContactResults(uri)

    with(body!!) {
      assertThat(content).isNotEmpty()
      assertThat(content.size).isEqualTo(2)
      assertThat(totalElements).isEqualTo(2)

      assertThat(totalPages).isEqualTo(1)

      val contact = content.first()
      assertThat(contact.id).isEqualTo(16)
      assertThat(contact.firstName).isEqualTo("Liam")

      val lastContact = content.last()
      assertThat(lastContact.id).isEqualTo(17)
      assertThat(lastContact.firstName).isEqualTo("Hannah")
    }
  }

  @Test
  fun `should get bad request when searched with empty last name`() {
    val uri = UriComponentsBuilder.fromPath("contact/search")
      .queryParam("lastName", "")
      .queryParam("firstName", "Jack")
      .build()
      .toUri()

    val errors = testAPIClient.getBadResponseErrors(uri)

    assertThat(errors.userMessage).isEqualTo("Validation failure(s): Last Name cannot be blank.")
  }

  @Test
  fun `should get bad request when searched with invalid date format for date of birth`() {
    val uri: URI = UriComponentsBuilder.fromPath("contact/search")
      .queryParam("lastName", "Eleven")
      .queryParam("dateOfBirth", "=01-10-2001")
      .build()
      .toUri()

    val errors = testAPIClient.getBadResponseErrors(uri)

    assertThat(errors.userMessage).contains("Validation failure(s): Failed to convert value of type 'java.lang.String' to required type 'java.time.LocalDate';")
  }

  @Test
  fun `should get bad request when searched with no last name`() {
    val uri: URI = UriComponentsBuilder.fromPath("contact/search")
      .build()
      .toUri()

    val errors = testAPIClient.getBadResponseErrors(uri)

    assertThat(errors.userMessage).contains("Validation failure(s): Parameter specified as non-null is null: ")
  }

  fun getFormattedDateOneDayInFuture(): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val futureDate = LocalDate.now().plusDays(1)
    return futureDate.format(formatter)
  }
}
