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
    assertThat(response.placeOfBirth).isEqualTo(contactEntity.placeOfBirth)
    assertThat(response.active).isEqualTo(contactEntity.active)
    assertThat(response.suspended).isEqualTo(contactEntity.suspended)
    assertThat(response.isStaff).isEqualTo(contactEntity.staffFlag)
    assertThat(response.deceasedFlag).isEqualTo(contactEntity.isDeceased)
    assertThat(response.deceasedDate).isEqualTo(contactEntity.deceasedDate)
    assertThat(response.coronerNumber).isEqualTo(contactEntity.coronerNumber)
    assertThat(response.gender).isEqualTo(contactEntity.gender)
    assertThat(response.domesticStatus).isEqualTo(contactEntity.domesticStatus)
    assertThat(response.languageCode).isEqualTo(contactEntity.languageCode)
    assertThat(response.nationalityCode).isEqualTo(contactEntity.nationalityCode)
    assertThat(response.interpreterRequired).isEqualTo(contactEntity.interpreterRequired)
    assertThat(response.amendedBy).isEqualTo(contactEntity.amendedBy)
    assertThat(response.amendedTime).isEqualTo(contactEntity.amendedTime)
  }

  private fun contactEntity(languageCode: String?): ContactEntity {
    val contactEntity = ContactEntity(
      contactId = 1L,
      title = "Mr",
      firstName = "John",
      lastName = "Doe",
      middleNames = "A B",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      estimatedIsOverEighteen = EstimatedIsOverEighteen.YES,
      createdBy = "system",
      isDeceased = false,
      deceasedDate = null,
      createdTime = LocalDateTime.now(),
      placeOfBirth = "London",
      active = true,
      suspended = false,
      staffFlag = false,
      coronerNumber = "1234",
      gender = "Male",
      domesticStatus = "Single",
      languageCode = languageCode,
      nationalityCode = "GB",
      interpreterRequired = false,
      remitterFlag = false,
      amendedBy = "admin",
      amendedTime = LocalDateTime.now(),
    )
    return contactEntity
  }
}
