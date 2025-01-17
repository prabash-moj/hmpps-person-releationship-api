package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppscontactsapi.facade.OrganisationFacade
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.City
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Organisation
import uk.gov.justice.digital.hmpps.hmppscontactsapi.swagger.AuthApiResponses

@Tag(name = "Organisation")
@RestController
@RequestMapping(value = ["organisation"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class OrganisationController(private val organisationFacade: OrganisationFacade) {

  @GetMapping("/{organisationId}")
  @Operation(
    summary = "Get organisation",
    description = "Gets a organisation by their id",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Found the organisation",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = City::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "No organisation with that id could be found",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN', 'ROLE_CONTACTS__R', 'ROLE_CONTACTS__RW')")
  fun getOrganisationById(@PathVariable organisationId: Long): Organisation = organisationFacade.getOrganisationById(organisationId)
}
