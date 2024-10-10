package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response

import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

// This is not directly used but are required to make Swagger produce a spec with page and content fields typed correctly
class PrisonerContactSummaryPage(content: List<PrisonerContactSummary>, pageable: Pageable, total: Long) :
  PageImpl<PrisonerContactSummary>(content, pageable, total)
