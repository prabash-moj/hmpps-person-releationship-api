package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.manage.users.User
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.OrganisationWithFixedIdEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.helper.TestAPIClient
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.wiremock.ManageUsersApiExtension
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.wiremock.ManageUsersApiExtension.Companion.manageUsersApiMockServer
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.wiremock.PrisonerSearchApiExtension
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.wiremock.PrisonerSearchApiExtension.Companion.prisonerSearchApiServer
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.OrganisationWithFixedIdRepository
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.time.LocalDateTime

@ExtendWith(HmppsAuthApiExtension::class, PrisonerSearchApiExtension::class, ManageUsersApiExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(TestConfiguration::class)
@ActiveProfiles("test")
abstract class IntegrationTestBase {

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @Autowired
  protected lateinit var stubEvents: StubOutboundEventsPublisher

  @Autowired
  private lateinit var organisationRepository: OrganisationWithFixedIdRepository

  protected lateinit var testAPIClient: TestAPIClient

  @BeforeEach
  fun setupTestApiClient() {
    testAPIClient = TestAPIClient(webTestClient, jwtAuthHelper)
    stubEvents.reset()
  }

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = testAPIClient.setAuthorisation(username = username, scopes = scopes, roles = roles)

  protected fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
    prisonerSearchApiServer.stubHealthPing(status)
  }

  protected fun stubPrisonSearchWithResponse(prisonerNumber: String) {
    prisonerSearchApiServer.stubGetPrisoner(prisonerNumber)
  }

  protected fun stubPrisonerSearch(prisoner: Prisoner) {
    prisonerSearchApiServer.stubGetPrisoner(prisoner)
  }

  protected fun stubPrisonSearchWithNotFoundResponse(prisonerNumber: String) {
    prisonerSearchApiServer.stubGetPrisonerReturnNoResults(prisonerNumber)
  }

  protected fun stubGetUserByUsername(user: User) {
    manageUsersApiMockServer.stubGetUser(user)
  }

  fun stubOrganisation(id: Long) {
    val entity = OrganisationWithFixedIdEntity(
      id,
      organisationName = "Name of $id",
      programmeNumber = null,
      vatNumber = null,
      caseloadId = null,
      comments = null,
      active = true,
      deactivatedDate = null,
      createdBy = "Created by",
      createdTime = LocalDateTime.now(),
      updatedBy = null,
      updatedTime = null,
    )
    organisationRepository.saveAndFlush(entity)
  }
}
