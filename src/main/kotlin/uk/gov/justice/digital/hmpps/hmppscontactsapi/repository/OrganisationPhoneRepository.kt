package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.OrganisationPhoneEntity

@Repository
interface OrganisationPhoneRepository : JpaRepository<OrganisationPhoneEntity, Long> {
  @Modifying
  @Query("delete from OrganisationPhoneEntity c where c.organisationId = :organisationId")
  fun deleteAllByOrganisationId(organisationId: Long): Int
}
