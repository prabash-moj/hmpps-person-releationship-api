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
import uk.gov.justice.digital.hmpps.hmppscontactsapi.repository.ReferenceCodeRepository

class ReferenceCodeServiceTest {
  private val referenceCodeRepository: ReferenceCodeRepository = mock()
  private val service = ReferenceCodeService(referenceCodeRepository)

  @BeforeEach
  fun setUp() {
    openMocks(this)
  }

  @Test
  fun `Should return a list of references codes for court hearing type`() {
    val groupCode = "RELATIONSHIP"
    val listOfCodes = listOf(
      ReferenceCodeEntity(1L, groupCode, "FRIEND", "Friend", 0, "name"),
      ReferenceCodeEntity(2L, groupCode, "MOTHER", "Mother", 1, "name"),
      ReferenceCodeEntity(3L, groupCode, "FATHER", "Father", 2, "name"),
    )

    whenever(referenceCodeRepository.findAllByGroupCodeEquals(groupCode, Sort.unsorted())).thenReturn(listOfCodes)

    assertThat(service.getReferenceDataByGroup(groupCode, Sort.unsorted())).isEqualTo(listOfCodes.toModel())

    verify(referenceCodeRepository).findAllByGroupCodeEquals(groupCode, Sort.unsorted())
  }

  @Test
  fun `Should return an empty list when no reference codes are matched`() {
    val groupCode = "TITLE"
    whenever(referenceCodeRepository.findAllByGroupCodeEquals(groupCode, Sort.unsorted())).thenReturn(emptyList())
    assertThat(service.getReferenceDataByGroup(groupCode, Sort.unsorted())).isEmpty()
    verify(referenceCodeRepository).findAllByGroupCodeEquals(groupCode, Sort.unsorted())
  }
}
