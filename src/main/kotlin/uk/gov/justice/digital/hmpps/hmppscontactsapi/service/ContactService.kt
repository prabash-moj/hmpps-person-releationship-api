package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity.Companion.newContact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Contact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import kotlin.jvm.optionals.getOrNull

@Service
class ContactService(
  private val contactRepository: ContactRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun createContact(request: CreateContactRequest) {
    val newContact = newContact(
      title = request.title,
      lastName = request.lastName,
      firstName = request.firstName,
      middleName = request.middleName,
      dateOfBirth = request.dateOfBirth,
      createdBy = request.createdBy,
    )
    contactRepository.saveAndFlush(newContact)
      .also { logger.info("Created new contact {}", newContact) }
  }

  fun getContact(id: Long): Contact? {
    return contactRepository.findById(id).getOrNull()
      ?.let { entity ->
        Contact(
          id = entity.contactId,
          title = entity.title,
          lastName = entity.lastName,
          firstName = entity.firstName,
          middleName = entity.middleName,
          dateOfBirth = entity.dateOfBirth,
          createdBy = entity.createdBy,
          createdTime = entity.createdTime,
        )
      }
  }
}
