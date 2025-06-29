// Admin Framework Integration: api.service.ts
// This file defines the API services for the Social Commerce Global HQ Admin

import axios from 'axios';
import { ApiService, RequestConfig } from 'admin-framework';
import AppConfig from '../core/AppConfig';

/**
 * Social Commerce API Service implementation
 */
export class SocialCommerceApiService extends ApiService {
  constructor() {
    super({
      baseURL: AppConfig.apiBasePath,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    // Add request interceptor for authentication
    this.client.interceptors.request.use(config => {
      const token = localStorage.getItem('auth_token');
      if (token) {
        config.headers.Authorization = Bearer \;
      }
      return config;
    });
  }
  
  /**
   * Get dashboard data
   */
  async getDashboardData(params: { timeRange: string, currency: string }) {
    return this.get('/dashboard', { params });
  }
  
  /**
   * Get regional performance data
   */
  async getRegionalPerformance(params: { timeRange: string, currency: string }) {
    return this.get('/regions/performance', { params });
  }
  
  /**
   * Get strategic analytics data
   */
  async getStrategicAnalytics(params: { timeRange: string }) {
    return this.get('/analytics/strategic', { params });
  }
  
  /**
   * Get system monitoring data
   */
  async getSystemMonitoring() {
    return this.get('/system/monitoring');
  }
  
  /**
   * Get global compliance data
   */
  async getGlobalCompliance() {
    return this.get('/compliance/global');
  }
}

// Export singleton instance
export const socialCommerceApi = new SocialCommerceApiService();
export default socialCommerceApi;
