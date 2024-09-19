package uk.gov.justice.digital.hmpps.hmppscontactsapi.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactAddressEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.hmppscontactsapi.model.request.ContactSearchRequest
import java.math.BigInteger
import java.time.LocalDate

@Repository
class ContactSearchRepository(
  @PersistenceContext
  private var entityManager: EntityManager,
) {
  fun searchContacts(request: ContactSearchRequest, pageable: Pageable): PageImpl<Array<Any>> {
    val cb = entityManager.criteriaBuilder
    val cq = cb.createQuery(Array<Any>::class.java)
    val contact = cq.from(ContactEntity::class.java)
    val contactAddress = cq.from(ContactAddressEntity::class.java)

    cq.multiselect(
      contact,
      contactAddress,
    )

    val predicates: List<Predicate> = buildPredicates(request, cb, contact, contactAddress)

    cq.where(*predicates.toTypedArray())

    applySorting(pageable, cq, cb, contact)

    val resultList = entityManager.createQuery(cq)
      .setFirstResult(pageable.offset.toInt())
      .setMaxResults(pageable.pageSize)
      .resultList

    val total = getTotalCount(request)

    return PageImpl(resultList, pageable, total)
  }

  private fun applySorting(
    pageable: Pageable,
    cq: CriteriaQuery<Array<Any>>,
    cb: CriteriaBuilder,
    contact: Root<ContactEntity>,
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
    contact: Root<ContactEntity>,
    contactAddress: Root<ContactAddressEntity>,
  ): MutableList<Predicate> {
    val predicates: MutableList<Predicate> = ArrayList()

    predicates.add(cb.equal(contact.get<BigInteger>("contactId"), contactAddress.get<BigInteger>("contactAddressId")))

    request.lastName.let {
      predicates.add(
        cb.like(
          cb.upper(contact.get("lastName")),
          cb.literal("%${it.trim().uppercase()}%"),
        ),
      )
    }

    request.firstName?.let {
      predicates.add(
        cb.like(
          cb.upper(contact.get("firstName")),
          cb.literal("%${it.trim().uppercase()}%"),
        ),
      )
    }

    request.middleName?.let {
      predicates.add(
        cb.like(
          cb.upper(contact.get("middleName")),
          cb.literal("%${it.trim().uppercase()}%"),
        ),
      )
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

  private fun getTotalCount(
    request: ContactSearchRequest,
  ): Long {
    val cb = entityManager.criteriaBuilder
    val countQuery = cb.createQuery(Long::class.java)
    val contact = countQuery.from(ContactEntity::class.java)
    val contactAddress = countQuery.from(ContactAddressEntity::class.java)

    val predicates: List<Predicate> = buildPredicates(request, cb, contact, contactAddress)

    countQuery.select(cb.count(contact)).where(*predicates.toTypedArray<Predicate>())
    val total = entityManager.createQuery(countQuery).singleResult
    return total
  }
}
