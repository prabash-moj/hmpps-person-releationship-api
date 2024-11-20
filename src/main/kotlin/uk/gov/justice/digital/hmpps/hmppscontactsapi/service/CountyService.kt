package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.County
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.CountyRepository

@Service
class CountyService(private val countyRepository: CountyRepository) {

  @Transactional(readOnly = true)
  fun getCountyById(id: Long): County =
    countyRepository.findByCountyId(id)
      .orElseThrow { EntityNotFoundException("County with id $id not found") }.toModel()

  @Transactional(readOnly = true)
  fun getCountyByNomisCode(code: String): County =
    countyRepository.findByNomisCode(code)
      .orElseThrow { EntityNotFoundException("County with nomis code $code not found") }.toModel()

  @Transactional(readOnly = true)
  fun getAllCounties(): List<County> = countyRepository.findAll().toModel()
}
