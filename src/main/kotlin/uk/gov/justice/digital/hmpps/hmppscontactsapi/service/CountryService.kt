package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Country
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.CountryRepository

@Service
class CountryService(private val countryRepository: CountryRepository) {

  @Transactional(readOnly = true)
  fun getCountryById(id: Long): Country =
    countryRepository.findByCountryId(id)
      .orElseThrow { EntityNotFoundException("Country with id $id not found") }.toModel()

  @Transactional(readOnly = true)
  fun getCountryByNomisCode(code: String): Country =
    countryRepository.findByNomisCode(code)
      .orElseThrow { EntityNotFoundException("Country with nomis code $code not found") }.toModel()

  @Transactional(readOnly = true)
  fun getCountryByIsoAlpha2(code: String): Country =
    countryRepository.findByIsoAlpha2(code)
      .orElseThrow { EntityNotFoundException("Country with iso alpha code 2 $code not found") }.toModel()

  @Transactional(readOnly = true)
  fun getCountryByIsoAlpha3(code: String): Country =
    countryRepository.findByIsoAlpha3(code)
      .orElseThrow { EntityNotFoundException("Country with iso alpha code 3 $code not found") }.toModel()

  @Transactional(readOnly = true)
  fun getAllCountries(): List<Country> = countryRepository.findAll().toModel()
}
