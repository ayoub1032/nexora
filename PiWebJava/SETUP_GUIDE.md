# PiWeb Java - CRUD Application with MySQL & JavaFX

This project provides a complete CRUD interface for managing **Assets**, **Orders**, and **Portfolios** using JavaFX and MySQL database.

## 📋 Project Structure

```
PiWebJava/
├── src/main/java/
│   └── tn/esprit/
│       ├── entities/           # Data models
│       │   ├── Asset.java
│       │   ├── Order.java
│       │   └── Portfolio.java
│       ├── services/           # JDBC Services for database operations
│       │   ├── AssetService.java
│       │   ├── OrderService.java
│       │   └── PortfolioService.java
│       ├── controllers/        # JavaFX Controllers
│       │   ├── AssetViewController.java
│       │   ├── OrderViewController.java
│       │   ├── PortfolioViewController.java
│       │   └── RoleSelectionController.java
│       ├── mains/              # Entry point
│       │   └── FxMain.java
│       └── utils/              # Utility classes
│           ├── DBConnection.java       # Database connection
│           ├── SceneNavigator.java     # Scene switching
│           ├── ValidationUtil.java     # Input validation
│           └── UserContext.java        # User role management
├── src/main/resources/
│   ├── AssetView.fxml         # Assets UI
│   ├── OrderView.fxml         # Orders UI
│   ├── PortfolioView.fxml     # Portfolios UI
│   ├── RoleSelection.fxml     # Main menu
│   └── styles/
│       └── app.css            # Global CSS styling
└── pom.xml                     # Maven configuration
```

## 🛠️ Setup Instructions

### 1. **Database Setup (MySQL)**

Create the following database and tables:

```sql
CREATE DATABASE piwebdb;
USE piwebdb;

-- Assets table
CREATE TABLE asset (
    asset_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    value DOUBLE NOT NULL DEFAULT 0,
    type VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Orders table
CREATE TABLE `order` (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL,
    type VARCHAR(20),
    FOREIGN KEY (asset_id) REFERENCES asset(asset_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Portfolios table
CREATE TABLE portfolio (
    portfolio_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    total_value DOUBLE DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Portfolio-Assets bridge (many-to-many)
CREATE TABLE portfolio_asset (
    portfolio_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    quantity INT DEFAULT 0,
    PRIMARY KEY (portfolio_id, asset_id),
    FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES asset(asset_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

### 2. **Database Credentials**

Update [tn/esprit/utils/DBConnection.java](src/main/java/tn/esprit/utils/DBConnection.java) if needed:

```java
private static final String URL = "jdbc:mysql://localhost:3306/piwebdb?useSSL=false&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "";
```

### 3. **Build & Run**

Using Maven:

```bash
# Clean and build
mvn clean install

# Run the application
mvn javafx:run
```

Or compile manually:

```bash
javac -cp src/main/java src/main/java/tn/esprit/mains/FxMain.java
java -cp target/classes tn.esprit.mains.FxMain
```

## 📱 Features

### Assets Module
- ✅ **Add** new assets with name, symbol, value, and type
- ✅ **View** all assets in a table
- ✅ **Update** asset details
- ✅ **Delete** assets from database

### Orders Module
- ✅ **Create** buy/sell orders for assets
- ✅ **Track** orders by user or asset
- ✅ **Modify** order details (quantity, price)
- ✅ **Remove** orders

### Portfolios Module
- ✅ **Create** portfolios for users
- ✅ **Manage** portfolio total values
- ✅ **Add/Remove** assets from portfolios
- ✅ **View** all portfolios

## 🎨 UI Components

### Main Menu (RoleSelection)
- Quick navigation to Assets, Orders, or Portfolios management

### Asset Management View
- Table view of all assets
- Form to add/update/delete assets
- Real-time data validation

### Order Management View
- Complete order tracking
- ComboBox for BUY/SELL selection
- User and Asset filtering

### Portfolio Management View
- Portfolio overview
- User-based portfolio management
- Total value tracking

## 🔐 Utilities

### DBConnection
- Singleton pattern for database connections
- Auto-handles connection errors
- URL, username, password configurable

### SceneNavigator
- Centralized scene management
- Automatic CSS application
- Loading FXML files with FXMLLoader

### ValidationUtil
- Input validation formatters
- Numeric, decimal, and integer formatters
- Blank string detection

### UserContext
- Manage user roles (ADMIN/USER)
- Session-based context

## 🎯 Key Technologies

- **Language**: Java 17+
- **GUI Framework**: JavaFX 21.0.3
- **ORM**: JDBC (Direct SQL)
- **Database**: MySQL 8.3.0
- **Build Tool**: Maven 3.9+

## 📝 DependenciesLoaded from `pom.xml`

- `mysql-connector-j:8.3.0`
- `javafx-controls:21.0.3`
- `javafx-fxml:21.0.3`
- `javafx-graphics:21.0.3`

## 🚀 Next Steps

1. **Create the MySQL database** using the SQL scripts above
2. **Build** the project with Maven
3. **Run** `FxMain` class
4. Start managing Assets, Orders, and Portfolios!

## 🐛 Troubleshooting

### Database Connection Failed
- Check MySQL is running
- Verify credentials in `DBConnection.java`
- Ensure database `piwebdb` exists

### FXML Not Found
- Verify FXML files are in `src/main/resources/`
- Check file paths in controllers

### CSS Not Applied
- Place `app.css` in `src/main/resources/styles/`
- SceneNavigator automatically applies it

## 📄 License

This project is open source and available for educational purposes.

---

**Happy Coding!** 🎉
