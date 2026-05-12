# Event Management System

## Project Overview

This project is a complete Java full-stack web application for managing events. It is built using Servlets, JDBC, JSP, MySQL, Maven, and Apache Tomcat. The application is suitable for college submission because it includes authentication, session handling, full CRUD operations, database scripts, sample data, and a clean folder structure.

## Features

- User registration, login, logout, and session management
- Event create, update, delete, search, and filter
- User roles: `ADMIN`, `ORGANIZER`, `MEMBER`
- Role-based access control for events, groups, invitations, and admin pages
- Group creation, member management, and event-to-group assignment
- User and group invitation workflow with accept/reject status tracking
- In-app dashboard notifications
- Group chat and event chat with JDBC persistence and auto-refresh polling
- Event feedback with 1 to 5 rating and comment support
- Admin panel to manage users and events
- Dashboard statistics for users, events, groups, upcoming events, and invitations
- JDBC + DAO based MVC structure for easy viva explanation

## Folder Structure

```text
EventManagementSystem/
|-- database/
|   |-- schema.sql
|   `-- sample_data.sql
|-- src/
|   `-- main/
|       |-- java/
|       |   `-- com/ems/
|       |       |-- dao/
|       |       |-- filter/
|       |       |-- model/
|       |       |-- servlet/
|       |       `-- util/
|       |-- resources/
|       |   `-- db.properties
|       `-- webapp/
|           |-- css/
|           |-- WEB-INF/
|           |-- dashboard.jsp
|           |-- event-form.jsp
|           |-- index.jsp
|           |-- login.jsp
|           `-- register.jsp
|-- pom.xml
`-- README.md
```

## Database Design

Main tables used in the upgraded system:

- `users`: stores login and role information
- `user_groups`: stores groups created by admins/organizers
- `group_members`: many-to-many mapping between users and groups
- `events`: stores event details
- `event_groups`: many-to-many mapping between events and groups
- `invitations`: tracks event invitations and their response status
- `notifications`: stores dashboard notifications
- `messages`: stores group chat and event chat messages
- `feedback`: stores event ratings and comments

The full SQL is available in `database/schema.sql`.
If you only want the new advanced-module tables, run `database/chat_feedback_tables.sql`.

## Backend Explanation

### Model Layer

- `User.java` stores user data.
- `Event.java` stores event data.
- `Message.java` stores sender, scope, text, and timestamp for chat.
- `Feedback.java` stores event rating, comment, and timestamp.

### DAO Layer

- `UserDao.java` handles registration, login, listing users, and admin deletion
- `EventDao.java` handles event CRUD, filtering, and ownership checks
- `GroupDao.java` handles groups and member management
- `InvitationDao.java` handles invitation sending and status updates
- `NotificationDao.java` handles dashboard notifications
- `DashboardDao.java` handles summary counts
- `MessageDao.java` handles group chat and event chat persistence
- `FeedbackDao.java` handles event feedback save/update and listing

### Utility Layer

- `DBConnection.java` reads database settings from `db.properties` and opens JDBC connections.
- `PasswordUtil.java` hashes passwords using SHA-256.

### Servlet Layer

- `RegisterServlet`, `LoginServlet`, `LogoutServlet`
- `DashboardServlet`
- `EventCreateServlet`, `EventEditServlet`, `EventUpdateServlet`, `EventDeleteServlet`
- `GroupManagementServlet`, `GroupActionServlet`
- `ChatServlet`
- `FeedbackServlet`
- `InvitationSendServlet`, `InvitationRespondServlet`
- `NotificationReadServlet`
- `AdminPanelServlet`, `AdminUserDeleteServlet`

### Filter Layer

- `AuthFilter.java` protects dashboard, event, group, invitation, notification, and admin URLs

## Frontend Pages

- `login.jsp` for user login
- `register.jsp` for new user registration
- `dashboard.jsp` for event listing and actions
- `dashboard.jsp` for event chat and feedback
- `groups.jsp` for group chat and group membership
- `WEB-INF/chat-messages.jsp` for chat auto-refresh partial rendering
- `event-form.jsp` for both create and update event operations
- `style.css` for simple clean styling

## How to Run the Project

### Requirements

- Java 17 or later
- MySQL Server 8.0 or later
- Maven, or IntelliJ IDEA with Maven support
- Apache Tomcat 10 if you want to deploy the WAR manually

### 1. Open the Project

1. Open IntelliJ IDEA.
2. Select `Open`.
3. Open this folder:

```text
EventManagementSystem
```

4. Wait for Maven dependencies to finish importing.

### 2. Configure MySQL

Open `src/main/resources/db.properties` and set your MySQL username and password:

```properties
db.url=jdbc:mysql://localhost:3306/event_management_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=your_mysql_password
db.driver=com.mysql.cj.jdbc.Driver
```

If you are using `root` with password `Root@123`, use:

```properties
db.username=root
db.password=Root@123
```

### 3. Create the Database

Run the included setup script from the project root:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\setup-database.ps1 -MySqlUser root -MySqlPassword "Root@123"
```

This imports:

- `database/schema.sql`
- `database/sample_data.sql`

You can also run those SQL files manually in MySQL Workbench.

### 4. Build the Project

In IntelliJ IDEA, use:

```text
Build > Build Project
```

Or run Maven if it is available in your terminal:

```powershell
mvn clean package
```

### 5. Run with Embedded Tomcat

The project includes an embedded launcher:

```text
src/main/java/com/ems/bootstrap/AppLauncher.java
```

In IntelliJ IDEA:

1. Open `AppLauncher.java`.
2. Click the green run button beside `main`.
3. Open this URL:

```text
http://localhost:8081/event-management-system/login
```

You can also run the command-line launcher after building:

```powershell
.\scripts\run-embedded.cmd
```

### 6. Run with Apache Tomcat

1. Install Apache Tomcat 10.
2. In IntelliJ IDEA, open `Run > Edit Configurations`.
3. Click `+` and choose `Tomcat Server > Local`, or use the included Smart Tomcat configuration.
4. Set the Tomcat home folder.
5. Open the `Deployment` tab.
6. Add artifact `event-management-system:war exploded`.
7. Set the context path to `/event-management-system`.
8. Click `Apply` and `OK`.

Then open:

```text
http://localhost:8081/event-management-system/login
```

### Runtime Environment Variables

These optional variables override `db.properties`:

```powershell
$env:EMS_DB_URL="jdbc:mysql://localhost:3306/event_management_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:EMS_DB_USERNAME="root"
$env:EMS_DB_PASSWORD="Root@123"
```

Other optional variables:

- `EMS_PORT` to change the port
- `EMS_CONTEXT_PATH` to change the context path

### Common Errors

- `Access denied for user 'root'@'localhost'`: update the MySQL password in `src/main/resources/db.properties`.
- `Unknown database 'event_management_system'`: run the database setup script in step 3.
- `Missing target\embedded.classpath.txt`: build the project once in IntelliJ or with Maven.

## Sample Data

Use these sample logins after importing `sample_data.sql`:

- Email: `admin@ems.com`
  Password: `admin`

- Email: `organizer@ems.com`
  Password: `password123`

## Expected Output Screens

### Screen 1: Login Page

- Heading: `Event Management System`
- Fields: Email and Password
- Buttons/Links: Login, Create an account

### Screen 2: Register Page

- Fields: Full Name, Email, Password
- Button: Register

### Screen 3: Dashboard

- Welcome message with user name
- Create Event button
- Logout button
- Event table showing title, description, date, location, creator, and actions

### Screen 4: Event Form

- Title changes based on action: Create Event / Update Event
- Fields: Title, Description, Event Date, Location

## Build Command

```bash
mvn clean package
```

Generated output:

```text
target/event-management-system.war
```

## Important Files

- `database/schema.sql`
- `database/chat_feedback_tables.sql`
- `database/sample_data.sql`
- `src/main/java/com/ems/dao/UserDao.java`
- `src/main/java/com/ems/dao/EventDao.java`
- `src/main/java/com/ems/dao/GroupDao.java`
- `src/main/java/com/ems/dao/MessageDao.java`
- `src/main/java/com/ems/dao/FeedbackDao.java`
- `src/main/java/com/ems/dao/InvitationDao.java`
- `src/main/java/com/ems/dao/NotificationDao.java`
- `src/main/java/com/ems/servlet/DashboardServlet.java`
- `src/main/java/com/ems/servlet/ChatServlet.java`
- `src/main/java/com/ems/servlet/FeedbackServlet.java`
- `src/main/java/com/ems/filter/AuthFilter.java`
- `src/main/webapp/dashboard.jsp`
- `src/main/webapp/groups.jsp`
- `src/main/webapp/WEB-INF/chat-messages.jsp`
- `src/main/webapp/admin-panel.jsp`
