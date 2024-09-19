package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.mapRelationship
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactSearchRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Contact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactSearch
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactSearchRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRepository
import kotlin.jvm.optionals.getOrNull

@Service
class ContactService(
  private val contactRepository: ContactRepository,
  private val prisonerContactRepository: PrisonerContactRepository,
  private val prisonerService: PrisonerService,
  val contactSearchRepository: ContactSearchRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun createContact(request: CreateContactRequest): Contact {
    validate(request)
    val newContact = request.toModel()
    val createdContact = contactRepository.saveAndFlush(newContact).toModel()
    val newRelationship = request.mapRelationship(createdContact)
      ?.let { prisonerContactRepository.saveAndFlush(it) }

    logger.info("Created new contact {}", createdContact)
    newRelationship?.let { logger.info("Created new relationship {}", newRelationship) }
    return createdContact
  }

  fun getContact(id: Long): Contact? {
    return contactRepository.findById(id).getOrNull()
      ?.toModel()
  }

  fun searchContacts(pageable: Pageable, request: ContactSearchRequest): Page<ContactSearch> =
    contactSearchRepository.searchContacts(request, pageable).toModel()

  private fun validate(request: CreateContactRequest) {
    if (request.relationship != null) {
      prisonerService.getPrisoner(request.relationship.prisonerNumber)
        ?: throw EntityNotFoundException("Prisoner number ${request.relationship.prisonerNumber} - not found")
    }
  }
}
