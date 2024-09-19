package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "contact_address")
data class ContactAddressEntity(
  @Id
  val contactAddressId: Long,

  val flat: String,

  val property: String,

  val street: String,

  val area: String,

  val cityCode: String,

  val countyCode: String,

  val postCode: String,

  val countryCode: String,

)
