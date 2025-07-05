# Setup Guide - Vendor Web App

## Prerequisites

Before setting up the Vendor Web App, ensure you have the following installed:

### Required Software
- **Node.js**: Version 18.0 or higher
- **npm**: Version 8.0 or higher (or yarn 1.22+)
- **Git**: For version control
- **VS Code**: Recommended IDE with extensions

### Recommended VS Code Extensions
- ES7+ React/Redux/React-Native snippets
- TypeScript Importer
- Prettier - Code formatter
- ESLint
- Auto Rename Tag
- Bracket Pair Colorizer

## Installation Steps

### 1. Clone Repository
```bash
git clone https://github.com/Gogidix-Application-Limited/gogidix-social-commerce.git
cd gogidix-social-commerce/vendor-web-app
```

### 2. Install Dependencies
```bash
# Using npm
npm install

# Using yarn
yarn install
```

### 3. Environment Configuration
```bash
# Copy environment template
cp .env.example .env

# Edit environment variables
nano .env
```

### 4. Environment Variables
Configure the following variables in your `.env` file:

```env
# API Configuration
REACT_APP_API_BASE_URL=http://localhost:8080/api/v1
REACT_APP_WS_URL=ws://localhost:8080/ws

# Environment
REACT_APP_ENVIRONMENT=development
REACT_APP_VERSION=1.0.0

# Features
REACT_APP_ENABLE_ANALYTICS=true
REACT_APP_ENABLE_NOTIFICATIONS=true

# External Services
REACT_APP_STRIPE_PUBLIC_KEY=pk_test_...
REACT_APP_GOOGLE_MAPS_API_KEY=AIza...

# Debugging
REACT_APP_LOG_LEVEL=debug
REACT_APP_ENABLE_REDUX_DEVTOOLS=true
```

### 5. Start Development Server
```bash
# Start the development server
npm start

# Application will be available at:
# http://localhost:3000
```

## Development Setup

### Project Structure
```
vendor-web-app/
├── public/              # Static assets
├── src/
│   ├── components/      # Reusable components
│   ├── pages/          # Page components
│   ├── store/          # Redux store
│   ├── services/       # API services
│   ├── hooks/          # Custom hooks
│   ├── utils/          # Utility functions
│   ├── types/          # TypeScript definitions
│   └── assets/         # Images, fonts, etc.
├── tests/              # Test files
├── docs/               # Documentation
└── k8s/                # Kubernetes manifests
```

### Development Commands
```bash
# Start development server
npm start

# Run tests
npm test

# Run tests with coverage
npm run test:coverage

# Type checking
npm run type-check

# Linting
npm run lint

# Fix linting issues
npm run lint:fix

# Build for production
npm run build

# Preview production build
npm run preview
```

## Docker Setup

### Using Docker Compose
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Manual Docker Build
```bash
# Build image
docker build -t vendor-web-app .

# Run container
docker run -p 3000:80 vendor-web-app
```

## Database Setup

The frontend connects to backend services that require database setup:

### PostgreSQL Setup
```bash
# Using Docker
docker run --name postgres-dev \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=vendor_web_app \
  -p 5432:5432 \
  -d postgres:14-alpine
```

### Redis Setup
```bash
# Using Docker
docker run --name redis-dev \
  -p 6379:6379 \
  -d redis:7-alpine
```

## IDE Configuration

### VS Code Settings
Create `.vscode/settings.json`:
```json
{
  "editor.formatOnSave": true,
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  "typescript.preferences.importModuleSpecifier": "relative",
  "emmet.includeLanguages": {
    "typescript": "html",
    "typescriptreact": "html"
  }
}
```

### Debug Configuration
Create `.vscode/launch.json`:
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Debug React App",
      "type": "node",
      "request": "launch",
      "cwd": "${workspaceFolder}",
      "runtimeExecutable": "npm",
      "runtimeArgs": ["start"]
    }
  ]
}
```

## Testing Setup

### Unit Tests
```bash
# Run all tests
npm test

# Run specific test file
npm test -- Dashboard.test.tsx

# Run tests in watch mode
npm test -- --watch
```

### E2E Tests
```bash
# Install Playwright
npx playwright install

# Run E2E tests
npm run test:e2e

# Run E2E tests in headed mode
npm run test:e2e:headed
```

## Troubleshooting

### Common Issues

#### Port Already in Use
```bash
# Find process using port 3000
lsof -ti:3000

# Kill the process
kill -9 <PID>
```

#### Module Resolution Issues
```bash
# Clear npm cache
npm cache clean --force

# Remove node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

#### TypeScript Errors
```bash
# Restart TypeScript service in VS Code
Ctrl+Shift+P → "TypeScript: Restart TS Server"

# Check TypeScript configuration
npm run type-check
```

### Performance Issues
- Enable React DevTools Profiler
- Check bundle analyzer: `npm run analyze`
- Monitor Network tab for API calls
- Use Lighthouse for performance audit

## Next Steps

After successful setup:

1. **Explore the codebase** - Review component structure
2. **Run tests** - Ensure everything works correctly  
3. **Configure API endpoints** - Update environment variables
4. **Start development** - Begin implementing features
5. **Read documentation** - Review architecture and API docs

## Getting Help

- **Documentation**: Check `/docs` folder
- **Issues**: Report bugs via GitHub issues
- **Development Team**: Contact via internal channels
- **Stack Overflow**: Search for React/TypeScript questions