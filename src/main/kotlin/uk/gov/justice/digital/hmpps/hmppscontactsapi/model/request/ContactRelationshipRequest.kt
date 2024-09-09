package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class ContactRelationshipRequest(

  @Schema(description = "Prisoner number (NOMS ID)", example = "A1234BC")
  val prisonerNumber: String,

  @Schema(description = "The relationship code between the prisoner and the contact", example = "FRI")
  val relationshipCode: String,

  @Schema(description = "Whether they are the next of kin for the prisoner", example = "true", required = true)
  @JsonProperty(required = true)
  val isNextOfKin: Boolean,

  @Schema(description = "Whether they are the emergency contact for the prisoner", example = "true", required = true)
  @JsonProperty(required = true)
  val isEmergencyContact: Boolean,

  @Schema(description = "Comments about the contacts relationship with the prisoner", example = "Some additional information", nullable = true)
  val comments: String? = null,

)
