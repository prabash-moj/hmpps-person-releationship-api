package uk.gov.justice.digital.hmpps.hmppscontactsapi.swagger

import io.swagger.v3.oas.annotations.Parameter

@Parameter(
  name = "prisonNumber",
  description = "The prison number of the prisoner who's contacts will be returned",
  example = "A1234BC",
)
annotation class PrisonNumberDoc
