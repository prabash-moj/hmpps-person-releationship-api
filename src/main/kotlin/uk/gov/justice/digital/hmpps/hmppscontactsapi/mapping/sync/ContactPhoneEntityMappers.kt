package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactPhoneEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.CreateContactPhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.ContactPhone

fun ContactPhoneEntity.toModel(): ContactPhone {
  return ContactPhone(
    contactPhoneId = this.contactPhoneId,
    contactId = this.contactId!!,
    phoneType = this.phoneType,
    phoneNumber = this.phoneNumber!!,
    extNumber = this.extNumber,
    primaryPhone = this.primaryPhone,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
    amendedBy = this.amendedBy,
    amendedTime = this.amendedTime,
  )
}

fun List<ContactPhoneEntity>.toModel() = map { it.toModel() }

fun CreateContactPhoneRequest.toEntity() = ContactPhoneEntity(
  contactPhoneId = 0L,
  contactId = contactId,
  phoneType = phoneType,
  phoneNumber = phoneNumber,
  extNumber = extNumber,
  primaryPhone = primaryPhone,
  createdBy = createdBy,
  createdTime = createdTime,
)
