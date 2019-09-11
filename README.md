# sshoc-marketplace-backend

Code for the backend

Only a draft model is currently available 

## Via Docker
This is how to build and launch the code for now
- mvn clean test package

Note: Obviously change the following environment variables

### Directly with the variables inline
- SPRING_DATA_SOURCE_URL=jdbc:postgresql://psql:5432/marketplace POSTGRES_DB=marketplace POSTGRES_USER=mp_user
 POSTGRES_PWD=mp_pwd docker-compose build
- SPRING_DATA_SOURCE_URL=jdbc:postgresql://psql:5432/marketplace POSTGRES_DB=marketplace POSTGRES_USER=mp_user
 POSTGRES_PWD=mp_pwd docker-compose up
 
 ### With the variables in an .env file next to the Dockerfile which includes those 4 variables as in a property and then simply:
- docker-compose build
- docker-compose up