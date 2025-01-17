package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Organisation
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.OrganisationRepository

@Service
class OrganisationService(private val organisationRepository: OrganisationRepository) {

  @Transactional(readOnly = true)
  fun getOrganisationById(id: Long): Organisation = organisationRepository.findById(id)
    .orElseThrow { EntityNotFoundException("Organisation with id $id not found") }.toModel()
}
