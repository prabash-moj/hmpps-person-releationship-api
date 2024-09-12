package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ReferenceCodeRepository

@Service
class ReferenceCodeService(private val referenceCodeRepository: ReferenceCodeRepository) {
  fun getReferenceDataByGroup(groupCode: String): List<ReferenceCode> =
    referenceCodeRepository.findAllByGroupCodeEquals(groupCode).toModel()
}
