package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

@Schema(description = "Request to create a new contact")
data class CreateContactRequest(

  @Schema(description = "The title of the contact, if any", example = "Mr", nullable = true, maxLength = 12)
  @field:Size(max = 12, message = "title must be <= 12 characters")
  val title: String? = null,

  @Schema(description = "The last name of the contact", example = "Doe", maxLength = 35)
  @field:Size(max = 35, message = "lastName must be <= 35 characters")
  val lastName: String,

  @Schema(description = "The first name of the contact", example = "John", maxLength = 35)
  @field:Size(max = 35, message = "firstName must be <= 35 characters")
  val firstName: String,

  @Schema(description = "The middle name of the contact, if any", example = "William", nullable = true, maxLength = 35)
  @field:Size(max = 35, message = "middleName must be <= 35 characters")
  val middleName: String? = null,

  @Schema(description = "The date of birth of the contact, if known", example = "1980-01-01", nullable = true, format = "yyyy-MM-dd")
  @field:DateTimeFormat(pattern = "yyyy-MM-dd")
  val dateOfBirth: LocalDate? = null,

  @Schema(description = "If the date of birth is not known, this indicates whether they are believed to be over 18 or not", example = "YES", nullable = true)
  val isOverEighteen: IsOverEighteen? = null,

  @Schema(description = "The id of the user creating the contact", example = "JD000001", maxLength = 100)
  @field:Size(max = 100, message = "createdBy must be <= 100 characters")
  val createdBy: String,
)
