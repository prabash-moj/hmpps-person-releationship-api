package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.helper

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearchapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.AddContactRelationshipRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Contact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactSearch
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactSummary
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.net.URI

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

  fun getPrisonerContacts(prisonerNumber: String): List<PrisonerContactSummary> = webTestClient.get()
    .uri("/prisoner/$prisonerNumber/contact")
    .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
    .exchange()
    .expectStatus()
    .isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBodyList(PrisonerContactSummary::class.java)
    .returnResult().responseBody!!

  fun getReferenceCodes(groupCode: String) = webTestClient.get()
    .uri("/reference-codes/group/$groupCode")
    .accept(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
    .exchange()
    .expectStatus().isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBodyList(ReferenceCode::class.java)
    .returnResult().responseBody

  fun addAContactRelationship(contactId: Long, request: AddContactRelationshipRequest) {
    webTestClient.post()
      .uri("/contact/$contactId/relationship")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised())
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isCreated
  }

  fun getSearchContactResults(uri: URI) = webTestClient.get()
    .uri(uri.toString())
    .accept(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
    .exchange()
    .expectStatus().isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(ContactSearchResponse::class.java)
    .returnResult().responseBody

  fun getBadResponseErrors(uri: URI) = webTestClient.get()
    .uri(uri.toString())
    .accept(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
    .exchange()
    .expectStatus()
    .isBadRequest
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(ErrorResponse::class.java)
    .returnResult().responseBody!!

  fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  private fun authorised() = setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN"))

  data class ContactSearchResponse(
    val content: List<ContactSearch>,
    val pageable: Pageable,
    val last: Boolean,
    val totalPages: Int,
    val totalElements: Int,
    val first: Boolean,
    val size: Int,
    val number: Int,
    val sort: Sort,
    val numberOfElements: Int,
    val empty: Boolean,
  )

  data class Pageable(
    val pageNumber: Int,
    val pageSize: Int,
    val sort: Sort,
    val offset: Int,
    val unpaged: Boolean,
    val paged: Boolean,
  )

  data class Sort(
    val empty: Boolean,
    val unsorted: Boolean,
    val sorted: Boolean,
  )
}
