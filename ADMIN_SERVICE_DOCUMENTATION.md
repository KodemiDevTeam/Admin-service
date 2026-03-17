# KodeMI Labs — Admin Service Documentation

**Service Name:** `Admin-service`  
**Artifact ID:** `admin_service`  
**Version:** `0.0.1-SNAPSHOT`  
**Java Version:** 17  
**Spring Boot Version:** 3.3.5  
**Spring Cloud Version:** 2023.0.3

---

## 1. Overview

The Admin Service is a dedicated microservice within the KodeMI Labs platform responsible for all administrative operations. It acts as a control plane for managing trainers, learners, courses, and other admins. It does not own user or course data directly — instead it orchestrates actions across other microservices via Feign clients, enforcing role-based access control at every endpoint.

---

## 2. Architecture & Position in the System

```
                        ┌─────────────────────┐
                        │   API Gateway / LB  │
                        └────────┬────────────┘
                                 │
                        ┌────────▼────────────┐
                        │    Admin Service     │  ← This service
                        │  (admin_service)     │
                        └──┬──────┬───────┬───┘
                           │      │       │
              ┌────────────▼─┐ ┌──▼────┐ ┌▼──────────────┐
              │  auth-service│ │user-  │ │course-service  │
              │              │ │service│ │                │
              └──────────────┘ └───────┘ └────────────────┘
                           │
                    ┌──────▼──────┐
                    │  DynamoDB   │
                    │ (Admin tbl) │
                    └─────────────┘
```

- Registers with **Eureka** service discovery at `http://172.18.6.203:8761/eureka/`
- Communicates with `auth-service`, `user-service`, and `course-service` via **OpenFeign**
- Persists admin credentials in **AWS DynamoDB** (region: `eu-north-1`)
- JWT tokens are validated locally using **JJWT** (v0.11.5)

---

## 3. Data Model

### 3.1 Admin Entity (`Admin.java`)

Stored in DynamoDB table: **`Admin`**

| Field       | Type        | DynamoDB Key | Description                          |
|-------------|-------------|--------------|--------------------------------------|
| `adminId`   | `String`    | Hash Key     | Unique identifier for the admin      |
| `password`  | `String`    | Attribute    | Admin password (stored as-is)        |
| `email`     | `String`    | Attribute    | Admin email address                  |
| `username`  | `String`    | Attribute    | Admin username                       |
| `userId`    | `String`    | Attribute    | Linked user ID in the user-service   |
| `adminRole` | `AdminRole` | Attribute    | Role enum: `TRAINER_ADMIN` or `COURSE_ADMIN` |

### 3.2 Enums

**`AdminRole`**
```
TRAINER_ADMIN   — manages trainer approvals/rejections
COURSE_ADMIN    — manages course verification/rejection
```

**`Role`** (used for cross-service user type resolution)
```
LEARNER
ADMIN
TRAINER
```

---

## 4. API Endpoints

Base path: `/api/v1/admin`

All endpoints except `/login` require a valid `Authorization: Bearer <token>` header and the `ADMIN` role (enforced via AOP).

### 4.1 Authentication

| Method | Path           | Auth Required | Description                  |
|--------|----------------|---------------|------------------------------|
| POST   | `/login`       | No            | Admin login with credentials |

**Request Body:**
```json
{
  "adminId": "string",
  "password": "string"
}
```

> Note: The login method currently validates admin existence in DynamoDB but the password check and token generation logic is incomplete (method returns `null` after the null check).

---

### 4.2 User Management

| Method | Path           | Auth Required | Description                                      |
|--------|----------------|---------------|--------------------------------------------------|
| GET    | `/user/{id}`   | ADMIN role    | Fetch any user by ID — resolves to correct type  |

The `getUser` method dynamically resolves the user type by first calling `auth-service` to get the user's role, then routing to the appropriate profile endpoint:
- `TRAINER` → `user-service /api/v1/trainer/details/{id}`
- `ADMIN` → `user-service /api/v1/user/admin/details/{id}`
- `LEARNER` → `user-service /api/v1/learner/details/{id}`

---

### 4.3 Trainer Management

| Method | Path                        | Auth Required | Description                          |
|--------|-----------------------------|---------------|--------------------------------------|
| POST   | `/trainer/verify/{userId}`  | ADMIN role    | Approve a pending trainer            |
| POST   | `/trainer/reject/{userId}`  | ADMIN role    | Reject a pending trainer             |
| GET    | `/pending/trainer`          | ADMIN role    | List all trainers pending approval   |
| GET    | `all/trainers`              | ADMIN role    | List all registered trainers         |

---

### 4.4 Course Management

| Method | Path                        | Auth Required | Description                          |
|--------|-----------------------------|---------------|--------------------------------------|
| POST   | `all/course`                | ADMIN role    | List all unverified courses          |
| POST   | `/course/verify/{courseId}` | ADMIN role    | Approve/verify a course              |
| POST   | `/course/reject/{courseId}` | ADMIN role    | Reject a course                      |

---

## 5. Security & Authorization

### 5.1 JWT Validation

JWT tokens are validated locally within the service using the secret key configured in `application.yaml`:

```yaml
jwt:
  secret: k8Jd9wQ2x+4aB3d1FJtL8vZ5X0yQ1V7n2gHqM4sP1tE=
```

The `RoleCheckAspect` parses the token using HMAC-SHA and extracts claims directly.

> Security Note: The JWT secret is hardcoded in `application.yaml`. This should be moved to a secrets manager (e.g., AWS Secrets Manager or environment variables) before production deployment.

### 5.2 AOP Role Enforcement

Two custom annotations enforce access control via Spring AOP (`RoleCheckAspect`):

**`@RequiresRole(String[] value)`**
- Extracts the `role` claim directly from the JWT token
- Checks if the role matches any of the allowed values
- Returns `401 Unauthorized` if no token is present
- Returns `403 Forbidden` if the role does not match

**`@RequiresSuperRole(String[] value)`**
- Extracts `userId` from the JWT token
- Calls `user-service` to fetch the admin's `adminRole` (i.e., `TRAINER_ADMIN` or `COURSE_ADMIN`)
- Applies the same allow/deny logic based on the fetched role

All controller endpoints (except `/login`) are annotated with `@RequiresRole("ADMIN")`.

### 5.3 Feign Token Propagation

`FeignConfig` registers a global `RequestInterceptor` that automatically forwards the `Authorization` header from the incoming HTTP request to all outbound Feign calls. This ensures downstream services receive the caller's token without manual passing in every method.

---

## 6. Inter-Service Communication (Feign Clients)

### 6.1 `AuthClient` → `auth-service`

| Method | Endpoint                                        | Description                        |
|--------|-------------------------------------------------|------------------------------------|
| POST   | `api/v1/auth/internal/trainer/activate/{userId}`| Activate a trainer account         |
| POST   | `/internal/trainer/reject/{userId}`             | Reject a trainer account           |
| GET    | `api/v1/auth/status`                            | Check user status by email         |
| GET    | `api/v1/auth/pending/trainer`                   | Get all pending trainer requests   |
| GET    | `api/v1/auth/user/id/{userId}`                  | Get user details by ID             |

### 6.2 `UserClient` → `user-service`

| Method | Endpoint                              | Description                    |
|--------|---------------------------------------|--------------------------------|
| GET    | `api/v1/trainer/details/{id}`         | Get trainer profile by ID      |
| GET    | `api/v1/trainer/detail/all`           | Get all trainer profiles       |
| GET    | `api/v1/user/admin/details/{id}`      | Get admin profile by ID        |
| GET    | `api/v1/learner/details/{id}`         | Get learner profile by ID      |

### 6.3 `CourseClient` → `course-service`

| Method | Endpoint                                  | Description                    |
|--------|-------------------------------------------|--------------------------------|
| GET    | `/api/v1/course/all/unverified`           | Get all unverified courses     |
| POST   | `/api/v1/course/internal/reject/{courseId}`  | Reject a course             |
| POST   | `/api/v1/course/internal/activate/{courseId}`| Verify/activate a course    |

---

## 7. DTOs (Data Transfer Objects)

| DTO                   | Purpose                                                        |
|-----------------------|----------------------------------------------------------------|
| `AdminLoginDTO`       | Login request: `adminId`, `password`                          |
| `AdminResponseDTO`    | Admin profile: userId, phone, designation, role, image, dept  |
| `UserDTO`             | Generic user: userId, name, email, username, active, role     |
| `TrainerResponseDTO`  | Full trainer profile with 25+ fields                          |
| `LearnerResponseDTO`  | Learner profile: name, email, DOB, social links, status       |
| `CourseResponseDTO`   | Course summary: courseId, code, slug, version, timestamps     |

---

## 8. Repository Layer

`AdminRepository` wraps `DynamoDBMapper` (AWS SDK v1) with three operations:

| Method              | Description                                      |
|---------------------|--------------------------------------------------|
| `save(Admin)`       | Persist or update an admin record                |
| `findById(adminId)` | Load admin by hash key, returns `null` if absent |
| `delete(adminId)`   | Load then delete admin; no-op if not found       |

---

## 9. Exception Handling

`AdminNotFoundException` (extends `RuntimeException`) is thrown when an admin lookup returns null.

Two constructors:
- `AdminNotFoundException(String message)` — generic message
- `AdminNotFoundException(String adminId, String message)` — prefixes with `"Admin not found with adminId: {adminId}"`

> There is no global `@ControllerAdvice` exception handler currently. Unhandled exceptions will result in default Spring Boot error responses.

---

## 10. Dependencies Summary

| Dependency                        | Purpose                                      |
|-----------------------------------|----------------------------------------------|
| `spring-boot-starter-web`         | REST API layer                               |
| `spring-boot-starter-aop`         | AOP for role-check annotations               |
| `spring-boot-starter-validation`  | Bean validation (`@Valid`)                   |
| `spring-cloud-starter-openfeign`  | Declarative HTTP clients                     |
| `spring-cloud-starter-netflix-eureka-client` | Service discovery              |
| `aws-java-sdk-dynamodb` (v1)      | DynamoDB mapper for Admin entity             |
| `dynamodb-enhanced` (SDK v2)      | Enhanced DynamoDB client (available, unused) |
| `software.amazon.awssdk:s3`       | S3 client (available, not yet used)          |
| `jjwt-api/impl/jackson` (0.11.5)  | JWT parsing and validation                   |
| `lombok`                          | Boilerplate reduction                        |
| `jsoup`                           | HTML parsing (available, not yet used)       |
| `mockito-core/junit-jupiter`      | Unit testing                                 |

---

## 11. Known Issues & Gaps

| # | Issue | Location | Severity |
|---|-------|----------|----------|
| 1 | `login()` method is incomplete — throws `AdminNotFoundException` on missing admin but returns `null` on success (no password check, no token generation) | `AdminService.java` | High |
| 2 | JWT secret is hardcoded in `application.yaml` | `application.yaml` | High |
| 3 | No global exception handler (`@ControllerAdvice`) — unhandled exceptions expose raw stack traces | Missing | Medium |
| 4 | `@PostMapping` used for `all/course` (list operation) — should be `@GetMapping` | `AdminController.java` | Low |
| 5 | `rejectTrainer` in `AuthClient` uses path `/internal/trainer/reject/{userId}` without the `api/v1/auth` prefix, inconsistent with other endpoints | `AuthClient.java` | Low |
| 6 | Password stored without hashing in DynamoDB | `Admin.java` / `AdminService.java` | High |
| 7 | No unit tests beyond the default Spring Boot context load test | `src/test/` | Medium |

---

## 12. Configuration Reference (`application.yaml`)

```yaml
spring:
  application:
    name: Admin-service

jwt:
  secret: <HMAC secret key>

eureka:
  client:
    service-url:
      defaultZone: http://172.18.6.203:8761/eureka/
  instance:
    prefer-ip-address: true

dynamodb:
  region: eu-north-1

cloud:
  aws:
    region:
      static: eu-north-1
    stack:
      auto: false

aws:
  s3:
    region: eu-north-1
    bucket-name: profile-picture-kodemi
```

---

## 13. Build & Run

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Run tests
./mvnw test
```

The service registers itself with Eureka on startup. Ensure the Eureka server is reachable at the configured `defaultZone` URL before starting.
