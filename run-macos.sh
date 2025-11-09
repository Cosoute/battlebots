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

# Run the applet
echo "Starting BattleRobots..."
echo ""
echo "Note: appletviewer is deprecated in modern Java versions."
echo "If appletviewer is not available, you can:"
echo "  1. Open BattleRobots.html in a browser with Java plugin"
echo "  2. Convert the applet to a standalone application"
echo ""

if command_exists appletviewer; then
    echo "Running with appletviewer..."
    appletviewer BattleRobots.html
else
    echo "=================================="
    echo "appletviewer not found!"
    echo "=================================="
    echo ""
    echo "Modern Java versions (9+) have removed appletviewer."
    echo ""
    echo "Alternative options:"
    echo ""
    echo "1. Install an older Java version (Java 8) that includes appletviewer:"
    echo "   brew install --cask temurin@8"
    echo ""
    echo "2. The compiled .class files are ready at:"
    echo "   $(pwd)"
    echo ""
    echo "   You can embed them in a browser with Java plugin support"
    echo "   or convert the applet to a standalone application."
    echo ""
fi
