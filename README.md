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

# Important Entities

## User
A user is used for distinguish between other users data, due to the properties of a login page using the attributes passwordHash and email. 
#### Attributes
* userId
* username
* email
* passwordHash
* roles

## Software
Software is the program installed on a host, and it is what the system scans for vulnerabilities. Its vendor, name and version are used to query the NVD API, and each match is stored as a finding.
### Attributes
* softwareId
* softwareName
* vendor
* version
  
## Finding
Finding is the bridge between the user's software and the external NVD API scans, due to its relation (seen in the ERD) between Software and Vulnerability. The entity also has properties for marking the finding with the enum attribute status, and information about the date when the finding was detected, updated and resolved.
### Attributes
* findingId
* status (OPEN, IN_PROGRESS, RESOLVED)
* detectedAt
* updatedAt
* resolvedAt

## Vulnerability
Vulnerability is where the program builds and stores the information, seen in the attributes, that the external API provides.
### Attributes
* vulnerabilityId
* description
* cweCategory
* metrics (cvssScore, cvssVector, severity)
---

# API Documentation

## Create a user
POST /api/auth/register

Request body: { "username": "Thomas", "email": "thomas@testmail.com", "password": "secret" }

Response: 201 Created

## Create a host
POST /api/hosts

Request body: { "hostname": "Firewall", "hostDescription": "Placed U12" }

Response: 201 Created

## Create software
POST /api/hosts/{hostId}/software

Request body: { "softwareName": "opnsense", "vendor": "opnsense", "version": "23.1" }

Response: 201 Created

## Scan software for vulnerabilities
POST /api/software/{id}/scan

Request body: none

Response: 200 OK

## Get findings sorted by highest CVSS score
GET /api/findings/cvss/desc

Request body: none

Response: 200 OK

## Edit the status of a finding
PUT /api/findings/{id}/status

Request body: { "status": "RESOLVED" }

Response: 200 OK

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

## Challenges 
- NVD does not support webhooks, which was not ideal for this project, as a user often wants information as fast as possible. The alternative was to make a polling class that asks NVD's API for new information. 

- Exceptions: I made custom exceptions too far into the project, which made it too time consuming to go through every method and use the new custom exceptions. I learned that custom exception classes need to be created while coding, not after the code has been written and the project is 80% finished.

## Things I learned
- I was introduced to Javadoc in the first semester, but never actually used it. During this project, I have used it a lot and found it practical for documenting methods and classes, even though it was sometimes hard to describe a method. I learned how to link methods together in Javadoc, and got much better at it.

  

