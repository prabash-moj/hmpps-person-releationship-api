package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.OrganisationPhoneDetailsEntity

@Repository
interface OrganisationPhoneDetailsRepository : JpaRepository<OrganisationPhoneDetailsEntity, Long> {

  fun findByOrganisationId(organisationId: Long): List<OrganisationPhoneDetailsEntity>
}
