package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "The details of a contact as an individual")
data class GetContactResponse(

  @Schema(description = "The id of the contact", example = "123456")
  val id: Long,

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

  @Schema(description = "Whether the contact is over 18, based on their date of birth if it is known", example = "YES")
  val estimatedIsOverEighteen: EstimatedIsOverEighteen?,

  @Schema(description = "The date the contact deceased, if known", example = "1980-01-01")
  val isDeceased: Boolean,

  @Schema(description = "The date the contact deceased, if known", example = "1980-01-01", nullable = true)
  val deceasedDate: LocalDate? = null,

  @Schema(description = "All addresses for the contact", nullable = true)
  val addresses: List<ContactAddressDetails>,

  @Schema(description = "The id of the user who created the contact", example = "JD000001")
  val createdBy: String,

  @Schema(description = "The timestamp of when the contact was created", example = "2024-01-01T00:00:00Z")
  val createdTime: LocalDateTime,
)
