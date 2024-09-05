package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity.Companion.newContact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.IsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Contact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import java.time.Clock
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

@Service
class ContactService(
  private val contactRepository: ContactRepository,
  private val clock: Clock,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun createContact(request: CreateContactRequest): Contact {
    val newContact = newContact(
      title = request.title,
      lastName = request.lastName,
      firstName = request.firstName,
      middleName = request.middleName,
      dateOfBirth = request.dateOfBirth,
      isOverEighteen = mapIsOverEighteen(request),
      createdBy = request.createdBy,
    )
    return mapEntityToContact(contactRepository.saveAndFlush(newContact))
      .also { logger.info("Created new contact {}", newContact) }
  }

  fun getContact(id: Long): Contact? {
    return contactRepository.findById(id).getOrNull()
      ?.let { entity -> mapEntityToContact(entity) }
  }

  private fun mapEntityToContact(entity: ContactEntity) = Contact(
    id = entity.contactId,
    title = entity.title,
    lastName = entity.lastName,
    firstName = entity.firstName,
    middleName = entity.middleName,
    dateOfBirth = entity.dateOfBirth,
    isOverEighteen = mapIsOverEighteen(entity),
    createdBy = entity.createdBy,
    createdTime = entity.createdTime,
  )

  private fun mapIsOverEighteen(entity: ContactEntity): IsOverEighteen {
    return if (entity.dateOfBirth != null) {
      if (!entity.dateOfBirth.isAfter(LocalDate.now(clock).minusYears(18))) {
        IsOverEighteen.YES
      } else {
        IsOverEighteen.NO
      }
    } else {
      when (entity.isOverEighteen) {
        true -> IsOverEighteen.YES
        false -> IsOverEighteen.NO
        null -> IsOverEighteen.DO_NOT_KNOW
      }
    }
  }

  private fun mapIsOverEighteen(request: CreateContactRequest): Boolean? {
    return if (request.dateOfBirth != null) {
      null
    } else {
      when (request.isOverEighteen) {
        IsOverEighteen.YES -> true
        IsOverEighteen.NO -> false
        IsOverEighteen.DO_NOT_KNOW -> null
        null -> null
      }
    }
  }
}
