/**
 * Services Index
 * Central export point for all API services
 */

// Main API client and configuration
export { 
  default as apiClient, 
  API_CONFIG, 
  isApiError, 
  getErrorMessage, 
  monitorApiHealth 
} from './api';

export type { 
  ApiResponse, 
  PaginatedApiResponse, 
  ApiError 
} from './api';

// Authentication service
export { 
  default as authService,
  authService as auth 
} from './auth.service';

export type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  ChangePasswordRequest,
  UpdateProfileRequest,
  VerifyEmailRequest,
  RefreshTokenResponse
} from './auth.service';

// Product service
export { 
  default as productService,
  productService as products 
} from './product.service';

export type {
  ProductSearchParams,
  CategoryResponse,
  ProductRecommendations,
  ProductPriceHistory,
  WishlistItem,
  CreateReviewRequest
} from './product.service';

// Order service
export { 
  default as orderService,
  orderService as orders 
} from './order.service';

export type {
  CreateOrderRequest,
  CreateOrderResponse,
  OrderFilters,
  OrderTrackingInfo,
  ShippingCalculationRequest,
  ShippingCalculationResponse,
  OrderInvoice,
  ReturnRequest,
  ReturnResponse,
  CancelOrderRequest
} from './order.service';

// Service status checker utility
export const checkServicesHealth = async (): Promise<{
  apiGateway: boolean;
  allServicesHealthy: boolean;
  timestamp: string;
}> => {
  try {
    const apiGatewayHealth = await apiClient.healthCheck();
    
    return {
      apiGateway: apiGatewayHealth,
      allServicesHealthy: apiGatewayHealth,
      timestamp: new Date().toISOString()
    };
  } catch (error) {
    console.error('Health check failed:', error);
    return {
      apiGateway: false,
      allServicesHealthy: false,
      timestamp: new Date().toISOString()
    };
  }
};

// Initialize monitoring (call this in your main App component)
export const initializeApiMonitoring = (): void => {
  monitorApiHealth();
};