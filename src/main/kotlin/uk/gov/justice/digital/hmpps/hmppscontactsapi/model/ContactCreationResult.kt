package uk.gov.justice.digital.hmpps.hmppscontactsapi.model

import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactDetails

data class ContactCreationResult(val createdContact: ContactDetails, val createdRelationship: Long?)
