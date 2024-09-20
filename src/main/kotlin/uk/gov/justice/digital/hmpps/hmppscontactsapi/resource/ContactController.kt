package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
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
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.AddContactRelationshipRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactSearchRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Contact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.ContactService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.swagger.AuthApiResponses
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.net.URI

@Tag(name = "Contact")
@RestController
@RequestMapping(value = ["contact"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
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
      ApiResponse(
        responseCode = "201",
        description = "Created the contact successfully",
        headers = [
          Header(
            name = "Location",
            description = "The URL where you can load the contact",
            example = "/contact/123456",
          ),
        ],
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = Contact::class),
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
        description = "Could not find the prisoner that this contact has a relationship to",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN')")
  fun createContact(@Valid @RequestBody createContactRequest: CreateContactRequest): ResponseEntity<Any> {
    val createdContact = contactService.createContact(createContactRequest)
    return ResponseEntity
      .created(URI.create("/contact/${createdContact.id}"))
      .body(createdContact)
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

  @PostMapping("/{contactId}/relationship", consumes = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Add a new contact relationship",
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
            schema = Schema(implementation = Contact::class),
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
    @PathVariable("contactId") @Parameter(
      name = "contactId",
      description = "The id of the contact",
      example = "123456",
    ) contactId: Long,
    @Valid @RequestBody relationshipRequest: AddContactRelationshipRequest,
  ) {
    contactService.addContactRelationship(contactId, relationshipRequest)
  }

  @GetMapping("/search")
  @Operation(
    summary = "Search contacts",
    description = "Search all contacts by their last name or first name or middle name or date of birth",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Found contacts",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = Contact::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN')")
  fun searchContacts(
    @Parameter(description = "Pageable configurations", required = false)
    @PageableDefault(sort = ["lastName", "firstName", "middleName", "createdTime"], direction = Direction.ASC)
    pageable: Pageable,
    @ModelAttribute @Valid @Parameter(description = "Contact search criteria", required = true) request: ContactSearchRequest,
  ) = contactService.searchContacts(pageable, request)
}
