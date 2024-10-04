package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactPhoneDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactPhoneNumberDetails

fun ContactPhoneDetailsEntity.toModel(): ContactPhoneNumberDetails = ContactPhoneNumberDetails(
  contactPhoneId = this.contactPhoneId,
  contactId = this.contactId,
  phoneType = this.phoneType,
  phoneTypeDescription = this.phoneTypeDescription,
  phoneNumber = this.phoneNumber,
  extNumber = this.extNumber,
  primaryPhone = this.primaryPhone,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  amendedBy = this.amendedBy,
  amendedTime = this.amendedTime,
)
