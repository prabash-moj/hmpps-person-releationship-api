package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRelationshipDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.PrisonerContactRelationshipService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.swagger.AuthApiResponses
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "prisoner-contact")
@RestController
@RequestMapping(value = ["prisoner-contact"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class PrisonerContactRelationshipController(
  private val prisonerContactRelationshipService: PrisonerContactRelationshipService,
) {

  @Operation(summary = "Endpoint to get a prisoner contact relationship by relationship id")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Prisoner Contact relationship",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = PrisonerContactRelationshipDetails::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The Prisoner contact relationship was not found.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  @GetMapping(value = ["/relationship/{prisonerContactId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN')")
  fun getPrisonerContactById(
    @PathVariable("prisonerContactId") @Parameter(
      name = "prisonerContactId",
      description = "The id of the prisoner contact relationship to be returned",
      example = "1L",
    ) prisonerContactId: Long,
  ): PrisonerContactRelationshipDetails = prisonerContactRelationshipService.getById(prisonerContactId)
}
