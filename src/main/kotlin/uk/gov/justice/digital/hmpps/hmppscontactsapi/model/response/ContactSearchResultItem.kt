package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "The details of a contact as an individual")
data class ContactSearchResultItem(

  @Schema(description = "The id of the contact", example = "123456")
  val id: Long,

  @Schema(description = "The last name of the contact", example = "Doe")
  val lastName: String,

  @Schema(description = "The first name of the contact", example = "John")
  val firstName: String,

  @Schema(description = "The middle name of the contact, if any", example = "William", nullable = true)
  val middleName: String? = null,

  @Schema(description = "The date of birth of the contact, if known", example = "1980-01-01", nullable = true)
  val dateOfBirth: LocalDate? = null,

  @Schema(description = "The id of the user who created the contact", example = "JD000001")
  val createdBy: String? = null,

  @Schema(description = "The timestamp of when the contact was created", example = "2024-01-01T00:00:00Z")
  val createdTime: LocalDateTime? = null,

  @Schema(description = "The flat of the contact address, if known", example = "01", nullable = true)
  val flat: String? = null,

  @Schema(description = "The property of the contact address, if known", example = "01", nullable = true)
  val property: String? = null,

  @Schema(description = "The street of the contact address, if known", example = "Bluebell Crescent", nullable = true)
  val street: String? = null,

  @Schema(description = "The area of the contact address, if known", example = "Birmingham", nullable = true)
  val area: String? = null,

  @Schema(description = "The city code of the contact address, if known", example = "Birmingham", nullable = true)
  val cityCode: String? = null,

  @Schema(description = "The county code of the contact address, if known", example = "SYORKS", nullable = true)
  val countyCode: String? = null,

  @Schema(description = "The postcode of the contact address, if known", example = "B42 2QJ", nullable = true)
  val postCode: String? = null,

  @Schema(description = "The country code of the contact address, if known", example = "UK", nullable = true)
  val countryCode: String? = null,

)
