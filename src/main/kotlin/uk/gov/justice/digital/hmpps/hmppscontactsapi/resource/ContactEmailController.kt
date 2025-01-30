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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppscontactsapi.facade.ContactEmailFacade
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactEmailDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.swagger.AuthApiResponses
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "Contacts")
@RestController
@RequestMapping(value = ["contact/{contactId}/email"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class ContactEmailController(private val contactEmailFacade: ContactEmailFacade) {

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Create new contact email",
    description = "Creates a new email for the specified contact",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Created the contact email successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ContactEmailDetails::class),
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
        description = "Could not find the the contact this email is for",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN','ROLE_CONTACTS__RW')")
  fun createEmailAddress(
    @PathVariable("contactId") @Parameter(
      name = "contactId",
      description = "The id of the contact",
      example = "123456",
    ) contactId: Long,
    @Valid @RequestBody request: CreateEmailRequest,
  ): ResponseEntity<Any> {
    val created = contactEmailFacade.create(contactId, request)
    return ResponseEntity
      .status(HttpStatus.CREATED)
      .body(created)
  }

  @PutMapping("/{contactEmailId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Update contact email",
    description = "Updates an existing contact email by id",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Updated the contact email successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ContactEmailDetails::class),
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
        description = "Could not find the the contact or email by their ids",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN', 'ROLE_CONTACTS__RW')")
  fun updateEmailAddress(
    @PathVariable("contactId") @Parameter(
      name = "contactId",
      description = "The id of the contact",
      example = "123456",
    ) contactId: Long,
    @PathVariable("contactEmailId") @Parameter(
      name = "contactEmailId",
      description = "The id of the contact email",
      example = "987654",
    ) contactEmailId: Long,
    @Valid @RequestBody request: UpdateEmailRequest,
  ): ResponseEntity<Any> = ResponseEntity.ok(contactEmailFacade.update(contactId, contactEmailId, request))

  @GetMapping("/{contactEmailId}")
  @Operation(
    summary = "Get an email",
    description = "Gets a contacts email by id",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Found the email successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ContactEmailDetails::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Could not find the the contact or email this request is for",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN', 'ROLE_CONTACTS__R', 'ROLE_CONTACTS__RW')")
  fun getEmailAddress(
    @PathVariable("contactId") @Parameter(
      name = "contactId",
      description = "The id of the contact",
      example = "123456",
    ) contactId: Long,
    @PathVariable("contactEmailId") @Parameter(
      name = "contactEmailId",
      description = "The id of the contact email",
      example = "987654",
    ) contactEmailId: Long,
  ): ResponseEntity<Any> = ResponseEntity.ok(contactEmailFacade.get(contactId, contactEmailId))

  @DeleteMapping("/{contactEmailId}")
  @Operation(
    summary = "Delete contact email",
    description = "Deletes an existing contact email by id",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Deleted the contact email successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ContactEmailDetails::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Could not find the the contact or email by their ids",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN','ROLE_CONTACTS__RW')")
  fun deleteEmailAddress(
    @PathVariable("contactId") @Parameter(
      name = "contactId",
      description = "The id of the contact",
      example = "123456",
    ) contactId: Long,
    @PathVariable("contactEmailId") @Parameter(
      name = "contactEmailId",
      description = "The id of the contact email",
      example = "987654",
    ) contactEmailId: Long,
  ): ResponseEntity<Any> {
    contactEmailFacade.delete(contactId, contactEmailId)
    return ResponseEntity.noContent().build()
  }
}
