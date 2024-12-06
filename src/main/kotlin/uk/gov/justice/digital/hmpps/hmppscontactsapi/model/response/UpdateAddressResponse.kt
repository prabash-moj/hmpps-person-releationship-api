package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response

data class UpdateAddressResponse(val updated: ContactAddressResponse, val otherUpdatedAddressIds: Set<Long>)
