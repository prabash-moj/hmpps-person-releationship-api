package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.helper

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Contact
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

class TestAPIClient(private val webTestClient: WebTestClient, private val jwtAuthHelper: JwtAuthorisationHelper) {

  fun createAContact(request: CreateContactRequest): Contact {
    return webTestClient.post()
      .uri("/contact")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised())
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isCreated
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectHeader().valuesMatch("Location", "/contact/(\\d)+")
      .expectBody(Contact::class.java)
      .returnResult().responseBody!!
  }

  fun getContact(id: Long): Contact {
    return webTestClient.get()
      .uri("/contact/$id")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(Contact::class.java)
      .returnResult().responseBody!!
  }

  fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  private fun authorised() = setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN"))
}
