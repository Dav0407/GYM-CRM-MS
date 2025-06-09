# Gym CRM API

## Overview

The **Gym CRM API** is a Spring Boot-based RESTful application designed to manage gym operations, including trainee and trainer profiles, training sessions, and user authentication. It provides a comprehensive set of endpoints to register users, manage profiles, assign trainers to trainees, schedule trainings, and handle user authentication.

This application leverages Spring Data JPA for database interactions, PostgreSQL as the database, and Swagger (OpenAPI) for API documentation. It supports multiple environments (`dev`, `local`, `prod`, `stg`) via Spring profiles.

---

## Features

- **Trainee Management**: Register, retrieve, update, delete, and toggle the status of trainee profiles.
- **Trainer Management**: Register, retrieve, update, toggle status, and manage trainer assignments to trainees.
- **Training Management**: Add training sessions, retrieve trainee/trainer trainings, and list training types.
- **User Management**: User login and password change functionality.
- **API Documentation**: Interactive Swagger UI for exploring endpoints.

---

## Prerequisites

- **Java**: 17 or higher
- **Gradle**: 7.0+ (for dependency management)
- **PostgreSQL**: 13+ (database)
- **IDE**: IntelliJ IDEA, Eclipse, or similar (optional)
- **Postman** or **cURL**: For testing API endpoints (optional)

---

## Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/Dav0407/gym-crm-api.git
cd gym-crm-api
```

### 2. Configure the Database
- Install PostgreSQL and create a database named `gym_app`.
- Update the database credentials in `src/main/resources/application-<profile>.yml` (e.g., `application-dev.yml`):
  ```yaml
  spring:
    datasource:
      url: jdbc:postgresql://localhost:5432/gym_app
      username: postgres
      password: 123
  ```

### 3. Install Dependencies
Run the following command to download dependencies:
```bash
gradle build
```

### 4. Run the Application
Start the application with a specific profile (default is `dev`):
```bash
gradle bootRun --args='--spring.profiles.active=dev'
```
- Available profiles: `dev` (port 8080), `local` (port 9000), `prod` (port 8000), `stg` (port 9090).

### 5. Access the API
- **Swagger UI**: `http://localhost:<port>/swagger-ui.html`
- **Base URL**: `http://localhost:<port>/api/v1`

---

## API Endpoints

All endpoints require authentication via `Username` and `Password` headers unless specified otherwise (e.g., `/users/login`).

### Trainee API (`/api/v1/trainees`)
| Method | Endpoint                  | Description                          | Request Body                     | Response                     |
|--------|---------------------------|--------------------------------------|----------------------------------|------------------------------|
| POST   | `/register`              | Register a new trainee              | `CreateTraineeProfileRequestDTO` | `TraineeResponseDTO` (201)  |
| GET    | `/{username}`            | Get trainee profile                 | -                                | `TraineeProfileResponseDTO` (302) |
| PUT    | `/`                      | Update trainee profile              | `UpdateTraineeProfileRequestDTO` | `TraineeProfileResponseDTO` (202) |
| DELETE | `/{username}`            | Delete trainee profile              | -                                | `TraineeProfileResponseDTO` (202) |
| PATCH  | `/{trainee-username}/status` | Toggle trainee status          | -                                | `TraineeProfileResponseDTO` (202) |

### Trainer API (`/api/v1/trainers`)
| Method | Endpoint                  | Description                          | Request Body                     | Response                     |
|--------|---------------------------|--------------------------------------|----------------------------------|------------------------------|
| POST   | `/register`              | Register a new trainer              | `CreateTrainerProfileRequestDTO` | `TrainerResponseDTO` (201)  |
| GET    | `/{username}`            | Get trainer profile                 | -                                | `TrainerProfileResponseDTO` (302) |
| PUT    | `/`                      | Update trainer profile              | `UpdateTrainerProfileRequestDTO` | `TrainerProfileResponseDTO` (202) |
| GET    | `/not-assigned/{username}` | Get unassigned trainers for a trainee | -                             | `List<TrainerSecureResponseDTO>` (302) |
| PUT    | `/assign`                | Update trainee's trainer list       | `UpdateTrainerListRequestDTO`    | `List<TrainerSecureResponseDTO>` (202) |
| PATCH  | `/{trainer-username}/status` | Toggle trainer status          | -                                | `TrainerProfileResponseDTO` (202) |

### Training API (`/api/v1/trainings`)
| Method | Endpoint                  | Description                          | Request Body                     | Response                     |
|--------|---------------------------|--------------------------------------|----------------------------------|------------------------------|
| POST   | `/`                      | Add a new training                  | `AddTrainingRequestDTO`          | `TrainingResponseDTO` (201)  |
| GET    | `/trainees`              | Get trainee trainings               | `GetTraineeTrainingsRequestDTO`  | `List<TraineeTrainingResponseDTO>` (302) |
| GET    | `/trainers`              | Get trainer trainings               | `GetTrainerTrainingsRequestDTO`  | `List<TrainerTrainingResponseDTO>` (302) |
| GET    | `/types`                 | Get all training types              | -                                | `List<TrainingTypeResponseDTO>` (302) |

### User API (`/api/v1/users`)
| Method | Endpoint                  | Description                          | Request Body                     | Response                     |
|--------|---------------------------|--------------------------------------|----------------------------------|------------------------------|
| GET    | `/login`                 | User login (no headers required)    | `LogInRequestDTO`                | `UserResponseDTO` (200)      |
| PUT    | `/change-password`       | Change user password                | `ChangePasswordRequestDTO`       | `UserResponseDTO` (202)      |

---

## Authentication

- Most endpoints require `Username` and `Password` headers for authentication (e.g., `Username: admin`, `Password: password123`).
- The `/users/login` endpoint authenticates users without headers, using a request body instead.

---

## Error Responses

Common error responses across all endpoints:
- **400 Bad Request**: Invalid request data.
- **401 Unauthorized**: Invalid credentials or missing authentication headers.
- **404 Not Found**: Resource (e.g., trainee, trainer) not found.
- **500 Internal Server Error**: Server-side issues.

---

## Configuration

The application supports multiple Spring profiles:
- **`dev`**: Port 8080, schema auto-creation enabled.
- **`local`**: Port 9000, schema auto-creation enabled.
- **`prod`**: Port 8000, schema validation only.
- **`stg`**: Port 9090, schema validation only.

Update `application-<profile>.yml` for database credentials and other settings.

---

## Dependencies

- **Spring Boot**: Core framework (Web, Data JPA, Actuator).
- **PostgreSQL**: Database driver.
- **Springdoc OpenAPI**: Swagger UI integration.
- **Lombok**: Reduces boilerplate code.
- **Micrometer**: Prometheus metrics (if enabled).

Add to `build.gradle`:
```groovy
plugins {
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.0'
    id 'java'
}

group = 'com.epam'
version = '1.0-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.postgresql:postgresql'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

---

## Running Tests

Unit and integration tests are located in `src/test/java`. Run them with:
```bash
gradle test
```

---

## Monitoring (Optional)

If Actuator and Prometheus are enabled:
- **Health Checks**: `http://localhost:<port>/actuator/health`
- **Metrics**: `http://localhost:<port>/actuator/metrics`
- **Prometheus**: `http://localhost:<port>/actuator/prometheus`

To enable Actuator and Prometheus, add to `build.gradle`:
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-registry-prometheus'
}
```