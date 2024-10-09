package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactIdentityEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreateContactIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.ContactIdentity

fun ContactIdentityEntity.toModel(): ContactIdentity {
  return ContactIdentity(
    contactIdentityId = this.contactIdentityId,
    contactId = this.contactId!!,
    identityType = this.identityType,
    identityValue = this.identityValue!!,
    issuingAuthority = this.issuingAuthority,
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
  issuingAuthority = issuingAuthority,
  createdBy = createdBy,
  createdTime = createdTime,
)
