package uk.gov.justice.digital.hmpps.hmppscontactsapi.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(enumAsRef = true)
enum class ReferenceCodeGroup(val isDocumented: Boolean) {
  DOMESTIC_STS(true),
  OFF_RELATION(true),
  ID_TYPE(true),
  LANGUAGE(true),
  GENDER(true),
  RELATIONSHIP(true),
  CITY(true),
  COUNTY(true),
  CONTACT_TYPE(true),
  COUNTRY(true),
  ADDRESS_TYPE(true),
  PHONE_TYPE(true),
  RESTRICTION(true),
  TITLE(true),
  TEST_TYPE(false),
}
