package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.OrganisationTypeDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.OrganisationTypeId

@Repository
interface OrganisationTypeDetailsRepository : JpaRepository<OrganisationTypeDetailsEntity, OrganisationTypeId> {
  fun findByIdOrganisationId(organisationId: Long): List<OrganisationTypeDetailsEntity>
}
