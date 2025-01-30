package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.persistence.EntityNotFoundException
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
import uk.gov.justice.digital.hmpps.hmppscontactsapi.facade.ContactPhoneFacade
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreatePhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdatePhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactPhoneDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.swagger.AuthApiResponses
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "Contacts")
@RestController
@RequestMapping(value = ["contact/{contactId}/phone"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class ContactPhoneController(private val contactPhoneFacade: ContactPhoneFacade) {

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Create new contact phone number",
    description = "Creates a new phone number for the specified contact",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Created the contact phone successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ContactPhoneDetails::class),
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
        description = "Could not find the the contact this phone is for",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN', 'ROLE_CONTACTS__RW')")
  fun createPhone(
    @PathVariable("contactId") @Parameter(
      name = "contactId",
      description = "The id of the contact",
      example = "123456",
    ) contactId: Long,
    @Valid @RequestBody request: CreatePhoneRequest,
  ): ResponseEntity<Any> {
    val createdPhone = contactPhoneFacade.create(contactId, request)
    return ResponseEntity
      .status(HttpStatus.CREATED)
      .body(createdPhone)
  }

  @GetMapping("/{contactPhoneId}")
  @Operation(
    summary = "Get a phone number",
    description = "Gets a contacts phone number by id",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Found the phone successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ContactPhoneDetails::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Could not find the the contact or phone this request is for",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN', 'ROLE_CONTACTS__R', 'ROLE_CONTACTS__RW')")
  fun getPhone(
    @PathVariable("contactId") @Parameter(
      name = "contactId",
      description = "The id of the contact",
      example = "123456",
    ) contactId: Long,
    @PathVariable("contactPhoneId") @Parameter(
      name = "contactPhoneId",
      description = "The id of the contact phone",
      example = "987654",
    ) contactPhoneId: Long,
  ): ResponseEntity<Any> = contactPhoneFacade.get(contactId, contactPhoneId)
    ?.let { ResponseEntity.ok(it) }
    ?: throw EntityNotFoundException("Contact phone with id ($contactPhoneId) not found for contact ($contactId)")

  @PutMapping("/{contactPhoneId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Update contact phone number",
    description = "Updates an existing contact phone by id",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Updated the contact phone successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ContactPhoneDetails::class),
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
        description = "Could not find the the contact or phone by their ids",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN', 'ROLE_CONTACTS__RW')")
  fun updatePhone(
    @PathVariable("contactId") @Parameter(
      name = "contactId",
      description = "The id of the contact",
      example = "123456",
    ) contactId: Long,
    @PathVariable("contactPhoneId") @Parameter(
      name = "contactPhoneId",
      description = "The id of the contact phone",
      example = "987654",
    ) contactPhoneId: Long,
    @Valid @RequestBody request: UpdatePhoneRequest,
  ): ResponseEntity<Any> {
    val updatedPhone = contactPhoneFacade.update(contactId, contactPhoneId, request)
    return ResponseEntity.ok(updatedPhone)
  }

  @DeleteMapping("/{contactPhoneId}")
  @Operation(
    summary = "Delete contact phone number",
    description = "Deletes an existing contact phone by id",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Deleted the contact phone successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ContactPhoneDetails::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Could not find the the contact or phone by their ids",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_CONTACTS_ADMIN', 'ROLE_CONTACTS__RW')")
  fun deletePhone(
    @PathVariable("contactId") @Parameter(
      name = "contactId",
      description = "The id of the contact",
      example = "123456",
    ) contactId: Long,
    @PathVariable("contactPhoneId") @Parameter(
      name = "contactPhoneId",
      description = "The id of the contact phone",
      example = "987654",
    ) contactPhoneId: Long,
  ): ResponseEntity<Any> {
    contactPhoneFacade.delete(contactId, contactPhoneId)
    return ResponseEntity.noContent().build()
  }
}
