package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactIdentityDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactIdentityDetails

fun ContactIdentityDetailsEntity.toModel(): ContactIdentityDetails = ContactIdentityDetails(
  contactIdentityId = this.contactIdentityId,
  contactId = this.contactId,
  identityType = this.identityType,
  identityTypeDescription = this.identityTypeDescription,
  identityValue = this.identityValue,
  issuingAuthority = this.issuingAuthority,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  amendedBy = this.amendedBy,
  amendedTime = this.amendedTime,
)
