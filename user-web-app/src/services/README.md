# API Services Documentation

This directory contains the API service layer for the User Web App, implementing Phase 2 API Integration with the standardized backend services using the com.exalt package structure.

## Architecture Overview

All services communicate through the API Gateway which routes requests to the appropriate microservices:

```
User Web App -> API Gateway -> Backend Services
```

## Services

### 1. API Client (`api.ts`)
Central HTTP client with interceptors for authentication, error handling, and request tracking.

**Features:**
- Automatic token management
- Request/response interceptors
- Error handling with automatic logout on 401
- Correlation ID generation for request tracking
- Health check monitoring
- File upload support

**Usage:**
```typescript
import { apiClient, API_CONFIG } from './services/api';

// Direct usage (usually not needed, use service classes instead)
const response = await apiClient.get('/api/v1/products');
```

### 2. Authentication Service (`auth.service.ts`)
Handles all authentication operations connecting to the auth-service backend.

**Features:**
- Login/logout with JWT token management
- User registration with email verification
- Password reset functionality
- Profile management
- Automatic token refresh
- Session persistence

**Usage:**
```typescript
import { authService } from './services/auth.service';

// Login
const loginResult = await authService.login({
  email: 'user@example.com',
  password: 'password123',
  rememberMe: true
});

// Register
const registerResult = await authService.register({
  firstName: 'John',
  lastName: 'Doe',
  email: 'user@example.com',
  password: 'password123',
  confirmPassword: 'password123',
  acceptTerms: true
});

// Get current user
const user = await authService.getCurrentUser();

// Check authentication status
const isAuth = authService.isAuthenticated();
```

### 3. Product Service (`product.service.ts`)
Manages all product-related operations connecting to the product-service backend.

**Features:**
- Product search and filtering
- Category management
- Product recommendations
- Wishlist management
- Product reviews
- Price history tracking
- Recently viewed products

**Usage:**
```typescript
import { productService } from './services/product.service';

// Search products
const products = await productService.searchProducts({
  search: 'laptop',
  category: 'electronics',
  filters: { minPrice: 100, maxPrice: 1000 },
  sort: SortOption.PRICE_LOW_TO_HIGH,
  page: 1,
  limit: 20
});

// Get product details
const product = await productService.getProductById('product-123');

// Add to wishlist
await productService.addToWishlist('product-123');

// Get recommendations
const recommendations = await productService.getProductRecommendations('product-123');
```

### 4. Order Service (`order.service.ts`)
Handles all order-related operations connecting to the order-service backend.

**Features:**
- Order creation and management
- Order tracking
- Shipping calculations
- Coupon validation
- Order cancellation and returns
- Invoice generation
- Reorder functionality

**Usage:**
```typescript
import { orderService } from './services/order.service';

// Create order
const order = await orderService.createOrder({
  items: [
    {
      productId: 'product-123',
      quantity: 2,
      unitPrice: 99.99
    }
  ],
  shippingAddress: { /* address object */ },
  billingAddress: { /* address object */ },
  paymentMethod: {
    type: PaymentType.CREDIT_CARD,
    cardToken: 'card-token-123'
  }
});

// Get user orders
const orders = await orderService.getOrders(1, 10);

// Track order
const tracking = await orderService.getOrderTracking('order-123');

// Calculate shipping
const shipping = await orderService.calculateShipping({
  items: [/* items */],
  shippingAddress: { /* address */ }
});
```

## Environment Configuration

Set these environment variables in your `.env` file:

```env
# API Gateway URL
REACT_APP_API_GATEWAY_URL=http://localhost:8080

# Optional: Override specific service URLs (if not using API Gateway)
REACT_APP_AUTH_SERVICE_URL=http://localhost:8081
REACT_APP_PRODUCT_SERVICE_URL=http://localhost:8082
REACT_APP_ORDER_SERVICE_URL=http://localhost:8083
```

## Error Handling

All services use standardized error handling:

```typescript
import { isApiError, getErrorMessage } from './services';

try {
  const result = await productService.getProductById('123');
} catch (error) {
  if (isApiError(error)) {
    console.error('API Error:', error.statusCode, error.message);
    if (error.errors) {
      // Handle validation errors
      error.errors.forEach(err => {
        console.error(`Field ${err.field}: ${err.message}`);
      });
    }
  } else {
    console.error('Unexpected error:', getErrorMessage(error));
  }
}
```

## Authentication Flow

The AuthContext automatically handles:

1. **Initialization**: Checks for existing tokens on app load
2. **Token Refresh**: Automatically refreshes tokens before expiry
3. **Logout**: Clears tokens on 401 responses
4. **Persistence**: Stores user data in localStorage

## Integration with React Components

### Using with Context

```typescript
import { useAuth } from '../contexts/AuthContext';

const MyComponent = () => {
  const { user, isAuthenticated, login, logout } = useAuth();
  
  // Component logic
};
```

### Direct Service Usage

```typescript
import { productService } from '../services';

const ProductList = () => {
  const [products, setProducts] = useState([]);
  
  useEffect(() => {
    const loadProducts = async () => {
      try {
        const result = await productService.searchProducts();
        setProducts(result.data);
      } catch (error) {
        console.error('Failed to load products:', error);
      }
    };
    
    loadProducts();
  }, []);
  
  // Component render
};
```

## Health Monitoring

Initialize API monitoring in your main App component:

```typescript
import { initializeApiMonitoring } from './services';

function App() {
  useEffect(() => {
    initializeApiMonitoring();
  }, []);
  
  // App component
}
```

## TypeScript Support

All services are fully typed with TypeScript interfaces. Import types as needed:

```typescript
import { 
  Product, 
  Order, 
  User, 
  ApiResponse,
  PaginatedApiResponse 
} from './types';

import {
  ProductSearchParams,
  CreateOrderRequest,
  LoginRequest
} from './services';
```

## Backend API Endpoints

Services connect to these standardized API endpoints:

- **Authentication**: `/api/v1/auth/*`
- **Products**: `/api/v1/products/*`
- **Orders**: `/api/v1/orders/*`
- **Users**: `/api/v1/users/*`
- **Payments**: `/api/v1/payments/*`

## Security Features

- **JWT Token Management**: Automatic token handling and refresh
- **Request Correlation**: Each request gets a unique correlation ID
- **Error Logging**: Comprehensive error logging for debugging
- **Health Monitoring**: Automatic API health checks
- **Secure Token Storage**: Tokens stored securely in localStorage with expiry

## Development Tips

1. **Use the services index**: Import from `./services` for cleaner imports
2. **Handle loading states**: All async operations should show loading indicators
3. **Implement error boundaries**: Catch and handle API errors gracefully
4. **Use TypeScript**: Leverage the full type system for better development experience
5. **Monitor network**: Use browser dev tools to monitor API calls
6. **Test offline**: Implement offline handling for better UX

## Testing

Services can be mocked for testing:

```typescript
import { authService } from './services/auth.service';

// Mock the service
jest.mock('./services/auth.service');
const mockAuthService = authService as jest.Mocked<typeof authService>;

// Setup mock responses
mockAuthService.login.mockResolvedValue({
  user: { /* mock user */ },
  accessToken: 'mock-token',
  refreshToken: 'mock-refresh-token',
  expiresIn: 3600
});
```