package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactAddressEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.ContactAddress
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactAddressRequest

fun ContactAddressEntity.toModel(): ContactAddress {
  return ContactAddress(
    contactAddressId = this.contactAddressId,
    contactId = this.contactId!!,
    addressType = this.addressType,
    primaryAddress = this.primaryAddress,
    flat = this.flat,
    property = this.property,
    street = this.street,
    area = this.area,
    cityCode = this.cityCode,
    countyCode = this.countyCode,
    postcode = this.postCode,
    countryCode = this.countryCode,
    verified = this.verified,
    verifiedBy = this.verifiedBy,
    verifiedTime = this.verifiedTime,
    mailFlag = this.mailFlag,
    startDate = this.startDate,
    endDate = this.endDate,
    noFixedAddress = this.noFixedAddress,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
    amendedBy = this.amendedBy,
    amendedTime = this.amendedTime,
  )
}

fun List<ContactAddressEntity>.toModel() = map { it.toModel() }

fun CreateContactAddressRequest.toEntity(): ContactAddressEntity {
  return ContactAddressEntity(
    contactAddressId = 0L,
    contactId = this.contactId,
    addressType = this.addressType,
    primaryAddress = this.primaryAddress,
    flat = this.flat,
    property = this.property,
    street = this.street,
    area = this.area,
    cityCode = this.cityCode,
    countyCode = this.countyCode,
    postCode = this.postcode,
    countryCode = this.countryCode,
    verified = this.verified ?: false,
    mailFlag = this.mailFlag ?: false,
    startDate = this.startDate,
    endDate = this.endDate,
    noFixedAddress = this.noFixedAddress ?: false,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
  )
}
