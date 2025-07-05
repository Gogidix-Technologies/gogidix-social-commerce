#!/bin/bash

# Setup script for vendor-web-app
echo "Setting up vendor-web-app..."

# Install dependencies
echo "Installing Node.js dependencies..."
npm install

# Create environment file if it doesn't exist
if [ ! -f ".env" ]; then
    echo "Creating .env file..."
    echo "REACT_APP_API_BASE_URL=http://localhost:8080/api/v1
REACT_APP_ENVIRONMENT=development
REACT_APP_VENDOR_PORTAL_URL=https://vendor.exalt-ecosystem.com
REACT_APP_VERSION=$(npm run version --silent 2>/dev/null || echo '1.0.0')" > .env
fi

# Create directories if they don't exist
mkdir -p public/assets/images
mkdir -p src/components/common
mkdir -p src/services/api
mkdir -p src/utils
mkdir -p src/hooks

# Run initial build to ensure everything works
echo "Running initial build test..."
npm run build

echo "Setup complete! Run 'npm start' to begin development."