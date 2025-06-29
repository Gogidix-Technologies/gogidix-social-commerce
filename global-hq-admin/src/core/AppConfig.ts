// Admin Framework Integration: AppConfig.ts
// This file defines the global configuration for the Social Commerce Global HQ Admin Dashboard

import { AdminConfig } from 'admin-framework';

/**
 * Social Commerce Global HQ Admin configuration 
 */
export const AppConfig: AdminConfig = {
  appName: 'Social Commerce Global HQ Admin',
  appDescription: 'Global headquarters admin dashboard for social commerce domain',
  apiBasePath: '/api/social-commerce/admin',
  defaultCurrency: 'USD',
  defaultTimeRange: '30d',
  supportedCurrencies: ['USD', 'EUR', 'GBP', 'JPY'],
  supportedTimeRanges: [
    { value: '7d', label: 'Last 7 days' },
    { value: '30d', label: 'Last 30 days' },
    { value: '90d', label: 'Last 90 days' },
    { value: '365d', label: 'Last year' },
  ],
  domain: 'social-commerce',
  dashboardType: 'global-hq',
  centralizedDashboardIntegration: {
    enabled: true,
    syncInterval: 60000, // 1 minute
    dataEndpoints: [
      'metrics',
      'regions',
      'users',
      'vendors',
      'revenue'
    ]
  }
};

export default AppConfig;
