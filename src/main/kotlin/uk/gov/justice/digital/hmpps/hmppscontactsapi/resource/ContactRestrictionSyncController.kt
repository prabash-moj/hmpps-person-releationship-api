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
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.SyncContactRestrictionService

@Tag(name = "Sync endpoints")
@RestController
@RequestMapping(value = ["/sync"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ContactRestrictionSyncController(
  val syncService: SyncContactRestrictionService,
) {
  @GetMapping(path = ["/contact-restriction/{contactRestrictionId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Returns the data for a contact restriction by contactRestrictionId",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to get the details for one contact restriction.
      """,
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun getContactRestrictionById(
    @Parameter(description = "The internal ID for a contact restriction.", required = true)
    @PathVariable contactRestrictionId: Long,
  ) = syncService.getContactRestrictionById(contactRestrictionId)

  @DeleteMapping(path = ["/contact-restriction/{contactRestrictionId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Deletes one contact restriction by internal ID",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to delete a contact restriction.
      """,
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun deleteContactRestrictionById(
    @Parameter(description = "The internal ID for the contact restriction.", required = true)
    @PathVariable contactRestrictionId: Long,
  ) = syncService.deleteContactRestriction(contactRestrictionId)

  @PutMapping(path = ["/contact-restriction"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @Operation(
    summary = "Creates a new contact restriction",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to create a contact restriction and associate it with a contact.
      """,
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun createContactRestriction(
    @Valid @RequestBody createContactRestrictionRequest: CreateContactRestrictionRequest,
  ) = syncService.createContactRestriction(createContactRestrictionRequest)

  @PostMapping(path = ["/contact-restriction/{contactRestrictionId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @Operation(
    summary = "Updates a contact restriction with new or extra detail",
    description = """
      Requires role: ROLE_CONTACTS_MIGRATION.
      Used to update a contact restriction.
      """,
  )
  @PreAuthorize("hasAnyRole('CONTACTS_MIGRATION')")
  fun updateContactRestriction(
    @Parameter(description = "The internal ID for the contact restriction.", required = true)
    @PathVariable contactRestrictionId: Long,
    @Valid @RequestBody updateContactRestrictionRequest: UpdateContactRestrictionRequest,
  ) = syncService.updateContactRestriction(contactRestrictionId, updateContactRestrictionRequest)
}
