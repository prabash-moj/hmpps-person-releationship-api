package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEmailEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactEmailDetails

fun ContactEmailEntity.toModel(): ContactEmailDetails = ContactEmailDetails(
  this.contactEmailId,
  this.contactId,
  this.emailAddress,
  this.createdBy,
  this.createdTime,
  this.amendedBy,
  this.amendedTime,
)
