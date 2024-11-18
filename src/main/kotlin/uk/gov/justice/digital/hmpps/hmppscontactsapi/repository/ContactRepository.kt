package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity

@Repository
interface ContactRepository : JpaRepository<ContactEntity, Long> {
  @Modifying
  @Query("delete from ContactEntity c where c.contactId = :contactId")
  fun deleteAllByContactId(contactId: Long): Int
}
