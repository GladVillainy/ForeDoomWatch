# FOREDOOMWATCH

## My vision with the project
The goal is a centralized blue team vulnerability management tool that provides a single overview of registered assets and 
the known vulnerabilities (CVEs) affecting them. In a network environment with many hosts and software components, it is 
difficult to maintain an up to date asset inventory and to know which assets are exposed. The tool matches registered assets
against the NVD on a schedule and presents prioritized findings, so security teams can focus on the most critical vulnerabilities first.

---

## Links

Portfolio website:  
https://gladvillainy.github.io/Portfolio/

Deployed application (No frontend yet):  
https://foredoomwatch.buades.online/

Source code repository:  
https://github.com/GladVillainy/ForeDoomWatch/tree/main/src

---

# Architecture

## System Overview


The system functions like a flow, where the data flows through the layers, and each layer has its own 
responsibility, and follow the Separation of Concerns (SoC) principle.

Like a pipeline, it starts at one spot, the endpoint, which receives the request from the client. 
Before the request reaches the controller layer, an access handler checks the request. It verifies the user’s
JSON Web Token (JWT) and checks that the user’s role is allowed to use the endpoint. If both are valid, 
the request continues to the controller, otherwise it is rejected.

Each endpoint is routed to a handler method in a controller, which works like a navigator, it coordinates the 
layers needed to handle the request. The controller is the bridge between the client and the backend. The client
communicates in JSON, which the backend represents as Data Transfer Objects (DTOs), while entities are used to 
persist the data in the database.

The controller receives the incoming JSON as a DTO. When working with user requests, it is important to handle the data input 
carefully, because user input cannot be trusted. Before the data is stored, it needs to be verified as correct and to fit the 
criteria we set for the entity, for example that a host name is not empty. Otherwise an exception will occur, which breaks the
flow of the request. When the data is valid, the controller calls the mapper layer with the DTO.

Mappers mold data from one representation into another, toEntity transforms the incoming DTO into an entity by giving its attributes their values, and toDTO transforms the entity back into an outgoing DTO. This keeps the entities hidden from the client, so only the data we choose to expose is sent.

Once the entity has been created, it is passed to the Data Access Object (DAO) layer. The DAO layer’s main function is communication 
between the backend and the database. It works in both directions, it persists the entity in the database, and when needed, it 
retrieves data from the database and hands it back to the layers above.

The service layer is a layer of its own. It receives the id of the software to be scanned, retrieves the software from the database 
through the DAO layer, and calls the external National Vulnerability Database (NVD) API. The JSON response is deserialized into a DTO 
and mapped to entities. For each vulnerability, a finding is created that connects the software and the vulnerability, and both are saved 
through the DAO layer, unless they already exist.

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

**Projects structure**

The project is built with object-oriented programming(OOP), and the layers follow the Separation of Concerns principle(SOC). 

The controller handles the request, the DTO represents the entity as JSON, the mappers convert between the DTO and the entity, and the DAO handles persistence. The service layer handles the external NVD API and saves the result through the DAO. This way, each layer has its own responsibility, and a change in one layer has little effect on the others due to low coupling.

The OOP concepts the project uses are generics, inheritance and interfaces.
- The DAO layer has a generic DAO Supclass called GenericDAO<T, ID>, which takes two type parameters. T is the entity type the DAO works with, and is replaced by a concrete type, for example Host. ID is the type of the primary key, and is used in the read and delete methods. This way, the CRUD operations only have to be written once and can be reused by all the entity DAOs, which inherit from GenericDAO.
- IMapper<E, Out, In> is another generic, and it is a common interface for all mappers, so they all have toEntity and toDTO. E is the entity the mapper works with, Out is the type of DTO the mapper sends out, and In is the type of DTO it receives. They are separated because of the User: the password is only received through the incoming DTO (AuthDTO) to register or verify a login, and it is never included in the outgoing DTO (UserDTO), so it is not sent to the client.

**Authentication**

When a user registers, they provide a username, an email and a password. The email is validated with a simple regex that checks the basic format of an email address, and an exception (400 Bad Request) is thrown if it does not match. The email must also be unique, otherwise a 409 Conflict is returned. The password must not be empty, but there are no further requirements for the password or the username. I have kept the validation simple because this is a school project, and the regex is easy to replace with a stricter one. I also believe users should be able to choose their own credentials without being forced to include special characters, so a strong password is up to the user, not the system. 

When the user is registered, the password is hashed with bcrypt inside the constructor of the User entity, so the entity never carries a plain text password. The hash cannot be changed from outside the class, as passwordHash has no setter because of encapsulation, the value can only be set through the constructor and the changePassword method, which both hash the password first.

After login or registration, the server returns a JWT token, signed with a secret key. The token contains the user’s email and roles and expires after four hours. The client sends it as a bearer token in the Authorization header on later requests. For protected endpoints, the server verifies the signature and the expiry time before the request is handled. Invalid or expired tokens are rejected with 401 Unauthorized, and a valid token without the required role is rejected with 403 Forbidden.

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

Response body: 
{
  "token": "JWT token",
  "username": "thomas",
  "email": "thomas@testmail.com"
}

Http Status code: 201 Created

## Create a host
POST /api/hosts

Response body:
{
  "id": "id value",
  "hostname": "Firewall",
  "hostDescription": "Placed U12"
}

Http status code: 201 Created

## Create software
POST /api/hosts/{hostId}/software

Request body:
{
  "softwareId": "id value",
  "softwareName": "opnsense",
  "version": "23.1",
  "vendor": "opnsense"
}

Response: 201 Created

## Scan software for vulnerabilities
POST /api/software/{id}/scan
Request body: none

Reponse body:
{
    "findingId": "id value",
    "status": "OPEN",
    "detectedAt": [
      year,
      month,
      day,
      hour,
      minute,
      secound,
      nano
    ],
    "cveId": "CVE-2023-38997",
    "cvssScore": 7.2,
    "softwareName": "opnsense",
    "version": "23.1",
    "hostName": "Firewall"
  }
Response: 200 OK

## Get findings sorted by highest common vulnerability scoring System(CVSS) score
GET /api/findings/cvss/desc

Request body: none

Reponse body:
{
    "findingId": "id value",
    "status": "OPEN",
    "detectedAt": [
      year,
      month,
      day,
      hour,
      minute,
      secound,
      nano
    ],
    "cveId": "CVE-2023-39001",
    "cvssScore": 9.8,
    "softwareName": "opnsense",
    "version": "23.1",
    "hostName": "Firewall"
}

Response: 200 OK

## Edit the status of a finding
PUT /api/findings/{id}/status

Request body: { "status": "RESOLVED" }

Reponse body: 

{
  "findingId": "id value",
  "status": "RESOLVED",
  "detectedAt": [
     year,
      month,
      day,
      hour,
      minute,
      secound,
      nano
  ],
  "updatedAt": [
      year,
      month,
      day,
      hour,
      minute,
      secound,
      nano
  ],
  "resolvedAt": [
      year,
      month,
      day,
      hour,
      minute,
      secound,
      nano
  ]
}

Response: 200 OK

---

# User Stories

1. As a user, I want to create an account, so that my assets and findings are saved and tied to me.

3. As a user, I want to add software with a name and version to a specific machine, so that the system knows what is running where.

6. As a user, I want the system to look up my software against National Vulnerability Database(NVD), so that I find out whether there are known common vulnerabilities and exposures(CVE}.

8. As a user, I want to see all my open findings across machines sorted by severity, so that I know what to tackle first.

---

# Development Notes

## Challenges 
- NVD does not support webhooks, which was not ideal for this project, as a user often wants information as fast as possible. The alternative was to make a polling class that asks NVD's API for new information. 

- Exceptions: I made custom exceptions too far into the project, which made it too time consuming to go through every method and use the new custom exceptions. I learned that custom exception classes need to be created while coding, not after the code has been written and the project is 80% finished.

- Working alone was hard, mainly because I had no one to push back on my ideas. I missed having a second opinion, and I fell into tunnel vision. As the codebase grew, it became large and overwhelming, and I had to keep track of everything myself. I am convinced this has affected my code and its structure in a negative way, without me being aware of it. One example is that I placed HTTP status codes in the DAO layer, which couples the DAO to the web layer and breaks the Separation of Concerns principle I describe in the architecture section. A second person might have caught this early. I have always been someone who loved working alone, but I can feel that going forward I would prefer some kind of partner to talk things through with, especially on big projects.

## Things I learned
- I was introduced to Javadoc in the first semester, but never actually used it. During this project, I have used it a lot and found it practical for documenting methods and classes, even though it was sometimes hard to describe a method. I learned how to link methods together in Javadoc, and got much better at it.

- I found generic classes very useful. With GenericDAO<T, ID> and IMapper<E, Out, In>, I only had to write the CRUD operations and the mapper contract once, and could reuse them for all entities. I want to practice generics more, so I can use them with more confidence in future projects.

- JPA and Hibernate: It was confusing to figure out where and how to place the relationships between entities, for example between Host and Software. My issue was not understanding what a relationship is, but where and how to place it in the code. This led to a mistake: at one point, Host had two foreign keys to Software. Even so, I found it very smart, and I could see the point of writing code this way. Hibernate creates the tables and foreign keys from the entities, so I work with objects instead of writing SQL for every relation or creating the tables manually in the database. As with most new technology, making mistakes was a natural part of learning it and understanding it.

