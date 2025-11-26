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

## Environment variables in use

### Handle service

The [handle](https://www.handle.net/) service function creates persistent identifiers (PIDs) for workflows. If all environment variables are set in a correct way, it will allow to gather a handle for a workflow if the parameter `createHandle` of the `POST /api/workflow` is set to `true`. See also this issue: https://github.com/SSHOC/sshoc-marketplace-backend/issues/518
The handle service is an alternative option. It is not required for running the SSHOMP. If the handle service is used, then all variables should be set. There are some default values in place but unless the key file for using the handle is not present in the `HANDLE_KEY_LOCATION` the service won't be active. The key file must be requested from a handle provider/user.

|Name|Type in repository (Variable or Secret)|Description|Example Value|
|----|----|-----------|-------------|
|HANDLE_BASE_URL|Variable|The URL of the workflow is registered at the handle service. The base url is defined in this variable. The other parts of the URL pointing to a specific workflow version are determined by the SSHOMP and applied to the base url. It should be base url where the frontend is running.|`https://marketplace.sshopencloud.eu`|
|HANDLE_APP_VALUE|Variable|This variable is part of the handle URL/PID and represents the registered app at a handle provider. Setup and get the correct value from a handle provider.|`21.11160`|
|HANDLE_USER_ID|Variable|The user id is used for the registration/authentication workflow of a new PID at the handle service. Setup and get the correct value from a handle provider.|`[some-given-value]/21.11160`|
|HANDLE_USER_ID_INDEX|Variable|This seems to be a fixed value given by the handle provider necessary for the registration/authentication workflow.|`300`|
|HANDLE_KEY_LOCATION|Variable|Points to the key file that is used and necessary for the registration/authentication workflow. It is provided by the handle provider and should be put on a volume in the Docker environment which is then mounted into the SSHOMP backend pod.|`/var/sshomp/handle/keyfile.bin`|

