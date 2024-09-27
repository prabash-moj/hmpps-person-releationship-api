package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEmailEntity

interface ContactEmailRepository : JpaRepository<ContactEmailEntity, Long>
