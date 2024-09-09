package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactSummaryEntity

@Repository
interface PrisonerContactSummaryRepository : ReadOnlyRepository<PrisonerContactSummaryEntity, Long> {

  @Query(
    value = """
      FROM PrisonerContactSummaryEntity pcs
      WHERE pcs.prisonerNumber = :prisonerNumber
      AND pcs.active = :activeFlag
    """,
  )
  fun findPrisonerContacts(prisonerNumber: String, activeFlag: Boolean = true): List<PrisonerContactSummaryEntity>
}
