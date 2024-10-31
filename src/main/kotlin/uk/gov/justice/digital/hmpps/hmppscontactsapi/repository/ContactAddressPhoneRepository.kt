package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactAddressPhoneEntity

@Repository
interface ContactAddressPhoneRepository : JpaRepository<ContactAddressPhoneEntity, Long> {

  fun findByContactId(contactId: Long): List<ContactAddressPhoneEntity>

  fun deleteByContactPhoneId(contactPhoneId: Long)
}
