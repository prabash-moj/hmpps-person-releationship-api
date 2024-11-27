package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppscontactsapi.facade.EstateWideRestrictionsFacade
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.swagger.AuthApiResponses
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "Estate Wide Restrictions", description = "Estate wide restrictions for a contact")
@RestController
@RequestMapping(value = ["contact/{contactId}/estate-wide-restrictions"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class EstateWideRestrictionController(
  val restrictionsFacade: EstateWideRestrictionsFacade,
) {

  @GetMapping
  @Operation(
    summary = "Get a contacts estate-wide restrictions",
    description = """
      Get a contacts estate-wide restrictions only. Estate-wide restrictions apply to all of a contacts relationships.

      Additional restrictions between the contact and specific prisoners may also apply.
      """,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Found the contact and their restrictions",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = ContactRestrictionDetails::class)),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "No contact with that id could be found",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN')")
  fun getEstateWideContactRestrictions(
    @PathVariable("contactId") @Parameter(
      name = "contactId",
      description = "The id of the contact",
      example = "123456",
    ) contactId: Long,
  ): List<ContactRestrictionDetails> {
    return restrictionsFacade.getEstateWideRestrictionsForContact(contactId)
  }

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Create new estate-wide restriction",
    description = "Creates a new estate-wide restriction for the specified contact",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Created the estate-wide restriction successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ContactRestrictionDetails::class),
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
        description = "Could not find the the contact this estate-wide restriction is for",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN')")
  fun createEstateWideRestriction(
    @PathVariable("contactId") @Parameter(
      name = "contactId",
      description = "The id of the contact",
      example = "123456",
    ) contactId: Long,
    @Valid @RequestBody request: CreateContactRestrictionRequest,
  ): ResponseEntity<Any> {
    val created = restrictionsFacade.createEstateWideRestriction(contactId, request)
    return ResponseEntity
      .status(HttpStatus.CREATED)
      .body(created)
  }

  @PutMapping("/{contactRestrictionId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Update estate-wide restriction",
    description = "Updates an estate-wide restriction for the specified contact and restriction id",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Updated the estate-wide restriction successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ContactRestrictionDetails::class),
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
        description = "Could not find the the contact or estate-wide restriction",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN')")
  fun updateEstateWideRestriction(
    @PathVariable("contactId") @Parameter(
      name = "contactId",
      description = "The id of the contact",
      example = "123456",
    ) contactId: Long,
    @PathVariable("contactRestrictionId") @Parameter(
      name = "contactRestrictionId",
      description = "The id of the estate-wide restriction",
      example = "123456",
    ) contactRestrictionId: Long,
    @Valid @RequestBody request: UpdateContactRestrictionRequest,
  ): ContactRestrictionDetails {
    return restrictionsFacade.updateEstateWideRestriction(contactId, contactRestrictionId, request)
  }
}
