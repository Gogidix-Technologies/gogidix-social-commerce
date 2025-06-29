import { apiClient, API_CONFIG, ApiResponse } from './api.ts';
import { User, UserRole } from '../types/index';

/**
 * Authentication Service
 * Connects to the auth-service backend using standardized com.exalt APIs
 */

export interface LoginRequest {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface LoginResponse {
  user: User;
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string;
  phone?: string;
  acceptTerms: boolean;
  subscribeNewsletter?: boolean;
}

export interface RegisterResponse {
  user: User;
  accessToken: string;
  refreshToken: string;
  emailVerificationRequired: boolean;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  password: string;
  confirmPassword: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface UpdateProfileRequest {
  firstName?: string;
  lastName?: string;
  phone?: string;
  preferences?: Record<string, any>;
}

export interface VerifyEmailRequest {
  token: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

/**
 * AuthService class for handling all authentication operations
 */
class AuthService {
  private readonly endpoint = API_CONFIG.ENDPOINTS.AUTH;

  /**
   * Login user with email and password
   */
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    try {
      const response = await apiClient.post<LoginResponse>(
        `${this.endpoint}/login`,
        credentials
      );

      if (response.success && response.data) {
        // Store tokens in localStorage
        localStorage.setItem('authToken', response.data.accessToken);
        localStorage.setItem('refreshToken', response.data.refreshToken);
        localStorage.setItem('tokenExpiresAt', 
          (Date.now() + response.data.expiresIn * 1000).toString()
        );

        // Store user data
        localStorage.setItem('userData', JSON.stringify(response.data.user));

        return response.data;
      }

      throw new Error(response.message || 'Login failed');
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  }

  /**
   * Register new user account
   */
  async register(userData: RegisterRequest): Promise<RegisterResponse> {
    try {
      const response = await apiClient.post<RegisterResponse>(
        `${this.endpoint}/register`,
        userData
      );

      if (response.success && response.data) {
        // Store tokens if registration doesn't require email verification
        if (!response.data.emailVerificationRequired) {
          localStorage.setItem('authToken', response.data.accessToken);
          localStorage.setItem('refreshToken', response.data.refreshToken);
          localStorage.setItem('userData', JSON.stringify(response.data.user));
        }

        return response.data;
      }

      throw new Error(response.message || 'Registration failed');
    } catch (error) {
      console.error('Registration error:', error);
      throw error;
    }
  }

  /**
   * Send forgot password email
   */
  async forgotPassword(data: ForgotPasswordRequest): Promise<void> {
    try {
      const response = await apiClient.post(
        `${this.endpoint}/forgot-password`,
        data
      );

      if (!response.success) {
        throw new Error(response.message || 'Failed to send password reset email');
      }
    } catch (error) {
      console.error('Forgot password error:', error);
      throw error;
    }
  }

  /**
   * Reset password with token from email
   */
  async resetPassword(data: ResetPasswordRequest): Promise<void> {
    try {
      const response = await apiClient.post(
        `${this.endpoint}/reset-password`,
        data
      );

      if (!response.success) {
        throw new Error(response.message || 'Failed to reset password');
      }
    } catch (error) {
      console.error('Reset password error:', error);
      throw error;
    }
  }

  /**
   * Change password for authenticated user
   */
  async changePassword(data: ChangePasswordRequest): Promise<void> {
    try {
      const response = await apiClient.put(
        `${this.endpoint}/change-password`,
        data
      );

      if (!response.success) {
        throw new Error(response.message || 'Failed to change password');
      }
    } catch (error) {
      console.error('Change password error:', error);
      throw error;
    }
  }

  /**
   * Verify email address with token
   */
  async verifyEmail(data: VerifyEmailRequest): Promise<void> {
    try {
      const response = await apiClient.post(
        `${this.endpoint}/verify-email`,
        data
      );

      if (!response.success) {
        throw new Error(response.message || 'Email verification failed');
      }
    } catch (error) {
      console.error('Email verification error:', error);
      throw error;
    }
  }

  /**
   * Resend email verification
   */
  async resendEmailVerification(): Promise<void> {
    try {
      const response = await apiClient.post(
        `${this.endpoint}/resend-verification`
      );

      if (!response.success) {
        throw new Error(response.message || 'Failed to resend verification email');
      }
    } catch (error) {
      console.error('Resend verification error:', error);
      throw error;
    }
  }

  /**
   * Get current user profile
   */
  async getCurrentUser(): Promise<User> {
    try {
      const response = await apiClient.get<User>(
        `${this.endpoint}/me`
      );

      if (response.success && response.data) {
        // Update stored user data
        localStorage.setItem('userData', JSON.stringify(response.data));
        return response.data;
      }

      throw new Error(response.message || 'Failed to get user profile');
    } catch (error) {
      console.error('Get current user error:', error);
      throw error;
    }
  }

  /**
   * Update user profile
   */
  async updateProfile(data: UpdateProfileRequest): Promise<User> {
    try {
      const response = await apiClient.put<User>(
        `${this.endpoint}/profile`,
        data
      );

      if (response.success && response.data) {
        // Update stored user data
        localStorage.setItem('userData', JSON.stringify(response.data));
        return response.data;
      }

      throw new Error(response.message || 'Failed to update profile');
    } catch (error) {
      console.error('Update profile error:', error);
      throw error;
    }
  }

  /**
   * Refresh authentication tokens
   */
  async refreshTokens(): Promise<RefreshTokenResponse> {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await apiClient.post<RefreshTokenResponse>(
        `${this.endpoint}/refresh`,
        { refreshToken }
      );

      if (response.success && response.data) {
        // Update stored tokens
        localStorage.setItem('authToken', response.data.accessToken);
        localStorage.setItem('refreshToken', response.data.refreshToken);
        localStorage.setItem('tokenExpiresAt', 
          (Date.now() + response.data.expiresIn * 1000).toString()
        );

        return response.data;
      }

      throw new Error(response.message || 'Token refresh failed');
    } catch (error) {
      console.error('Token refresh error:', error);
      // Clear invalid tokens
      this.logout();
      throw error;
    }
  }

  /**
   * Logout user and clear stored data
   */
  async logout(): Promise<void> {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      
      // Attempt to invalidate tokens on server
      if (refreshToken) {
        await apiClient.post(`${this.endpoint}/logout`, { refreshToken });
      }
    } catch (error) {
      // Continue with logout even if server request fails
      console.warn('Logout server request failed:', error);
    } finally {
      // Clear all stored authentication data
      localStorage.removeItem('authToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('tokenExpiresAt');
      localStorage.removeItem('userData');
    }
  }

  /**
   * Check if user is currently authenticated
   */
  isAuthenticated(): boolean {
    const token = localStorage.getItem('authToken');
    const expiresAt = localStorage.getItem('tokenExpiresAt');

    if (!token || !expiresAt) {
      return false;
    }

    // Check if token is expired
    const isExpired = Date.now() >= parseInt(expiresAt);
    if (isExpired) {
      this.logout();
      return false;
    }

    return true;
  }

  /**
   * Get current user data from localStorage
   */
  getCurrentUserData(): User | null {
    try {
      const userData = localStorage.getItem('userData');
      return userData ? JSON.parse(userData) : null;
    } catch (error) {
      console.error('Error parsing user data:', error);
      return null;
    }
  }

  /**
   * Get stored authentication token
   */
  getAuthToken(): string | null {
    return localStorage.getItem('authToken');
  }

  /**
   * Check if token is about to expire (within 5 minutes)
   */
  isTokenAboutToExpire(): boolean {
    const expiresAt = localStorage.getItem('tokenExpiresAt');
    if (!expiresAt) return false;

    const fiveMinutesFromNow = Date.now() + (5 * 60 * 1000);
    return parseInt(expiresAt) <= fiveMinutesFromNow;
  }

  /**
   * Automatically refresh token if needed
   */
  async ensureValidToken(): Promise<void> {
    if (this.isTokenAboutToExpire()) {
      try {
        await this.refreshTokens();
      } catch (error) {
        console.error('Failed to refresh token:', error);
        throw error;
      }
    }
  }
}

// Export singleton instance
export const authService = new AuthService();
export default authService;