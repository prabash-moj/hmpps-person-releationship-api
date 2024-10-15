package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ReferenceCodeRepository

@Service
class ReferenceCodeService(private val referenceCodeRepository: ReferenceCodeRepository) {
  fun getReferenceDataByGroup(groupCode: String, sort: Sort): List<ReferenceCode> =
    referenceCodeRepository.findAllByGroupCodeEquals(groupCode, sort).toModel()

  fun getReferenceDataByGroupAndCode(groupCode: String, code: String): ReferenceCode? =
    referenceCodeRepository.findByGroupCodeAndCode(groupCode, code)?.toModel()
}
