package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEmailDetailsEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactEmailDetails
import java.time.LocalDateTime

class ContactEmailDetailsMappersKtTest {

  @Test
  fun `should map all fields from entity to DTO`() {
    assertThat(
      ContactEmailDetailsEntity(
        contactEmailId = 1,
        contactId = 1,
        emailType = "WORK",
        emailTypeDescription = "Work email",
        emailAddress = "test@example.com",
        primaryEmail = false,
        createdBy = "USER",
        createdTime = LocalDateTime.of(2024, 1, 1, 1, 1, 1, 1),
        amendedBy = "AMEND_USER",
        amendedTime = LocalDateTime.of(2024, 2, 2, 2, 2, 2, 2),
      ).toModel(),
    ).isEqualTo(
      ContactEmailDetails(
        contactEmailId = 1,
        contactId = 1,
        emailType = "WORK",
        emailTypeDescription = "Work email",
        emailAddress = "test@example.com",
        primaryEmail = false,
        createdBy = "USER",
        createdTime = LocalDateTime.of(2024, 1, 1, 1, 1, 1, 1),
        amendedBy = "AMEND_USER",
        amendedTime = LocalDateTime.of(2024, 2, 2, 2, 2, 2, 2),
      ),
    )
  }
}
