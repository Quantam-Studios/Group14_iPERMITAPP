# iPERMIT App — Group 14

A Spring Boot web application for managing environmental permit requests. Regulated Entities (REs) can register, submit permit requests, and pay fees online. Environmental Officers (EOs) can review applications, issue permits, and send notifications.

---

## Prerequisites

Make sure you have the following installed before getting started:

- **Java 17+** (the project targets Java 17 +)
- **Maven 3.6+** (or use the included `mvnw` wrapper)
- **PostgreSQL 13+**

---

## Database Setup

1. Open your PostgreSQL client (e.g., `psql` or pgAdmin).

2. Create the database:

```sql
CREATE DATABASE group14_ipermitdb;
```

3. Make sure a user named `postgres` exists with the password `password`. If you'd like to use different credentials, update `src/main/resources/application.properties` accordingly (see the Configuration section below).

The application uses `spring.jpa.hibernate.ddl-auto=update`, so Hibernate will automatically create and update all tables on first run. Seed data (two default permit types and a default EO account) is loaded from `src/main/resources/data.sql` on startup.

---

## Configuration

All application settings live in:

```
src/main/resources/application.properties
```

The key properties to review before running:

| Property | Default Value | Description |
|---|---|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/group14_ipermitdb` | PostgreSQL connection URL |
| `spring.datasource.username` | `postgres` | Database username |
| `spring.datasource.password` | `password` | Database password |
| `spring.mail.username` | `environmentalministry158@gmail.com` | Gmail account used to send notifications |
| `spring.mail.password` | `Ipermit123` | Gmail app password |

> **Note:** The email credentials in `application.properties` are used by the EO notification system. If you want email sending to work in your own environment, replace these with your own Gmail account and an [App Password](https://support.google.com/accounts/answer/185833).

---

## Running the Application

### Option 1 — Using the Maven Wrapper (recommended)

From inside the `Group14_iPERMITAPP/` directory:

**macOS / Linux:**
```bash
./mvnw spring-boot:run
```

**Windows:**
```cmd
mvnw.cmd spring-boot:run
```

### Option 2 — Using your system Maven

```bash
mvn spring-boot:run
```

### Option 3 — Build a JAR and run it

```bash
mvn clean package
java -jar target/Group14_iPERMITAPP-0.0.1-SNAPSHOT.jar
```

Once the application starts, open your browser and navigate to:

```
http://localhost:8080
```

---

## Default Accounts

Two types of users exist in the system:

### Environmental Officer (EO)
The EO account is seeded automatically by `data.sql`.

| Field | Value |
|---|---|
| Email | `environmentalministry158@gmail.com` |
| Password | `password` |

Log in with the EO credentials to access the EO dashboard, where you can review submitted permit applications and issue permits.

### Regulated Entity (RE)
REs self-register via the registration form on the login page. After registering, REs can log in to submit permit requests, track application status, and pay fees.

---

## Project Structure

```
Group14_iPERMITAPP/
├── src/main/java/edu/mizzou/Group14_iPERMITAPP/
│   ├── controller/        # MVC controllers (auth, RE, EO flows)
│   ├── model/             # JPA entity classes
│   ├── repository/        # Spring Data JPA repositories
│   └── service/           # Business logic (registration, payments, emails, etc.)
├── src/main/resources/
│   ├── application.properties   # App & DB configuration
│   ├── data.sql                 # Seed data (permits + default EO)
│   └── templates/               # Thymeleaf HTML templates
└── pom.xml
```

---

## Key Dependencies

- **Spring Boot 4.0.5** — application framework
- **Spring Data JPA + Hibernate** — ORM / database access
- **PostgreSQL Driver** — JDBC driver for PostgreSQL
- **Thymeleaf** — server-side HTML templating
- **Spring Boot Mail (Jakarta Mail)** — email notifications via Gmail SMTP
- **Lombok** — reduces boilerplate (getters, setters, constructors)

---

## Troubleshooting

**`Connection refused` on startup**
Ensure PostgreSQL is running and listening on port 5432. Verify the database `group14_ipermitdb` exists and that the credentials in `application.properties` match your local setup.

**`Password authentication failed for user "postgres"`**
Either update the `spring.datasource.password` in `application.properties` to match your actual Postgres password, or reset the `postgres` user's password in psql:
```sql
ALTER USER postgres WITH PASSWORD 'password';
```

**Tables not created automatically**
Confirm `spring.jpa.hibernate.ddl-auto=update` is set in `application.properties`. On a brand-new database, Hibernate will generate all tables on the first run.

**Email notifications not sending**
Gmail requires an [App Password](https://support.google.com/accounts/answer/185833) (not your regular Gmail password) when 2-Step Verification is enabled. Update `spring.mail.username` and `spring.mail.password` in `application.properties` with your own credentials.
