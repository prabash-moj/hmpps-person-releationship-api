package uk.gov.justice.digital.hmpps.hmppscontactsapi.exception

import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.ReferenceCodeGroup

class InvalidReferenceCodeGroupException(requestedGroup: String) : Exception(""""$requestedGroup" is not a valid reference code group. Valid groups are ${ReferenceCodeGroup.entries.filter { it.isDocumented }.joinToString(", ")}""")
