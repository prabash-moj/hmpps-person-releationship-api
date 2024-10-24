package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.patch.mapToResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.patch.PatchContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.patch.PatchContactResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import java.time.LocalDateTime

@Service
class ContactPatchService(
  private val contactRepository: ContactRepository,
  private val languageService: LanguageService,
) {

  @Transactional
  fun patch(id: Long, request: PatchContactRequest): PatchContactResponse {
    val contact = contactRepository.findById(id)
      .orElseThrow { EntityNotFoundException("Contact not found") }

    validateLanguageCode(request)
    validateInterpreterRequiredType(request)

    val changedContact = contact.patchRequest(request)

    val savedContact = contactRepository.saveAndFlush(changedContact)
    return savedContact.mapToResponse()
  }

  fun ContactEntity.patchRequest(
    request: PatchContactRequest,
  ): ContactEntity {
    val changedContact = this.copy().also {
      it.placeOfBirth = this.placeOfBirth
      it.active = this.active
      it.suspended = this.suspended
      it.staffFlag = this.staffFlag
      it.coronerNumber = this.coronerNumber
      it.gender = this.gender
      it.domesticStatus = this.domesticStatus
      it.nationalityCode = this.nationalityCode
      it.interpreterRequired = request.interpreterRequired.orElse(this.interpreterRequired)
      it.languageCode = request.languageCode.orElse(this.languageCode)
      it.amendedBy = request.updatedBy
      it.amendedTime = LocalDateTime.now()
    }

    return changedContact
  }

  private fun validateLanguageCode(request: PatchContactRequest) {
    if (request.languageCode.isPresent && request.languageCode.get() != null) {
      languageService.getLanguageByNomisCode(request.languageCode.get()!!)
    }
  }

  private fun validateInterpreterRequiredType(request: PatchContactRequest) {
    if (request.interpreterRequired.isPresent && request.interpreterRequired.get() == null) {
      throw ValidationException("Unsupported interpreter required type null.")
    }
  }
}
