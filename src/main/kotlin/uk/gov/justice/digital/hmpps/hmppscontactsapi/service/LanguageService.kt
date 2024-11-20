package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Language
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.LanguageRepository

@Service
class LanguageService(private val languageRepository: LanguageRepository) {

  @Transactional(readOnly = true)
  fun getLanguageById(id: Long): Language =
    languageRepository.findByLanguageId(id)
      .orElseThrow { EntityNotFoundException("Language with id $id not found") }.toModel()

  @Transactional(readOnly = true)
  fun getLanguageByNomisCode(code: String): Language =
    languageRepository.findByNomisCode(code)
      .orElseThrow { EntityNotFoundException("Language with nomis code $code not found") }.toModel()

  @Transactional(readOnly = true)
  fun getLanguageByIsoAlpha2(code: String): Language =
    languageRepository.findByIsoAlpha2(code)
      .orElseThrow { EntityNotFoundException("Language with alpha code 2 $code not found") }.toModel()

  @Transactional(readOnly = true)
  fun getLanguageByIsoAlpha3(code: String): Language =
    languageRepository.findByIsoAlpha3(code)
      .orElseThrow { EntityNotFoundException("Language with alpha code 3 $code not found") }.toModel()

  @Transactional(readOnly = true)
  fun getAllLanguages(): List<Language> = languageRepository.findAll().toModel()
}
