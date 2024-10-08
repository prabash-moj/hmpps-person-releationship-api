package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEmailDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactEmailDetails

fun ContactEmailDetailsEntity.toModel(): ContactEmailDetails = ContactEmailDetails(
  this.contactEmailId,
  this.contactId,
  this.emailType,
  this.emailTypeDescription,
  this.emailAddress,
  this.primaryEmail,
  this.createdBy,
  this.createdTime,
  this.amendedBy,
  this.amendedTime,
)
