package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.City
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.CityRepository

@Service
class CityService(private val cityRepository: CityRepository) {

  @Transactional(readOnly = true)
  fun getCityById(id: Long): City = cityRepository.findByCityId(id)
    .orElseThrow { EntityNotFoundException("City with id $id not found") }.toModel()

  @Transactional(readOnly = true)
  fun getCityByNomisCode(code: String): City = cityRepository.findByNomisCode(code)
    .orElseThrow { EntityNotFoundException("City with code $code not found") }.toModel()

  @Transactional(readOnly = true)
  fun getAllCountries(): List<City> = cityRepository.findAll().toModel()
}
