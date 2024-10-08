package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource.sync

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreateContactEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.UpdateContactEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.ContactEmail
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncContactEmailService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "Sync endpoints - contact email")
@RestController
@RequestMapping(value = ["/sync"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ContactEmailSyncController(
  val syncService: SyncContactEmailService,
) {
  @GetMapping(path = ["/contact-email/{contactEmailId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Returns the data for a contact email by contactEmailId",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to get the details for one contact email.
      """,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Found the contact email",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ContactEmail::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "No contact email reference with that id could be found",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun getContactEmailById(
    @Parameter(description = "The internal ID for a contact email.", required = true)
    @PathVariable contactEmailId: Long,
  ) = syncService.getContactEmailById(contactEmailId)

  @DeleteMapping(path = ["/contact-email/{contactEmailId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Deletes one contact email by internal ID",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to delete a contact email.
      """,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Successfully deleted contact email",
      ),
      ApiResponse(
        responseCode = "404",
        description = "No contact email reference with that id could be found",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun deleteContactEmailById(
    @Parameter(description = "The internal ID for the contact email.", required = true)
    @PathVariable contactEmailId: Long,
  ) = syncService.deleteContactEmail(contactEmailId)

  @PostMapping(path = ["/contact-email"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseBody
  @Operation(
    summary = "Creates a new contact email",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to create a contact email and associate it with a contact.
      """,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Successfully created contact email",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ContactEmail::class),
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
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun createContactEmail(
    @Valid @RequestBody createContactEmailRequest: CreateContactEmailRequest,
  ) = syncService.createContactEmail(createContactEmailRequest)

  @PutMapping(path = ["/contact-email/{contactEmailId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseBody
  @Operation(
    summary = "Updates a contact email with new or extra detail",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to update a contact email.
      """,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successfully updated contact email",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ContactEmail::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Contact email not found",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid input data",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun updateContactEmail(
    @Parameter(description = "The internal ID for the contact email.", required = true)
    @PathVariable contactEmailId: Long,
    @Valid @RequestBody updateContactEmailRequest: UpdateContactEmailRequest,
  ) = syncService.updateContactEmail(contactEmailId, updateContactEmailRequest)
}
