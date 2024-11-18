package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEmploymentEntity

interface ContactEmploymentRepository : JpaRepository<ContactEmploymentEntity, Long> {
  @Modifying
  @Query("delete from ContactEmploymentEntity ce where ce.contactId = :contactId")
  fun deleteAllByContactId(contactId: Long): Int
}
