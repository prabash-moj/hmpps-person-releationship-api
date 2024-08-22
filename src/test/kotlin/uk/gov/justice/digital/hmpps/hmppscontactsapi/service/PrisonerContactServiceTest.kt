package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.PrisonerContactSummaryEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.helper.hasSize
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.helper.isBool
import uk.gov.justice.digital.hmpps.hmppscontactsapi.integration.helper.isEqualTo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PrisonerContactServiceTest {

  @Mock
  private lateinit var prisonerContactRepository: PrisonerContactRepository

  @Mock
  private lateinit var prisonerService: PrisonerService

  @InjectMocks
  private lateinit var prisonerContactService: PrisonerContactService

  @Test
  fun `should fetch all contacts`() {
    // Given

    val prisonerContactSummaryEntity1 = PrisonerContactSummaryEntity(
      prisonerContactId = 1L,
      contactId = 2L,
      title = "Mr.",
      firstName = "John",
      middleName = "A.",
      lastName = "Doe",
      dateOfBirth = LocalDate.of(1980, 5, 10),
      contactAddressId = 3L,
      flat = "2B",
      property = "123",
      street = "Baker Street",
      area = "Westminster",
      cityCode = "LON",
      countyCode = "Greater London",
      postCode = "NW1 6XE",
      countryCode = "UK",
      contactPhoneId = 4L,
      phoneType = "Mobile",
      phoneTypeDescription = "Mobile Phone",
      phoneNumber = "07123456789",
      contactEmailId = 5L,
      emailType = "Personal",
      emailTypeDescription = "Personal Email",
      emailAddress = "john.doe@example.com",
      prisonerNumber = "A1234BC",
      relationshipType = "FRIEND",
      relationshipDescription = "Friend",
      active = true,
      canBeContacted = true,
      approvedVisitor = true,
      awareOfCharges = false,
      nextOfKin = false,
      emergencyContact = false,
      comments = "No comments",
    )

    val prisonerContactSummaryEntity2 = PrisonerContactSummaryEntity(
      prisonerContactId = 1L,
      contactId = 2L,
      title = "Mr.",
      firstName = "John",
      middleName = "A.",
      lastName = "Doe",
      dateOfBirth = LocalDate.of(1980, 5, 10),
      contactAddressId = 3L,
      flat = "2B",
      property = "123",
      street = "Baker Street",
      area = "Westminster",
      cityCode = "LON",
      countyCode = "Greater London",
      postCode = "NW1 6XE",
      countryCode = "UK",
      contactPhoneId = 4L,
      phoneType = "Mobile",
      phoneTypeDescription = "Mobile Phone",
      phoneNumber = "07123456789",
      contactEmailId = 5L,
      emailType = "Personal",
      emailTypeDescription = "Personal Email",
      emailAddress = "john.doe@example.com",
      prisonerNumber = "A1234BC",
      relationshipType = "FRIEND",
      relationshipDescription = "Friend",
      active = true,
      canBeContacted = true,
      approvedVisitor = true,
      awareOfCharges = false,
      nextOfKin = false,
      emergencyContact = false,
      comments = "No comments",
    )

    val prisoner = Prisoner(
      prisonerNumber = "somePrisonerNumber",
      prisonId = "somePrisonId",
    )

    val contacts = listOf(prisonerContactSummaryEntity1, prisonerContactSummaryEntity2)

    `when`(prisonerService.getPrisoner("somePrisonerNumber")).thenReturn(prisoner)
    `when`(prisonerContactRepository.findPrisonerContacts("somePrisonerNumber", true)).thenReturn(contacts)

    // When
    val result = prisonerContactService.getAllContacts("somePrisonerNumber", true)

    // Then
    result hasSize 2
    result.containsAll(listOf(prisonerContactSummaryEntity1.toModel(), prisonerContactSummaryEntity2.toModel())) isBool true
    verify(prisonerContactRepository).findPrisonerContacts("somePrisonerNumber", true)
  }

  @Test
  fun `should throw exception`() {
    // Given

    `when`(prisonerService.getPrisoner("somePrisonerNumber")).thenReturn(null)

    // When

    val exception = assertThrows<EntityNotFoundException> {
      prisonerContactService.getAllContacts("somePrisonerNumber", true)
    }

    // Then
    exception.message isEqualTo "the prisoner number somePrisonerNumber not found"
  }
}
