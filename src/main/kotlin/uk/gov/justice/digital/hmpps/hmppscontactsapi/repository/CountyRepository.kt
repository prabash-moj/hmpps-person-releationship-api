package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.CountyEntity
import java.util.*

@org.springframework.stereotype.Repository
interface CountyRepository : org.springframework.data.repository.Repository<CountyEntity, Long> {

  fun findByCountyId(countyId: Long): Optional<CountyEntity>

  fun findByNomisCode(nomisCode: String): Optional<CountyEntity>

  fun findAll(): List<CountyEntity>
}
