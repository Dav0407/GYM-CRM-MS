# Gym CRM Microservice Application

A comprehensive microservice-based Customer Relationship Management system designed for gym and fitness center management. The application provides complete functionality for managing trainers, trainees, training sessions, and trainer workload tracking.

## Architecture Overview

The application follows a microservice architecture pattern with three main components:

### 🏗️ Service Architecture

- **Main Service A (Gym CRM Core)**: Primary service handling trainee, trainer, and training management
- **Service B (Trainer Session Management)**: Dedicated service for tracking trainer working hours and workload
- **Eureka Service**: Service discovery and registration server for microservice coordination

## 🚀 Features

### User Management
- User authentication with JWT tokens
- Password management and security
- Token refresh functionality
- Role-based access control

### Trainee Management
- Complete trainee profile lifecycle (CRUD operations)
- Profile registration with personal details
- Status management (active/inactive)
- Training history tracking
- Trainer assignment management

### Trainer Management
- Trainer profile management with specializations
- Training type assignments
- Workload and schedule tracking
- Trainee assignment capabilities
- Status management

### Training Session Management
- Training session creation and scheduling
- Training type categorization
- Duration and date tracking
- Automated trainer workload calculation
- Training history for both trainers and trainees

### Trainer Workload Tracking
- Automatic calculation of trainer working hours
- Monthly and yearly workload reports
- Training session impact on workload
- Circuit breaker pattern for resilience

## 📋 API Endpoints & JSON Examples

### 🔐 Authentication & User Management

#### POST /api/v1/users/login
**Description**: Authenticates a user and returns profile details with JWT tokens.

**Request Body** (`LogInRequestDTO`):
```json
{
  "username": "john.doe",
  "password": "securePassword123"
}
```

**Response** (`AuthenticationResponseDTO`):
```json
{
  "user": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "username": "john.doe",
    "password": "encodedPassword",
    "isActive": true
  },
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### PUT /api/v1/users/change-password
**Description**: Updates the password for an authenticated user.

**Request Body** (`ChangePasswordRequestDTO`):
```json
{
  "username": "john.doe",
  "oldPassword": "currentPassword123",
  "newPassword": "newSecurePassword456"
}
```

**Response** (`UserResponseDTO`):
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "username": "john.doe",
  "password": "newEncodedPassword",
  "isActive": true
}
```

#### POST /api/v1/users/refresh-token
**Description**: Refreshes the user's access token using the refresh token from request headers.
- **Request**: Refresh token in Authorization header
- **Response**: New access and refresh tokens in response body

---

### 👤 Trainee Management

#### POST /api/v1/trainees/register
**Description**: Creates a new trainee profile with auto-generated username and password.

**Request Body** (`CreateTraineeProfileRequestDTO`):
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-05-15",
  "address": "123 Main Street, New York, NY"
}
```

**Response** (`TraineeResponseDTO`):
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "username": "john.doe",
  "password": "generatedPassword123",
  "isActive": true,
  "birthDate": "1990-05-15",
  "address": "123 Main Street, New York, NY",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### GET /api/v1/trainees/{username}
**Description**: Retrieves a trainee's profile with their assigned trainers. Requires ownership validation.

**Response** (`TraineeProfileResponseDTO`):
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "username": "john.doe",
  "password": "encodedPassword",
  "isActive": true,
  "birthDate": "1990-05-15",
  "address": "123 Main Street, New York, NY",
  "trainers": [
    {
      "id": 1,
      "username": "jane.smith",
      "firstName": "Jane",
      "lastName": "Smith",
      "specialization": "Cardio"
    },
    {
      "id": 2,
      "username": "mike.wilson",
      "firstName": "Mike",
      "lastName": "Wilson",
      "specialization": "Strength Training"
    }
  ]
}
```

#### PUT /api/v1/trainees
**Description**: Updates an existing trainee profile with new information.

**Request Body** (`UpdateTraineeProfileRequestDTO`):
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "username": "john.doe",
  "dateOfBirth": "1990-05-15",
  "address": "456 Oak Avenue, Boston, MA",
  "isActive": true
}
```

**Response**: Same as `TraineeProfileResponseDTO` above.

#### DELETE /api/v1/trainees/{username}
**Description**: Hard deletes a trainee profile.

**Response**: Returns the deleted trainee's profile information.

#### PATCH /api/v1/trainees/{username}/status
**Description**: Toggles the active status of a trainee profile (active ↔ inactive).

**Response**: Returns updated trainee profile with new status.

---

### 💪 Trainer Management

#### POST /api/v1/trainers/register
**Description**: Creates a new trainer profile with specialization.

**Request Body** (`CreateTrainerProfileRequestDTO`):
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "trainingType": "Cardio"
}
```

**Response** (`TrainerResponseDTO`):
```json
{
  "id": 1,
  "firstName": "Jane",
  "lastName": "Smith",
  "username": "jane.smith",
  "password": "generatedPassword456",
  "isActive": true,
  "specialization": "Cardio",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### GET /api/v1/trainers/{username}
**Description**: Retrieves a trainer's profile with their assigned trainees.

**Response** (`TrainerProfileResponseDTO`):
```json
{
  "id": 1,
  "firstName": "Jane",
  "lastName": "Smith",
  "username": "jane.smith",
  "password": "encodedPassword",
  "isActive": true,
  "specialization": "Cardio",
  "trainees": [
    {
      "username": "john.doe",
      "firstName": "John",
      "lastName": "Doe"
    },
    {
      "username": "alice.johnson",
      "firstName": "Alice",
      "lastName": "Johnson"
    }
  ]
}
```

#### PUT /api/v1/trainers
**Description**: Updates an existing trainer profile.

**Request Body** (`UpdateTrainerProfileRequestDTO`):
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "username": "jane.smith",
  "trainingTypeName": "Strength Training",
  "isActive": true
}
```

#### GET /api/v1/trainers/not-assigned/{username}
**Description**: Retrieves trainers not currently assigned to a specific trainee.

**Response** (Array of `TrainerSecureResponseDTO`):
```json
[
  {
    "id": 3,
    "username": "mike.wilson",
    "firstName": "Mike",
    "lastName": "Wilson",
    "specialization": "Yoga"
  },
  {
    "id": 4,
    "username": "sarah.brown",
    "firstName": "Sarah",
    "lastName": "Brown",
    "specialization": "Pilates"
  }
]
```

#### PUT /api/v1/trainers/assign
**Description**: Updates the list of trainers assigned to a trainee.

**Request Body** (`UpdateTrainerListRequestDTO`):
```json
{
  "traineeUsername": "john.doe",
  "trainerUsernames": ["jane.smith", "mike.wilson", "sarah.brown"]
}
```

**Response**: Array of assigned trainers (`TrainerSecureResponseDTO`).

#### PATCH /api/v1/trainers/{username}/status
**Description**: Toggles the active status of a trainer profile.

---

### 🏋️ Training Management

#### POST /api/v1/trainings
**Description**: Creates a new training session and automatically updates trainer workload via Service B.

**Request Body** (`AddTrainingRequestDTO`):
```json
{
  "traineeUsername": "john.doe",
  "trainerUsername": "jane.smith",
  "trainingName": "Morning Cardio Session",
  "trainingDate": "2024-03-15",
  "trainingDuration": 60
}
```

**Response** (`TrainingResponseDTO`):
```json
{
  "id": 1,
  "trainee": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "username": "john.doe",
    "password": "encodedPassword",
    "isActive": true,
    "birthDate": "1990-05-15",
    "address": "123 Main Street, New York, NY",
    "accessToken": null,
    "refreshToken": null
  },
  "trainer": {
    "id": 1,
    "firstName": "Jane",
    "lastName": "Smith",
    "username": "jane.smith",
    "password": "encodedPassword",
    "isActive": true,
    "specialization": "Cardio",
    "accessToken": null,
    "refreshToken": null
  },
  "trainingName": "Morning Cardio Session",
  "trainingType": "Cardio",
  "trainingDate": "2024-03-15",
  "trainingDuration": 60
}
```

#### POST /api/v1/trainings/trainees
**Description**: Retrieves training history for a specific trainee with optional filtering.

**Request Body** (`GetTraineeTrainingsRequestDTO`):
```json
{
  "traineeUsername": "john.doe",
  "from": "2024-01-01",
  "to": "2024-12-31",
  "trainerUsername": "jane.smith",
  "trainingType": "Cardio"
}
```

**Response** (Array of `TraineeTrainingResponseDTO`):
```json
[
  {
    "trainingName": "Morning Cardio Session",
    "trainingDate": "2024-03-15",
    "trainingType": "Cardio",
    "trainingDuration": 60,
    "trainerName": "Jane Smith"
  },
  {
    "trainingName": "Evening Cardio Workout",
    "trainingDate": "2024-03-20",
    "trainingType": "Cardio",
    "trainingDuration": 45,
    "trainerName": "Jane Smith"
  }
]
```

#### POST /api/v1/trainings/trainers
**Description**: Retrieves training sessions conducted by a specific trainer.

**Request Body** (`GetTrainerTrainingsRequestDTO`):
```json
{
  "trainerUsername": "jane.smith",
  "from": "2024-01-01",
  "to": "2024-12-31",
  "traineeUsername": "john.doe"
}
```

**Response** (Array of `TrainerTrainingResponseDTO`):
```json
[
  {
    "trainingName": "Morning Cardio Session",
    "trainingDate": "2024-03-15",
    "trainingType": "Cardio",
    "trainingDuration": 60,
    "traineeName": "John Doe"
  }
]
```

#### GET /api/v1/trainings/types
**Description**: Retrieves all available training types in the system.

**Response** (Array of `TrainingTypeResponseDTO`):
```json
[
  {
    "id": 1,
    "trainingTypeName": "Cardio"
  },
  {
    "id": 2,
    "trainingTypeName": "Strength Training"
  },
  {
    "id": 3,
    "trainingTypeName": "Yoga"
  },
  {
    "id": 4,
    "trainingTypeName": "Pilates"
  }
]
```

#### DELETE /api/v1/trainings/{trainingId}
**Description**: Deletes a training session and updates trainer workload accordingly.

**Response**: 200 OK with empty body.

---

### ⏰ Trainer Workload Management (Service B)

#### POST /api/v1/manage-working-hours
**Description**: Calculates and saves trainer working hours based on training sessions.

**Request Body** (`TrainerWorkloadRequest`):
```json
{
  "trainerUsername": "jane.smith",
  "trainerFirstName": "Jane",
  "trainerLastName": "Smith",
  "isActive": true,
  "trainingDate": "2024-03-15",
  "trainingDuration": 60,
  "actionType": "ADD"
}
```

**Response** (`TrainerWorkloadResponse`):
```json
{
  "trainerUsername": "jane.smith",
  "year": "2024",
  "month": "03",
  "workingHours": 25.5
}
```

#### GET /api/v1/manage-working-hours
**Description**: Retrieves trainer working hours for a specific month and year.

**Query Parameters**:
- `trainerUsername`: jane.smith
- `year`: 2024
- `month`: 03

**Response** (`TrainerWorkloadResponse`):
```json
{
  "trainerUsername": "jane.smith",
  "year": "2024",
  "month": "03",
  "workingHours": 25.5
}
```

## 🔧 Key Components & Business Logic

### Core Business Rules

#### User Registration & Authentication
- **Auto-generated Credentials**: When registering trainees/trainers, usernames are auto-generated from first/last names, and random passwords are created
- **JWT Security**: All endpoints (except registration and login) require valid JWT tokens
- **Ownership Validation**: Users can only access/modify their own profiles via `checkOwnership()` method
- **Token Refresh**: Automatic token refresh mechanism prevents session expiration

#### Trainee-Trainer Relationships
- **Many-to-Many Assignment**: Trainees can be assigned to multiple trainers and vice versa
- **Auto-Assignment**: When a training session is created, it automatically creates trainee-trainer relationships
- **Unassigned Trainers**: System tracks which trainers are available for assignment to specific trainees

#### Training Session Management
- **Automatic Workload Calculation**: Creating/deleting training sessions triggers automatic trainer workload updates in Service B
- **Date Validation**: Training dates must be in the past (historical tracking)
- **Specialization Matching**: Training type must match trainer's specialization
- **Duration Tracking**: All sessions track duration for workload calculations

#### Trainer Workload System (Service B Integration)
- **Real-time Updates**: Service A calls Service B whenever training sessions are added/deleted
- **Monthly Aggregation**: Working hours are calculated and stored by month/year
- **Circuit Breaker**: Resilient communication with fallback mechanisms
- **Action Types**: ADD/DELETE operations for accurate workload tracking

### Data Transfer Objects (DTOs)

#### Authentication DTOs
- `LogInRequestDTO`: Username/password authentication
- `AuthenticationResponseDTO`: Login response with user details and JWT tokens
- `ChangePasswordRequestDTO`: Secure password change with old/new password validation
- `UserResponseDTO`: Basic user profile information

#### Trainee Management DTOs
- `CreateTraineeProfileRequestDTO`: Registration with personal details (auto-generates credentials)
- `UpdateTraineeProfileRequestDTO`: Profile updates including status changes
- `TraineeResponseDTO`: Complete profile with JWT tokens (used after registration/login)
- `TraineeProfileResponseDTO`: Profile with assigned trainers list
- `TraineeSecureResponseDTO`: Minimal trainee info for trainer's trainee list

#### Trainer Management DTOs
- `CreateTrainerProfileRequestDTO`: Registration with specialization
- `UpdateTrainerProfileRequestDTO`: Profile updates including specialization changes
- `TrainerResponseDTO`: Complete profile with JWT tokens
- `TrainerProfileResponseDTO`: Profile with assigned trainees list
- `TrainerSecureResponseDTO`: Minimal trainer info for trainee's trainer list
- `UpdateTrainerListRequestDTO`: Bulk trainer assignment to trainees

#### Training Management DTOs
- `AddTrainingRequestDTO`: Session creation with participants and details
- `TrainingResponseDTO`: Complete training session with participant details
- `GetTraineeTrainingsRequestDTO`: Filtered training history requests for trainees
- `GetTrainerTrainingsRequestDTO`: Filtered training history requests for trainers
- `TraineeTrainingResponseDTO`: Training session details from trainee perspective
- `TrainerTrainingResponseDTO`: Training session details from trainer perspective
- `TrainingTypeResponseDTO`: Available training specializations

#### Workload Management DTOs (Service B)
- `TrainerWorkloadRequest`: Workload calculation requests with action type (ADD/DELETE)
- `TrainerWorkloadResponse`: Monthly workload summary with hours

### Security Features
- JWT-based authentication
- Token refresh mechanism
- Ownership validation for profile access
- Input validation and sanitization

### Integration Patterns
- **Circuit Breaker**: Resilient communication between services
- **Service Discovery**: Eureka-based service registration
- **Feign Client**: Declarative REST client for inter-service communication
- **Fallback Mechanisms**: Graceful degradation for service failures

## 🏃‍♂️ Getting Started

### Prerequisites
- Java 17+
- Spring Boot 3.x
- Maven 3.6+
- Database (PostgreSQL/MySQL)
- Eureka Server

### Configuration
The application uses the following key configurations:
- JWT token expiration and secret keys
- Database connection settings
- Eureka service discovery URLs
- Circuit breaker thresholds
- Feign client configurations

### API Documentation
The application includes comprehensive OpenAPI/Swagger documentation with:
- Detailed endpoint descriptions
- Request/response schemas
- Error code documentation
- Example payloads

## 🔄 Service Communication

### Main Service A → Service B Communication
When training sessions are added or deleted, Service A automatically communicates with Service B to:
- Calculate trainer workload changes
- Update monthly working hours
- Maintain accurate trainer availability

### Circuit Breaker Implementation
The application implements circuit breaker patterns for:
- Trainer workload calculations
- Service-to-service communication
- Fallback responses during service unavailability

## 📊 Data Models

### Core Entities
- **User**: Base user information (trainers and trainees)
- **Trainee**: Extended user with birth date and address
- **Trainer**: Extended user with specialization
- **Training**: Training session details
- **TrainingType**: Available training categories
- **TrainerWorkload**: Monthly trainer working hours

### Relationships
- One-to-Many: Trainer → Training Sessions
- One-to-Many: Trainee → Training Sessions
- Many-to-Many: Trainee ↔ Trainer (assignments)
- One-to-Many: TrainingType → Trainers

## 🛡️ Error Handling

### HTTP Status Codes
- `200`: Successful operations
- `201`: Resource created successfully
- `204`: Resource deleted successfully
- `400`: Invalid request data
- `401`: Unauthorized access
- `404`: Resource not found
- `500`: Internal server error

### Validation
- Input validation using Bean Validation annotations
- Custom validation for business rules
- Comprehensive error messages

## 🔍 Monitoring & Logging

The application includes:
- Structured logging with SLF4J
- Circuit breaker metrics
- Service health checks
- Request/response logging

## 🚀 Deployment

The microservice architecture supports:
- Container deployment (Docker)

## 📝 Contributing

When contributing to this project:
1. Follow the established API patterns
2. Maintain comprehensive test coverage
3. Update documentation for new endpoints
4. Ensure proper error handling
5. Follow security best practices

## 🔧 Technology Stack

- **Backend**: Spring Boot 3.x, Spring Security, Spring Data JPA
- **Authentication**: JWT tokens
- **Service Discovery**: Netflix Eureka
- **Circuit Breaker**: Resilience4j
- **HTTP Client**: OpenFeign
- **Documentation**: OpenAPI 3.0 (Swagger)
- **Build Tool**: Gradle
- **Database**: Postgres

---