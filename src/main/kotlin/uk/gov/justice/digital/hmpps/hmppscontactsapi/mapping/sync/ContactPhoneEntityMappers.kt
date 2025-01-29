package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactPhoneEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateContactPhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.SyncContactPhone

fun ContactPhoneEntity.toModel(): SyncContactPhone = SyncContactPhone(
  contactPhoneId = this.contactPhoneId,
  contactId = this.contactId!!,
  phoneType = this.phoneType,
  phoneNumber = this.phoneNumber!!,
  extNumber = this.extNumber,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  updatedBy = this.updatedBy,
  updatedTime = this.updatedTime,
)

fun List<ContactPhoneEntity>.toModel() = map { it.toModel() }

fun SyncCreateContactPhoneRequest.toEntity() = ContactPhoneEntity(
  contactPhoneId = 0L,
  contactId = contactId,
  phoneType = phoneType,
  phoneNumber = phoneNumber,
  extNumber = extNumber,
  createdBy = createdBy,
  createdTime = createdTime,
)
