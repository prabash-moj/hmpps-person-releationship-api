package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEmailEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateContactEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.SyncContactEmail

fun ContactEmailEntity.toModel(): SyncContactEmail = SyncContactEmail(
  contactEmailId = this.contactEmailId,
  contactId = this.contactId,
  emailAddress = this.emailAddress,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  updatedBy = this.updatedBy,
  updatedTime = this.updatedTime,
)

fun List<ContactEmailEntity>.toModel() = map { it.toModel() }

fun SyncCreateContactEmailRequest.toEntity() = ContactEmailEntity(
  contactEmailId = 0L,
  contactId = contactId,
  emailAddress = emailAddress,
  createdBy = createdBy,
  createdTime = createdTime,
)
