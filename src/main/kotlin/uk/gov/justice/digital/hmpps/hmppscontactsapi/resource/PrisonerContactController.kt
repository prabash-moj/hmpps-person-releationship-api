package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppscontactsapi.facade.ContactFacade
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.AddContactRelationshipRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateRelationshipRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRelationshipDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRestrictionsResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.PrisonerContactRelationshipService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.RestrictionsService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.swagger.AuthApiResponses
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "prisoner-contact")
@RestController
@RequestMapping(value = ["prisoner-contact"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class PrisonerContactController(
  private val prisonerContactRelationshipService: PrisonerContactRelationshipService,
  private val contactFacade: ContactFacade,
  private val restrictionsService: RestrictionsService,
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
  @GetMapping(value = ["/{prisonerContactId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN')")
  fun getPrisonerContactById(
    @PathVariable("prisonerContactId") @Parameter(
      name = "prisonerContactId",
      description = "The id of the prisoner contact relationship to be returned",
      example = "1L",
    ) prisonerContactId: Long,
  ): PrisonerContactRelationshipDetails = prisonerContactRelationshipService.getById(prisonerContactId)

  @PatchMapping("{prisonerContactId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Update prisoner contact relationship",
    description = "Update the relationship between the contact and a prisoner.",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Updated the relationship successfully",
      ),
      ApiResponse(
        responseCode = "400",
        description = "The request has invalid or missing fields",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Could not find the prisoner contact that this relationship relates to",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  fun patchContactRelationship(
    @PathVariable("prisonerContactId") @Parameter(
      name = "prisonerContactId",
      description = "The id of the prisoner contact",
      example = "123456",
    ) prisonerContactId: Long,
    @Valid @RequestBody relationshipRequest: UpdateRelationshipRequest,
  ): ResponseEntity<Any> {
    contactFacade.patchRelationship(prisonerContactId, relationshipRequest)
    return ResponseEntity.noContent().build()
  }

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Add a new prisoner contact relationship",
    description = "Creates a new relationship between the contact and a prisoner.",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Created the relationship successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = PrisonerContactRelationshipDetails::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "The request has invalid or missing fields",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Could not find the prisoner or contact that this relationship relates to",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  fun addContactRelationship(
    @Valid @RequestBody relationshipRequest: AddContactRelationshipRequest,
  ): PrisonerContactRelationshipDetails {
    return contactFacade.addContactRelationship(relationshipRequest)
  }

  @Operation(
    summary = "Get the prisoner contact restrictions",
    description = """
      Get the restrictions that apply for this relationship.
      
      This includes prisoner-contact restrictions for this specific relationship only and any estate-wide restrictions for the contact.
      
      If the prisoner and contact have multiple relationships, the prisoner-contact restrictions for the other relationships will not be returned. 
    """,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Prisoner Contact relationship",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = PrisonerContactRestrictionsResponse::class),
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
  @GetMapping(value = ["/{prisonerContactId}/restrictions"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN')")
  fun getPrisonerContactRestrictionsByPrisonerContactId(
    @PathVariable("prisonerContactId") @Parameter(
      name = "prisonerContactId",
      description = "The id of the prisoner contact",
      example = "1L",
    ) prisonerContactId: Long,
  ): PrisonerContactRestrictionsResponse = restrictionsService.getPrisonerContactRestrictions(prisonerContactId)
}
