package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.now

@MappedSuperclass
abstract class BaseContactEntity(

  open val title: String?,

  open val firstName: String,

  open val lastName: String,

  open val middleNames: String?,

  open val dateOfBirth: LocalDate?,

  @Enumerated(EnumType.STRING)
  open val estimatedIsOverEighteen: EstimatedIsOverEighteen?,

  @Column(name = "deceased_flag")
  open val isDeceased: Boolean,

  open val deceasedDate: LocalDate?,

  @Column(updatable = false)
  open val createdBy: String,

  @Column(updatable = false)
  @CreationTimestamp
  open val createdTime: LocalDateTime = now(),

  open val staffFlag: Boolean = false,

  open val remitterFlag: Boolean = false,

  open val gender: String? = null,

  open val domesticStatus: String? = null,

  open val languageCode: String? = null,

  open val interpreterRequired: Boolean = false,

  open val amendedBy: String? = null,

  open val amendedTime: LocalDateTime? = null,
) {
  abstract fun id(): Long
}
