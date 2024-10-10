package uk.gov.justice.digital.hmpps.hmppscontactsapi.service.migrate

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations.openMocks
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactIdentityEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactPhoneEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.migrate.CodedValue
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.migrate.MigrateAuditInfo
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.migrate.MigrateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.migrate.MigrateIdentifier
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.migrate.MigratePhoneNumber
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactAddressRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactEmailRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactIdentityRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactPhoneRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ContactRestrictionRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRepository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.PrisonerContactRestrictionRepository
import java.time.LocalDateTime

class MigrationServiceTest {
  private val contactRepository: ContactRepository = mock()
  private val contactAddressRepository: ContactAddressRepository = mock()
  private val contactPhoneRepository: ContactPhoneRepository = mock()
  private val contactEmailRepository: ContactEmailRepository = mock()
  private val contactIdentityRepository: ContactIdentityRepository = mock()
  private val contactRestrictionRepository: ContactRestrictionRepository = mock()
  private val prisonerContactRepository: PrisonerContactRepository = mock()
  private val prisonerContactRestrictionRepository: PrisonerContactRestrictionRepository = mock()

  val migrationService = MigrationService(
    contactRepository,
    contactAddressRepository,
    contactPhoneRepository,
    contactEmailRepository,
    contactIdentityRepository,
    contactRestrictionRepository,
    prisonerContactRepository,
    prisonerContactRestrictionRepository,
  )

  @BeforeEach
  fun setUp() {
    openMocks(this)
  }

  @Nested
  inner class MigrationExtractFromRequest {

    @Test
    fun `should extract contact details`() {
      val auditInfo = migrateAuditInfo()
      val request = migrateRequest(auditInfo)
      val contact = aContactEntity(1L, auditInfo)

      whenever(contactRepository.save(any())).thenReturn(contact)

      val contactCaptor = argumentCaptor<ContactEntity>()

      val result = migrationService.extractAndSaveContact(request)

      assertThat(result.first).isEqualTo(request.personId)
      assertThat(result.second)
        .extracting("contactId", "lastName", "firstName")
        .contains(contact.contactId, contact.lastName, contact.firstName)

      verify(contactRepository).save(contactCaptor.capture())

      with(contactCaptor.firstValue) {
        assertThat(this)
          .extracting("contactId", "lastName", "firstName", "createdBy", "createdTime", "amendedBy", "amendedTime")
          .contains(
            0L,
            contact.lastName,
            contact.firstName,
            auditInfo.createUsername,
            auditInfo.createDateTime,
            auditInfo.modifyUserId,
            auditInfo.modifyDateTime,
          )
      }
    }

    @Test
    fun `should extract contact phone numbers`() {
      val auditInfo = migrateAuditInfo()
      val request = migrateRequest(auditInfo).copy(phoneNumbers = phoneNumbers())
      val responses = listOf(
        ContactPhoneEntity(
          contactId = 1L,
          contactPhoneId = 1L,
          phoneType = request.phoneNumbers[0].type.code,
          phoneNumber = request.phoneNumbers[0].number,
          primaryPhone = true,
          createdBy = auditInfo.createUsername!!,
          createdTime = auditInfo.createDateTime!!,
        ),
        ContactPhoneEntity(
          contactId = 1L,
          contactPhoneId = 2L,
          phoneType = request.phoneNumbers[1].type.code,
          phoneNumber = request.phoneNumbers[1].number,
          primaryPhone = true,
          createdBy = auditInfo.createUsername!!,
          createdTime = auditInfo.createDateTime!!,
        ),
      )

      whenever(contactPhoneRepository.save(any()))
        .thenReturn(responses[0])
        .thenReturn(responses[1])

      val contactPhoneCaptor = argumentCaptor<ContactPhoneEntity>()

      val result = migrationService.extractAndSavePhones(request, 1L)

      assertThat(result.size).isEqualTo(2)

      for (i in 0..1) {
        assertThat(result[i].first).isEqualTo(request.phoneNumbers[i].phoneId)
        assertThat(result[i].second)
          .extracting("contactId", "contactPhoneId", "phoneType", "phoneNumber", "primaryPhone")
          .contains(
            responses[i].contactId,
            responses[i].contactPhoneId,
            responses[i].phoneType,
            responses[i].phoneNumber,
            responses[i].primaryPhone,
          )
      }

      verify(contactPhoneRepository, times(2)).save(contactPhoneCaptor.capture())

      for (x in 0..1) {
        with(contactPhoneCaptor.allValues[x]) {
          assertThat(this)
            .extracting("contactId", "contactPhoneId", "createdBy", "createdTime", "amendedBy", "amendedTime")
            .contains(
              responses[x].contactId,
              // ID generated by DB so not set on call to save
              0L,
              auditInfo.createUsername,
              auditInfo.createDateTime,
              auditInfo.modifyUserId,
              auditInfo.modifyDateTime,
            )
        }
      }
    }

    @Test
    fun `should extract contact identifiers`() {
      val auditInfo = migrateAuditInfo()
      val request = migrateRequest(auditInfo).copy(identifiers = identifiers())
      val responses = listOf(
        ContactIdentityEntity(
          contactId = 1L,
          contactIdentityId = 1L,
          identityType = request.identifiers[0].type.code,
          identityValue = request.identifiers[0].identifier,
          issuingAuthority = request.identifiers[0].issuedAuthority,
          createdBy = auditInfo.createUsername!!,
          createdTime = auditInfo.createDateTime!!,
        ),
        ContactIdentityEntity(
          contactId = 1L,
          contactIdentityId = 2L,
          identityType = request.identifiers[1].type.code,
          identityValue = request.identifiers[1].identifier,
          issuingAuthority = request.identifiers[1].issuedAuthority,
          createdBy = auditInfo.createUsername!!,
          createdTime = auditInfo.createDateTime!!,
        ),
      )

      whenever(contactIdentityRepository.save(any()))
        .thenReturn(responses[0])
        .thenReturn(responses[1])

      val contactIdentityCaptor = argumentCaptor<ContactIdentityEntity>()

      val result = migrationService.extractAndSaveIdentities(request, 1L)

      assertThat(result.size).isEqualTo(2)

      for (i in 0..1) {
        assertThat(result[i].first).isEqualTo(request.identifiers[i].sequence)
        assertThat(result[i].second)
          .extracting("contactId", "contactIdentityId", "identityType", "identityValue", "issuingAuthority")
          .contains(
            responses[i].contactId,
            responses[i].contactIdentityId,
            responses[i].identityType,
            responses[i].identityValue,
            responses[i].issuingAuthority,
          )
      }

      verify(contactIdentityRepository, times(2)).save(contactIdentityCaptor.capture())

      for (x in 0..1) {
        with(contactIdentityCaptor.allValues[x]) {
          assertThat(this)
            .extracting("contactId", "contactIdentityId", "createdBy", "createdTime", "amendedBy", "amendedTime")
            .contains(
              responses[x].contactId,
              // ID generated by DB so not set on call to save
              0L,
              auditInfo.createUsername,
              auditInfo.createDateTime,
              auditInfo.modifyUserId,
              auditInfo.modifyDateTime,
            )
        }
      }
    }
  }

  private fun migrateAuditInfo() =
    MigrateAuditInfo(
      createUsername = "J999J",
      createDateTime = LocalDateTime.of(2024, 1, 1, 13, 47),
      createDisplayName = "Jay Jaysen",
      modifyUserId = "K999K",
      modifyDisplayName = "Kay Kaysen",
      modifyDateTime = LocalDateTime.of(2024, 2, 2, 14, 48),
    )

  private fun migrateRequest(audit: MigrateAuditInfo): MigrateContactRequest =
    MigrateContactRequest(
      personId = 1,
      title = CodedValue("MR", "Mr"),
      lastName = "Smith",
      firstName = "John",
      gender = CodedValue("Male", "Male"),
      keepBiometrics = false,
      audit = audit,
    )

  private fun aContactEntity(contactId: Long = 1L, audit: MigrateAuditInfo) =
    ContactEntity(
      contactId = contactId,
      title = "Mr",
      firstName = "John",
      middleNames = null,
      lastName = "Smith",
      dateOfBirth = null,
      estimatedIsOverEighteen = EstimatedIsOverEighteen.NO,
      isDeceased = false,
      deceasedDate = null,
      createdBy = audit.createUsername!!,
      createdTime = audit.createDateTime!!,
    )

  private fun phoneNumbers() =
    listOf(
      MigratePhoneNumber(phoneId = 1L, number = "11111", extension = "1", type = CodedValue("HOME", "Home")),
      MigratePhoneNumber(phoneId = 2L, number = "22222", extension = "2", type = CodedValue("WORK", "Home")),
    )

  private fun identifiers() =
    listOf(
      MigrateIdentifier(
        sequence = 1L,
        type = CodedValue("DRIVING_LICENCE", "Driving Licence"),
        identifier = "DL1",
        issuedAuthority = "DVLA",
      ),
      MigrateIdentifier(
        sequence = 2L,
        type = CodedValue("PASSPORT", "Passport"),
        identifier = "PASS1",
        issuedAuthority = "UKBORDER",
      ),
    )
}
