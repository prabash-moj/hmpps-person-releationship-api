package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactRestrictionDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createPrisonerContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createPrisonerContactRestrictionDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.PrisonerContactRestrictionsResponse
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRestrictionDetailsRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRestrictionDetailsRepository
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
  private val contactRepository: ContactRepository = mock()
  private val prisonerContactRepository: PrisonerContactRepository = mock()
  private val prisonerContactRestrictionDetailsRepository: PrisonerContactRestrictionDetailsRepository = mock()
  private val service = RestrictionsService(
    contactRestrictionDetailsRepository,
    contactRepository,
    prisonerContactRepository,
    prisonerContactRestrictionDetailsRepository,
  )

  @Test
  fun `get estate wide restrictions successfully`() {
    val now = now()
    whenever(contactRepository.findById(contactId)).thenReturn(Optional.of(aContact))
    whenever(contactRestrictionDetailsRepository.findAllByContactId(contactId)).thenReturn(
      listOf(
        createContactRestrictionDetailsEntity(123, createdTime = now),
        createContactRestrictionDetailsEntity(321, createdTime = now),
      ),
    )

    val restrictions = service.getEstateWideRestrictionsForContact(contactId)

    assertThat(restrictions).hasSize(2)
    assertThat(restrictions).isEqualTo(
      listOf(
        createContactRestrictionDetails(123, createdTime = now),
        createContactRestrictionDetails(321, createdTime = now),
      ),
    )
  }

  @Test
  fun `should blow up if contact is not found getting estate wide restrictions`() {
    whenever(contactRepository.findById(contactId)).thenReturn(Optional.empty())

    val exception = assertThrows<EntityNotFoundException> {
      service.getEstateWideRestrictionsForContact(contactId)
    }
    assertThat(exception.message).isEqualTo("Contact (99) could not be found")
  }

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
        contactEstateWideRestrictions = listOf(
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
