# hmpps-person-releationship-api
[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-person-releationship-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#hmpps-person-releationship-api "Link to report")
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/ministryofjustice/hmpps-person-releationship-api/tree/main.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/ministryofjustice/hmpps-person-releationship-api/tree/main)
[![Docker Repository on Quay](https://quay.io/repository/hmpps/hmpps-person-releationship-api/status "Docker Repository on Quay")](https://quay.io/repository/hmpps/hmpps-person-releationship-api)
[![API docs](https://img.shields.io/badge/API_docs-view-85EA2D.svg?logo=swagger)](https://contacts-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html#/)

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

Start up the docker dependencies using the docker-compose file in the `hmpps-person-releationship-api` service.

```
docker-compose up --remove-orphans
```

There is a script to help, which sets local profiles, port and DB connection properties to the
values required.

```
./run-local.sh
```

or you can use the `Run API Locally` run config, which should be automatically picked up in intellij but is located in .run if you need to add it manually.

## DPS and NOMIS contact_id and person_id sync
To allow searching for a contact by the same id in both NOMIS and DPS we are keeping contact_id in sync with NOMIS' person_id.
To prevent an overlap of IDs we are starting the DPS contact_id sequence at 20000000. 
When we receive a new contact from NOMIS we use the person_id as the contact_id, ignoring the sequence.
When we create a new contact in DPS we will generate a new contact_id > 20000000 and NOMIS will use the contact_id as the person_id in their database.
The result is that contacts which originate in the DPS service will be in the person/contact ID range 2,000,000+ and those that originate in NOMIS will be in the ID range < 1,000,000.

# NOMIS reference data

We had originally planned that some reference data domains would be temporarily hosted in the Contacts service. 
These were the coded values, descriptions and sort order for domains including COUNTRY, COUNTY, CITY and LANGUAGE.
They do not really belong in the contacts (or personal relationships) domain, but they are used on addresses and to 
specify the primary spoken language for contacts.

These same coded values are used in a variety of other areas of NOMIS, for example, for prisoners, organisations, finance, ROTL and agencies.

It became clear that there is much more analysis required in these areas, and some different ideas about what format addresses outside NOMIS 
should take going forward. There are also some clear omissions and mistakes in the NOMIS data sets for language, country, county 
and city data, but we needed to exactly match these mistakes and all.

Though we have built the endpoints for some of this data, and services and data model to store and expose it, it became clear that the 
analysis work to enrich it, is not an easy or quick task,nor was it core to our current work in extracting and managing contacts outside NOMIS.

We therefore took the decision to deprecate these endpoints until such time as some of these decisions are made, and a suitable domain found
to host a single copy of reference data, with the time to conduct proper analysis.

In the interim, we have re-imported an exact copy NOMIS reference data for the values we need, and these are stored in our `reference_codes` 
table, with an endpoint to return them by `groupName`. 

For sync and migrate to operate, we needed exactly the same coded values as NOMIS in lots of areas, including organisation types, contact types, 
employment types, phone number types, address types etc... ).  
