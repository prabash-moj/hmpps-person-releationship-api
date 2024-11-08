package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEmailEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.ContactEmailDetails
import java.time.LocalDateTime

class SyncContactEmailDetailsMappersKtTest {

  @Test
  fun `should map all fields from entity to DTO`() {
    assertThat(
      ContactEmailEntity(
        contactEmailId = 1,
        contactId = 1,
        emailAddress = "test@example.com",
        createdBy = "USER",
        createdTime = LocalDateTime.of(2024, 1, 1, 1, 1, 1, 1),
        amendedBy = "AMEND_USER",
        amendedTime = LocalDateTime.of(2024, 2, 2, 2, 2, 2, 2),
      ).toModel(),
    ).isEqualTo(
      ContactEmailDetails(
        contactEmailId = 1,
        contactId = 1,
        emailAddress = "test@example.com",
        createdBy = "USER",
        createdTime = LocalDateTime.of(2024, 1, 1, 1, 1, 1, 1),
        amendedBy = "AMEND_USER",
        amendedTime = LocalDateTime.of(2024, 2, 2, 2, 2, 2, 2),
      ),
    )
  }
}
