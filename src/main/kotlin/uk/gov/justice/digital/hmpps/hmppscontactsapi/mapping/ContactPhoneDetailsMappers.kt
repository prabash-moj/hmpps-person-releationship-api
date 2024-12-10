package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactPhoneDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactPhoneDetails

fun ContactPhoneDetailsEntity.toModel(): ContactPhoneDetails = ContactPhoneDetails(
  contactPhoneId = this.contactPhoneId,
  contactId = this.contactId,
  phoneType = this.phoneType,
  phoneTypeDescription = this.phoneTypeDescription,
  phoneNumber = this.phoneNumber,
  extNumber = this.extNumber,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  updatedBy = this.updatedBy,
  updatedTime = this.updatedTime,
)
