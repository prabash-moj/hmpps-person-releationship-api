package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.CountryEntity
import java.util.Optional

@org.springframework.stereotype.Repository
interface CountryRepository : org.springframework.data.repository.Repository<CountryEntity, Long> {

  fun findByCountryId(countryId: Long): Optional<CountryEntity>

  fun findByNomisCode(nomisCode: String): Optional<CountryEntity>

  fun findByIsoAlpha2(isoAlpha2: String): Optional<CountryEntity>

  fun findByIsoAlpha3(isoAlpha3: String): Optional<CountryEntity>

  fun findAll(): List<CountryEntity>
}
