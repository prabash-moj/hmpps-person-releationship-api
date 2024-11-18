package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactAddressEntity

@Repository
interface ContactAddressRepository : JpaRepository<ContactAddressEntity, Long> {

  @Modifying
  @Query("delete from ContactAddressEntity ca where ca.contactId = :contactId")
  fun deleteAllByContactId(contactId: Long): Int
}
