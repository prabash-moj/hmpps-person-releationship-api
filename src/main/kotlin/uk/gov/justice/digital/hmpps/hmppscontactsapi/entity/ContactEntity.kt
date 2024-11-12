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

  val staffFlag: Boolean = false,

  val remitterFlag: Boolean = false,

  val gender: String? = null,

  val domesticStatus: String? = null,

  val languageCode: String? = null,

  val interpreterRequired: Boolean = false,

  val amendedBy: String? = null,

  val amendedTime: LocalDateTime? = null,
)
