package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.EmploymentEntity

@Repository
interface EmploymentRepository : JpaRepository<EmploymentEntity, Long> {
  @Modifying
  @Query("delete from EmploymentEntity cr where cr.contactId = :contactId")
  fun deleteAllByContactId(contactId: Long): Int
}
