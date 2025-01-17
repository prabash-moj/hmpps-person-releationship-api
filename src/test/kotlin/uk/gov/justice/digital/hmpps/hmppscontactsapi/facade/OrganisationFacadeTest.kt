package uk.gov.justice.digital.hmpps.hmppscontactsapi.facade

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Organisation
import uk.gov.justice.digital.hmpps.hmppscontactsapi.service.OrganisationService
import java.time.LocalDate
import java.time.LocalDateTime

class OrganisationFacadeTest {

  private val organisationService: OrganisationService = mock()
  private val facade = OrganisationFacade(organisationService)

  @Test
  fun `should not send event on get`() {
    val organisationId = 1L
    val deactivatedDate = LocalDate.now()
    val createdTime = LocalDateTime.now().minusMinutes(20)
    val updatedTime = LocalDateTime.now().plusMinutes(20)
    val organisation = Organisation(
      organisationId = 1L,
      organisationName = "Name",
      programmeNumber = "P1",
      vatNumber = "V1",
      caseloadId = "C1",
      comments = "C2",
      active = false,
      deactivatedDate = deactivatedDate,
      createdBy = "Created by",
      createdTime = createdTime,
      updatedBy = "U1",
      updatedTime = updatedTime,
    )
    whenever(organisationService.getOrganisationById(organisationId)).thenReturn(organisation)

    val result = facade.getOrganisationById(organisationId)

    assertThat(result).isEqualTo(organisation)
    verify(organisationService).getOrganisationById(organisationId)
  }
}
