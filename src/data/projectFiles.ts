export interface FileNode {
  name: string;
  path: string;
  type: 'file' | 'folder';
  children?: FileNode[];
  language?: string;
  content?: string;
}

export const projectFilesTree: FileNode[] = [
  {
    name: 'pom.xml',
    path: 'pom.xml',
    type: 'file',
    language: 'xml',
    content: `<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.supplychainx</groupId>
    <artifactId>chainops-system</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    <name>ChainOps – Supply Chain Management System</name>
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <javafx.version>21.0.2</javafx.version>
        <mysql.version>8.3.0</mysql.version>
        <jbcrypt.version>0.4</jbcrypt.version>
        <junit.version>5.10.2</junit.version>
        <main.class>com.supplychainx.Main</main.class>
    </properties>
    <dependencies>
        <dependency><groupId>org.openjfx</groupId><artifactId>javafx-controls</artifactId><version>\${javafx.version}</version></dependency>
        <dependency><groupId>org.openjfx</groupId><artifactId>javafx-fxml</artifactId><version>\${javafx.version}</version></dependency>
        <dependency><groupId>org.openjfx</groupId><artifactId>javafx-graphics</artifactId><version>\${javafx.version}</version></dependency>
        <dependency><groupId>com.mysql</groupId><artifactId>mysql-connector-j</artifactId><version>\${mysql.version}</version></dependency>
        <dependency><groupId>com.zaxxer</groupId><artifactId>HikariCP</artifactId><version>5.1.0</version></dependency>
        <dependency><groupId>org.mindrot</groupId><artifactId>jbcrypt</artifactId><version>\${jbcrypt.version}</version></dependency>
        <dependency><groupId>org.slf4j</groupId><artifactId>slf4j-simple</artifactId><version>2.0.12</version></dependency>
        <dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter-api</artifactId><version>\${junit.version}</version><scope>test</scope></dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin><groupId>org.openjfx</groupId><artifactId>javafx-maven-plugin</artifactId><version>0.0.8</version>
                <configuration><mainClass>\${main.class}</mainClass></configuration>
            </plugin>
        </plugins>
    </build>
</project>`
  },
  {
    name: 'README.md',
    path: 'README.md',
    type: 'file',
    language: 'markdown',
    content: `# ChainOps – Enterprise Supply Chain Management System
Built in Java 17+, JavaFX 21, Maven, MySQL & JDBC.
Run locally:
mvn clean compile javafx:run`
  },
  {
    name: 'database',
    path: 'database',
    type: 'folder',
    children: [
      {
        name: 'schema.sql',
        path: 'database/schema.sql',
        type: 'file',
        language: 'sql',
        content: `-- Schema for supplychainx database
CREATE DATABASE IF NOT EXISTS supplychainx;
USE supplychainx;
-- Tables: roles, users, categories, products, suppliers, warehouses, inventory,
-- stock_transactions, purchase_orders, purchase_order_items, sales_orders,
-- sales_order_items, shipments, stock_transfers, stock_transfer_items...`
      },
      {
        name: 'seed.sql',
        path: 'database/seed.sql',
        type: 'file',
        language: 'sql',
        content: `-- Seed records for 22 products, 4 warehouses, 8 suppliers, 6 categories, demo users, inventory, orders...`
      }
    ]
  },
  {
    name: 'src',
    path: 'src',
    type: 'folder',
    children: [
      {
        name: 'main',
        path: 'src/main',
        type: 'folder',
        children: [
          {
            name: 'java',
            path: 'src/main/java',
            type: 'folder',
            children: [
              {
                name: 'com/supplychainx',
                path: 'src/main/java/com/supplychainx',
                type: 'folder',
                children: [
                  {
                    name: 'Main.java',
                    path: 'src/main/java/com/supplychainx/Main.java',
                    type: 'file',
                    language: 'java',
                    content: `package com.supplychainx;
import com.supplychainx.config.DatabaseConnection;
import com.supplychainx.ui.NavigationManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        NavigationManager.setPrimaryStage(primaryStage);
        DatabaseConnection.testConnection();
        NavigationManager.showLoginView();
    }
    public static void main(String[] args) {
        launch(args);
    }
}`
                  },
                  {
                    name: 'config',
                    path: 'src/main/java/com/supplychainx/config',
                    type: 'folder',
                    children: [
                      { name: 'DatabaseConfig.java', path: 'src/main/java/com/supplychainx/config/DatabaseConfig.java', type: 'file', language: 'java', content: '// DatabaseConfig property loader' },
                      { name: 'DatabaseConnection.java', path: 'src/main/java/com/supplychainx/config/DatabaseConnection.java', type: 'file', language: 'java', content: '// HikariCP Connection Pool & Transactions' }
                    ]
                  },
                  {
                    name: 'service',
                    path: 'src/main/java/com/supplychainx/service',
                    type: 'folder',
                    children: [
                      { name: 'AuthService.java', path: 'src/main/java/com/supplychainx/service/AuthService.java', type: 'file', language: 'java', content: '// User login and session' },
                      { name: 'InventoryService.java', path: 'src/main/java/com/supplychainx/service/InventoryService.java', type: 'file', language: 'java', content: '// Stock In/Out and transactions' },
                      { name: 'PurchaseOrderService.java', path: 'src/main/java/com/supplychainx/service/PurchaseOrderService.java', type: 'file', language: 'java', content: '// Inbound receiving' },
                      { name: 'SalesOrderService.java', path: 'src/main/java/com/supplychainx/service/SalesOrderService.java', type: 'file', language: 'java', content: '// Stock reservation and fulfillment' },
                      { name: 'StockTransferService.java', path: 'src/main/java/com/supplychainx/service/StockTransferService.java', type: 'file', language: 'java', content: '// Inter-warehouse transfer' },
                      { name: 'ProductService.java', path: 'src/main/java/com/supplychainx/service/ProductService.java', type: 'file', language: 'java', content: '// Product catalog' },
                      { name: 'ReportService.java', path: 'src/main/java/com/supplychainx/service/ReportService.java', type: 'file', language: 'java', content: '// 8 Custom reports and CSV export' }
                    ]
                  }
                ]
              }
            ]
          },
          {
            name: 'resources',
            path: 'src/main/resources',
            type: 'folder',
            children: [
              {
                name: 'css',
                path: 'src/main/resources/css',
                type: 'folder',
                children: [
                  { name: 'style.css', path: 'src/main/resources/css/style.css', type: 'file', language: 'css', content: '/* ChainOps Enterprise CSS Theme */' }
                ]
              },
              {
                name: 'fxml',
                path: 'src/main/resources/fxml',
                type: 'folder',
                children: [
                  { name: 'Login.fxml', path: 'src/main/resources/fxml/Login.fxml', type: 'file', language: 'xml', content: '<!-- Login view -->' },
                  { name: 'MainLayout.fxml', path: 'src/main/resources/fxml/MainLayout.fxml', type: 'file', language: 'xml', content: '<!-- Main layout view -->' },
                  { name: 'Dashboard.fxml', path: 'src/main/resources/fxml/Dashboard.fxml', type: 'file', language: 'xml', content: '<!-- Dashboard view -->' },
                  { name: 'Inventory.fxml', path: 'src/main/resources/fxml/Inventory.fxml', type: 'file', language: 'xml', content: '<!-- Inventory view -->' },
                  { name: 'PurchaseOrders.fxml', path: 'src/main/resources/fxml/PurchaseOrders.fxml', type: 'file', language: 'xml', content: '<!-- Purchase orders view -->' },
                  { name: 'SalesOrders.fxml', path: 'src/main/resources/fxml/SalesOrders.fxml', type: 'file', language: 'xml', content: '<!-- Sales orders view -->' },
                  { name: 'Shipments.fxml', path: 'src/main/resources/fxml/Shipments.fxml', type: 'file', language: 'xml', content: '<!-- Shipments view -->' },
                  { name: 'StockTransfers.fxml', path: 'src/main/resources/fxml/StockTransfers.fxml', type: 'file', language: 'xml', content: '<!-- Transfers view -->' },
                  { name: 'Products.fxml', path: 'src/main/resources/fxml/Products.fxml', type: 'file', language: 'xml', content: '<!-- Products view -->' },
                  { name: 'Reports.fxml', path: 'src/main/resources/fxml/Reports.fxml', type: 'file', language: 'xml', content: '<!-- Reports view -->' }
                ]
              }
            ]
          }
        ]
      }
    ]
  }
];
