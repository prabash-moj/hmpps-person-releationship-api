package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppscontactsapi.facade.OrganisationFacade
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateOrganisationRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.OrganisationSearchRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.City
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Organisation
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.OrganisationSummaryResultItemPage
import uk.gov.justice.digital.hmpps.hmppscontactsapi.swagger.AuthApiResponses
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

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
  fun getOrganisationById(@PathVariable organisationId: Long): Organisation =
    organisationFacade.getOrganisationById(organisationId)

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Create new organisation",
    description = "Creates a new organisation",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Created the organisation successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = Organisation::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "The request has invalid or missing fields",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN','ROLE_CONTACTS__RW')")
  fun createOrganisation(
    @Valid @RequestBody request: CreateOrganisationRequest,
  ): ResponseEntity<Organisation> {
    return organisationFacade.create(request)
      .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }
  }

  @GetMapping("/search")
  @Operation(
    summary = "Search organisations",
    description = "Search all organisations by their name",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Organisations searched successfully. There may be no results.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = OrganisationSummaryResultItemPage::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN', 'ROLE_CONTACTS__R', 'ROLE_CONTACTS__RW')")
  fun searchOrganisations(
    @ModelAttribute @Valid @Parameter(
      description = "Search criteria",
      required = true,
    ) request: OrganisationSearchRequest,
    @Parameter(description = "Pageable configurations", required = false)
    @PageableDefault(sort = ["organisationName"], direction = Direction.ASC)
    pageable: Pageable,
  ) = organisationFacade.search(request, pageable)
}
