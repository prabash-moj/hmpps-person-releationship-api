package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.ReferenceCodeGroup
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ReferenceCodeRepository

@Service
class ReferenceCodeService(private val referenceCodeRepository: ReferenceCodeRepository) {
  fun getReferenceDataByGroup(groupCode: ReferenceCodeGroup, sort: Sort, activeOnly: Boolean): List<ReferenceCode> {
    return if (activeOnly) {
      referenceCodeRepository.findAllByGroupCodeAndIsActiveEquals(groupCode, true, sort).toModel()
    } else {
      referenceCodeRepository.findAllByGroupCodeEquals(groupCode, sort).toModel()
    }
  }

  fun getReferenceDataByGroupAndCode(groupCode: ReferenceCodeGroup, code: String): ReferenceCode? =
    referenceCodeRepository.findByGroupCodeAndCode(groupCode, code)?.toModel()
}
