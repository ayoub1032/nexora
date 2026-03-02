# PiWebJava CRUD Application

JavaFX CRUD application for managing `Asset`, `Order`, and `Portfolio` entities.

## Project Structure

```
PiWebJava/
├── src/main/java/           # Source code
│   ├── MainApp.java         # JavaFX main application
│   ├── Asset.java           # Asset entity
│   ├── Order.java           # Order entity
│   ├── Portfolio.java       # Portfolio entity
│   ├── AssetDao.java        # Asset DAO
│   ├── OrderDao.java        # Order DAO
│   └── PortfolioDao.java    # Portfolio DAO
├── src/main/resources/      # Resources (FXML, CSS, etc.)
├── target/                  # Build output
├── pom.xml                  # Maven configuration
├── README.md                # This file
└── .vscode/tasks.json       # VS Code build tasks
```

## Prerequisites

- **JDK 11+** (download from [oracle.com](https://www.oracle.com/java/technologies/downloads/) or use [Eclipse Temurin](https://adoptium.net/))
- **Maven 3.6+** (download from [maven.apache.org](https://maven.apache.org/download.cgi))

Verify installations:
```cmd
java -version
mvn -version
```

## Setup & Build

### Option 1: Maven (Recommended)

1. **Clean and build:**
   ```cmd
   mvn clean package
   ```

2. **Run the application:**
   ```cmd
   mvn javafx:run
   ```

### Option 2: VS Code Tasks

1. Open the command palette: `Ctrl+Shift+P`
2. Run **"Maven: clean package"** (default build task)
3. Run **"Maven: javafx run"** to start the app

### Option 3: Command-line (without Maven)

Requires JavaFX SDK 11+ available at `C:\javafx-sdk`:

```cmd
javac --module-path "C:\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml src/main/java/*.java -d target/classes
java --module-path "C:\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml -cp target/classes MainApp
```

## Running the Application

After building with Maven, run via:

**Maven:**
```cmd
mvn javafx:run
```

**Or execute the JAR directly:**
```cmd
java -jar target/PiWebJava.jar
```

## Features

- **Assets Tab:** Create, read, update, delete assets with name, symbol, value, and type.
- **Orders Tab:** Manage orders with asset ID, user ID, quantity, price, and order type.
- **Portfolios Tab:** Manage portfolios with user ID and total value.

## Dependencies

- **JavaFX 21.0.2** – UI framework
- Managed via Maven (see `pom.xml`)

## Notes

- Data is stored in-memory using simple DAOs (`AssetDao`, `OrderDao`, `PortfolioDao`).
- IDs are auto-generated with AtomicLong counters.
- For production use, replace DAOs with database persistence (JPA, JDBC, etc.).
