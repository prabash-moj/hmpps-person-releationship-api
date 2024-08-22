package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Contact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.ContactService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "Contact")
@RestController
@RequestMapping(value = ["contact"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ContactController(val contactService: ContactService) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Create a new contact",
    description = "Creates a new contact that is not yet associated with any prisoner.",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "201", description = "Created the contact successfully"),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
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
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN')")
  fun createContact(@Valid @RequestBody createContactRequest: CreateContactRequest): ResponseEntity<Any> {
    contactService.createContact(createContactRequest)
    return ResponseEntity.status(HttpStatus.CREATED).build()
  }

  @GetMapping("/{contactId}")
  @Operation(
    summary = "Get contact",
    description = "Gets a contact by their id",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Found the contact",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = Contact::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "No contact with that id could be found",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN')")
  fun getContact(
    @PathVariable("contactId") @Parameter(
      name = "contactId",
      description = "The id of the contact",
      example = "123456",
    ) contactId: Long,
  ): ResponseEntity<Any> {
    val contact = contactService.getContact(contactId)
    return if (contact != null) {
      ResponseEntity.ok(contact)
    } else {
      logger.info("Couldn't find contact with id '{}'", contactId)
      ResponseEntity.notFound().build()
    }
  }
}
