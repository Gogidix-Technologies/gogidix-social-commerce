# Gogidix Marketplace Website

A modern React-based marketplace web application for the Gogidix Social Commerce ecosystem.

## 🚀 Features

- **Modern React Architecture**: Built with React 18, TypeScript, and Material-UI
- **State Management**: Redux Toolkit with RTK Query for efficient data management
- **Responsive Design**: Mobile-first design with PWA capabilities
- **Internationalization**: Multi-language support with i18next
- **Performance Optimized**: Code splitting, lazy loading, and optimized builds
- **SEO Friendly**: Server-side rendering support and meta tag management
- **Accessibility**: WCAG 2.1 AA compliant components
- **Testing**: Comprehensive test suite with Jest and React Testing Library

## 🛠️ Technology Stack

### Frontend
- **React** 18.2.0 - UI library
- **TypeScript** 4.9.5 - Type safety
- **Material-UI** 5.x - Component library
- **Redux Toolkit** - State management
- **RTK Query** - Data fetching and caching
- **React Router** v6 - Client-side routing
- **i18next** - Internationalization
- **Formik + Yup** - Form handling and validation

### Development Tools
- **ESLint** - Code linting
- **Prettier** - Code formatting
- **Jest** - Unit testing
- **React Testing Library** - Component testing
- **Storybook** - Component development

### Build & Deployment
- **Docker** - Containerization
- **Nginx** - Web server
- **Kubernetes** - Container orchestration
- **GitHub Actions** - CI/CD pipeline

## 📦 Quick Start

### Prerequisites
- Node.js 18+ and npm
- Docker (for containerization)
- Kubernetes cluster (for deployment)

### Installation

1. **Clone and install dependencies:**
```bash
git clone <repository-url>
cd marketplace-website
npm install
```

2. **Environment setup:**
```bash
cp .env.example .env
# Edit .env with your configuration
```

3. **Start development server:**
```bash
npm start
```

4. **Build for production:**
```bash
npm run build
```

### Docker Deployment

```bash
# Build Docker image
docker build -t gogidix-marketplace-website .

# Run container
docker run -p 3000:80 gogidix-marketplace-website

# Using docker-compose
docker-compose up
```

### Kubernetes Deployment

```bash
# Apply Kubernetes manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -n gogidix-production -l app=marketplace-website
```

## 🏗️ Project Structure

```
src/
├── components/           # Reusable UI components
│   ├── common/          # Common components
│   ├── layout/          # Layout components
│   ├── marketplace/     # Marketplace-specific components
│   ├── product/         # Product-related components
│   ├── cart/           # Shopping cart components
│   └── ...
├── contexts/            # React contexts
├── hooks/              # Custom React hooks
├── layouts/            # Page layouts
├── pages/              # Page components
├── services/           # API services
├── store/              # Redux store configuration
│   ├── api/            # RTK Query API definitions
│   └── slices/         # Redux slices
├── styles/             # Global styles and theme
├── types/              # TypeScript type definitions
├── utils/              # Utility functions
└── assets/             # Static assets
```

## 🎨 Component Development

### Creating New Components

```tsx
// components/common/MyComponent.tsx
import React from 'react';
import { Box, Typography } from '@mui/material';

interface MyComponentProps {
  title: string;
  children?: React.ReactNode;
}

const MyComponent: React.FC<MyComponentProps> = ({ title, children }) => {
  return (
    <Box>
      <Typography variant="h6">{title}</Typography>
      {children}
    </Box>
  );
};

export default MyComponent;
```

### State Management

```tsx
// Using Redux Toolkit
import { useSelector, useDispatch } from 'react-redux';
import { RootState } from '../store';
import { addToCart } from '../store/slices/cartSlice';

const MyComponent = () => {
  const dispatch = useDispatch();
  const cart = useSelector((state: RootState) => state.cart);
  
  const handleAddToCart = (product) => {
    dispatch(addToCart(product));
  };
  
  return (
    // Component JSX
  );
};
```

## 🌐 API Integration

### Using RTK Query

```tsx
// store/api/productApi.ts
import { api } from './index';
import { Product } from '../../types/product';

export const productApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getProducts: builder.query<Product[], void>({
      query: () => '/products',
      providesTags: ['Product'],
    }),
    getProduct: builder.query<Product, string>({
      query: (id) => `/products/${id}`,
      providesTags: (result, error, id) => [{ type: 'Product', id }],
    }),
  }),
});

export const { useGetProductsQuery, useGetProductQuery } = productApi;
```

## 🧪 Testing

### Running Tests

```bash
# Run all tests
npm test

# Run tests with coverage
npm run test:coverage

# Run tests in CI mode
npm run test:ci
```

### Writing Tests

```tsx
// __tests__/components/MyComponent.test.tsx
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { store } from '../store';
import MyComponent from '../components/MyComponent';

const Wrapper = ({ children }) => (
  <Provider store={store}>{children}</Provider>
);

describe('MyComponent', () => {
  it('renders correctly', () => {
    render(<MyComponent title="Test" />, { wrapper: Wrapper });
    expect(screen.getByText('Test')).toBeInTheDocument();
  });
});
```

## 🌍 Internationalization

### Adding Translations

```json
// public/locales/en/translation.json
{
  "common": {
    "welcome": "Welcome to Gogidix Marketplace"
  }
}
```

```tsx
// Using in components
import { useTranslation } from 'react-i18next';

const MyComponent = () => {
  const { t } = useTranslation();
  
  return <h1>{t('common.welcome')}</h1>;
};
```

## 🚀 Deployment

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `REACT_APP_API_BASE_URL` | Backend API URL | `http://localhost:8080/api/v1` |
| `REACT_APP_ENVIRONMENT` | Environment name | `development` |
| `REACT_APP_ENABLE_ANALYTICS` | Enable analytics | `true` |

### Production Build

```bash
# Build optimized production bundle
npm run build

# Analyze bundle size
npm run analyze
```

## 📊 Performance

- **Lighthouse Score**: 95+ (Performance, Accessibility, Best Practices, SEO)
- **Bundle Size**: < 2MB (optimized with code splitting)
- **First Contentful Paint**: < 1.5s
- **Time to Interactive**: < 3s

## 🔒 Security

- **Content Security Policy** implemented
- **HTTPS** enforced in production
- **Input validation** and sanitization
- **XSS protection** enabled
- **CSRF protection** implemented

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Commit changes: `git commit -am 'Add new feature'`
4. Push to the branch: `git push origin feature/new-feature`
5. Submit a pull request

### Code Style

- Follow ESLint and Prettier configurations
- Write TypeScript with strict mode
- Include tests for new features
- Update documentation as needed

## 📄 License

This project is proprietary software owned by Gogidix Application Limited.

## 📞 Support

For technical support or questions:
- Email: dev@exaltapp.com
- Slack: #marketplace-frontend
- Documentation: https://docs.exaltapp.com/marketplace