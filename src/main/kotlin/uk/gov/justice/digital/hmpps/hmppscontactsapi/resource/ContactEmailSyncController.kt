package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.SyncContactEmailService

@Tag(name = "Sync endpoints")
@RestController
@RequestMapping(value = ["/sync"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ContactEmailSyncController(
  val syncService: SyncContactEmailService,
) {
  @GetMapping(path = ["/contact-email/{contactEmailId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Returns the data for a contact email by contactEmailId",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to get the details for one contact email.
      """,
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun getContactEmailById(
    @Parameter(description = "The internal ID for a contact email.", required = true)
    @PathVariable contactEmailId: Long,
  ) = syncService.getContactEmailById(contactEmailId)

  @DeleteMapping(path = ["/contact-email/{contactEmailId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Deletes one contact email by internal ID",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to delete a contact email.
      """,
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun deleteContactEmailById(
    @Parameter(description = "The internal ID for the contact email.", required = true)
    @PathVariable contactEmailId: Long,
  ) = syncService.deleteContactEmail(contactEmailId)

  @PutMapping(path = ["/contact-email"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @Operation(
    summary = "Creates a new contact email",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to create a contact email and associate it with a contact.
      """,
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun createContactEmail(
    @Valid @RequestBody createContactEmailRequest: CreateContactEmailRequest,
  ) = syncService.createContactEmail(createContactEmailRequest)

  @PostMapping(path = ["/contact-email/{contactEmailId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @Operation(
    summary = "Updates a contact email with new or extra detail",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to update a contact email.
      """,
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun updateContactEmail(
    @Parameter(description = "The internal ID for the contact email.", required = true)
    @PathVariable contactEmailId: Long,
    @Valid @RequestBody updateContactEmailRequest: UpdateContactEmailRequest,
  ) = syncService.updateContactEmail(contactEmailId, updateContactEmailRequest)
}
