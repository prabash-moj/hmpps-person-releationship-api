package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.time.LocalDateTime.now

@Entity
@Table(name = "contact_address")
data class ContactAddressEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var contactAddressId: Long,

  val contactId: Long? = null,

  val addressType: String,

  val primaryAddress: Boolean = false,

  val flat: String? = null,

  val property: String? = null,

  val street: String? = null,

  val area: String? = null,

  val cityCode: String? = null,

  val countyCode: String? = null,

  val postCode: String? = null,

  val countryCode: String? = null,

  val verified: Boolean = false,

  val verifiedBy: String? = null,

  val verifiedTime: LocalDateTime? = null,

  val createdBy: String,

  @CreationTimestamp
  val createdTime: LocalDateTime = now(),
) {
  var amendedBy: String? = null
  var amendedTime: LocalDateTime? = null

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ContactAddressEntity

    return contactAddressId == other.contactAddressId
  }

  override fun hashCode(): Int {
    return contactAddressId.hashCode()
  }
}
