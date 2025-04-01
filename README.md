# sshoc-marketplace-backend

Code for the backend of SSHOC Marketplace Portal

## Running application via Docker

Minimal requirements:

* docker : 19.03.0
* docker-compose : 1.24.0

Run:

```
docker compose build
export egi_id=dummy egi_secret=dummy token_secret=dummy recaptcha_site_key=dummy recaptcha_secret_key=dummy
docker compose up
```

On earlier docker versions, you may need `docker-compose` (with hyphen) instead of `docker
compose`.

When upgrading from an earlier version *in a development environment*, you may
need to remove any outdated persistent volumes as follows:

```
docker volume rm sshoc-marketplace-backend-media-data sshoc-marketplace-backend-psql-data sshoc-marketplace-backend-solr-data
```

Also make ensure you are using the latest images after upgraded and no previously cached containers.
You can also build with `docker compose build --no-cache`.

Once running, you can access the OpenAPI specification here:

* <http://localhost:8080/v3/api-docs>
* <http://localhost:8080/swagger-ui/index.html?url=http://localhost:8080/v3/api-docs>

The development data has the following default users and passwords predefined:

| Username | Password |
| -------- | -------- |
| `Administrator` | `q1w2e3r4t5` |
| `Moderator` | `q1w2e3r4t5` |
| `Contributor` | `q1w2e3r4t5` |
| `System importer` | `q1w2e3r4t5` |

## Running application to development

Requirements in addition to docker:

```
java 11
maven 3.6.0
IntelliJ / Eclipse with Lombok (https://projectlombok.org/) and MapStruct Support (https://mapstruct.org/development/ide-setup/)
```

Run:

```
docker-compose build
docker-compose up psql solr
In IntelliJ / Eclipse run eu.sshopencloud.marketplace.MarketplaceApplication
```


Run tests:

```
APPLICATION_PROFILE=test mvn verify
```
