package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.openMocks
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Sort
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ReferenceCodeEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.ReferenceCodeGroup
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ReferenceCodeRepository

class ReferenceCodeServiceTest {
  private val referenceCodeRepository: ReferenceCodeRepository = mock()
  private val service = ReferenceCodeService(referenceCodeRepository)

  @BeforeEach
  fun setUp() {
    openMocks(this)
  }

  @Test
  fun `Should return a list of references codes with active only`() {
    val groupCode = ReferenceCodeGroup.RELATIONSHIP
    val listOfCodes = listOf(
      ReferenceCodeEntity(1L, groupCode, "FRIEND", "Friend", 0, true, "name"),
      ReferenceCodeEntity(2L, groupCode, "MOTHER", "Mother", 1, true, "name"),
      ReferenceCodeEntity(3L, groupCode, "FATHER", "Father", 2, true, "name"),
    )

    whenever(referenceCodeRepository.findAllByGroupCodeAndIsActiveEquals(groupCode, true, Sort.unsorted())).thenReturn(listOfCodes)

    assertThat(service.getReferenceDataByGroup(groupCode, Sort.unsorted(), true)).isEqualTo(listOfCodes.toModel())

    verify(referenceCodeRepository).findAllByGroupCodeAndIsActiveEquals(groupCode, true, Sort.unsorted())
  }

  @Test
  fun `Should return a list of references codes for all is active status`() {
    val groupCode = ReferenceCodeGroup.RELATIONSHIP
    val listOfCodes = listOf(
      ReferenceCodeEntity(1L, groupCode, "FRIEND", "Friend", 0, true, "name"),
      ReferenceCodeEntity(2L, groupCode, "MOTHER", "Mother", 1, true, "name"),
      ReferenceCodeEntity(3L, groupCode, "FATHER", "Father", 2, true, "name"),
    )

    whenever(referenceCodeRepository.findAllByGroupCodeAndIsActiveEquals(groupCode, true, Sort.unsorted())).thenReturn(listOfCodes)

    assertThat(service.getReferenceDataByGroup(groupCode, Sort.unsorted(), true)).isEqualTo(listOfCodes.toModel())

    verify(referenceCodeRepository).findAllByGroupCodeAndIsActiveEquals(groupCode, true, Sort.unsorted())
  }

  @Test
  fun `Should return an empty list when no reference codes are matched active only`() {
    val groupCode = ReferenceCodeGroup.TITLE
    whenever(referenceCodeRepository.findAllByGroupCodeEquals(groupCode, Sort.unsorted())).thenReturn(emptyList())
    assertThat(service.getReferenceDataByGroup(groupCode, Sort.unsorted(), false)).isEmpty()
    verify(referenceCodeRepository).findAllByGroupCodeEquals(groupCode, Sort.unsorted())
  }

  @Test
  fun `Should return an empty list when no reference codes are matched inactive included`() {
    val groupCode = ReferenceCodeGroup.TITLE
    whenever(referenceCodeRepository.findAllByGroupCodeEquals(groupCode, Sort.unsorted())).thenReturn(emptyList())
    assertThat(service.getReferenceDataByGroup(groupCode, Sort.unsorted(), false)).isEmpty()
    verify(referenceCodeRepository).findAllByGroupCodeEquals(groupCode, Sort.unsorted())
  }
}
