package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync

import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.EmploymentEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateEmploymentRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.SyncEmployment

fun SyncCreateEmploymentRequest.toEntity(): EmploymentEntity = EmploymentEntity(
  employmentId = 0L,
  organisationId = this.organisationId,
  contactId = this.contactId,
  active = this.active,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  updatedBy = null,
  updatedTime = null,
)

fun EmploymentEntity.toResponse(): SyncEmployment = SyncEmployment(
  employmentId = this.employmentId,
  organisationId = organisationId,
  contactId = this.contactId,
  active = this.active,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  updatedBy = this.updatedBy,
  updatedTime = this.updatedTime,
)
