package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.helper

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearchapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.AddContactRelationshipRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreatePhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdatePhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.migrate.MigrateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.patch.PatchContactResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactIdentityDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactPhoneDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactSearchResultItem
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.GetContactResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactSummary
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.migrate.MigrateContactResponse
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.net.URI

class TestAPIClient(private val webTestClient: WebTestClient, private val jwtAuthHelper: JwtAuthorisationHelper) {

  fun createAContact(request: CreateContactRequest): GetContactResponse {
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
      .expectBody(GetContactResponse::class.java)
      .returnResult().responseBody!!
  }

  fun patchAContact(request: Any, url: String): PatchContactResponse {
    return webTestClient.patch()
      .uri(url)
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised())
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(PatchContactResponse::class.java)
      .returnResult().responseBody!!
  }

  fun getContact(id: Long): GetContactResponse {
    return webTestClient.get()
      .uri("/contact/$id")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(GetContactResponse::class.java)
      .returnResult().responseBody!!
  }

  fun getPrisonerContacts(prisonerNumber: String): PrisonerContactSummaryResponse = webTestClient.get()
    .uri("/prisoner/$prisonerNumber/contact")
    .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
    .exchange()
    .expectStatus()
    .isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(PrisonerContactSummaryResponse::class.java)
    .returnResult().responseBody!!

  fun getReferenceCodes(groupCode: String, sort: String? = null, activeOnly: Boolean? = null): MutableList<ReferenceCode>? {
    return webTestClient.get()
      .uri("/reference-codes/group/$groupCode?${sort?.let { "sort=$sort&" } ?: ""}${activeOnly?.let { "&activeOnly=$activeOnly" } ?: ""}")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(ReferenceCode::class.java)
      .returnResult().responseBody
  }

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

  fun <T> getBadResponseErrorsWithPatch(request: T, uri: URI) = webTestClient.patch()
    .uri(uri.toString())
    .accept(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
    .bodyValue(request!!)
    .exchange()
    .expectStatus()
    .isBadRequest
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(ErrorResponse::class.java)
    .returnResult().responseBody!!

  fun createAContactPhone(contactId: Long, request: CreatePhoneRequest): ContactPhoneDetails {
    return webTestClient.post()
      .uri("/contact/$contactId/phone")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised())
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isCreated
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactPhoneDetails::class.java)
      .returnResult().responseBody!!
  }

  fun updateAContactPhone(contactId: Long, contactPhoneId: Long, request: UpdatePhoneRequest): ContactPhoneDetails {
    return webTestClient.put()
      .uri("/contact/$contactId/phone/$contactPhoneId")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised())
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactPhoneDetails::class.java)
      .returnResult().responseBody!!
  }

  fun getContactPhone(contactId: Long, contactPhoneId: Long): ContactPhoneDetails {
    return webTestClient.get()
      .uri("/contact/$contactId/phone/$contactPhoneId")
      .accept(MediaType.APPLICATION_JSON)
      .headers(authorised())
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactPhoneDetails::class.java)
      .returnResult().responseBody!!
  }

  fun createAContactIdentity(contactId: Long, request: CreateIdentityRequest): ContactIdentityDetails {
    return webTestClient.post()
      .uri("/contact/$contactId/identity")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised())
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isCreated
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactIdentityDetails::class.java)
      .returnResult().responseBody!!
  }

  fun updateAContactIdentity(contactId: Long, contactIdentityId: Long, request: UpdateIdentityRequest): ContactIdentityDetails {
    return webTestClient.put()
      .uri("/contact/$contactId/identity/$contactIdentityId")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised())
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactIdentityDetails::class.java)
      .returnResult().responseBody!!
  }

  fun getContactIdentity(contactId: Long, contactIdentityId: Long): ContactIdentityDetails {
    return webTestClient.get()
      .uri("/contact/$contactId/identity/$contactIdentityId")
      .accept(MediaType.APPLICATION_JSON)
      .headers(authorised())
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactIdentityDetails::class.java)
      .returnResult().responseBody!!
  }
  fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  private fun authorised() = setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN"))

  fun migrateAContact(request: MigrateContactRequest) =
    webTestClient.post()
      .uri("/migrate/contact")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_PRISONER_CONTACTS__RW")))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(MigrateContactResponse::class.java)
      .returnResult().responseBody!!

  data class ContactSearchResponse(
    val content: List<ContactSearchResultItem>,
    val pageable: ReturnedPageable,
    val last: Boolean,
    val totalPages: Int,
    val totalElements: Int,
    val first: Boolean,
    val size: Int,
    val number: Int,
    val sort: ReturnedSort,
    val numberOfElements: Int,
    val empty: Boolean,
  )
  data class PrisonerContactSummaryResponse(
    val content: List<PrisonerContactSummary>,
    val pageable: ReturnedPageable,
    val last: Boolean,
    val totalPages: Int,
    val totalElements: Int,
    val first: Boolean,
    val size: Int,
    val number: Int,
    val sort: ReturnedSort,
    val numberOfElements: Int,
    val empty: Boolean,
  )

  data class ReturnedPageable(
    val pageNumber: Int,
    val pageSize: Int,
    val sort: ReturnedSort,
    val offset: Int,
    val unpaged: Boolean,
    val paged: Boolean,
  )

  data class ReturnedSort(
    val empty: Boolean,
    val unsorted: Boolean,
    val sorted: Boolean,
  )
}
