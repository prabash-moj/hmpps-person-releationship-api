package uk.gov.justice.digital.hmpps.hmppscontactsapi.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppscontactsapi.facade.EstateWideRestrictionsFacade
import uk.gov.justice.digital.hmpps.hmppscontactsapi.helpers.createContactRestrictionDetails
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.CreateContactRestrictionRequest
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.UpdateContactRestrictionRequest
import java.time.LocalDate

class EstateWideRestrictionControllerTest {
  private val facade: EstateWideRestrictionsFacade = mock()
  private val controller = EstateWideRestrictionController(facade)

  @Nested
  inner class GetContactRestrictions {
    @Test
    fun `should get restrictions`() {
      val expected = listOf(createContactRestrictionDetails())
      whenever(facade.getEstateWideRestrictionsForContact(9)).thenReturn(expected)

      val response = controller.getEstateWideContactRestrictions(9)

      assertThat(response).isEqualTo(expected)
    }

    @Test
    fun `should propagate exceptions getting a restriction`() {
      val expected = RuntimeException("Bang!")
      whenever(facade.getEstateWideRestrictionsForContact(9)).thenThrow(expected)

      val result = assertThrows<RuntimeException> {
        controller.getEstateWideContactRestrictions(9)
      }
      assertThat(result).isEqualTo(expected)
    }
  }

  @Nested
  inner class CreateContactRestrictions {
    private val request = CreateContactRestrictionRequest(
      restrictionType = "BAN",
      startDate = LocalDate.of(2020, 1, 1),
      expiryDate = LocalDate.of(2022, 2, 2),
      comments = "Some comments",
      createdBy = "created",
    )

    @Test
    fun `should create restrictions`() {
      val expected = createContactRestrictionDetails()
      whenever(facade.createEstateWideRestriction(9, request)).thenReturn(expected)

      val response = controller.createEstateWideRestriction(9, request)

      assertThat(response.body).isEqualTo(expected)
      verify(facade).createEstateWideRestriction(9, request)
    }

    @Test
    fun `should propagate exceptions creating a restriction`() {
      val expected = RuntimeException("Bang!")
      whenever(facade.createEstateWideRestriction(9, request)).thenThrow(expected)

      val result = assertThrows<RuntimeException> {
        controller.createEstateWideRestriction(9, request)
      }
      assertThat(result).isEqualTo(expected)
    }
  }

  @Nested
  inner class UpdateContactRestrictions {
    private val contactRestrictionId = 564L
    private val request = UpdateContactRestrictionRequest(
      restrictionType = "BAN",
      startDate = LocalDate.of(2020, 1, 1),
      expiryDate = LocalDate.of(2022, 2, 2),
      comments = "Some comments",
      updatedBy = "updated",
    )

    @Test
    fun `should update restrictions`() {
      val expected = createContactRestrictionDetails()
      whenever(facade.updateEstateWideRestriction(9, contactRestrictionId, request)).thenReturn(expected)

      val response = controller.updateEstateWideRestriction(9, contactRestrictionId, request)

      assertThat(response).isEqualTo(expected)
      verify(facade).updateEstateWideRestriction(9, contactRestrictionId, request)
    }

    @Test
    fun `should propagate exceptions updating a restriction`() {
      val expected = RuntimeException("Bang!")
      whenever(facade.updateEstateWideRestriction(9, contactRestrictionId, request)).thenThrow(expected)

      val result = assertThrows<RuntimeException> {
        controller.updateEstateWideRestriction(9, contactRestrictionId, request)
      }
      assertThat(result).isEqualTo(expected)
    }
  }
}
