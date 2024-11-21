package uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.patch

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.EstimatedIsOverEighteen
import java.time.LocalDate
import java.time.LocalDateTime

class PatchContactEntityMappersKtTest {

  @Test
  fun `mapToResponse should correctly map ContactEntity to PatchContactResponse`() {
    val contactEntity = contactEntity("EN")

    val response = contactEntity.mapToResponse()

    assertThat(response.id).isEqualTo(contactEntity.contactId)
    assertThat(response.title).isEqualTo(contactEntity.title)
    assertThat(response.firstName).isEqualTo(contactEntity.firstName)
    assertThat(response.lastName).isEqualTo(contactEntity.lastName)
    assertThat(response.middleNames).isEqualTo(contactEntity.middleNames)
    assertThat(response.dateOfBirth).isEqualTo(contactEntity.dateOfBirth)
    assertThat(response.estimatedIsOverEighteen).isEqualTo(contactEntity.estimatedIsOverEighteen)
    assertThat(response.createdBy).isEqualTo(contactEntity.createdBy)
    assertThat(response.createdTime).isEqualTo(contactEntity.createdTime)
    assertThat(response.isStaff).isEqualTo(contactEntity.staffFlag)
    assertThat(response.deceasedFlag).isEqualTo(contactEntity.isDeceased)
    assertThat(response.deceasedDate).isEqualTo(contactEntity.deceasedDate)
    assertThat(response.gender).isEqualTo(contactEntity.gender)
    assertThat(response.domesticStatus).isEqualTo(contactEntity.domesticStatus)
    assertThat(response.languageCode).isEqualTo(contactEntity.languageCode)
    assertThat(response.interpreterRequired).isEqualTo(contactEntity.interpreterRequired)
    assertThat(response.updatedBy).isEqualTo(contactEntity.amendedBy)
    assertThat(response.updatedTime).isEqualTo(contactEntity.amendedTime)
  }

  private fun contactEntity(languageCode: String?) =
    ContactEntity(
      contactId = 1L,
      title = "MR",
      firstName = "John",
      lastName = "Doe",
      middleNames = "A B",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      estimatedIsOverEighteen = EstimatedIsOverEighteen.YES,
      createdBy = "system",
      isDeceased = false,
      deceasedDate = null,
      createdTime = LocalDateTime.now(),
      staffFlag = false,
      gender = "M",
      domesticStatus = "S",
      languageCode = languageCode,
      interpreterRequired = false,
      remitterFlag = false,
      amendedBy = "admin",
      amendedTime = LocalDateTime.now(),
    )
}
