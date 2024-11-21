package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactIdentityEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateContactIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.SyncContactIdentity

fun ContactIdentityEntity.toModel(): SyncContactIdentity {
  return SyncContactIdentity(
    contactIdentityId = this.contactIdentityId,
    contactId = this.contactId!!,
    identityType = this.identityType,
    identityValue = this.identityValue!!,
    issuingAuthority = this.issuingAuthority,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
    updatedBy = this.amendedBy,
    updatedTime = this.amendedTime,
  )
}

fun List<ContactIdentityEntity>.toModel() = map { it.toModel() }

fun SyncCreateContactIdentityRequest.toEntity() = ContactIdentityEntity(
  contactIdentityId = 0L,
  contactId = contactId,
  identityType = identityType,
  identityValue = identityValue,
  issuingAuthority = issuingAuthority,
  createdBy = createdBy,
  createdTime = createdTime,
)
