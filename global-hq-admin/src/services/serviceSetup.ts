// Admin Framework Integration: serviceSetup.ts
// This file sets up all services for the Social Commerce Global HQ Admin

import { BaseAdminApplication } from 'admin-framework';
import { socialCommerceApi } from './api.service';

/**
 * Setup all Social Commerce services and register them with the admin application
 */
export const setupSocialCommerceServices = (app: BaseAdminApplication) => {
  // Register API service
  app.registerService('api', socialCommerceApi);
  
  // Register additional domain-specific services
  // ... 
  
  // Register event handlers
  app.on('regionChange', (region) => {
    console.log('Region changed to:', region);
    // Implement region change logic
  });
  
  app.on('timeRangeChange', (timeRange) => {
    console.log('Time range changed to:', timeRange);
    // Implement time range change logic
  });
  
  app.on('currencyChange', (currency) => {
    console.log('Currency changed to:', currency);
    // Implement currency change logic
  });
};
