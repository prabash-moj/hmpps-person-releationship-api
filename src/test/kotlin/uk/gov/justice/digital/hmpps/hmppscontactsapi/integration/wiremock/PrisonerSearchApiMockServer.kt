package uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.prisoner

class PrisonerSearchApiMockServer : MockServer(8092) {

  fun stubGetPrisoner(prisonerNumber: String, prisonId: String = "MDI") {
    stubGetPrisoner(prisoner(prisonerNumber, prisonId))
  }

  fun stubGetPrisoner(prisoner: Prisoner) {
    stubFor(
      get("/prisoner/${prisoner.prisonerNumber}")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              mapper.writeValueAsString(prisoner),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubGetPrisonerReturnNoResults(prisonNumber: String) {
    stubFor(
      get("/prisoner/$prisonNumber")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(404),
        ),
    )
  }
}

class PrisonerSearchApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val prisonerSearchApiServer = PrisonerSearchApiMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    prisonerSearchApiServer.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    prisonerSearchApiServer.resetAll()
  }

  override fun afterAll(context: ExtensionContext) {
    prisonerSearchApiServer.stop()
  }
}
