package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity.Companion.newContact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.PrisonerContactSummary
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRepository

@Service
class ContactService(
  private val prisonerContactRepository: PrisonerContactRepository,
  private val contactRepository: ContactRepository,
  private val prisonerService: PrisonerService,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getAllContacts(prisonerNumber: String, active: Boolean): List<PrisonerContactSummary> {
    logger.info("Fetching all contacts")
    // Check if the prisoner number is valid
    prisonerService.getPrisoner(prisonerNumber)
      ?: throw EntityNotFoundException("the prisoner number $prisonerNumber not found")

    val contacts = prisonerContactRepository.findPrisonerContacts(prisonerNumber, active).toModel()
    logger.info("Found {} contacts", contacts.size)
    return contacts
  }

  @Transactional
  fun createContact(request: CreateContactRequest) {
    contactRepository.saveAndFlush(
      newContact(
        title = request.title,
        lastName = request.lastName,
        firstName = request.firstName,
        middleName = request.middleName,
        dateOfBirth = request.dateOfBirth,
        createdBy = request.createdBy,
      ),
    )
  }
}
