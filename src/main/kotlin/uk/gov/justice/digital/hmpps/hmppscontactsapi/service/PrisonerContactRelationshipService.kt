package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactSummaryEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRelationshipDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactSummaryRepository

@Service
class PrisonerContactRelationshipService(
  private val prisonerContactSummaryRepository: PrisonerContactSummaryRepository,
) {

  fun getById(prisonerContactId: Long): PrisonerContactRelationshipDetails {
    return prisonerContactSummaryRepository.findById(prisonerContactId)
      .orElseThrow { EntityNotFoundException("prisoner contact relationship with id $prisonerContactId not found") }.toRelationshipModel()
  }

  fun PrisonerContactSummaryEntity.toRelationshipModel(): PrisonerContactRelationshipDetails {
    return PrisonerContactRelationshipDetails(
      prisonerContactId = this.prisonerContactId,
      contactId = this.contactId,
      prisonerNumber = this.prisonerNumber,
      relationshipCode = this.relationshipType,
      relationshipDescription = this.relationshipDescription ?: "",
      nextOfKin = this.nextOfKin,
      emergencyContact = this.emergencyContact,
      isRelationshipActive = this.active,
      comments = this.comments,
    )
  }
}
