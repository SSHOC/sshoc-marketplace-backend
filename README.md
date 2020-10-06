# sshoc-marketplace-backend

Code for the backend of SSHOC Marketplace Portal

## Running application via Docker
Requirements:

```
docker : 19.03.0
docker-compose : 1.24.0
```

Run:

```
docker-compose build
docker-compose up
```

OpenAPI specification:

```
http://localhost:8080/v3/api-docs
http://localhost:8080/swagger-ui/index.html?url=http://localhost:8080/v3/api-docs
```

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
