# Payment Project

A Spring Boot backend application providing a core banking/payment system alongside real-time chat capabilities.

## Features
- **User Management & Security:** Secure authentication and authorization using Spring Security and JWT (JSON Web Tokens). Includes Role-Based Access Control (RBAC).
- **Account Management:** Manage customer information and track account balances.
- **Real-Time Chat:** Integrated WebSocket functionality for real-time, private messaging between users.
- **Database:** Uses MySQL for robust data persistence with Spring Data JPA and Hibernate.
- **Caching / Advanced Data Structures:** Spring Data Redis integration.
- **API Documentation:** Swagger UI configured for easy exploration and testing of REST endpoints.
- **Dockerized:** Includes a `docker-compose.yml` file for effortless containerized deployment of both the application and the MySQL database.

## Technologies Used
- **Language:** Java 17
- **Framework:** Spring Boot 3.4.4
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Spring Boot WebSocket
  - Spring Data Redis
  - Spring Boot Actuator
- **Database:** MySQL 8
- **Authentication:** JSON Web Token (JJWT)
- **API Docs:** Springdoc OpenAPI (Swagger)
- **Tools:** Lombok, Maven, Docker

## Getting Started

### Prerequisites
- JDK 17
- Maven (or use the provided `mvnw` wrapper)
- Docker & Docker Compose (optional, but recommended for database setup)

### Option 1: Running with Docker Compose
The easiest way to run the application along with its MySQL database is using Docker Compose.

1. Ensure you have Docker running.
2. Set the `MYSQL_PASSWORD` environment variable, or manually update the `docker-compose.yml` file with a password.
3. Run the following command in the project root:
   ```bash
   docker-compose up -d --build
   ```
This will start both the `payment-mysql` database and the `payment-app` containers.

### Option 2: Running Locally (Manual Setup)
1. Start a local MySQL instance (e.g., on port 3306).
2. Create a database schema named `paymentschema`.
3. Update the database credentials in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/paymentschema
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```
4. Run the application using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```

## API Documentation
Once the application is running, you can access the Swagger UI documentation to explore the available REST endpoints (e.g., User, Admin, and Chat controllers) at:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Project Structure
- `Model/`: Contains JPA entities (e.g., `customerInfo`, `Role`, `ChatMessage`).
- `ControllerLayer/`: REST controllers (`UserController`, `AdminController`, `ChatController`, `HomeController`).
- `RepositoryLevel/`: Spring Data JPA repositories.
- `ServiceStructure/` & `ServiceStructureImplementation/`: Business logic interfaces and implementations.
- `config/`: Application configuration classes (Security, WebSocket, Swagger, etc.).
- `Filters/`: Custom filters (e.g., JWT authentication filter).
- `dto/`: Data Transfer Objects for API requests and responses.
