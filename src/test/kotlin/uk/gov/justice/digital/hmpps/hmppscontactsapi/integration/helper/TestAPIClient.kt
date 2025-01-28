package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.helper

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.ReferenceCodeGroup
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.AddContactRelationshipRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactAddressPhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreatePhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.OrganisationSearchRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.PatchContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.PatchEmploymentsRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactAddressPhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactAddressRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdatePhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateRelationshipRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.migrate.MigrateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.migrate.MigrateOrganisationRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactAddressPhoneDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactAddressResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactCreationResult
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactEmailDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactIdentityDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactPhoneDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactSearchResultItem
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.EmploymentDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.LinkedPrisonerDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.OrganisationDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.OrganisationSummary
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PatchContactResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRelationshipDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRestrictionsResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactSummary
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.migrate.MigrateContactResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.migrate.MigrateOrganisationResponse
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.net.URI

class TestAPIClient(private val webTestClient: WebTestClient, private val jwtAuthHelper: JwtAuthorisationHelper) {

  fun createAContact(request: CreateContactRequest, role: String = "ROLE_CONTACTS_ADMIN"): ContactDetails {
    return createAContactWithARelationship(request, role).createdContact
  }

  fun createAContactWithARelationship(request: CreateContactRequest, role: String = "ROLE_CONTACTS_ADMIN"): ContactCreationResult {
    return webTestClient.post()
      .uri("/contact")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isCreated
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectHeader().valuesMatch("Location", "/contact/(\\d)+")
      .expectBody(ContactCreationResult::class.java)
      .returnResult().responseBody!!
  }

  fun patchAContact(request: Any, url: String, role: String = "ROLE_CONTACTS_ADMIN"): PatchContactResponse {
    return webTestClient.patch()
      .uri(url)
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(PatchContactResponse::class.java)
      .returnResult().responseBody!!
  }

  fun getContact(id: Long, role: String = "ROLE_CONTACTS_ADMIN"): ContactDetails {
    return webTestClient.get()
      .uri("/contact/$id")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf(role)))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactDetails::class.java)
      .returnResult().responseBody!!
  }

  fun getOrganisation(id: Long, role: String = "ROLE_CONTACTS_ADMIN"): OrganisationDetails {
    return webTestClient.get()
      .uri("/organisation/$id")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf(role)))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(OrganisationDetails::class.java)
      .returnResult().responseBody!!
  }

  fun searchOrganisations(request: OrganisationSearchRequest, page: Long? = null, size: Long? = null, sort: List<String> = emptyList(), role: String = "ROLE_CONTACTS_ADMIN"): OrganisationSearchResponse {
    return webTestClient.get()
      .uri("/organisation/search?name=${request.name}${page?.let {"&page=$page"} ?: "" }${size?.let {"&size=$size"} ?: "" }${sort.joinToString("") { "&sort=$it" }}")
      .accept(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(OrganisationSearchResponse::class.java)
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

  fun getReferenceCodes(
    groupCode: ReferenceCodeGroup,
    sort: String? = null,
    activeOnly: Boolean? = null,
    role: String = "ROLE_CONTACTS_ADMIN",
  ): MutableList<ReferenceCode>? {
    return webTestClient.get()
      .uri("/reference-codes/group/$groupCode?${sort?.let { "sort=$sort&" } ?: ""}${activeOnly?.let { "&activeOnly=$activeOnly" } ?: ""}")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf(role)))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(ReferenceCode::class.java)
      .returnResult().responseBody
  }

  fun addAContactRelationship(request: AddContactRelationshipRequest, role: String = "ROLE_CONTACTS_ADMIN"): PrisonerContactRelationshipDetails {
    return webTestClient.post()
      .uri("/prisoner-contact")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isCreated
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(PrisonerContactRelationshipDetails::class.java)
      .returnResult().responseBody!!
  }

  fun getSearchContactResults(uri: URI, role: String = "ROLE_CONTACTS_ADMIN") = webTestClient.get()
    .uri(uri.toString())
    .accept(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf(role)))
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

  fun createAContactPhone(contactId: Long, request: CreatePhoneRequest, role: String = "ROLE_CONTACTS_ADMIN"): ContactPhoneDetails {
    return webTestClient.post()
      .uri("/contact/$contactId/phone")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isCreated
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactPhoneDetails::class.java)
      .returnResult().responseBody!!
  }

  fun updateAContactPhone(contactId: Long, contactPhoneId: Long, request: UpdatePhoneRequest, role: String = "ROLE_CONTACTS_ADMIN"): ContactPhoneDetails {
    return webTestClient.put()
      .uri("/contact/$contactId/phone/$contactPhoneId")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactPhoneDetails::class.java)
      .returnResult().responseBody!!
  }

  fun getContactPhone(contactId: Long, contactPhoneId: Long, role: String = "ROLE_CONTACTS_ADMIN"): ContactPhoneDetails {
    return webTestClient.get()
      .uri("/contact/$contactId/phone/$contactPhoneId")
      .accept(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactPhoneDetails::class.java)
      .returnResult().responseBody!!
  }

  fun createAContactAddressPhone(
    contactId: Long,
    contactAddressId: Long,
    request: CreateContactAddressPhoneRequest,
    role: String = "ROLE_CONTACTS_ADMIN",
  ): ContactAddressPhoneDetails {
    return webTestClient.post()
      .uri("/contact/$contactId/address/$contactAddressId/phone")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isCreated
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactAddressPhoneDetails::class.java)
      .returnResult().responseBody!!
  }

  fun updateAContactAddressPhone(
    contactId: Long,
    contactAddressId: Long,
    contactAddressPhoneId: Long,
    request: UpdateContactAddressPhoneRequest,
    role: String = "ROLE_CONTACTS_ADMIN",
  ): ContactAddressPhoneDetails {
    return webTestClient.put()
      .uri("/contact/$contactId/address/$contactAddressId/phone/$contactAddressPhoneId")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactAddressPhoneDetails::class.java)
      .returnResult().responseBody!!
  }

  fun createAContactIdentity(contactId: Long, request: CreateIdentityRequest, role: String = "ROLE_CONTACTS_ADMIN"): ContactIdentityDetails {
    return webTestClient.post()
      .uri("/contact/$contactId/identity")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isCreated
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactIdentityDetails::class.java)
      .returnResult().responseBody!!
  }

  fun updateAContactIdentity(
    contactId: Long,
    contactIdentityId: Long,
    request: UpdateIdentityRequest,
    role: String = "ROLE_CONTACTS_ADMIN",
  ): ContactIdentityDetails {
    return webTestClient.put()
      .uri("/contact/$contactId/identity/$contactIdentityId")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactIdentityDetails::class.java)
      .returnResult().responseBody!!
  }

  fun getContactIdentity(contactId: Long, contactIdentityId: Long, role: String = "ROLE_CONTACTS_ADMIN"): ContactIdentityDetails {
    return webTestClient.get()
      .uri("/contact/$contactId/identity/$contactIdentityId")
      .accept(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactIdentityDetails::class.java)
      .returnResult().responseBody!!
  }

  fun createAContactEmail(contactId: Long, request: CreateEmailRequest, role: String = "ROLE_CONTACTS_ADMIN"): ContactEmailDetails {
    return webTestClient.post()
      .uri("/contact/$contactId/email")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isCreated
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactEmailDetails::class.java)
      .returnResult().responseBody!!
  }

  fun updateAContactEmail(contactId: Long, contactEmailId: Long, request: UpdateEmailRequest, role: String = "ROLE_CONTACTS_ADMIN"): ContactEmailDetails {
    return webTestClient.put()
      .uri("/contact/$contactId/email/$contactEmailId")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactEmailDetails::class.java)
      .returnResult().responseBody!!
  }

  fun updateRelationship(prisonerContactId: Long, request: UpdateRelationshipRequest, role: String = "ROLE_CONTACTS_ADMIN") {
    webTestClient.patch()
      .uri("/prisoner-contact/$prisonerContactId")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf(role)))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isNoContent
  }

  fun getContactEmail(contactId: Long, contactEmailId: Long, role: String = "ROLE_CONTACTS_ADMIN"): ContactEmailDetails {
    return webTestClient.get()
      .uri("/contact/$contactId/email/$contactEmailId")
      .accept(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactEmailDetails::class.java)
      .returnResult().responseBody!!
  }

  fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  fun migrateAContact(request: MigrateContactRequest, authRole: String = "ROLE_CONTACTS_MIGRATION") =
    webTestClient.post()
      .uri("/migrate/contact")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf(authRole)))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(MigrateContactResponse::class.java)
      .returnResult().responseBody!!

  fun migrateAnOrganisation(request: MigrateOrganisationRequest, authRole: String = "ROLE_CONTACTS_MIGRATION") =
    webTestClient.post()
      .uri("/migrate/organisation")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf(authRole)))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(MigrateOrganisationResponse::class.java)
      .returnResult().responseBody!!

  fun getContactGlobalRestrictions(contactId: Long, role: String = "ROLE_CONTACTS_ADMIN"): List<ContactRestrictionDetails> = webTestClient.get()
    .uri("/contact/$contactId/restriction")
    .accept(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf(role)))
    .exchange()
    .expectStatus().isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBodyList(ContactRestrictionDetails::class.java)
    .returnResult().responseBody!!

  fun createContactGlobalRestriction(
    contactId: Long,
    request: CreateContactRestrictionRequest,
    role: String = "ROLE_CONTACTS_ADMIN",
  ): ContactRestrictionDetails = webTestClient.post()
    .uri("/contact/$contactId/restriction")
    .accept(MediaType.APPLICATION_JSON)
    .contentType(MediaType.APPLICATION_JSON)
    .headers(authorised(role))
    .bodyValue(request)
    .exchange()
    .expectStatus()
    .isCreated
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(ContactRestrictionDetails::class.java)
    .returnResult().responseBody!!

  fun updateContactGlobalRestriction(
    contactId: Long,
    contactRestrictionId: Long,
    request: UpdateContactRestrictionRequest,
    role: String = "ROLE_CONTACTS_ADMIN",
  ): ContactRestrictionDetails = webTestClient.put()
    .uri("/contact/$contactId/restriction/$contactRestrictionId")
    .accept(MediaType.APPLICATION_JSON)
    .contentType(MediaType.APPLICATION_JSON)
    .headers(authorised(role))
    .bodyValue(request)
    .exchange()
    .expectStatus()
    .isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(ContactRestrictionDetails::class.java)
    .returnResult().responseBody!!

  fun getPrisonerContactRestrictions(prisonerContactId: Long, role: String = "ROLE_CONTACTS_ADMIN"): PrisonerContactRestrictionsResponse = webTestClient.get()
    .uri("/prisoner-contact/$prisonerContactId/restriction")
    .accept(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf(role)))
    .exchange()
    .expectStatus().isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(PrisonerContactRestrictionsResponse::class.java)
    .returnResult().responseBody!!

  fun createPrisonerContactRestriction(
    prisonerContactId: Long,
    request: CreatePrisonerContactRestrictionRequest,
    role: String = "ROLE_CONTACTS_ADMIN",
  ): PrisonerContactRestrictionDetails = webTestClient.post()
    .uri("/prisoner-contact/$prisonerContactId/restriction")
    .accept(MediaType.APPLICATION_JSON)
    .contentType(MediaType.APPLICATION_JSON)
    .headers(authorised(role))
    .bodyValue(request)
    .exchange()
    .expectStatus()
    .isCreated
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(PrisonerContactRestrictionDetails::class.java)
    .returnResult().responseBody!!

  fun updatePrisonerContactRestriction(
    prisonerContactId: Long,
    prisonerRestrictionContactId: Long,
    request: UpdatePrisonerContactRestrictionRequest,
    role: String = "ROLE_CONTACTS_ADMIN",
  ): PrisonerContactRestrictionDetails = webTestClient.put()
    .uri("/prisoner-contact/$prisonerContactId/restriction/$prisonerRestrictionContactId")
    .accept(MediaType.APPLICATION_JSON)
    .contentType(MediaType.APPLICATION_JSON)
    .headers(authorised(role))
    .bodyValue(request)
    .exchange()
    .expectStatus()
    .isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(PrisonerContactRestrictionDetails::class.java)
    .returnResult().responseBody!!

  fun createAContactAddress(contactId: Long, request: CreateContactAddressRequest, role: String = "ROLE_CONTACTS_ADMIN"): ContactAddressResponse {
    return webTestClient.post()
      .uri("/contact/$contactId/address")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isCreated
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactAddressResponse::class.java)
      .returnResult().responseBody!!
  }

  fun updateAContactAddress(contactId: Long, contactAddressId: Long, request: UpdateContactAddressRequest, role: String = "ROLE_CONTACTS_ADMIN"): ContactAddressResponse {
    return webTestClient.put()
      .uri("/contact/$contactId/address/$contactAddressId")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactAddressResponse::class.java)
      .returnResult().responseBody!!
  }

  fun patchAContactAddress(contactId: Long, contactAddressId: Long, request: PatchContactAddressRequest, role: String = "ROLE_CONTACTS_ADMIN"): ContactAddressResponse {
    return webTestClient.patch()
      .uri("/contact/$contactId/address/$contactAddressId")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ContactAddressResponse::class.java)
      .returnResult().responseBody!!
  }

  fun getLinkedPrisoners(contactId: Long): List<LinkedPrisonerDetails> = webTestClient.get()
    .uri("/contact/$contactId/linked-prisoners")
    .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
    .exchange()
    .expectStatus()
    .isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBodyList(LinkedPrisonerDetails::class.java)
    .returnResult().responseBody!!

  fun patchEmployments(contactId: Long, request: PatchEmploymentsRequest, role: String = "ROLE_CONTACTS_ADMIN"): List<EmploymentDetails> {
    return webTestClient.patch()
      .uri("/contact/$contactId/employment")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(authorised(role))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(EmploymentDetails::class.java)
      .returnResult().responseBody!!
  }
  private fun authorised(role: String = "ROLE_CONTACTS_ADMIN") = setAuthorisation(roles = listOf(role))

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

  data class OrganisationSearchResponse(
    val content: List<OrganisationSummary>,
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
