<p align="center">
  <h1 align="center">🔗 SortURL — URL Shortener Backend</h1>
  <p align="center">
    A production-ready RESTful URL shortener API built with <strong>Spring Boot 4</strong>, featuring JWT authentication, click analytics, and Docker support.
  </p>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen?logo=springboot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/JWT-Auth-critical?logo=jsonwebtokens&logoColor=white" alt="JWT" />
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white" alt="Docker" />
</p>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Environment Variables](#environment-variables)
  - [Run Locally](#run-locally)
  - [Run with Docker](#run-with-docker)
- [API Reference](#-api-reference)
  - [Authentication](#authentication)
  - [URL Management](#url-management)
  - [Redirect](#redirect)
- [Database Schema](#-database-schema)
- [Security](#-security)
- [Error Handling](#-error-handling)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🌟 Overview

**SortURL** is a full-featured URL shortener backend that enables authenticated users to shorten long URLs, track click analytics over time, and manage their links — all through a clean REST API. It generates cryptographically secure 8-character short codes, records every click event with timestamps, and provides date-range analytics for individual links and aggregate dashboards.

---

## ✨ Features

| Feature | Description |
|---|---|
| **URL Shortening** | Generate unique 8-character short codes using `SecureRandom` with collision retry logic |
| **Click Tracking** | Every redirect records a `ClickEvent` with a timestamp for granular analytics |
| **Date-Range Analytics** | Query click events by date range per URL or aggregated across all user URLs |
| **JWT Authentication** | Dual-token system — short-lived access tokens + long-lived refresh tokens |
| **Secure Refresh Tokens** | Refresh tokens are delivered via `HttpOnly`, `Secure`, `SameSite=Strict` cookies |
| **User Registration & Login** | Full auth flow with BCrypt password hashing and input validation |
| **Role-Based Access Control** | Endpoints are protected with `@PreAuthorize("hasRole('USER')")` |
| **CORS Configuration** | Configurable allowed origins via environment variable for frontend integration |
| **Global Exception Handling** | Centralized error handling with consistent JSON error responses |
| **Input Validation** | Request DTOs validated with Jakarta Bean Validation (`@NotBlank`, `@Email`, `@URL`, `@Size`) |
| **Docker Support** | Multi-stage Dockerfile for optimized production images |
| **Owner-Only Deletion** | Users can only delete their own shortened URLs |

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 21 |
| **Framework** | Spring Boot 4.0.2 |
| **Security** | Spring Security 6 + JWT (jjwt 0.13.0) |
| **Database** | PostgreSQL |
| **ORM** | Spring Data JPA / Hibernate |
| **Validation** | Jakarta Bean Validation (Hibernate Validator) |
| **Build Tool** | Maven (with Maven Wrapper) |
| **Containerization** | Docker (multi-stage build) |
| **Utilities** | Lombok |

---

## 🏗 Architecture

The project follows a **layered architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                      CLIENT REQUEST                         │
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│  CORS Filter  →  JWT Auth Filter  →  Security Filter Chain  │
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│                    CONTROLLER LAYER                          │
│  AuthController  │  UrlMappingController  │  RedirectController│
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│                     SERVICE LAYER                            │
│  AuthService   │   UrlMappingService   │   UserService       │
│  (Interface + Impl)                                          │
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│                   REPOSITORY LAYER (JPA)                     │
│  UserRepository  │  UrlMappingRepository  │  ClickEventRepo  │
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│                     PostgreSQL Database                       │
│      users  │  url_mapping  │  click_event                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 Project Structure

```
SortUrlBackend/
├── src/
│   ├── main/
│   │   ├── java/com/sorturlbackend/
│   │   │   ├── SortUrlBackendApplication.java      # Application entry point
│   │   │   │
│   │   │   ├── config/
│   │   │   │   └── CorsConfig.java                 # CORS configuration (env-driven origins)
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java             # Register, login, refresh-token
│   │   │   │   ├── RedirectController.java         # GET /{shortUrl} → 302 redirect
│   │   │   │   └── UrlMappingController.java       # CRUD + analytics for shortened URLs
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateUrlRequest.java       # { originalUrl }
│   │   │   │   │   ├── LoginRequest.java           # { email, password }
│   │   │   │   │   └── RegisterRequest.java        # { name, email, password }
│   │   │   │   └── response/
│   │   │   │       ├── ApiResponse.java            # Generic { message, status }
│   │   │   │       ├── AuthResponse.java           # Registration response
│   │   │   │       ├── ClickEventResponse.java     # { date, count }
│   │   │   │       ├── LoginResponse.java          # { accessToken, refreshToken }
│   │   │   │       └── UrlMappingResponse.java     # { id, originalUrl, shortUrl, clickCount, ... }
│   │   │   │
│   │   │   ├── entity/
│   │   │   │   ├── ClickEvent.java                 # Click tracking entity
│   │   │   │   ├── UrlMapping.java                 # URL mapping entity
│   │   │   │   └── User.java                       # User entity (implements UserDetails)
│   │   │   │
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java     # @RestControllerAdvice
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── UserAlreadyExistsException.java
│   │   │   │   └── UserNotFoundException.java
│   │   │   │
│   │   │   ├── mapper/
│   │   │   │   └── UrlMappingMapper.java           # Entity ↔ DTO mapping
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── ClickEventRepository.java       # JPA repo for click events
│   │   │   │   ├── UrlMappingRepository.java       # JPA repo for URL mappings
│   │   │   │   └── UserRepository.java             # JPA repo for users
│   │   │   │
│   │   │   ├── security/
│   │   │   │   ├── AuthEntryPointJwt.java          # Custom 401 entry point
│   │   │   │   ├── SecurityConfig.java             # Security filter chain config
│   │   │   │   ├── jwt/
│   │   │   │   │   ├── JwtAuthFilter.java          # Bearer token extraction filter
│   │   │   │   │   └── JwtService.java             # Token generation & validation
│   │   │   │   └── service/
│   │   │   │       ├── AuthService.java            # Auth service interface
│   │   │   │       └── AuthServiceImpl.java        # Auth service implementation
│   │   │   │
│   │   │   └── service/
│   │   │       ├── UrlMappingService.java          # URL service interface
│   │   │       ├── UserService.java                # User service interface
│   │   │       └── impl/
│   │   │           ├── UrlMappingServiceImpl.java   # URL service implementation
│   │   │           └── UserServiceImpl.java         # User service implementation
│   │   │
│   │   └── resources/
│   │       └── application.yaml                     # App configuration
│   │
│   └── test/                                        # Test sources
│
├── Dockerfile                                       # Multi-stage Docker build
├── pom.xml                                          # Maven dependencies & build config
├── mvnw / mvnw.cmd                                  # Maven Wrapper scripts
├── .env                                             # Environment variables (git-ignored)
├── .gitignore
└── README.md                                        # ← You are here
```

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version |
|---|---|
| **Java JDK** | 21+ |
| **Maven** | 3.9+ (or use included `mvnw`) |
| **PostgreSQL** | 14+ |
| **Docker** *(optional)* | 20+ |

### Environment Variables

Create a `.env` file in the project root (this file is git-ignored):

```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/sorturldb
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# JWT
JWT_SECRET=your-256-bit-secret-key-minimum-32-characters-long

# Frontend
FRONTEND_URL=http://localhost:5173
```

> **Note:** The `JWT_SECRET` must be at least 32 characters long (256 bits) for HMAC-SHA signing.

### Run Locally

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/SortUrlBackend.git
   cd SortUrlBackend
   ```

2. **Create the PostgreSQL database**
   ```bash
   createdb sorturldb
   ```

3. **Set up environment variables**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

4. **Build and run**
   ```bash
   # Using Maven Wrapper
   ./mvnw spring-boot:run

   # Or build the JAR first
   ./mvnw clean package -DskipTests
   java -jar target/SortUrlBackend-0.0.1-SNAPSHOT.jar
   ```

5. The API will be available at **`http://localhost:8080`**

### Run with Docker

```bash
# Build the image
docker build -t sorturl-backend .

# Run the container
docker run -d \
  --name sorturl-backend \
  -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/sorturldb \
  -e DB_USERNAME=your_db_username \
  -e DB_PASSWORD=your_db_password \
  -e JWT_SECRET=your-256-bit-secret-key-minimum-32-characters-long \
  -e FRONTEND_URL=http://localhost:5173 \
  sorturl-backend
```

> The Dockerfile uses a **multi-stage build**: Maven builds the JAR in stage 1, then only the JAR is copied to a lightweight `eclipse-temurin:21-jdk-jammy` runtime image.

---

## 📡 API Reference

**Base URL:** `http://localhost:8080`

### Authentication

#### Register a New User

```http
POST /auth/register
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securepassword123"
}
```

**Response** `200 OK`:
```json
{
  "message": "User registered successfully",
  "status": true
}
```

**Validation Rules:**
- `name` — required, non-blank
- `email` — required, valid email format
- `password` — required, minimum 6 characters

---

#### Login

```http
POST /auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "securepassword123"
}
```

**Response** `200 OK`:
```json
{
  "accessToken": "eyJhbGciOiJIUzM4NCJ9..."
}
```

> The **refresh token** is set as an `HttpOnly` cookie named `refreshToken` (not returned in the JSON body).

---

#### Refresh Access Token

```http
POST /auth/refresh-token
Cookie: refreshToken=eyJhbGciOiJIUzM4NCJ9...
```

**Response** `200 OK`:
```json
{
  "accessToken": "eyJhbGciOiJIUzM4NCJ9..."
}
```

> Requires the `refreshToken` cookie to be present. Returns a new access token.

---

### URL Management

> All URL management endpoints require authentication. Include the access token in the `Authorization` header:
> ```
> Authorization: Bearer <access_token>
> ```

#### Shorten a URL

```http
POST /urls/shorten
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "originalUrl": "https://www.example.com/very/long/url/path"
}
```

**Response** `200 OK`:
```json
{
  "id": 1,
  "originalUrl": "https://www.example.com/very/long/url/path",
  "shortUrl": "aB3xK9mQ",
  "clickCount": 0,
  "createdDate": "2026-02-19T19:00:00",
  "username": "john@example.com"
}
```

---

#### Get My URLs

```http
GET /urls/my-urls
Authorization: Bearer <token>
```

**Response** `200 OK`:
```json
[
  {
    "id": 1,
    "originalUrl": "https://www.example.com/very/long/url/path",
    "shortUrl": "aB3xK9mQ",
    "clickCount": 42,
    "createdDate": "2026-02-19T19:00:00",
    "username": "john@example.com"
  }
]
```

---

#### Get Click Analytics for a URL

```http
GET /urls/analytics/{shortUrl}?startDate=2026-02-01T00:00:00&endDate=2026-02-28T23:59:59
Authorization: Bearer <token>
```

**Query Parameters:**
| Parameter | Format | Example |
|---|---|---|
| `startDate` | ISO 8601 DateTime | `2026-02-01T00:00:00` |
| `endDate` | ISO 8601 DateTime | `2026-02-28T23:59:59` |

**Response** `200 OK`:
```json
[
  { "date": "2026-02-15", "count": 12 },
  { "date": "2026-02-16", "count": 8 },
  { "date": "2026-02-17", "count": 22 }
]
```

---

#### Get Total Clicks Across All URLs

```http
GET /urls/total-clicks?startDate=2026-02-01&endDate=2026-02-28
Authorization: Bearer <token>
```

**Query Parameters:**
| Parameter | Format | Example |
|---|---|---|
| `startDate` | ISO 8601 Date | `2026-02-01` |
| `endDate` | ISO 8601 Date | `2026-02-28` |

**Response** `200 OK`:
```json
{
  "2026-02-15": 25,
  "2026-02-16": 18,
  "2026-02-17": 30
}
```

---

#### Delete a URL

```http
DELETE /urls/{shortUrl}
Authorization: Bearer <token>
```

**Response** `204 No Content`

> Only the URL owner can delete their URLs. Returns `403 Forbidden` if attempted by another user.

---

### Redirect

#### Access a Shortened URL

```http
GET /{shortUrl}
```

**Response** `302 Found`:
```
Location: https://www.example.com/very/long/url/path
```

> This endpoint is **public** (no authentication required). Each access records a click event with a timestamp.

---

## 🗄 Database Schema

The application uses **Hibernate auto DDL** (`ddl-auto: update`) to manage the schema. There are three primary tables:

```
┌───────────────┐       ┌───────────────────┐       ┌─────────────────┐
│    users      │       │   url_mapping     │       │  click_event    │
├───────────────┤       ├───────────────────┤       ├─────────────────┤
│ id       PK   │◄──┐  │ id           PK   │◄──┐  │ id         PK   │
│ name          │   │  │ original_url      │   │  │ click_date      │
│ email  UNIQUE │   └──│ user_id      FK   │   └──│ url_mapping_id FK│
│ password      │      │ short_url  UNIQUE │      └─────────────────┘
│ role          │      │ click_count       │
└───────────────┘      │ created_date      │
                       └───────────────────┘
```

| Table | Column | Type | Constraints |
|---|---|---|---|
| **users** | `id` | `BIGINT` | PK, Auto-increment |
| | `name` | `VARCHAR` | NOT NULL |
| | `email` | `VARCHAR` | NOT NULL, UNIQUE |
| | `password` | `VARCHAR` | NOT NULL (BCrypt hashed) |
| | `role` | `VARCHAR` | Default: `ROLE_USER` |
| **url_mapping** | `id` | `BIGINT` | PK, Auto-increment |
| | `original_url` | `VARCHAR` | — |
| | `short_url` | `VARCHAR` | UNIQUE |
| | `click_count` | `INT` | Default: `0` |
| | `created_date` | `TIMESTAMP` | — |
| | `user_id` | `BIGINT` | FK → `users.id` |
| **click_event** | `id` | `BIGINT` | PK, Auto-increment |
| | `click_date` | `TIMESTAMP` | — |
| | `url_mapping_id` | `BIGINT` | FK → `url_mapping.id`, CASCADE DELETE |

---

## 🔐 Security

### Authentication Flow

```
  ┌──────────┐                         ┌───────────┐
  │  Client  │                         │  Server   │
  └──────┬───┘                         └─────┬─────┘
         │  POST /auth/login                 │
         │  { email, password }              │
         │──────────────────────────────────►│
         │                                   │  Validate credentials
         │                                   │  Generate access + refresh tokens
         │  200 OK                           │
         │  { accessToken }                  │
         │  Set-Cookie: refreshToken=...     │
         │◄──────────────────────────────────│
         │                                   │
         │  GET /urls/my-urls                │
         │  Authorization: Bearer <token>    │
         │──────────────────────────────────►│
         │                                   │  JwtAuthFilter validates token
         │  200 OK  [ ... urls ... ]         │
         │◄──────────────────────────────────│
         │                                   │
         │  POST /auth/refresh-token         │
         │  Cookie: refreshToken=...         │
         │──────────────────────────────────►│
         │                                   │  Validate refresh token
         │  200 OK { accessToken }           │  Generate new access token
         │◄──────────────────────────────────│
```

### Key Security Details

| Aspect | Implementation |
|---|---|
| **Password Storage** | BCrypt via `BCryptPasswordEncoder` |
| **Access Token** | JWT, expires in **1 hour** (3,600,000 ms) |
| **Refresh Token** | JWT, expires in **7 days** (604,800,000 ms) |
| **Refresh Token Delivery** | `HttpOnly`, `Secure` (dynamic), `SameSite=Strict` cookie |
| **Session Management** | Stateless (`SessionCreationPolicy.STATELESS`) |
| **CSRF** | Disabled (stateless JWT-based auth) |
| **Public Endpoints** | `/auth/**`, `/{shortUrl}` |
| **Protected Endpoints** | `/urls/**` (requires `ROLE_USER`) |
| **Token Signing** | HMAC-SHA with configurable secret key |

---

## ⚠️ Error Handling

All errors are returned as consistent JSON responses via `GlobalExceptionHandler`:

```json
{
  "message": "Error description",
  "status": false
}
```

| Exception | HTTP Status | When |
|---|---|---|
| `MethodArgumentNotValidException` | `400 Bad Request` | Invalid request body fields |
| `BadCredentialsException` | `401 Unauthorized` | Wrong email or password |
| `AuthenticationServiceException` | `401 Unauthorized` | Missing/invalid refresh token |
| `ResourceNotFoundException` | `404 Not Found` | URL or resource not found |
| `UserAlreadyExistsException` | `409 Conflict` | Email already registered |
| `AccessDeniedException` | `403 Forbidden` | Deleting another user's URL |
| `RuntimeException` | `500 Internal Server Error` | Unexpected server errors |
| `Exception` | `500 Internal Server Error` | Catch-all for unhandled exceptions |

**Validation errors** return a field-specific error map:
```json
{
  "email": "Invalid email format",
  "password": "Password must be at least 6 characters"
}
```

---

## 🤝 Contributing

1. **Fork** the repository
2. **Create** a feature branch
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit** your changes
   ```bash
   git commit -m "feat: add amazing feature"
   ```
4. **Push** to the branch
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Open** a Pull Request

---

## 📜 License

This project is open source and available under the [MIT License](LICENSE).

---

<p align="center">
  Built with ❤️ using Spring Boot
</p>
