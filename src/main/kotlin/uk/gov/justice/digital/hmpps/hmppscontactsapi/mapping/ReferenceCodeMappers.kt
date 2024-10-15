package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ReferenceCodeEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode

fun ReferenceCodeEntity.toModel() = ReferenceCode(
  referenceCodeId = referenceCodeId,
  groupCode = groupCode,
  code = code,
  displayOrder = displayOrder,
  description = description,
)

fun List<ReferenceCodeEntity>.toModel() = map { it.toModel() }
