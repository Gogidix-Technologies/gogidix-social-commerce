# user-web-app

## Overview
user-web-app is a web application component of the Social Commerce Ecosystem.

## Technology Stack
- React 18.x
- TypeScript
- Redux Toolkit (if applicable)
- React Router
- Axios for API calls
- Material-UI or Ant Design (as applicable)

## Prerequisites
- Node.js 18+
- npm or yarn

## Getting Started

### Installation
1. Clone the repository
2. Navigate to the app directory:
   ```bash
   cd user-web-app
   ```

3. Install dependencies:
   ```bash
   npm install
   # or
   yarn install
   ```

### Development
Start the development server:
```bash
npm start
# or
yarn start
```

The application will be available at http://localhost:3000

## Available Scripts

- `npm start` - Runs the app in development mode
- `npm test` - Launches the test runner
- `npm run build` - Builds the app for production
- `npm run lint` - Runs the linter
- `npm run format` - Formats the code

## Build
Build for production:
```bash
npm run build
```

The build artifacts will be stored in the `build/` or `dist/` directory.

## Testing

### Unit Tests
```bash
npm test
```

### E2E Tests
```bash
npm run test:e2e
```

## Environment Variables
Create a `.env` file in the root directory:
```
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_ENV=development
```

## Project Structure
```
src/
├── components/     # Reusable components
├── pages/          # Page components
├── routes/         # Routing configuration
├── services/       # API services
├── store/          # State management
├── utils/          # Utility functions
├── types/          # TypeScript types
└── styles/         # Global styles
```

## Docker
Build and run with Docker:
```bash
docker build -t user-web-app .
docker run -p 3000:3000 user-web-app
```

## Contributing
Please read the main project's contributing guidelines before submitting pull requests.

## License
This project is part of the Social Commerce Ecosystem and follows the same license terms.
