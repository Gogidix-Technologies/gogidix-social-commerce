import axios from 'axios';
import { LoginCredentials, RegisterData, AuthResponse, User, PasswordResetRequest } from '../types/auth';

const API_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api/v1';

class AuthService {
  private tokenKey = process.env.REACT_APP_TOKEN_STORAGE_KEY || 'exalt_marketplace_token';
  private refreshTokenKey = process.env.REACT_APP_REFRESH_TOKEN_KEY || 'exalt_marketplace_refresh_token';

  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    const response = await axios.post(`${API_URL}/auth/login`, credentials);
    const { user, token, refreshToken } = response.data;
    
    if (token) {
      localStorage.setItem(this.tokenKey, token);
      if (refreshToken) {
        localStorage.setItem(this.refreshTokenKey, refreshToken);
      }
      this.setAuthHeader(token);
    }
    
    return { user, token };
  }

  async register(data: RegisterData): Promise<AuthResponse> {
    const response = await axios.post(`${API_URL}/auth/register`, data);
    const { user, token, refreshToken } = response.data;
    
    if (token) {
      localStorage.setItem(this.tokenKey, token);
      if (refreshToken) {
        localStorage.setItem(this.refreshTokenKey, refreshToken);
      }
      this.setAuthHeader(token);
    }
    
    return { user, token };
  }

  async logout(): Promise<void> {
    try {
      await axios.post(`${API_URL}/auth/logout`);
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      localStorage.removeItem(this.tokenKey);
      localStorage.removeItem(this.refreshTokenKey);
      delete axios.defaults.headers.common['Authorization'];
    }
  }

  async getCurrentUser(): Promise<User> {
    const response = await axios.get(`${API_URL}/auth/me`);
    return response.data;
  }

  async refreshToken(): Promise<string> {
    const refreshToken = localStorage.getItem(this.refreshTokenKey);
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    const response = await axios.post(`${API_URL}/auth/refresh`, { refreshToken });
    const { token, refreshToken: newRefreshToken } = response.data;
    
    localStorage.setItem(this.tokenKey, token);
    if (newRefreshToken) {
      localStorage.setItem(this.refreshTokenKey, newRefreshToken);
    }
    this.setAuthHeader(token);
    
    return token;
  }

  async forgotPassword(data: PasswordResetRequest): Promise<void> {
    await axios.post(`${API_URL}/auth/forgot-password`, data);
  }

  async resetPassword(token: string, password: string): Promise<void> {
    await axios.post(`${API_URL}/auth/reset-password`, { token, password });
  }

  async verifyEmail(token: string): Promise<void> {
    await axios.post(`${API_URL}/auth/verify-email`, { token });
  }

  async resendVerificationEmail(email: string): Promise<void> {
    await axios.post(`${API_URL}/auth/resend-verification`, { email });
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  setAuthHeader(token: string): void {
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  }

  initializeAuth(): void {
    const token = this.getToken();
    if (token) {
      this.setAuthHeader(token);
    }
  }
}

const authService = new AuthService();
authService.initializeAuth();

export { authService };