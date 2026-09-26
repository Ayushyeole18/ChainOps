#!/usr/bin/env bash
# =====================================================================
# ChainOps Startup Script for Linux & macOS
# =====================================================================

set -e

echo "=========================================================="
echo "  Starting ChainOps – Supply Chain Management System      "
echo "=========================================================="

# Check Java
if ! command -v java &> /dev/null; then
    echo "ERROR: Java 17+ is not found. Please install JDK 17 or higher."
    exit 1
fi

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not found. Please install Apache Maven 3.8+."
    exit 1
fi

echo "Compiling and launching JavaFX application..."
mvn clean compile javafx:run
