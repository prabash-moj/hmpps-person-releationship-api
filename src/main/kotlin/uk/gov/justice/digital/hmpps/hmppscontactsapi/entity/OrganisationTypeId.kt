package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class OrganisationTypeId(val organisationId: Long, val organisationType: String) : Serializable
