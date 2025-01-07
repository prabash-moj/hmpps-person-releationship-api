package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import org.springframework.core.convert.converter.Converter
import uk.gov.justice.digital.hmpps.hmppscontactsapi.exception.InvalidReferenceCodeGroupException
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.ReferenceCodeGroup

class ReferenceCodeGroupEnumConverter : Converter<String, ReferenceCodeGroup> {
  override fun convert(source: String): ReferenceCodeGroup {
    return ReferenceCodeGroup.entries.find { it.name === source } ?: throw InvalidReferenceCodeGroupException(source)
  }
}
