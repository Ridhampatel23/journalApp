# Journal App 📓

A full-featured **journaling REST API** built with **Java + Spring Boot**, offering secure authentication, asynchronous sentiment analysis, scheduled email digests, and weather-aware journal entries.

![Architecture](architecture-diagram.svg)

---

## ✨ Features

- 🔐 **Authentication** — JWT-based login + **Google OAuth** sign-in
- 📝 **Journal Entries** — full CRUD for personal journal entries
- 💡 **Sentiment Analysis** — entries are sent to **AWS SQS** and processed asynchronously
- 📧 **Scheduled Email Digests** — weekly sentiment summaries emailed to users via a scheduler
- ☁️ **Weather Integration** — enrich entries with real-time weather data via OpenWeather API
- ⚡ **Redis Caching** — caches app config for low-latency access
- 👥 **Role-based Access** — admin and user roles via Spring Security
- 📖 **Swagger / OpenAPI** — interactive API docs
- 🐳 **Dockerized** + **CI/CD** via GitHub Actions

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot 3, Spring Security |
| Database | MongoDB |
| Cache | Redis |
| Messaging | AWS SQS |
| Auth | JWT, Google OAuth 2.0 |
| External APIs | OpenWeather |
| Build | Maven |
| Containerization | Docker |
| CI/CD | GitHub Actions |
| Docs | SpringDoc OpenAPI (Swagger UI) |

---

## 🏗️ Architecture Overview

```
      ┌──────────────┐
      │   Client     │
      └──────┬───────┘
             │ REST + JWT
             ▼
  ┌────────────────────────┐
  │    Journal App API     │◄───── Redis (config cache)
  │   (Spring Boot)        │
  └────┬───────┬───────┬───┘
       │       │       │
       ▼       ▼       ▼
   MongoDB  AWS SQS  OpenWeather API
              │
              ▼
      Sentiment Consumer ──► Email Service (scheduled digests)
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- MongoDB (local or Atlas)
- Redis
- AWS account with SQS queue (or LocalStack)
- Gmail credentials (for email notifications)
- Google OAuth client credentials
- OpenWeather API key

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Ridhampatel23/journalApp.git
   cd journalApp
   ```

2. **Configure environment** — copy the example file and fill in credentials:
   ```bash
   cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
   ```

   Required values include:
   - MongoDB URI
   - Redis host/port
   - JWT secret
   - AWS access key, secret, region, SQS queue URL
   - Google OAuth client ID & secret
   - OpenWeather API key
   - SMTP / Gmail credentials

3. **Build:**
   ```bash
   ./mvnw clean package
   ```

4. **Run:**
   ```bash
   ./mvnw spring-boot:run
   ```

   Or with Docker:
   ```bash
   docker build -t journal-app .
   docker run -p 8080:8080 --env-file .env journal-app
   ```

---

## 📡 API Endpoints

Swagger UI available at: `http://localhost:8080/swagger-ui.html`

### Public
- `POST /public/signup` — register a new user
- `POST /public/login` — log in and receive a JWT
- `GET  /public/health-check` — service health status

### Google OAuth
- `GET /oauth2/authorization/google` — initiate Google sign-in

### User (JWT required)
- `GET    /user` — get current user
- `PUT    /user` — update user
- `DELETE /user` — delete account

### Journal Entries (JWT required)
- `GET    /journal` — list user's entries
- `POST   /journal` — create a new entry (enriched with weather + queued for sentiment)
- `GET    /journal/{id}` — get a specific entry
- `PUT    /journal/{id}` — update an entry
- `DELETE /journal/{id}` — delete an entry

### Admin (ADMIN role)
- `GET  /admin/all-users` — list all users
- `POST /admin/create-admin-user` — promote a user to admin

---

## ⏰ Scheduled Jobs

A **scheduler** runs periodically to email each user a sentiment summary of their journal entries from the past week — aggregating positive, negative, and neutral moods into a digest.

---

## 🧪 Testing

```bash
./mvnw test
```

Includes unit and integration tests for:
- User service, repository, scheduler
- Email service
- Redis cache layer

---

## 📁 Project Structure

```
src/main/java/net/ridham/journalApp/
├── api/              # External API response models (e.g. weather)
├── cache/            # In-memory app cache
├── config/           # Spring Security, Redis, AWS SQS, OpenAPI config
├── controller/       # REST controllers
├── dto/              # Request / response DTOs
├── entity/           # MongoDB entities
├── enums/            # Enums (Sentiment, etc.)
├── filter/           # JWT authentication filter
├── mapper/           # DTO ↔ entity mappers
├── model/            # SQS message models
├── repository/       # MongoDB repositories
├── scheduler/        # Scheduled tasks (email digests)
├── service/          # Business logic (journal, user, weather, SQS, email, sentiment)
└── utils/            # JWT utilities
```

---

## 🔄 CI/CD

GitHub Actions workflow (`.github/workflows/build.yml`) runs on every push:
- Builds the project with Maven
- Executes the test suite

---

## 📝 License

Provided as-is for educational and portfolio purposes.

---

## 👤 Author

**Ridham Patel** — [@Ridhampatel23](https://github.com/Ridhampatel23)
