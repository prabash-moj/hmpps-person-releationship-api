package uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearch

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Component
class PrisonerSearchClient(private val prisonerSearchApiWebClient: WebClient) {

  fun getPrisoner(prisonerNumber: String): Prisoner? = prisonerSearchApiWebClient
    .get()
    .uri("/prisoner/{prisonerNumber}", prisonerNumber)
    .retrieve()
    .bodyToMono(Prisoner::class.java)
    .onErrorResume(WebClientResponseException.NotFound::class.java) { Mono.empty() }
    .block()
}
