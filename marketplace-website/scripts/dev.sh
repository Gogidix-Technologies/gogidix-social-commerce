#!/bin/bash

# Exalt Marketplace Website - Development Setup Script
# This script sets up the development environment for the marketplace website

set -e

echo "🚀 Setting up Exalt Marketplace Website development environment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    print_error "Node.js is not installed. Please install Node.js 18+ first."
    exit 1
fi

# Check Node.js version
NODE_VERSION=$(node --version | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    print_error "Node.js version 18+ is required. Current version: $(node --version)"
    exit 1
fi

print_success "Node.js $(node --version) is installed"

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    print_error "npm is not installed"
    exit 1
fi

print_success "npm $(npm --version) is installed"

# Install dependencies
print_status "Installing dependencies..."
if [ -f "package-lock.json" ]; then
    npm ci
else
    npm install
fi

print_success "Dependencies installed successfully"

# Create .env file if it doesn't exist
if [ ! -f ".env" ]; then
    print_status "Creating .env file from template..."
    cp .env.example .env
    print_warning "Please update .env file with your configuration"
fi

# Ensure required directories exist
print_status "Creating required directories..."
mkdir -p public/images
mkdir -p src/assets/images
mkdir -p coverage
mkdir -p dist

# Check if backend services are running
print_status "Checking backend services..."

# Function to check if a service is running
check_service() {
    local url=$1
    local service_name=$2
    
    if curl -s --head "$url" | head -n 1 | grep -q "200 OK"; then
        print_success "$service_name is running"
        return 0
    else
        print_warning "$service_name is not accessible at $url"
        return 1
    fi
}

# Check marketplace API
MARKETPLACE_API_URL=${REACT_APP_MARKETPLACE_API_URL:-"http://localhost:8081/api/v1"}
check_service "$MARKETPLACE_API_URL/health" "Marketplace API"

# Check user service
USER_API_URL=${REACT_APP_USER_SERVICE_URL:-"http://localhost:8082/api/v1"}
check_service "$USER_API_URL/health" "User Service"

# Run tests to ensure everything is working
print_status "Running tests..."
if npm run test:ci; then
    print_success "All tests passed"
else
    print_warning "Some tests failed. Check the output above."
fi

# Build the application to check for build errors
print_status "Testing build process..."
if npm run build > /dev/null 2>&1; then
    print_success "Build completed successfully"
    rm -rf build # Clean up build directory
else
    print_error "Build failed. Check for compilation errors."
fi

# Start development server
print_status "Starting development server..."
print_success "Development environment setup complete!"
print_status "Starting the application at http://localhost:3000"
print_status "API endpoints configured:"
print_status "  - Marketplace API: $MARKETPLACE_API_URL"
print_status "  - User Service: $USER_API_URL"

# Start the development server
npm start