package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactRestrictionEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncUpdatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRestrictionRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class SyncPrisonerContactRestrictionEntityServiceTest {
  private val prisonerContactRestrictionRepository: PrisonerContactRestrictionRepository = mock()
  private val syncService = SyncPrisonerContactRestrictionService(prisonerContactRestrictionRepository)

  @Nested
  inner class PrisonerContactRestrictionEntityTests {
    @Test
    fun `should get a prisoner contact by ID`() {
      whenever(prisonerContactRestrictionRepository.findById(1L)).thenReturn(
        Optional.of(
          contactEntity(),
        ),
      )
      val prisonerContactRestriction = syncService.getPrisonerContactRestrictionById(1L)
      with(prisonerContactRestriction) {
        assertThat(prisonerContactRestrictionId).isEqualTo(1L)
        assertThat(contactId).isEqualTo(12345L)
        assertThat(restrictionType).isEqualTo("NONCON")
        assertThat(startDate).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(expiryDate).isEqualTo(LocalDate.of(2024, 12, 31))
        assertThat(comments).isEqualTo("Restriction due to ongoing investigation")
        assertThat(authorisedBy).isEqualTo("John Doe")
        assertThat(authorisedTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(createdBy).isEqualTo("admin")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isEqualTo("editor")
        assertThat(updatedTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }
      verify(prisonerContactRestrictionRepository).findById(1L)
    }

    @Test
    fun `should fail to get a prisoner contact restriction by ID when not found`() {
      whenever(prisonerContactRestrictionRepository.findById(1L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.getPrisonerContactRestrictionById(1L)
      }
      verify(prisonerContactRestrictionRepository).findById(1L)
    }

    @Test
    fun `should create a prisoner contact restriction`() {
      val request = createPrisonerContactRestrictionRequest()
      whenever(prisonerContactRestrictionRepository.saveAndFlush(request.toEntity())).thenReturn(contactEntity(null, null))

      val contact = syncService.createPrisonerContactRestriction(request)
      val contactCaptor = argumentCaptor<PrisonerContactRestrictionEntity>()

      verify(prisonerContactRestrictionRepository).saveAndFlush(contactCaptor.capture())

      // Checks the entity saved
      with(contactCaptor.firstValue) {
        assertThat(prisonerContactRestrictionId).isEqualTo(0L)
        assertThat(prisonerContactId).isEqualTo(12345L)
        assertThat(restrictionType).isEqualTo("NONCON")
        assertThat(startDate).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(expiryDate).isEqualTo(LocalDate.of(2024, 12, 31))
        assertThat(comments).isEqualTo("Restriction due to ongoing investigation")
        assertThat(authorisedBy).isEqualTo("John Doe")
        assertThat(authorisedTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(createdBy).isEqualTo("admin")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(amendedBy).isNull()
        assertThat(amendedTime).isNull()
      }

      // Checks the model response
      with(contact) {
        assertThat(prisonerContactRestrictionId).isGreaterThan(0)
        assertThat(contactId).isEqualTo(12345L)
        assertThat(restrictionType).isEqualTo("NONCON")
        assertThat(startDate).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(expiryDate).isEqualTo(LocalDate.of(2024, 12, 31))
        assertThat(comments).isEqualTo("Restriction due to ongoing investigation")
        assertThat(authorisedBy).isEqualTo("John Doe")
        assertThat(authorisedTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(createdBy).isEqualTo("admin")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isNull()
        assertThat(updatedTime).isNull()
      }
    }

    @Test
    fun `should delete prisoner contact restriction by ID`() {
      whenever(prisonerContactRestrictionRepository.findById(1L)).thenReturn(
        Optional.of(
          contactEntity(),
        ),
      )
      syncService.deletePrisonerContactRestriction(1L)
      verify(prisonerContactRestrictionRepository).deleteById(1L)
    }

    @Test
    fun `should fail to delete prisoner contact restriction by ID when not found`() {
      whenever(prisonerContactRestrictionRepository.findById(1L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.deletePrisonerContactRestriction(1L)
      }
      verify(prisonerContactRestrictionRepository).findById(1L)
    }

    @Test
    fun `should update a prisoner contact restriction by ID`() {
      val request = updatePrisonerContactRestrictionRequest()
      val prisonerContactRestrictionID = 1L
      whenever(prisonerContactRestrictionRepository.findById(prisonerContactRestrictionID)).thenReturn(
        Optional.of(
          contactEntity(),
        ),
      )
      whenever(prisonerContactRestrictionRepository.saveAndFlush(any())).thenReturn(request.toEntity())

      val updated = syncService.updatePrisonerContactRestriction(prisonerContactRestrictionID, request)

      val contactCaptor = argumentCaptor<PrisonerContactRestrictionEntity>()

      verify(prisonerContactRestrictionRepository).saveAndFlush(contactCaptor.capture())

      // Checks the entity saved
      with(contactCaptor.firstValue) {
        assertThat(prisonerContactRestrictionId).isEqualTo(1L)
        assertThat(prisonerContactId).isEqualTo(12345L)
        assertThat(restrictionType).isEqualTo("NONCON")
        assertThat(startDate).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(expiryDate).isEqualTo(LocalDate.of(2024, 12, 31))
        assertThat(comments).isEqualTo("Restriction due to ongoing investigation")
        assertThat(authorisedBy).isEqualTo("John Doe")
        assertThat(authorisedTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(createdBy).isEqualTo("admin")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(amendedBy).isEqualTo("editor")
        assertThat(amendedTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }

      // Checks the model returned
      with(updated) {
        assertThat(prisonerContactRestrictionId).isEqualTo(1L)
        assertThat(contactId).isEqualTo(12345L)
        assertThat(restrictionType).isEqualTo("NONCON")
        assertThat(startDate).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(expiryDate).isEqualTo(LocalDate.of(2024, 12, 31))
        assertThat(comments).isEqualTo("Restriction due to ongoing investigation")
        assertThat(authorisedBy).isEqualTo("John Doe")
        assertThat(authorisedTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(createdBy).isEqualTo("admin")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isEqualTo("editor")
        assertThat(updatedTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }
    }

    @Test
    fun `should fail to update a prisoner contact restriction when prisoner contact restriction is not found`() {
      val updateRequest = updatePrisonerContactRestrictionRequest()
      whenever(prisonerContactRestrictionRepository.findById(1L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.updatePrisonerContactRestriction(1L, updateRequest)
      }
      verify(prisonerContactRestrictionRepository).findById(1L)
    }
  }

  private fun updatePrisonerContactRestrictionRequest() =
    SyncUpdatePrisonerContactRestrictionRequest(
      contactId = 12345L,
      restrictionType = "NONCON",
      startDate = LocalDate.of(2024, 1, 1),
      expiryDate = LocalDate.of(2024, 12, 31),
      comments = "Restriction due to ongoing investigation",
      staffUsername = "editor",
      authorisedBy = "John Doe",
      authorisedTime = LocalDateTime.now(),
      updatedBy = "editor",
      updatedTime = LocalDateTime.now(),
    )

  private fun createPrisonerContactRestrictionRequest() =
    SyncCreatePrisonerContactRestrictionRequest(
      contactId = 12345L,
      restrictionType = "NONCON",
      startDate = LocalDate.of(2024, 1, 1),
      expiryDate = LocalDate.of(2024, 12, 31),
      comments = "Restriction due to ongoing investigation",
      staffUsername = "admin",
      authorisedBy = "John Doe",
      authorisedTime = LocalDateTime.now(),
      createdBy = "admin",
      createdTime = LocalDateTime.now(),
    )

  private fun contactEntity(
    amendedBy: String? = "editor",
    amendedTime: LocalDateTime? = LocalDateTime.now(),
  ) =
    PrisonerContactRestrictionEntity(
      prisonerContactRestrictionId = 1L,
      prisonerContactId = 12345L,
      restrictionType = "NONCON",
      startDate = LocalDate.of(2024, 1, 1),
      expiryDate = LocalDate.of(2024, 12, 31),
      comments = "Restriction due to ongoing investigation",
      staffUsername = "admin",
      authorisedBy = "John Doe",
      authorisedTime = LocalDateTime.now(),
      createdBy = "admin",
      createdTime = LocalDateTime.now(),
    ).also {
      it.amendedBy = amendedBy
      it.amendedTime = amendedTime
    }

  private fun SyncUpdatePrisonerContactRestrictionRequest.toEntity(): PrisonerContactRestrictionEntity {
    val updatedBy = this.updatedBy
    val updatedTime = this.updatedTime

    return PrisonerContactRestrictionEntity(
      prisonerContactRestrictionId = 1L,
      prisonerContactId = 12345L,
      restrictionType = "NONCON",
      startDate = LocalDate.of(2024, 1, 1),
      expiryDate = LocalDate.of(2024, 12, 31),
      comments = "Restriction due to ongoing investigation",
      staffUsername = "admin",
      authorisedBy = "John Doe",
      authorisedTime = LocalDateTime.now(),
      createdBy = "admin",
      createdTime = LocalDateTime.now(),
    ).also {
      it.amendedBy = updatedBy
      it.amendedTime = updatedTime
    }
  }
}
