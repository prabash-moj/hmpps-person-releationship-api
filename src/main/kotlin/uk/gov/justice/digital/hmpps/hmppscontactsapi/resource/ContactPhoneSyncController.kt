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
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactPhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactPhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.SyncContactPhoneService

@Tag(name = "Sync endpoints")
@RestController
@RequestMapping(value = ["/sync"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ContactPhoneSyncController(
  val syncService: SyncContactPhoneService,
) {
  @GetMapping(path = ["/contact-phone/{contactPhoneId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Returns the data for a contact phone by contactPhoneId",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to get the details for one contact phone.
      """,
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun getContactPhoneById(
    @Parameter(description = "The internal ID for a contact phone.", required = true)
    @PathVariable contactPhoneId: Long,
  ) = syncService.getContactPhoneById(contactPhoneId)

  @DeleteMapping(path = ["/contact-phone/{contactPhoneId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Deletes one contact phone by internal ID",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to delete a contact phone.
      """,
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun deleteContactPhoneById(
    @Parameter(description = "The internal ID for the contact phone.", required = true)
    @PathVariable contactPhoneId: Long,
  ) = syncService.deleteContactPhone(contactPhoneId)

  @PutMapping(path = ["/contact-phone"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @Operation(
    summary = "Creates a new contact phone",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to create a contact phone and associate it with a contact.
      """,
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun createContactPhone(
    @Valid @RequestBody createContactPhoneRequest: CreateContactPhoneRequest,
  ) = syncService.createContactPhone(createContactPhoneRequest)

  @PostMapping(path = ["/contact-phone/{contactPhoneId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @Operation(
    summary = "Updates a contact phone with new or extra detail",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to update a contact phone.
      """,
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun updateContactPhone(
    @Parameter(description = "The internal ID for the contact phone.", required = true)
    @PathVariable contactPhoneId: Long,
    @Valid @RequestBody updateContactPhoneRequest: UpdateContactPhoneRequest,
  ) = syncService.updateContactPhone(contactPhoneId, updateContactPhoneRequest)
}
