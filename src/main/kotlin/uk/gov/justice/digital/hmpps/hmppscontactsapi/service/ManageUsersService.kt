package uk.gov.justice.digital.hmpps.hmppscontactsapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.manage.users.ManageUsersApiClient
import uk.gov.justice.digital.hmpps.hmppscontactsapi.client.manage.users.User

@Service
class ManageUsersService(private val manageUsersClient: ManageUsersApiClient) {
  fun getUserByUsername(username: String): User? {
    return manageUsersClient.getUserByUsername(username)
  }
}
