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

## Running application to development
Requirements in addition to docker:

```
java 8
maven 3.6.0
IntelliJ / Eclipse with Lombok (https://projectlombok.org/)
```

Run:

```
docker-compose build
docker-compose up psql solr
In IntelliJ / Eclipse run eu.sshopencloud.marketplace.MarketplaceApplication
```
