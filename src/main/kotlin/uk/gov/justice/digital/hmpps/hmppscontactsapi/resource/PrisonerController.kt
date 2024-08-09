package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.PrisonerService

@Tag(name = "Get Prisoner")
@RestController
@RequestMapping(value = ["contacts"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PrisonerController(private val prisonerService: PrisonerService) {
  @GetMapping(value = ["/prisoner/{prisonerNumber}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @PreAuthorize("hasAnyRole('PRISONER_SEARCH')")
  fun getPrisoner(
    @PathVariable("prisonerNumber") prisonerNumber: String,
  ): Prisoner? = prisonerService.getPrisoner(prisonerNumber)
}
