package uk.gov.justice.digital.hmpps.hmppscontactsapi.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Request to create a new contact")
data class CreateContactRequest(

  @Schema(description = "The title of the contact, if any", example = "Mr", nullable = true)
  val title: String? = null,

  @Schema(description = "The last name of the contact", example = "Doe")
  val lastName: String,

  @Schema(description = "The first name of the contact", example = "John")
  val firstName: String,

  @Schema(description = "The middle name of the contact, if any", example = "William", nullable = true)
  val middleName: String? = null,

  @Schema(description = "The date of birth of the contact, if known", example = "1980-01-01", nullable = true)
  val dateOfBirth: LocalDate? = null,

  @Schema(description = "The id of the user creating the contact", example = "JD000001")
  val createdBy: String,
)
