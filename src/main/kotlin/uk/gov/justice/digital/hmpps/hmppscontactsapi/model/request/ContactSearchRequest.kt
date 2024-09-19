package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Pattern
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

@Schema(description = "Contact Search Request")
data class ContactSearchRequest(

  @Schema(description = "Last name of the contact", example = "Jones", nullable = false, maxLength = 12)
  @field:Pattern(regexp = "^[a-zA-Z0-9_ ]*$", message = "Special characters are not allowed for Last Name.")
  @field:NotBlank(message = "Last Name cannot be blank.")
  val lastName: String,

  @Schema(description = "First name of the contact", example = "Elton", nullable = true, maxLength = 12)
  @field:Pattern(regexp = "^[a-zA-Z0-9_ ]*$", message = "Special characters are not allowed for First Name.")
  val firstName: String?,

  @Schema(description = "Middle name of the contact", example = "Simon", nullable = true, maxLength = 12)
  @field:Pattern(regexp = "^[a-zA-Z0-9_ ]*$", message = "Special characters are not allowed for Middle Name.")
  val middleName: String?,

  @Schema(description = "Date of Birth of the contact in ISO format", example = "30/12/2010", nullable = true, format = "dd/MM/yyyy")
  @field:Past(message = "The date of birth must be in the past")
  @field:DateTimeFormat(pattern = "dd/MM/yyyy")
  val dateOfBirth: LocalDate?,
)
