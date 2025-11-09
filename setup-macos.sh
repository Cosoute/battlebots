#!/bin/bash
# BattleRobots - macOS Setup Script
# This script installs all dependencies needed to run BattleRobots on macOS

set -e  # Exit on error

echo "=================================="
echo "BattleRobots - macOS Setup Script"
echo "=================================="
echo ""

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check if Homebrew is installed
echo "Checking for Homebrew..."
if ! command_exists brew; then
    echo "Homebrew not found. Installing Homebrew..."
    echo "This may take a few minutes..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

    # Add Homebrew to PATH for Apple Silicon Macs
    if [[ $(uname -m) == 'arm64' ]]; then
        echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
        eval "$(/opt/homebrew/bin/brew shellenv)"
    fi

    echo "✓ Homebrew installed successfully"
else
    echo "✓ Homebrew is already installed"
fi

echo ""

# Check if Java is installed
echo "Checking for Java..."
if ! command_exists java; then
    echo "Java not found. Installing Java (OpenJDK)..."
    brew install openjdk

    # Add Java to PATH
    echo 'export PATH="/opt/homebrew/opt/openjdk/bin:$PATH"' >> ~/.zprofile
    export PATH="/opt/homebrew/opt/openjdk/bin:$PATH"

    echo "✓ Java installed successfully"
else
    echo "✓ Java is already installed"
fi

echo ""

# Display Java version
echo "Installed Java version:"
java -version

echo ""
echo "=================================="
echo "Setup Complete!"
echo "=================================="
echo ""
echo "You can now run the application using:"
echo "  ./run-macos.sh"
echo ""
echo "Note: You may need to restart your terminal for"
echo "PATH changes to take effect."
echo ""
