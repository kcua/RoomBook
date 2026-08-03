# RoomBook

RoomBook is a small room reservation system with a Java REST API and static HTML/CSS/JavaScript pages.

This project was created for college purposes.

## Requirements

- Java 17 or newer
- Maven
- A Windows machine, or an updated database path in `src/main/java/com/mycompany/roombook/api/database/DB.java`

## Database

The app uses SQLite and currently stores the database at:

```text
C:\DB\Booking.db
```

Before running the app for the first time, create the folder:

```powershell
New-Item -ItemType Directory -Force C:\DB
```

When the API starts, it creates the required tables automatically and seeds:

- Three default rooms
- One default admin user

Default admin login:

```text
Email: admin@roombook.local
Password: admin123
```

## Run The API

From the project root, install dependencies and start the server:

```powershell
mvn clean compile
mvn exec:java
```

The API runs at:

```text
http://localhost:8080/api/
```

You can check that it is running by opening:

```text
http://localhost:8080/api/hello
```

To stop the server, return to the terminal and press `Enter`.

## Run The Frontend

The frontend files are static HTML pages in the project root:

- `index.html`
- `login.html`
- `register.html`
- `my-reservations.html`
- `admin-dashboard.html`
- `change-password.html`

Start the API first, then open `index.html` in a browser.

## Run Tests

Use Maven to run the test suite:

```powershell
mvn test
```

## Project Structure

```text
src/main/java/com/mycompany/roombook/
  Main.java                         API server entry point
  api/                              REST resources, services, models, database setup

src/test/java/com/mycompany/roombook/
  api/services/                     Unit tests

*.html, *.js, styles.css            Static frontend files
RoomBook-Bg.png                     Frontend background image
pom.xml                            Maven project configuration
```
