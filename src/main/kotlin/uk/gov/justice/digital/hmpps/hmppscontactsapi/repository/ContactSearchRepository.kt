package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactWithAddressEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactSearchRequest
import java.time.LocalDate

@Repository
class ContactSearchRepository(
  @PersistenceContext
  private var entityManager: EntityManager,
) {
  fun searchContacts(request: ContactSearchRequest, pageable: Pageable): Page<ContactWithAddressEntity> {
    val cb = entityManager.criteriaBuilder
    val cq = cb.createQuery(ContactWithAddressEntity::class.java)
    val contact = cq.from(ContactWithAddressEntity::class.java)

    val predicates: List<Predicate> = buildPredicates(request, cb, contact)

    cq.where(*predicates.toTypedArray())

    applySorting(pageable, cq, cb, contact)

    val resultList = entityManager.createQuery(cq)
      .setFirstResult(pageable.offset.toInt())
      .setMaxResults(pageable.pageSize)
      .resultList

    val total = getTotalCount(request)

    return PageImpl(resultList, pageable, total)
  }

  private fun getTotalCount(
    request: ContactSearchRequest,
  ): Long {
    val cb = entityManager.criteriaBuilder
    val countQuery = cb.createQuery(Long::class.java)
    val contact = countQuery.from(ContactWithAddressEntity::class.java)

    val predicates: List<Predicate> = buildPredicates(request, cb, contact)

    countQuery.select(cb.count(contact)).where(*predicates.toTypedArray<Predicate>())
    return entityManager.createQuery(countQuery).singleResult
  }

  private fun applySorting(
    pageable: Pageable,
    cq: CriteriaQuery<ContactWithAddressEntity>,
    cb: CriteriaBuilder,
    contact: Root<ContactWithAddressEntity>,
  ) {
    if (pageable.sort.isSorted) {
      pageable.sort.forEach {
        when {
          it.isAscending -> cq.orderBy(cb.asc(contact.get<String>(it.property)))
          else -> cq.orderBy(cb.desc(contact.get<String>(it.property)))
        }
      }
    }
  }

  private fun buildPredicates(
    request: ContactSearchRequest,
    cb: CriteriaBuilder,
    contact: Root<ContactWithAddressEntity>,
  ): MutableList<Predicate> {
    val predicates: MutableList<Predicate> = ArrayList()

    predicates.add(cb.ilikePredicate(contact, "lastName", request.lastName))

    request.firstName?.let {
      predicates.add(cb.ilikePredicate(contact, "firstName", it))
    }

    request.middleNames?.let {
      predicates.add(cb.ilikePredicate(contact, "middleNames", it))
    }

    request.dateOfBirth?.let {
      predicates.add(
        cb.equal(
          contact.get<LocalDate>("dateOfBirth"),
          request.dateOfBirth,
        ),
      )
    }

    return predicates
  }
}
