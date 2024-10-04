package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactPhoneDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactPhoneNumberDetails
import java.time.LocalDateTime

class ContactPhoneDetailsMappersKtTest {

  @Test
  fun `should map all fields`() {
    assertThat(
      ContactPhoneDetailsEntity(
        contactPhoneId = 99,
        contactId = 45,
        phoneType = "HOME",
        phoneTypeDescription = "Home phone",
        phoneNumber = "123456789",
        extNumber = "987654321",
        primaryPhone = true,
        createdBy = "CREATOR",
        createdTime = LocalDateTime.of(2024, 2, 3, 4, 5, 6),
        amendedBy = "AM",
        amendedTime = LocalDateTime.of(2026, 5, 4, 3, 2, 1),
      ).toModel(),
    ).isEqualTo(
      ContactPhoneNumberDetails(
        contactPhoneId = 99,
        contactId = 45,
        phoneType = "HOME",
        phoneTypeDescription = "Home phone",
        phoneNumber = "123456789",
        extNumber = "987654321",
        primaryPhone = true,
        createdBy = "CREATOR",
        createdTime = LocalDateTime.of(2024, 2, 3, 4, 5, 6),
        amendedBy = "AM",
        amendedTime = LocalDateTime.of(2026, 5, 4, 3, 2, 1),
      ),
    )
  }
}
