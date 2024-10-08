package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEmailEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreateContactEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.ContactEmail

fun ContactEmailEntity.toModel(): ContactEmail {
  return ContactEmail(
    contactEmailId = this.contactEmailId,
    contactId = this.contactId!!,
    emailType = this.emailType,
    emailAddress = this.emailAddress!!,
    primaryEmail = this.primaryEmail,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
    amendedBy = this.amendedBy,
    amendedTime = this.amendedTime,
  )
}

fun List<ContactEmailEntity>.toModel() = map { it.toModel() }

fun CreateContactEmailRequest.toEntity() = ContactEmailEntity(
  contactEmailId = 0L,
  contactId = contactId,
  emailType = emailType,
  emailAddress = emailAddress,
  primaryEmail = primaryEmail,
  createdBy = createdBy,
  createdTime = createdTime,
)
