package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.facade.OrganisationFacade
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.response.Organisation
import java.time.LocalDate
import java.time.LocalDateTime

class OrganisationControllerTest {

  private val organisationFacade: OrganisationFacade = mock()
  private val organisationController = OrganisationController(organisationFacade)

  @Test
  fun `getOrganisationById should return organisation when found`() {
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
    whenever(organisationFacade.getOrganisationById(organisationId)).thenReturn(organisation)

    val response = organisationController.getOrganisationById(organisationId)

    assertThat(response).isEqualTo(organisation)
    verify(organisationFacade).getOrganisationById(organisationId)
  }

  @Test
  fun `getOrganisationById should return null when organisation not found`() {
    val organisationId = 1L
    whenever(organisationFacade.getOrganisationById(organisationId)).thenReturn(null)

    val response = organisationController.getOrganisationById(organisationId)

    assertNull(response)
    verify(organisationFacade).getOrganisationById(organisationId)
  }
}
