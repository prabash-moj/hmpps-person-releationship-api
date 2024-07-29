package uk.gov.justice.digital.hmpps.hmppscontactsapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HmppsContactsApi

fun main(args: Array<String>) {
  runApplication<HmppsContactsApi>(*args)
}
