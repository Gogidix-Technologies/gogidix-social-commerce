#!/bin/bash

# Development script for vendor-web-app
echo "Starting vendor-web-app development environment..."

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install
fi

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo "Creating .env file from template..."
    cp .env.example .env 2>/dev/null || echo "REACT_APP_API_BASE_URL=http://localhost:8080/api/v1
REACT_APP_ENVIRONMENT=development
REACT_APP_VENDOR_PORTAL_URL=https://vendor.exalt-ecosystem.com" > .env
fi

# Start development server
echo "Starting development server..."
npm start