#!/bin/bash

# Exalt Marketplace Website - Initial Setup Script
# This script performs the initial setup for the marketplace website

set -e

echo "🛠️ Exalt Marketplace Website - Initial Setup"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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

# Ensure we're in the correct directory
if [ ! -f "package.json" ]; then
    print_error "package.json not found. Make sure you're in the marketplace-website directory."
    exit 1
fi

# Check system requirements
print_status "Checking system requirements..."

# Check Node.js
if ! command -v node &> /dev/null; then
    print_error "Node.js is required but not installed."
    print_status "Please install Node.js 18+ from https://nodejs.org/"
    exit 1
fi

NODE_VERSION=$(node --version)
print_success "Node.js $NODE_VERSION is installed"

# Check npm
if ! command -v npm &> /dev/null; then
    print_error "npm is required but not installed."
    exit 1
fi

NPM_VERSION=$(npm --version)
print_success "npm $NPM_VERSION is installed"

# Check Docker (optional)
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version)
    print_success "Docker is available: $DOCKER_VERSION"
else
    print_warning "Docker not found. Docker is optional but recommended for deployment."
fi

# Install dependencies
print_status "Installing project dependencies..."
npm install

print_success "Dependencies installed successfully"

# Setup environment
print_status "Setting up environment configuration..."

if [ ! -f ".env" ]; then
    cp .env.example .env
    print_success "Environment file created from template"
    print_warning "Please review and update .env file with your configuration"
else
    print_warning ".env file already exists"
fi

# Create necessary directories
print_status "Creating project directories..."

directories=(
    "public/images"
    "src/assets/images"
    "coverage"
    "dist"
    "logs"
)

for dir in "${directories[@]}"; do
    mkdir -p "$dir"
    print_status "Created directory: $dir"
done

# Setup Git hooks (if in a git repository)
if [ -d ".git" ]; then
    print_status "Setting up Git hooks..."
    
    # Create pre-commit hook
    cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
echo "Running pre-commit checks..."

# Run linting
npm run lint
if [ $? -ne 0 ]; then
    echo "Linting failed. Please fix errors before committing."
    exit 1
fi

# Run tests
npm run test:ci
if [ $? -ne 0 ]; then
    echo "Tests failed. Please fix failing tests before committing."
    exit 1
fi

echo "Pre-commit checks passed!"
EOF
    
    chmod +x .git/hooks/pre-commit
    print_success "Git pre-commit hook installed"
fi

# Install Husky for Git hooks management
if [ -f "package.json" ] && grep -q "husky" package.json; then
    npx husky install
    print_success "Husky Git hooks configured"
fi

# Generate project documentation
print_status "Generating project documentation..."

# Create component documentation
cat > docs/COMPONENTS.md << 'EOF'
# Component Documentation

This file contains documentation for all components in the marketplace website.

## Directory Structure

```
src/components/
├── common/          # Reusable common components
├── layout/          # Layout-specific components
├── marketplace/     # Marketplace-specific components
├── product/         # Product-related components
├── cart/           # Shopping cart components
├── checkout/       # Checkout process components
├── search/         # Search functionality components
└── user/           # User account components
```

## Component Guidelines

1. Use TypeScript for all components
2. Follow Material-UI design patterns
3. Include proper prop types and documentation
4. Write unit tests for each component
5. Use React.memo for performance optimization when needed
EOF

print_success "Component documentation created"

# Run initial quality checks
print_status "Running quality checks..."

# Lint check
if npm run lint > /dev/null 2>&1; then
    print_success "Code linting passed"
else
    print_warning "Linting issues found. Run 'npm run lint:fix' to fix automatically."
fi

# Type check
if npm run type-check > /dev/null 2>&1; then
    print_success "TypeScript type checking passed"
else
    print_warning "TypeScript type errors found. Please review and fix."
fi

# Test run
print_status "Running initial test suite..."
if npm run test:ci > /dev/null 2>&1; then
    print_success "All tests passed"
else
    print_warning "Some tests failed. Review test output for details."
fi

# Build test
print_status "Testing production build..."
if npm run build > build.log 2>&1; then
    print_success "Production build successful"
    rm -rf build # Clean up
    rm -f build.log
else
    print_error "Production build failed. Check build.log for details."
fi

print_success "🎉 Marketplace website setup completed successfully!"
print_status ""
print_status "Next steps:"
print_status "1. Review and update .env file with your configuration"
print_status "2. Start development server: npm start"
print_status "3. Open http://localhost:3000 in your browser"
print_status "4. Begin development!"
print_status ""
print_status "Useful commands:"
print_status "  npm start              - Start development server"
print_status "  npm test               - Run tests in watch mode"
print_status "  npm run build          - Build for production"
print_status "  npm run lint           - Check code quality"
print_status "  npm run type-check     - Check TypeScript types"
print_status ""