package uk.gov.justice.digital.hmpps.hmppscontactsapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.HmppsQueueOutboundEventsPublisher
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsPublisher
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@Configuration
class EventConfiguration {

  @Bean
  fun outboundEventsPublisher(hmppsQueueService: HmppsQueueService, mapper: ObjectMapper, features: FeatureSwitches): OutboundEventsPublisher = HmppsQueueOutboundEventsPublisher(hmppsQueueService, mapper, features)
}
