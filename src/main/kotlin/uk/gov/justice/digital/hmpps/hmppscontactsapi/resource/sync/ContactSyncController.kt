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
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.UpdateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.Contact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncContactService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.swagger.AuthApiResponses
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "Sync endpoints - contact")
@RestController
@RequestMapping(value = ["/sync"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class ContactSyncController(
  val syncService: SyncContactService,
) {
  @GetMapping(path = ["/contact/{contactId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Returns the data for a contact by contactId",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to get the details for one contact.
      """,
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
        description = "No contact reference with that id could be found",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun getContactById(
    @Parameter(description = "The internal ID for a contact.", required = true)
    @PathVariable contactId: Long,
  ) = syncService.getContactById(contactId)

  @DeleteMapping(path = ["/contact/{contactId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Deletes one contact by internal ID",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to delete a contact.
      """,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Successfully deleted contact",
      ),
      ApiResponse(
        responseCode = "404",
        description = "No contact reference with that id could be found",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun deleteContactById(
    @Parameter(description = "The internal ID for the contact.", required = true)
    @PathVariable contactId: Long,
  ) = syncService.deleteContact(contactId)

  @PostMapping(path = ["/contact"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseBody
  @Operation(
    summary = "Creates a new contact",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to create a contact and associate it with a contact.
      """,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Successfully created contact",
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
    ],
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun createContact(
    @Valid @RequestBody createContactRequest: CreateContactRequest,
  ) = syncService.createContact(createContactRequest)

  @PutMapping(path = ["/contact/{contactId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseBody
  @Operation(
    summary = "Updates a contact with new or extra detail",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to update a contact.
      """,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successfully updated contact",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = Contact::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Contact not found",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid input data",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun updateContact(
    @Parameter(description = "The internal ID for the contact.", required = true)
    @PathVariable contactId: Long,
    @Valid @RequestBody updateContactRequest: UpdateContactRequest,
  ) = syncService.updateContact(contactId, updateContactRequest)
}
