package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactSummary
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactSummaryRepository

@Service
class PrisonerContactService(
  private val prisonerContactSummaryRepository: PrisonerContactSummaryRepository,
  private val prisonerService: PrisonerService,
) {
  fun getAllContacts(prisonerNumber: String, active: Boolean): List<PrisonerContactSummary> {
    prisonerService.getPrisoner(prisonerNumber)
      ?: throw EntityNotFoundException("Prisoner number $prisonerNumber - not found")
    return prisonerContactSummaryRepository.findPrisonerContacts(prisonerNumber, active).toModel()
  }
}
