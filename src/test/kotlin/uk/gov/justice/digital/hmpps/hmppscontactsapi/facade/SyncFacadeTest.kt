package uk.gov.justice.digital.hmpps.hmppscontactsapi.facade

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateContactEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateContactIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateContactPhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncCreateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncUpdateContactEmailRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncUpdateContactIdentityRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncUpdateContactPhoneRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.sync.SyncUpdateContactRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.SyncContact
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.SyncContactEmail
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.SyncContactIdentity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.sync.SyncContactPhone
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.OutboundEventsService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.events.Source
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncContactAddressService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncContactEmailService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncContactIdentityService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncContactPhoneService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncContactRestrictionService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncContactService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncPrisonerContactRestrictionService
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.sync.SyncPrisonerContactService
import java.time.LocalDate
import java.time.LocalDateTime

class SyncFacadeTest {
  private val syncContactService: SyncContactService = mock()
  private val syncContactPhoneService: SyncContactPhoneService = mock()
  private val syncContactAddressService: SyncContactAddressService = mock()
  private val syncContactEmailService: SyncContactEmailService = mock()
  private val syncContactIdentityService: SyncContactIdentityService = mock()
  private val syncContactRestrictionService: SyncContactRestrictionService = mock()
  private val syncPrisonerContactService: SyncPrisonerContactService = mock()
  private val syncPrisonerContactRestrictionService: SyncPrisonerContactRestrictionService = mock()
  private val outboundEventsService: OutboundEventsService = mock()

  private val facade = SyncFacade(
    syncContactService,
    syncContactPhoneService,
    syncContactAddressService,
    syncContactEmailService,
    syncContactIdentityService,
    syncContactRestrictionService,
    syncPrisonerContactService,
    syncPrisonerContactRestrictionService,
    outboundEventsService,
  )

  @Nested
  inner class ContactSyncFacadeEvents {
    @Test
    fun `should send domain event on contact create success`() {
      val request = createSyncContactRequest(personId = 1L)
      val response = contactResponse(1L)

      whenever(syncContactService.createContact(any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any(), any())).then {}

      val result = facade.createContact(request)

      assertThat(result.id).isEqualTo(request.personId)

      verify(syncContactService).createContact(request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.CONTACT_CREATED,
        identifier = result.id,
        contactId = result.id,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send domain event on contact create failure`() {
      val request = createSyncContactRequest(personId = 2L)
      val expectedException = RuntimeException("Bang!")

      whenever(syncContactService.createContact(any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.createContact(request)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)

      verify(syncContactService).createContact(request)
      verify(outboundEventsService, never()).send(any(), any(), any(), any(), any())
    }

    @Test
    fun `should send domain event on contact update success`() {
      val request = updateContactSyncRequest()
      val response = contactResponse(3L)

      whenever(syncContactService.updateContact(any(), any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any(), any())).then {}

      val result = facade.updateContact(3L, request)

      assertThat(result.id).isEqualTo(3L)

      verify(syncContactService).updateContact(3L, request)

      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.CONTACT_UPDATED,
        identifier = result.id,
        contactId = result.id,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send domain event on contact update failure`() {
      val request = updateContactSyncRequest()
      val expectedException = RuntimeException("Bang!")

      whenever(syncContactService.updateContact(any(), any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.updateContact(4L, request)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)

      verify(syncContactService).updateContact(4L, request)
      verify(outboundEventsService, never()).send(any(), any(), any(), any(), any())
    }

    @Test
    fun `should send domain event on contact delete success`() {
      whenever(syncContactService.deleteContact(any())).then {}
      whenever(outboundEventsService.send(any(), any(), any(), any(), any())).then {}

      facade.deleteContact(1L)

      verify(syncContactService).deleteContact(1L)

      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.CONTACT_DELETED,
        identifier = 1L,
        contactId = 1L,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send domain event on contact delete failure`() {
      val expectedException = RuntimeException("Bang!")

      whenever(syncContactService.deleteContact(any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.deleteContact(1L)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)
      verify(syncContactService).deleteContact(1L)
      verify(outboundEventsService, never()).send(any(), any(), any(), any(), any())
    }

    private fun createSyncContactRequest(personId: Long) =
      SyncCreateContactRequest(
        personId = personId,
        firstName = "John",
        lastName = "Doe",
        dateOfBirth = LocalDate.of(1980, 1, 1),
        createdBy = "ANYONE",
      )

    private fun contactResponse(contactId: Long) =
      SyncContact(
        id = contactId,
        firstName = "John",
        lastName = "Doe",
        createdBy = "ANYONE",
        createdTime = LocalDateTime.now(),
        updatedBy = null,
        updatedTime = null,
        estimatedIsOverEighteen = null,
      )

    private fun updateContactSyncRequest() =
      SyncUpdateContactRequest(
        firstName = "John",
        lastName = "Doe",
        estimatedIsOverEighteen = null,
        updatedBy = "ANYONE",
        updatedTime = LocalDateTime.now(),
      )
  }

  @Nested
  inner class ContactPhoneSyncFacadeEvents {
    @Test
    fun `should send domain event on contact phone create success`() {
      val request = createContactPhoneRequest(contactId = 1L)
      val response = contactPhoneResponse(contactId = 1L, contactPhoneId = 1L)

      whenever(syncContactPhoneService.createContactPhone(any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any(), any())).then {}

      val result = facade.createContactPhone(request)

      verify(syncContactPhoneService).createContactPhone(request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.CONTACT_PHONE_CREATED,
        identifier = result.contactPhoneId,
        contactId = result.contactId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should send domain event on contact phone update success`() {
      val request = updateContactPhoneRequest(contactId = 2L)
      val response = contactPhoneResponse(contactId = 2L, contactPhoneId = 3L)

      whenever(syncContactPhoneService.updateContactPhone(any(), any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any(), any())).then {}

      val result = facade.updateContactPhone(3L, request)

      verify(syncContactPhoneService).updateContactPhone(3L, request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.CONTACT_PHONE_UPDATED,
        identifier = result.contactPhoneId,
        contactId = result.contactId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should send domain event on contact phone delete success`() {
      val response = contactPhoneResponse(contactId = 3L, contactPhoneId = 4L)

      whenever(syncContactPhoneService.deleteContactPhone(any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any(), any())).then {}

      val result = facade.deleteContactPhone(4L)

      verify(syncContactPhoneService).deleteContactPhone(4L)

      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.CONTACT_PHONE_DELETED,
        identifier = result.contactPhoneId,
        contactId = result.contactId,
        source = Source.NOMIS,
      )
    }

    private fun createContactPhoneRequest(contactId: Long) =
      SyncCreateContactPhoneRequest(
        contactId = contactId,
        phoneType = "MOB",
        phoneNumber = "0909 111222",
        createdBy = "CREATOR",
        createdTime = LocalDateTime.now(),
      )

    private fun updateContactPhoneRequest(contactId: Long) =
      SyncUpdateContactPhoneRequest(
        contactId = contactId,
        phoneType = "MOB",
        phoneNumber = "0909 111222",
        updatedBy = "UPDATER",
        updatedTime = LocalDateTime.now(),
      )
    private fun contactPhoneResponse(contactId: Long, contactPhoneId: Long) =
      SyncContactPhone(
        contactPhoneId = contactPhoneId,
        contactId = contactId,
        phoneType = "MOB",
        phoneNumber = "0909 111222",
        extNumber = null,
        createdBy = "CREATOR",
        createdTime = LocalDateTime.now(),
        updatedBy = null,
        updatedTime = null,
      )
  }

  @Nested
  inner class ContactEmailSyncFacadeEvents {
    @Test
    fun `should send domain event on contact email create success`() {
      val request = createContactEmailRequest(contactId = 1L)
      val response = contactEmailResponse(contactId = 1L, contactEmailId = 1L)

      whenever(syncContactEmailService.createContactEmail(any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any(), any())).then {}

      val result = facade.createContactEmail(request)

      verify(syncContactEmailService).createContactEmail(request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.CONTACT_EMAIL_CREATED,
        identifier = result.contactEmailId,
        contactId = result.contactId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should send domain event on contact email update success`() {
      val request = updateContactEmailRequest(contactId = 2L)
      val response = contactEmailResponse(contactId = 2L, contactEmailId = 3L)

      whenever(syncContactEmailService.updateContactEmail(any(), any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any(), any())).then {}

      val result = facade.updateContactEmail(3L, request)

      verify(syncContactEmailService).updateContactEmail(3L, request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.CONTACT_EMAIL_UPDATED,
        identifier = result.contactEmailId,
        contactId = result.contactId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should send domain event on contact email delete success`() {
      val response = contactEmailResponse(contactId = 3L, contactEmailId = 4L)

      whenever(syncContactEmailService.deleteContactEmail(any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any(), any())).then {}

      val result = facade.deleteContactEmail(4L)

      verify(syncContactEmailService).deleteContactEmail(4L)

      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.CONTACT_EMAIL_DELETED,
        identifier = result.contactEmailId,
        contactId = result.contactId,
        source = Source.NOMIS,
      )
    }

    private fun createContactEmailRequest(contactId: Long) =
      SyncCreateContactEmailRequest(
        contactId = contactId,
        emailAddress = "0909 111222",
        createdBy = "CREATOR",
        createdTime = LocalDateTime.now(),
      )

    private fun updateContactEmailRequest(contactId: Long) =
      SyncUpdateContactEmailRequest(
        contactId = contactId,
        emailAddress = "test@test.com",
        updatedBy = "UPDATER",
        updatedTime = LocalDateTime.now(),
      )
    private fun contactEmailResponse(contactId: Long, contactEmailId: Long) =
      SyncContactEmail(
        contactEmailId = contactEmailId,
        contactId = contactId,
        emailAddress = "test@test.com",
        createdBy = "CREATOR",
        createdTime = LocalDateTime.now(),
        updatedBy = null,
        updatedTime = null,
      )
  }

  @Nested
  inner class ContactIdentitySyncFacadeEvents {
    @Test
    fun `should send domain event on contact identity create success`() {
      val request = createContactIdentityRequest(contactId = 1L)
      val response = contactIdentityResponse(contactId = 1L, contactIdentityId = 1L)

      whenever(syncContactIdentityService.createContactIdentity(any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any(), any())).then {}

      val result = facade.createContactIdentity(request)

      verify(syncContactIdentityService).createContactIdentity(request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.CONTACT_IDENTITY_CREATED,
        identifier = result.contactIdentityId,
        contactId = result.contactId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should send domain event on contact identity update success`() {
      val request = updateContactIdentityRequest(contactId = 2L)
      val response = contactIdentityResponse(contactId = 2L, contactIdentityId = 3L)

      whenever(syncContactIdentityService.updateContactIdentity(any(), any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any(), any())).then {}

      val result = facade.updateContactIdentity(3L, request)

      verify(syncContactIdentityService).updateContactIdentity(3L, request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.CONTACT_IDENTITY_UPDATED,
        identifier = result.contactIdentityId,
        contactId = result.contactId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should send domain event on contact identity delete success`() {
      val response = contactIdentityResponse(contactId = 3L, contactIdentityId = 4L)

      whenever(syncContactIdentityService.deleteContactIdentity(any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any(), any())).then {}

      val result = facade.deleteContactIdentity(4L)

      verify(syncContactIdentityService).deleteContactIdentity(4L)

      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.CONTACT_IDENTITY_DELETED,
        identifier = result.contactIdentityId,
        contactId = result.contactId,
        source = Source.NOMIS,
      )
    }

    private fun createContactIdentityRequest(contactId: Long) =
      SyncCreateContactIdentityRequest(
        contactId = contactId,
        identityType = "PASS",
        identityValue = "PW12345678",
        issuingAuthority = "Passport Office",
        createdBy = "CREATOR",
        createdTime = LocalDateTime.now(),
      )

    private fun updateContactIdentityRequest(contactId: Long) =
      SyncUpdateContactIdentityRequest(
        contactId = contactId,
        identityType = "PASS",
        identityValue = "PW12345678",
        issuingAuthority = "Passport Office",
        updatedBy = "UPDATER",
        updatedTime = LocalDateTime.now(),
      )
    private fun contactIdentityResponse(contactId: Long, contactIdentityId: Long) =
      SyncContactIdentity(
        contactIdentityId = contactIdentityId,
        contactId = contactId,
        identityType = "PASS",
        identityValue = "PW12345678",
        issuingAuthority = "Passport Office",
        createdBy = "CREATOR",
        createdTime = LocalDateTime.now(),
        updatedBy = null,
        updatedTime = null,
      )
  }
}
