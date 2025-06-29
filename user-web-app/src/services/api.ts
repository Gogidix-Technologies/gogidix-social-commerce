import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';

/**
 * API Configuration for User Web App
 * Connects to standardized backend services using com.exalt package structure
 */

// Environment configuration - API Gateway endpoints
const API_CONFIG = {
  // API Gateway base URL (all services route through this)
  API_GATEWAY_URL: process.env.REACT_APP_API_GATEWAY_URL || 'http://localhost:8080',
  
  // Service endpoints (routed through API Gateway)
  ENDPOINTS: {
    AUTH: '/api/v1/auth',
    PRODUCTS: '/api/v1/products',
    ORDERS: '/api/v1/orders',
    USERS: '/api/v1/users',
    PAYMENTS: '/api/v1/payments',
    MARKETPLACE: '/api/v1/marketplace',
    SOCIAL_MEDIA: '/api/v1/social-media'
  },
  
  // Request timeout
  TIMEOUT: 30000,
  
  // Retry configuration
  RETRY_ATTEMPTS: 3,
  RETRY_DELAY: 1000
};

export interface ApiResponse<T = any> {
  data: T;
  message?: string;
  success: boolean;
  timestamp: string;
  errors?: Array<{
    field?: string;
    message: string;
    code?: string;
  }>;
}

export interface PaginatedApiResponse<T = any> extends ApiResponse<T[]> {
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  };
}

export interface ApiError {
  message: string;
  statusCode: number;
  errors?: Array<{
    field?: string;
    message: string;
    code?: string;
  }>;
}

/**
 * Custom API Client class for handling requests
 */
class ApiClient {
  private client: AxiosInstance;

  constructor(baseURL: string) {
    this.client = axios.create({
      baseURL,
      timeout: API_CONFIG.TIMEOUT,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });

    this.setupInterceptors();
  }

  private setupInterceptors(): void {
    // Request interceptor for authentication
    this.client.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('authToken');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }

        // Add correlation ID for request tracking
        config.headers['X-Correlation-ID'] = this.generateCorrelationId();
        
        // Add timestamp for API analytics
        config.headers['X-Request-Timestamp'] = new Date().toISOString();

        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor for error handling
    this.client.interceptors.response.use(
      (response: AxiosResponse) => {
        return response;
      },
      (error) => {
        return this.handleApiError(error);
      }
    );
  }

  private generateCorrelationId(): string {
    return `user-web-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  private async handleApiError(error: any): Promise<never> {
    if (error.response) {
      // Server responded with error status
      const apiError: ApiError = {
        message: error.response.data?.message || 'An error occurred',
        statusCode: error.response.status,
        errors: error.response.data?.errors
      };

      // Handle specific status codes
      switch (error.response.status) {
        case 401:
          // Unauthorized - clear token and redirect to login
          localStorage.removeItem('authToken');
          window.location.href = '/login';
          break;
        case 403:
          // Forbidden - insufficient permissions
          console.error('Access forbidden:', apiError.message);
          break;
        case 429:
          // Too many requests - implement retry with backoff
          console.warn('Rate limit exceeded, please retry later');
          break;
        case 500:
        case 502:
        case 503:
        case 504:
          // Server errors - log for monitoring
          console.error('Server error:', apiError);
          break;
      }

      throw apiError;
    } else if (error.request) {
      // Network error
      throw {
        message: 'Network error - please check your connection',
        statusCode: 0
      } as ApiError;
    } else {
      // Other error
      throw {
        message: error.message || 'An unexpected error occurred',
        statusCode: 0
      } as ApiError;
    }
  }

  // Generic HTTP methods
  async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.get(url, config);
    return response.data;
  }

  async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.post(url, data, config);
    return response.data;
  }

  async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.put(url, data, config);
    return response.data;
  }

  async patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.patch(url, data, config);
    return response.data;
  }

  async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.delete(url, config);
    return response.data;
  }

  // File upload method
  async upload<T = any>(url: string, formData: FormData, onProgress?: (progress: number) => void): Promise<ApiResponse<T>> {
    const response = await this.client.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(progress);
        }
      }
    });
    return response.data;
  }

  // Health check method
  async healthCheck(): Promise<boolean> {
    try {
      await this.client.get('/health');
      return true;
    } catch (error) {
      console.error('API health check failed:', error);
      return false;
    }
  }
}

// Create and export the main API client instance
export const apiClient = new ApiClient(API_CONFIG.API_GATEWAY_URL);

// Export configuration for use in services
export { API_CONFIG };

// Export utility functions
export const isApiError = (error: any): error is ApiError => {
  return error && typeof error.statusCode === 'number' && typeof error.message === 'string';
};

export const getErrorMessage = (error: any): string => {
  if (isApiError(error)) {
    return error.message;
  }
  return error?.message || 'An unexpected error occurred';
};

// API health monitoring
export const monitorApiHealth = (): void => {
  setInterval(async () => {
    const isHealthy = await apiClient.healthCheck();
    if (!isHealthy) {
      console.warn('API Gateway is not responding');
      // Could dispatch to a global state management system here
    }
  }, 60000); // Check every minute
};

export default apiClient;