package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactSummaryEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.helper.hasSize
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.helper.isEqualTo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactSummaryRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PrisonerContactServiceTest {

  @Mock
  private lateinit var prisonerContactSummaryRepository: PrisonerContactSummaryRepository

  @Mock
  private lateinit var prisonerService: PrisonerService

  @InjectMocks
  private lateinit var prisonerContactService: PrisonerContactService

  private lateinit var prisoner: Prisoner

  private val prisonerNumber = "A1111AA"

  @BeforeEach
  fun before() {
    prisoner = Prisoner(prisonerNumber, prisonId = "MDI")
  }

  private val pageable = Pageable.ofSize(10)

  @Test
  fun `should fetch all contacts for a prisoner`() {
    val dateOfBirth = LocalDate.of(1980, 5, 10)
    val c1 = makePrisonerContact(id = 1L, contactId = 2L, dateOfBirth, firstName = "John", lastName = "Doe", EstimatedIsOverEighteen.DO_NOT_KNOW)
    val c2 = makePrisonerContact(id = 2L, contactId = 2L, dateOfBirth, firstName = "David", lastName = "Doe", EstimatedIsOverEighteen.YES)
    val contacts = listOf(c1, c2)
    val page = PageImpl(contacts, pageable, contacts.size.toLong())

    whenever(prisonerService.getPrisoner(prisonerNumber)).thenReturn(prisoner)
    whenever(prisonerContactSummaryRepository.findByPrisonerNumberAndActive(prisonerNumber, true, pageable)).thenReturn(page)

    val result = prisonerContactService.getAllContacts(prisonerNumber, true, pageable)

    result.content hasSize 2
    assertThat(result).containsAll(listOf(c1.toModel(), c2.toModel()))

    verify(prisonerContactSummaryRepository).findByPrisonerNumberAndActive(prisonerNumber, true, pageable)
  }

  @Test
  fun `should throw exception`() {
    whenever(prisonerService.getPrisoner(prisonerNumber)).thenReturn(null)
    val exception = assertThrows<EntityNotFoundException> {
      prisonerContactService.getAllContacts(prisonerNumber, true, pageable)
    }
    exception.message isEqualTo "Prisoner number $prisonerNumber - not found"
  }

  private fun makePrisonerContact(
    id: Long,
    contactId: Long,
    dateOfBirth: LocalDate?,
    firstName: String,
    lastName: String,
    estimatedIsOverEighteen: EstimatedIsOverEighteen,
    active: Boolean = true,
  ): PrisonerContactSummaryEntity =
    PrisonerContactSummaryEntity(
      prisonerContactId = id,
      contactId = contactId,
      title = "Mr.",
      firstName = firstName,
      middleNames = "Any",
      lastName = lastName,
      dateOfBirth = dateOfBirth,
      estimatedIsOverEighteen = estimatedIsOverEighteen,
      contactAddressId = 3L,
      flat = "2B",
      property = "123",
      street = "Baker Street",
      area = "Westminster",
      cityCode = "SHEF",
      cityDescription = "Sheffield",
      countyCode = "SYORKS",
      countyDescription = "South Yorkshire",
      postCode = "NW1 6XE",
      countryCode = "UK",
      countryDescription = "United Kingdom",
      contactPhoneId = 4L,
      phoneType = "Mobile",
      phoneTypeDescription = "Mobile Phone",
      phoneNumber = "07123456789",
      extNumber = "0123",
      contactEmailId = 5L,
      emailType = "Personal",
      emailTypeDescription = "Personal Email",
      emailAddress = "john.doe@example.com",
      prisonerNumber = "A1234BC",
      relationshipType = "FRIEND",
      relationshipDescription = "Friend",
      active = active,
      canBeContacted = true,
      approvedVisitor = true,
      awareOfCharges = false,
      nextOfKin = false,
      emergencyContact = false,
      comments = "No comments",
    )
}
