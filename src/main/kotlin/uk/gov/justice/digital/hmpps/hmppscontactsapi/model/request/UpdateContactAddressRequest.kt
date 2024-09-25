package uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Request to update a contact address")
data class UpdateContactAddressRequest(
  @Schema(description = "The id of the contact linked to this address", example = "123456")
  val contactId: Long,

  @Schema(description = "The type of address", example = "HOME")
  val addressType: String,

  @Schema(description = "True if this is the primary address otherwise false", example = "true")
  val primaryAddress: Boolean,

  @Schema(description = "Flat number or name", example = "Flat 2B", nullable = true)
  val flat: String? = null,

  @Schema(description = "Building or house number or name", example = "Mansion House", nullable = true)
  val property: String? = null,

  @Schema(description = "Street or road name", example = "Acacia Avenue", nullable = true)
  val street: String? = null,

  @Schema(description = "Area", example = "Morton Heights", nullable = true)
  val area: String? = null,

  @Schema(description = "City code - from NOMIS reference data", example = "BIRM", nullable = true)
  val cityCode: String? = null,

  @Schema(description = "County code - from NOMIS reference data", example = "WMIDS", nullable = true)
  val countyCode: String? = null,

  @Schema(description = "Postcode", example = "S13 4FH", nullable = true)
  val postcode: String? = null,

  @Schema(description = "Country code - from NOMIS reference data", example = "UK", nullable = true)
  val countryCode: String? = null,

  @Schema(description = "Whether the address has been verified by postcode lookup", example = "false")
  val verified: Boolean = false,

  @Schema(description = "The id of the user who updated the address", example = "JD000001")
  val updatedBy: String,

  @Schema(description = "The timestamp of when the address was changed", example = "2024-01-01T00:00:00Z")
  val updatedTime: LocalDateTime,
)
