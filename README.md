# FOREDOOMWATCH

## My vision with the project
A centralized blue team vulnerability management tool for monitoring registered assets.
The tool is meant to be part of a networking ecosystem, where it can be complicated to keep track of every asset and if they have vulnerabilities.

---

## Links

Portfolio website:  
https://gladvillainy.github.io/Portfolio/

Project overview video (max 5 min):  
<LINK>

Deployed application (optional):  
<LINK>

Source code repository:  
<LINK>

---

# Architecture

## System Overview

The system functions like a flow, where the data flows through the layers,
and each layer controls its own responsibilities.

Like a pipeline, it starts at one spot, the endpoint, which receives the request from the user.
Before the request reaches the controller, an access handler verifies the user’s JWT token and role.
If the token is valid, the request is allowed into the system.

Each endpoint is routed to a controller method, and the controller works like a navigator: it calls the 
layers needed to handle the request, which keeps the flow seamless. Controllers are essential for communication,
because they are the bridge between the view and the backend. In the view we use JSON, while the backend works with DTOs and entities.


The controller receives the incoming JSON as a DTO. When working with user requests, it is important to handle the data input 
carefully, because user input cannot be trusted. Before the data is stored, it needs to be verified as correct and to fit the 
criteria we set for the entity, for example that a host name is not empty. Otherwise an exception will occur, which breaks the
flow of the request. When the data is valid, the controller calls the mapper layer with the DTO.

Mappers mold data either into DTOs or into entities. They are able to map the correct data to the respective entity and give 
the entity’s attributes their values.

Once the entities have been created, they go through the Data Access Object (DAO) layer.
The DAO layer’s main function is communication between the backend and the database. The DAO layer stores data, and when
needed, it is also able to retrieve data.

The service layer is a layer of its own. It receives the software that needs to be scanned, retrieves it from the
database, and calls the external API (NVD). The response is then mapped to entities, which are saved through the DAO layer.

## Architecture Diagram

<img width="3168" height="1496" alt="image" src="https://github.com/user-attachments/assets/f7ebc88d-0140-4639-8fe3-3ca937aa7924" />


---
# Technologies used

### Language and build
- Java 26
- Maven
- Maven Compiler Plugin
- Maven Shade Plugin
- Maven Surefire Plugin
- JitPack

### REST API
- Javalin
- Jackson

### Database and persistence
- PostgreSQL
- Hibernate
- Hibernate HikariCP

### Security
- TokenSecurity (JWT, by Thomas Hartmann)
- jBCrypt

### Utilities and logging
- Lombok
- SLF4J
- Logback

### Testing
- JUnit Jupiter
- Hamcrest
- REST Assured
- Testcontainers

### Deployment
- Docker
- GitHub Actions
- Caddy
- DigitalOcean

---

## Key Design Decisions

Describe important technical decisions you made.

Examples:

- Why you chose your project structure
- How authentication works
- How errors are handled
- Why you chose specific libraries or frameworks

Example:

**Authentication**

Authentication is implemented using JWT tokens.  
Users log in and receive a token which must be included in subsequent API requests.

---

# Data Model

## ERD

<img width="2081" height="755" alt="image" src="https://github.com/user-attachments/assets/0a997432-7913-4144-95a6-f263b0611612" />

The Entity Relationship Diagram(ERD) shows the project's relational database tables, and the relations between them. The tables are connected by using the table's primary key, in the form of a unique ID.
The ID is also placed in another table as a foreign key, thus making a relation between them.

Most of the tables use integer IDs, but one exception occurred, and it is the vulnerability table. Because it is constructed from foreignAPI data, it 
uses their ID type, String. This was made as a design choice, to make it easier to distinguish their data when stored.

The chain of relations, as seen in the picture, goes from user, with a one-to-many composition relation to host, which has a one-to-many composition relation to software,
where the API and the user meet in the finding table. Finding is a table that connects the foreign API's data to the software that the user owns, through an association relation.

---

## Important Entities

Briefly describe the main entities in your system.

### User

Represents a registered user in the system.

Fields:

- id
- username
- password
- role

### Activity

Represents a recorded workout activity.

Fields:

- id
- userId
- date
- description

---

# API Documentation

Describe the most important endpoints.

## Example Endpoints

### Create User
POST /api/users


Request body:
{ “username”: “john”, “password”: “secret” }


Response:
201 Created


---

### Get All Activities
GET /api/activities


Response:
[ { “id”:1, “userId”:1, “description”:“Running” } ]


---

# User Stories

List the most important user stories for the project.

Example:

- As a user, I want to register an account.
- As a user, I want to log in and receive a token.
- As a user, I want to record a workout activity.
- As a user, I want to view my activity history.

---

# Development Notes

(Optional section)

Here you can briefly describe important challenges or lessons learned during development.

Examples:

- Handling authentication with JWT
- Designing the database schema
- Structuring the service layer
- Testing the API
