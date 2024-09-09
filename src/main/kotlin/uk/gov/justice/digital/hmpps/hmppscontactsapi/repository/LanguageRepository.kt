package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.LanguageEntity
import java.util.*

@org.springframework.stereotype.Repository
interface LanguageRepository : org.springframework.data.repository.Repository<LanguageEntity, Long> {

  fun findByLanguageId(languageId: Long): Optional<LanguageEntity>

  fun findByNomisCode(nomisCode: String): Optional<LanguageEntity>

  fun findByIsoAlpha2(isoAlpha2: String): Optional<LanguageEntity>

  fun findByIsoAlpha3(isoAlpha3: String): Optional<LanguageEntity>

  fun findAll(): List<LanguageEntity>
}
