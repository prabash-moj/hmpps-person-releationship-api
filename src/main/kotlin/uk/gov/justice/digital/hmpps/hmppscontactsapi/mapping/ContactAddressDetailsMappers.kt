package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactAddressDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactAddressDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactPhoneNumberDetails

fun ContactAddressDetailsEntity.toModel(phoneNumbers: List<ContactPhoneNumberDetails>): ContactAddressDetails {
  return ContactAddressDetails(
    contactAddressId = this.contactAddressId,
    contactId = this.contactId,
    addressType = this.addressType,
    addressTypeDescription = this.addressTypeDescription,
    primaryAddress = this.primaryAddress,
    flat = this.flat,
    property = this.property,
    street = this.street,
    area = this.area,
    cityCode = this.cityCode,
    cityDescription = this.cityDescription,
    countyCode = this.countyCode,
    countyDescription = this.countyDescription,
    postcode = this.postCode,
    countryCode = this.countryCode,
    countryDescription = this.countryDescription,
    verified = this.verified,
    verifiedBy = this.verifiedBy,
    verifiedTime = this.verifiedTime,
    mailFlag = this.mailFlag,
    startDate = this.startDate,
    endDate = this.endDate,
    noFixedAddress = this.noFixedAddress,
    comments = this.comments,
    phoneNumbers = phoneNumbers,
    createdBy = this.createdBy,
    createdTime = this.createdTime,
    amendedBy = this.amendedBy,
    amendedTime = this.amendedTime,
  )
}
