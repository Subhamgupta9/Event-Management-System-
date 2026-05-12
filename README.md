# Event Management System

Java web application for managing events, groups, invitations, notifications, and role-based access in a single system. The project is built with Servlets, JSP, JDBC, MySQL, Maven, and Apache Tomcat.

## Demo

Demo Video: https://drive.google.com/file/d/1oPxPWxhUoxwpI5sAtu4OXRZRC98aC_nu/view?usp=sharing

## Highlights

- User registration, login, logout, and session-based authentication
- Role-aware workflows for `ADMIN`, `ORGANIZER`, and `MEMBER`
- Event creation, editing, deletion, and ownership checks
- Group management with member assignment
- Invitation flow with accept and reject handling
- Dashboard notifications and summary statistics
- Event feedback with rating and comments
- Admin panel for user and event management
- Embedded Tomcat launcher for local development

## Tech Stack

- Java 17
- Jakarta Servlet API 6
- JSP
- JDBC
- MySQL 8
- Maven
- Apache Tomcat 10

## Project Structure

```text
src/main/java/com/ems/
|-- bootstrap/   Embedded launcher
|-- dao/         Database access layer
|-- filter/      Authentication filter
|-- model/       Domain models
|-- servlet/     HTTP request handlers
`-- util/        Shared utilities

src/main/resources/
`-- db.properties

src/main/webapp/
|-- css/
|-- WEB-INF/
|-- admin-panel.jsp
|-- dashboard.jsp
|-- event-form.jsp
|-- groups.jsp
|-- index.jsp
|-- login.jsp
`-- register.jsp

database/
|-- schema.sql
|-- sample_data.sql
|-- chat_feedback_tables.sql
|-- remove_dummy_events.sql
`-- reset_users.sql
```

## Core Modules

- `UserDao`: registration, authentication, user listing, and admin deletion
- `EventDao`: event CRUD, filtering, and ownership checks
- `GroupDao`: group creation, membership, and event-group relationships
- `InvitationDao`: invitation delivery and response updates
- `NotificationDao`: dashboard notifications
- `DashboardDao`: aggregate dashboard counts
- `FeedbackDao`: event feedback persistence and retrieval
- `AuthFilter`: route protection for authenticated areas
- `AppLauncher`: embedded Tomcat startup entry point

## Getting Started

### Prerequisites

- Java 17 or later
- MySQL Server 8.0 or later
- Maven

### 1. Configure the database

Update `src/main/resources/db.properties` with your local MySQL settings:

```properties
db.url=jdbc:mysql://localhost:3306/event_management_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=your_mysql_username
db.password=your_mysql_password
db.driver=com.mysql.cj.jdbc.Driver
```

You can also override these values with environment variables:

```powershell
$env:EMS_DB_URL="jdbc:mysql://localhost:3306/event_management_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:EMS_DB_USERNAME="root"
$env:EMS_DB_PASSWORD="your_mysql_password"
```

### 2. Create the schema and sample data

Run the setup script from the project root:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\setup-database.ps1 -MySqlUser root -MySqlPassword "your_mysql_password"
```

This imports:

- `database/schema.sql`
- `database/sample_data.sql`

### 3. Build the project

```powershell
mvn clean package
```

Generated artifact:

```text
target/event-management-system.war
```

### 4. Run the application

Option A: Embedded Tomcat

```powershell
.\scripts\run-embedded.cmd
```

Option B: Run `com.ems.bootstrap.AppLauncher` from your IDE.

Option C: Deploy `target/event-management-system.war` to Apache Tomcat 10.

Default local URL:

```text
http://localhost:8081/event-management-system/login
```

## Configuration Notes

- `EMS_PORT` overrides the default port `8081`
- `EMS_CONTEXT_PATH` overrides the default context path `/event-management-system`
- `DBConnection` validates that the password is not left as the placeholder value

## Sample Accounts

After importing `database/sample_data.sql`:

- `admin@ems.com` / `admin`
- `organizer@ems.com` / `password123`

## Common Issues

- `Access denied for user`: verify your MySQL username and password
- `Unknown database 'event_management_system'`: run the database setup script
- `Compiled classes folder not found`: build the project before launching the embedded server

## Repository Notes

- Generated output such as `target/`, `.embedded-tomcat/`, and IDE metadata are excluded from version control
- Database credentials in the tracked config are placeholders and should be replaced locally before running the app
