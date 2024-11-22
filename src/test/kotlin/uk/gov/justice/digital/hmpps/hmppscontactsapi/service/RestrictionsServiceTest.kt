package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactRestrictionDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRestrictionDetailsRepository
import java.time.LocalDateTime.now
import java.util.*

class RestrictionsServiceTest {

  private val contactId = 99L
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

  private val contactRestrictionDetailsRepository: ContactRestrictionDetailsRepository = mock()
  private val contactRepository: ContactRepository = mock()
  private val service = RestrictionsService(contactRestrictionDetailsRepository, contactRepository)

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
    val now = now()
    whenever(contactRepository.findById(contactId)).thenReturn(Optional.empty())
    whenever(contactRestrictionDetailsRepository.findAllByContactId(contactId)).thenReturn(
      listOf(
        createContactRestrictionDetailsEntity(123, createdTime = now),
        createContactRestrictionDetailsEntity(321, createdTime = now),
      ),
    )

    val exception = assertThrows<EntityNotFoundException> {
      service.getEstateWideRestrictionsForContact(contactId)
    }
    assertThat(exception.message).isEqualTo("Contact (99) could not be found")
  }
}
