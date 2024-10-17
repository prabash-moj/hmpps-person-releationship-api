package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.now

@Entity
@Table(name = "contact")
data class ContactEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val contactId: Long,

  val title: String?,

  val firstName: String,

  val lastName: String,

  val middleNames: String?,

  val dateOfBirth: LocalDate?,

  @Enumerated(EnumType.STRING)
  val estimatedIsOverEighteen: EstimatedIsOverEighteen?,

  @Column(name = "deceased_flag")
  val isDeceased: Boolean,

  val deceasedDate: LocalDate?,

  @Column(updatable = false)
  val createdBy: String,

  @Column(updatable = false)
  @CreationTimestamp
  val createdTime: LocalDateTime = now(),
) {

  var placeOfBirth: String? = null

  var active: Boolean? = false

  var suspended: Boolean? = false

  var staffFlag: Boolean? = false

  var coronerNumber: String? = null

  var gender: String? = null

  var domesticStatus: String? = null

  var languageCode: String? = null

  var nationalityCode: String? = null

  var interpreterRequired: Boolean = false

  var amendedBy: String? = null

  var amendedTime: LocalDateTime? = null
}
