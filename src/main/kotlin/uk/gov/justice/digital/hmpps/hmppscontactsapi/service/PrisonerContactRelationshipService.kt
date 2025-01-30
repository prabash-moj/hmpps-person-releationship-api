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

  fun getById(prisonerContactId: Long): PrisonerContactRelationshipDetails = prisonerContactSummaryRepository.findById(prisonerContactId)
    .orElseThrow { EntityNotFoundException("prisoner contact relationship with id $prisonerContactId not found") }.toRelationshipModel()

  fun PrisonerContactSummaryEntity.toRelationshipModel(): PrisonerContactRelationshipDetails = PrisonerContactRelationshipDetails(
    prisonerContactId = this.prisonerContactId,
    contactId = this.contactId,
    prisonerNumber = this.prisonerNumber,
    relationshipType = this.relationshipType,
    relationshipTypeDescription = this.relationshipTypeDescription,
    relationshipToPrisonerCode = this.relationshipToPrisoner,
    relationshipToPrisonerDescription = this.relationshipToPrisonerDescription ?: "",
    nextOfKin = this.nextOfKin,
    emergencyContact = this.emergencyContact,
    isRelationshipActive = this.active,
    isApprovedVisitor = this.approvedVisitor,
    comments = this.comments,
  )
}
