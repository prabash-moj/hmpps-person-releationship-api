package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactIdentityEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactIdentity

fun ContactIdentityEntity.toModel(): ContactIdentity {
  return ContactIdentity(
    contactIdentityId = this.contactIdentityId,
    contactId = this.contactId!!,
    identityType = this.identityType,
    identityValue = this.identityValue!!,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
    amendedBy = this.amendedBy,
    amendedTime = this.amendedTime,
  )
}

fun List<ContactIdentityEntity>.toModel() = map { it.toModel() }

fun CreateContactIdentityRequest.toEntity() = ContactIdentityEntity(
  contactIdentityId = 0L,
  contactId = contactId,
  identityType = identityType,
  identityValue = identityValue,
  createdBy = createdBy,
  createdTime = createdTime,
)
