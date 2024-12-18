package uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearch

data class Prisoner(
  val prisonerNumber: String,
  val prisonId: String?,
  val lastName: String,
  val firstName: String,
  val middleNames: String?,
)
