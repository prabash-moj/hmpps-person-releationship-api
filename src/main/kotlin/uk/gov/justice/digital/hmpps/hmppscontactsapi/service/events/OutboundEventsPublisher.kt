package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events

interface OutboundEventsPublisher {
  fun send(event: OutboundHMPPSDomainEvent)
}
