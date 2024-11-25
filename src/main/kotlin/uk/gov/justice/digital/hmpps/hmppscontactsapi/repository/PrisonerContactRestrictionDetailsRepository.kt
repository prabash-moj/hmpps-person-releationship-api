package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactRestrictionDetailsEntity

interface PrisonerContactRestrictionDetailsRepository : JpaRepository<PrisonerContactRestrictionDetailsEntity, Long> {

  fun findAllByPrisonerContactId(contactId: Long): List<PrisonerContactRestrictionDetailsEntity>
}
