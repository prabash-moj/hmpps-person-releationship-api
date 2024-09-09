package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.CityEntity
import java.util.Optional

@org.springframework.stereotype.Repository
interface CityRepository : org.springframework.data.repository.Repository<CityEntity, Long> {

  fun findByCityId(cityId: Long): Optional<CityEntity>

  fun findByNomisCode(nomisCode: String): Optional<CityEntity>

  fun findAll(): List<CityEntity>
}
