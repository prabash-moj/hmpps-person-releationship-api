# hmpps-contacts-api
[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-contacts-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#hmpps-contacts-api "Link to report")
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/ministryofjustice/hmpps-contacts-api/tree/main.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/ministryofjustice/hmpps-contacts-api/tree/main)
[![Docker Repository on Quay](https://quay.io/repository/hmpps/hmpps-contacts-api/status "Docker Repository on Quay")](https://quay.io/repository/hmpps/hmpps-contacts-api)
[![API docs](https://img.shields.io/badge/API_docs-view-85EA2D.svg?logo=swagger)](https://contacts-api-dev.prison.service.justice.gov.uk/swagger-ui/index.html#/)

API to support the front end service allowing court and probation users to manage contacts for the people in prison.

## Building the project

Tools required:

* JDK v21+
* Kotlin (Intellij)
* docker
* docker-compose

Useful tools but not essential:

* KUBECTL not essential for building the project but will be needed for other tasks. Can be installed with `brew`.
* [k9s](https://k9scli.io/) a terminal based UI to interact with your Kubernetes clusters. Can be installed with `brew`.
* [jq](https://jqlang.github.io/jq/) a lightweight and flexible command-line JSON processor. Can be installed with `brew`.

## Install gradle and build the project

```
./gradlew
```

```
./gradlew clean build
```

## Running the service

There are two key environment variables needed to run the service. The system client id and secret used to retrieve the OAuth 2.0 access token needed for service to service API calls can be set as local environment variables.
This allows API calls made from this service that do not use the caller's token to successfully authenticate.

Add the following to a local `.env` file in the root folder of this project (_you can extract the credentials from the dev k8s project namespace_).

N.B. you must escape any '$' characters with '\\$'

```
SYSTEM_CLIENT_ID=<system.client.id>
SYSTEM_CLIENT_SECRET=<system.client.secret>
HMPPS_AUTH_URL=https://sign-in-dev.hmpps.service.justice.gov.uk/auth
PRISON_SEARCH_API_URL=https://prisoner-search-dev.prison.service.justice.gov.uk
DB_SERVER=localhost
DB_NAME=contacts-db
DB_USER=contacts
DB_PASS=contacts
DB_SSL_MODE=prefer
```

Start up the docker dependencies using the docker-compose file in the `hmpps-contacts-api` service.

```
docker-compose up --remove-orphans
```

There is a script to help, which sets local profiles, port and DB connection properties to the
values required.

```
./run-local.sh
```

or you can use the `Run API Locally` run config, which should be automatically picked up in intellij but is located in .run if you need to add it manually.

## Testing GOV Notify locally

To test Gov Notify emails locally, you just need to add one more variable to your `.env` file.

```
export NOTIFY_API_KEY=<gov.notify.api.key>
```
If you have added it correctly, you will see the log on startup with the following output:

```
Gov Notify emails are enabled
```
