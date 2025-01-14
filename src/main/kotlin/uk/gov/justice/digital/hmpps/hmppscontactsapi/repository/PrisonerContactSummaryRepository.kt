package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactSummaryEntity

@Repository
interface PrisonerContactSummaryRepository : ReadOnlyRepository<PrisonerContactSummaryEntity, Long> {
  fun findByPrisonerNumberAndActiveAndRelationshipType(prisonerNumber: String, active: Boolean, relationshipType: String, pageable: Pageable): Page<PrisonerContactSummaryEntity>
  fun findByContactIdAndActive(contactId: Long, active: Boolean = true): List<PrisonerContactSummaryEntity>
}
