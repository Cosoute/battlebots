#!/bin/bash
# BattleRobots - macOS Run Script
# This script compiles and runs the BattleRobots application

set -e  # Exit on error

echo "=================================="
echo "BattleRobots - macOS Run Script"
echo "=================================="
echo ""

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check if Java is installed
if ! command_exists java; then
    echo "ERROR: Java is not installed!"
    echo "Please run the setup script first:"
    echo "  ./setup-macos.sh"
    exit 1
fi

# Check if javac is installed
if ! command_exists javac; then
    echo "ERROR: Java compiler (javac) is not installed!"
    echo "Please run the setup script first:"
    echo "  ./setup-macos.sh"
    exit 1
fi

echo "Java version:"
java -version
echo ""

# Compile Java files
echo "Compiling Java files..."
javac *.java

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful"
else
    echo "✗ Compilation failed"
    exit 1
fi

echo ""

# Run the standalone application
echo "Starting BattleRobots..."
echo ""
echo "BattleRobots is now a standalone Java application."
echo "Use the following controls:"
echo "  - Click on cells to create/remove robots"
echo "  - Click 'Start' to begin the simulation"
echo "  - Click 'Stop' to pause the simulation"
echo "  - Click 'Next' to advance one time step"
echo "  - Use UP/DOWN arrow keys to adjust simulation speed"
echo ""
echo "Robots use linear regression AI to decide when to attack!"
echo ""

java BattleRobots

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ BattleRobots closed successfully"
else
    echo ""
    echo "✗ Error running BattleRobots"
    exit 1
fi
