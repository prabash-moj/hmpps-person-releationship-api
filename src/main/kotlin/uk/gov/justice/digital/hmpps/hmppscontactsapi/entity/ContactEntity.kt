package uk.gov.justice.digital.hmpps.hmppscontactsapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "contact")
data class ContactEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val contactId: Long,

  @Column(name = "title")
  val title: String?,

  @Column(name = "first_name")
  val firstName: String,

  @Column(name = "last_name")
  val lastName: String,

  @Column(name = "middle_name")
  val middleName: String?,

  @Column(name = "date_of_birth")
  val dateOfBirth: LocalDate?,

  @Column(name = "is_over_eighteen")
  val isOverEighteen: Boolean?,

  @Column(updatable = false, name = "created_by")
  val createdBy: String,

  @Column(updatable = false, name = "created_time")
  @CreationTimestamp
  val createdTime: LocalDateTime,
) {

  companion object {
    fun newContact(
      title: String?,
      firstName: String,
      lastName: String,
      middleName: String?,
      dateOfBirth: LocalDate?,
      isOverEighteen: Boolean?,
      createdBy: String,
    ): ContactEntity {
      return ContactEntity(
        0,
        title,
        firstName,
        lastName,
        middleName,
        dateOfBirth,
        isOverEighteen,
        createdBy,
        LocalDateTime.now(),
      )
    }
  }
}
