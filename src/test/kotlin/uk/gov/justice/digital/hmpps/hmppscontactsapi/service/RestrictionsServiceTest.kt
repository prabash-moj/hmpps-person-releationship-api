package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactRestrictionEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactRestrictionEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactRestrictionDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createPrisonerContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createPrisonerContactRestrictionDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdatePrisonerContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRestrictionsResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRestrictionDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRestrictionRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRestrictionDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRestrictionRepository
import java.time.LocalDate
import java.time.LocalDateTime.now
import java.util.*

class RestrictionsServiceTest {

  private val contactId = 99L
  private val prisonerContactId = 66L
  private val aContact = ContactEntity(
    contactId = contactId,
    title = "MR",
    lastName = "last",
    middleNames = "middle",
    firstName = "first",
    dateOfBirth = null,
    estimatedIsOverEighteen = EstimatedIsOverEighteen.YES,
    isDeceased = false,
    deceasedDate = null,
    createdBy = "user",
    createdTime = now(),
  )
  private val aPrisonerContact = PrisonerContactEntity(
    prisonerContactId = prisonerContactId,
    contactId = contactId,
    prisonerNumber = "A1234BC",
    contactType = "S",
    relationshipType = "FRI",
    nextOfKin = true,
    emergencyContact = true,
    active = true,
    approvedVisitor = true,
    currentTerm = true,
    comments = null,
    createdBy = "user",
    createdTime = now(),
  )

  private val contactRestrictionDetailsRepository: ContactRestrictionDetailsRepository = mock()
  private val contactRestrictionRepository: ContactRestrictionRepository = mock()
  private val contactRepository: ContactRepository = mock()
  private val prisonerContactRepository: PrisonerContactRepository = mock()
  private val prisonerContactRestrictionDetailsRepository: PrisonerContactRestrictionDetailsRepository = mock()
  private val prisonerContactRestrictionRepository: PrisonerContactRestrictionRepository = mock()
  private val referenceCodeService: ReferenceCodeService = mock()
  private val service = RestrictionsService(
    contactRestrictionDetailsRepository,
    contactRestrictionRepository,
    contactRepository,
    prisonerContactRepository,
    prisonerContactRestrictionDetailsRepository,
    prisonerContactRestrictionRepository,
    referenceCodeService,
  )

  @Nested
  inner class GetGlobalRestrictions {
    @Test
    fun `get global restrictions successfully`() {
      val now = now()
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(aContact))
      whenever(contactRestrictionDetailsRepository.findAllByContactId(contactId)).thenReturn(
        listOf(
          createContactRestrictionDetailsEntity(123, createdTime = now),
          createContactRestrictionDetailsEntity(321, createdTime = now),
        ),
      )

      val restrictions = service.getGlobalRestrictionsForContact(contactId)

      assertThat(restrictions).hasSize(2)
      assertThat(restrictions).isEqualTo(
        listOf(
          createContactRestrictionDetails(123, createdTime = now),
          createContactRestrictionDetails(321, createdTime = now),
        ),
      )
    }

    @Test
    fun `should blow up if contact is not found getting global restrictions`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.empty())

      val exception = assertThrows<EntityNotFoundException> {
        service.getGlobalRestrictionsForContact(contactId)
      }
      assertThat(exception.message).isEqualTo("Contact (99) could not be found")
    }
  }

  @Nested
  inner class GetPrisonerContact {
    @Test
    fun `get prisoner contact restrictions successfully`() {
      val now = now()
      whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(aPrisonerContact))
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(aContact))
      whenever(contactRestrictionDetailsRepository.findAllByContactId(contactId)).thenReturn(
        listOf(
          createContactRestrictionDetailsEntity(123, createdTime = now),
          createContactRestrictionDetailsEntity(321, createdTime = now),
        ),
      )
      whenever(prisonerContactRestrictionDetailsRepository.findAllByPrisonerContactId(prisonerContactId)).thenReturn(
        listOf(
          createPrisonerContactRestrictionDetailsEntity(
            id = 789,
            prisonerContactId = prisonerContactId,
            createdTime = now,
          ),
          createPrisonerContactRestrictionDetailsEntity(
            id = 987,
            prisonerContactId = prisonerContactId,
            createdTime = now,
          ),
        ),
      )

      val restrictions = service.getPrisonerContactRestrictions(prisonerContactId)

      assertThat(restrictions).isEqualTo(
        PrisonerContactRestrictionsResponse(
          prisonerContactRestrictions = listOf(
            createPrisonerContactRestrictionDetails(
              id = 789,
              prisonerContactId = prisonerContactId,
              contactId = contactId,
              prisonerNumber = aPrisonerContact.prisonerNumber,
              createdTime = now,
            ),
            createPrisonerContactRestrictionDetails(
              id = 987,
              prisonerContactId = prisonerContactId,
              contactId = contactId,
              prisonerNumber = aPrisonerContact.prisonerNumber,
              createdTime = now,
            ),
          ),
          contactGlobalRestrictions = listOf(
            createContactRestrictionDetails(id = 123, createdTime = now),
            createContactRestrictionDetails(id = 321, createdTime = now),
          ),
        ),
      )
    }

    @Test
    fun `should blow up if prisoner contact is not found getting prisoner contact restrictions`() {
      whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.empty())

      val exception = assertThrows<EntityNotFoundException> {
        service.getPrisonerContactRestrictions(prisonerContactId)
      }
      assertThat(exception.message).isEqualTo("Prisoner contact (66) could not be found")
    }
  }

  @Nested
  inner class CreateGlobal {
    @Test
    fun `blow up creating global restriction if contact is missing`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.empty())

      val exception = assertThrows<EntityNotFoundException> {
        service.createContactGlobalRestriction(contactId, aCreateGlobalRestrictionRequest())
      }
      assertThat(exception.message).isEqualTo("Contact (99) could not be found")
    }

    @Test
    fun `blow up creating global restriction if type is not supported`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(aContact))
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("RESTRICTION", "BAN")).thenReturn(null)

      val exception = assertThrows<ValidationException> {
        service.createContactGlobalRestriction(contactId, aCreateGlobalRestrictionRequest())
      }
      assertThat(exception.message).isEqualTo("Unsupported restriction type (BAN)")
    }

    @Test
    fun `blow up creating global restriction if type is no longer active`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(aContact))
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("RESTRICTION", "BAN")).thenReturn(
        ReferenceCode(
          referenceCodeId = 0,
          "RESTRICTION",
          "BAN",
          "Banned",
          99,
          false,
        ),
      )

      val exception = assertThrows<ValidationException> {
        service.createContactGlobalRestriction(contactId, aCreateGlobalRestrictionRequest())
      }
      assertThat(exception.message).isEqualTo("Restriction type (BAN) is no longer supported for creating or updating restrictions")
    }

    @Test
    fun `create global restriction`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(aContact))
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("RESTRICTION", "BAN")).thenReturn(
        ReferenceCode(
          referenceCodeId = 0,
          "RESTRICTION",
          "BAN",
          "Banned",
          99,
          true,
        ),
      )
      whenever(contactRestrictionRepository.saveAndFlush(any())).thenAnswer { i ->
        (i.arguments[0] as ContactRestrictionEntity).copy(
          contactRestrictionId = 9999,
        )
      }

      val created = service.createContactGlobalRestriction(contactId, aCreateGlobalRestrictionRequest())
      assertThat(created).isEqualTo(
        ContactRestrictionDetails(
          contactRestrictionId = 9999,
          contactId = contactId,
          restrictionType = "BAN",
          restrictionTypeDescription = "Banned",
          startDate = LocalDate.of(2020, 1, 1),
          expiryDate = LocalDate.of(2022, 2, 2),
          comments = "Some comments",
          createdBy = "created",
          createdTime = created.createdTime,
          updatedBy = null,
          updatedTime = null,
        ),
      )
      verify(contactRestrictionRepository).saveAndFlush(any())
    }

    private fun aCreateGlobalRestrictionRequest(): CreateContactRestrictionRequest =
      CreateContactRestrictionRequest(
        restrictionType = "BAN",
        startDate = LocalDate.of(2020, 1, 1),
        expiryDate = LocalDate.of(2022, 2, 2),
        comments = "Some comments",
        createdBy = "created",
      )
  }

  @Nested
  inner class UpdateGlobalRestriction {
    private val contactRestrictionId = 654L
    private val existingEntity = ContactRestrictionEntity(
      contactRestrictionId = contactRestrictionId,
      contactId = contactId,
      restrictionType = "BAN",
      startDate = LocalDate.of(2020, 1, 1),
      expiryDate = LocalDate.of(2022, 2, 2),
      comments = "Some comments",
      createdBy = "created",
      createdTime = now(),
      amendedBy = null,
      amendedTime = null,
    )

    @Test
    fun `blow up updating global restriction if contact is missing`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.empty())

      val exception = assertThrows<EntityNotFoundException> {
        service.updateContactGlobalRestriction(contactId, contactRestrictionId, anUpdateGlobalRestrictionRequest())
      }
      assertThat(exception.message).isEqualTo("Contact (99) could not be found")
    }

    @Test
    fun `blow up updating global restriction if contact restriction is missing`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(aContact))
      whenever(contactRestrictionRepository.findById(contactRestrictionId)).thenReturn(Optional.empty())

      val exception = assertThrows<EntityNotFoundException> {
        service.updateContactGlobalRestriction(contactId, contactRestrictionId, anUpdateGlobalRestrictionRequest())
      }
      assertThat(exception.message).isEqualTo("Contact restriction (654) could not be found")
    }

    @Test
    fun `blow up updating global restriction if type is not supported`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(aContact))
      whenever(contactRestrictionRepository.findById(contactRestrictionId)).thenReturn(Optional.of(existingEntity))
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("RESTRICTION", "CCTV")).thenReturn(null)

      val exception = assertThrows<ValidationException> {
        service.updateContactGlobalRestriction(contactId, contactRestrictionId, anUpdateGlobalRestrictionRequest())
      }
      assertThat(exception.message).isEqualTo("Unsupported restriction type (CCTV)")
    }

    @Test
    fun `blow up updating global restriction if type is no longer active`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(aContact))
      whenever(contactRestrictionRepository.findById(contactRestrictionId)).thenReturn(Optional.of(existingEntity))
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("RESTRICTION", "CCTV")).thenReturn(
        ReferenceCode(
          referenceCodeId = 0,
          "RESTRICTION",
          "CCTV",
          "CCTV",
          99,
          false,
        ),
      )

      val exception = assertThrows<ValidationException> {
        service.updateContactGlobalRestriction(contactId, contactRestrictionId, anUpdateGlobalRestrictionRequest())
      }
      assertThat(exception.message).isEqualTo("Restriction type (CCTV) is no longer supported for creating or updating restrictions")
    }

    @Test
    fun `updated global restriction`() {
      whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(aContact))
      whenever(contactRestrictionRepository.findById(contactRestrictionId)).thenReturn(Optional.of(existingEntity))
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("RESTRICTION", "CCTV")).thenReturn(
        ReferenceCode(
          referenceCodeId = 0,
          "RESTRICTION",
          "CCTV",
          "CCTV",
          99,
          true,
        ),
      )
      whenever(contactRestrictionRepository.saveAndFlush(any())).thenAnswer { i ->
        (i.arguments[0] as ContactRestrictionEntity).copy(
          contactRestrictionId = 9999,
        )
      }

      val updated = service.updateContactGlobalRestriction(contactId, contactRestrictionId, anUpdateGlobalRestrictionRequest())
      assertThat(updated).isEqualTo(
        ContactRestrictionDetails(
          contactRestrictionId = 9999,
          contactId = contactId,
          restrictionType = "CCTV",
          restrictionTypeDescription = "CCTV",
          startDate = LocalDate.of(1990, 1, 1),
          expiryDate = LocalDate.of(1992, 2, 2),
          comments = "Updated comments",
          createdBy = "created",
          createdTime = updated.createdTime,
          updatedBy = "updated",
          updatedTime = updated.updatedTime,
        ),
      )
      verify(contactRestrictionRepository).saveAndFlush(any())
    }

    private fun anUpdateGlobalRestrictionRequest(): UpdateContactRestrictionRequest =
      UpdateContactRestrictionRequest(
        restrictionType = "CCTV",
        startDate = LocalDate.of(1990, 1, 1),
        expiryDate = LocalDate.of(1992, 2, 2),
        comments = "Updated comments",
        updatedBy = "updated",
      )
  }

  @Nested
  inner class CreatePrisonerContactRestriction {
    @Test
    fun `blow up creating prisoner contact restriction if prisoner contact is missing`() {
      whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.empty())

      val exception = assertThrows<EntityNotFoundException> {
        service.createPrisonerContactRestriction(prisonerContactId, aCreatePrisonerContactRestrictionRequest())
      }
      assertThat(exception.message).isEqualTo("Prisoner contact (66) could not be found")
    }

    @Test
    fun `blow up creating prisoner contact restriction if type is not supported`() {
      whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(aPrisonerContact))
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("RESTRICTION", "BAN")).thenReturn(null)

      val exception = assertThrows<ValidationException> {
        service.createPrisonerContactRestriction(prisonerContactId, aCreatePrisonerContactRestrictionRequest())
      }
      assertThat(exception.message).isEqualTo("Unsupported restriction type (BAN)")
    }

    @Test
    fun `blow up creating prisoner contact restriction if type is no longer active`() {
      whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(aPrisonerContact))
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("RESTRICTION", "BAN")).thenReturn(
        ReferenceCode(
          referenceCodeId = 0,
          "RESTRICTION",
          "BAN",
          "Banned",
          99,
          false,
        ),
      )

      val exception = assertThrows<ValidationException> {
        service.createPrisonerContactRestriction(prisonerContactId, aCreatePrisonerContactRestrictionRequest())
      }
      assertThat(exception.message).isEqualTo("Restriction type (BAN) is no longer supported for creating or updating restrictions")
    }

    @Test
    fun `create prisoner contact restriction`() {
      whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(aPrisonerContact))
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("RESTRICTION", "BAN")).thenReturn(
        ReferenceCode(
          referenceCodeId = 0,
          "RESTRICTION",
          "BAN",
          "Banned",
          99,
          true,
        ),
      )
      whenever(prisonerContactRestrictionRepository.saveAndFlush(any())).thenAnswer { i ->
        (i.arguments[0] as PrisonerContactRestrictionEntity).copy(
          prisonerContactRestrictionId = 9999,
        )
      }

      val created = service.createPrisonerContactRestriction(prisonerContactId, aCreatePrisonerContactRestrictionRequest())
      assertThat(created).isEqualTo(
        PrisonerContactRestrictionDetails(
          prisonerContactRestrictionId = 9999,
          contactId = contactId,
          prisonerContactId = prisonerContactId,
          prisonerNumber = aPrisonerContact.prisonerNumber,
          restrictionType = "BAN",
          restrictionTypeDescription = "Banned",
          startDate = LocalDate.of(2020, 1, 1),
          expiryDate = LocalDate.of(2022, 2, 2),
          comments = "Some comments",
          createdBy = "created",
          createdTime = created.createdTime,
          updatedBy = null,
          updatedTime = null,
        ),
      )
      verify(prisonerContactRestrictionRepository).saveAndFlush(any())
    }

    private fun aCreatePrisonerContactRestrictionRequest(): CreatePrisonerContactRestrictionRequest =
      CreatePrisonerContactRestrictionRequest(
        restrictionType = "BAN",
        startDate = LocalDate.of(2020, 1, 1),
        expiryDate = LocalDate.of(2022, 2, 2),
        comments = "Some comments",
        createdBy = "created",
      )
  }

  @Nested
  inner class UpdatePrisonerContactRestriction {
    private val prisonerContactRestrictionId = 654L
    private val existingEntity = PrisonerContactRestrictionEntity(
      prisonerContactRestrictionId = prisonerContactRestrictionId,
      prisonerContactId = prisonerContactId,
      restrictionType = "BAN",
      startDate = LocalDate.of(2020, 1, 1),
      expiryDate = LocalDate.of(2022, 2, 2),
      comments = "Some comments",
      createdBy = "created",
      createdTime = now(),
      amendedBy = null,
      amendedTime = null,
    )

    @Test
    fun `blow up updating prisoner contact restriction if prisoner contact is missing`() {
      whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.empty())

      val exception = assertThrows<EntityNotFoundException> {
        service.updatePrisonerContactRestriction(prisonerContactId, prisonerContactRestrictionId, anUpdatePrisonerContactRestrictionRequest())
      }
      assertThat(exception.message).isEqualTo("Prisoner contact (66) could not be found")
    }

    @Test
    fun `blow up updating prisoner contact restriction if prisoner contact restriction is missing`() {
      whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(aPrisonerContact))
      whenever(prisonerContactRestrictionRepository.findById(prisonerContactRestrictionId)).thenReturn(Optional.empty())

      val exception = assertThrows<EntityNotFoundException> {
        service.updatePrisonerContactRestriction(prisonerContactId, prisonerContactRestrictionId, anUpdatePrisonerContactRestrictionRequest())
      }
      assertThat(exception.message).isEqualTo("Prisoner contact restriction (654) could not be found")
    }

    @Test
    fun `blow up updating prisoner contact restriction if type is not supported`() {
      whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(aPrisonerContact))
      whenever(prisonerContactRestrictionRepository.findById(prisonerContactRestrictionId)).thenReturn(Optional.of(existingEntity))
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("RESTRICTION", "CCTV")).thenReturn(null)

      val exception = assertThrows<ValidationException> {
        service.updatePrisonerContactRestriction(prisonerContactId, prisonerContactRestrictionId, anUpdatePrisonerContactRestrictionRequest())
      }
      assertThat(exception.message).isEqualTo("Unsupported restriction type (CCTV)")
    }

    @Test
    fun `blow up updating prisoner contact restriction if type is no longer active`() {
      whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(aPrisonerContact))
      whenever(prisonerContactRestrictionRepository.findById(prisonerContactRestrictionId)).thenReturn(Optional.of(existingEntity))
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("RESTRICTION", "CCTV")).thenReturn(
        ReferenceCode(
          referenceCodeId = 0,
          "RESTRICTION",
          "CCTV",
          "CCTV",
          99,
          false,
        ),
      )

      val exception = assertThrows<ValidationException> {
        service.updatePrisonerContactRestriction(prisonerContactId, prisonerContactRestrictionId, anUpdatePrisonerContactRestrictionRequest())
      }
      assertThat(exception.message).isEqualTo("Restriction type (CCTV) is no longer supported for creating or updating restrictions")
    }

    @Test
    fun `updated prisoner contact restriction`() {
      whenever(prisonerContactRepository.findById(prisonerContactId)).thenReturn(Optional.of(aPrisonerContact))
      whenever(prisonerContactRestrictionRepository.findById(prisonerContactRestrictionId)).thenReturn(Optional.of(existingEntity))
      whenever(referenceCodeService.getReferenceDataByGroupAndCode("RESTRICTION", "CCTV")).thenReturn(
        ReferenceCode(
          referenceCodeId = 0,
          "RESTRICTION",
          "CCTV",
          "CCTV",
          99,
          true,
        ),
      )
      whenever(prisonerContactRestrictionRepository.saveAndFlush(any())).thenAnswer { i ->
        (i.arguments[0] as PrisonerContactRestrictionEntity).copy(
          prisonerContactRestrictionId = 9999,
        )
      }

      val updated = service.updatePrisonerContactRestriction(prisonerContactId, prisonerContactRestrictionId, anUpdatePrisonerContactRestrictionRequest())
      assertThat(updated).isEqualTo(
        PrisonerContactRestrictionDetails(
          prisonerContactRestrictionId = 9999,
          prisonerContactId = prisonerContactId,
          contactId = contactId,
          prisonerNumber = aPrisonerContact.prisonerNumber,
          restrictionType = "CCTV",
          restrictionTypeDescription = "CCTV",
          startDate = LocalDate.of(1990, 1, 1),
          expiryDate = LocalDate.of(1992, 2, 2),
          comments = "Updated comments",
          createdBy = "created",
          createdTime = updated.createdTime,
          updatedBy = "updated",
          updatedTime = updated.updatedTime,
        ),
      )
      verify(prisonerContactRestrictionRepository).saveAndFlush(any())
    }

    private fun anUpdatePrisonerContactRestrictionRequest(): UpdatePrisonerContactRestrictionRequest =
      UpdatePrisonerContactRestrictionRequest(
        restrictionType = "CCTV",
        startDate = LocalDate.of(1990, 1, 1),
        expiryDate = LocalDate.of(1992, 2, 2),
        comments = "Updated comments",
        updatedBy = "updated",
      )
  }
}
