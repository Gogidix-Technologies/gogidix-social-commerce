// Admin Framework Integration: SocialCommerceAdmin.tsx
// This file creates the main admin application using the AdminFramework

import React from 'react';
import { BaseAdminApplication, AdminAppProps } from 'admin-framework';
import AppConfig from './AppConfig';
import GlobalDashboard from '../GlobalDashboard';
import { socialCommerceRegions } from '../data/regions';
import { socialCommerceReports } from '../data/reports';
import { setupSocialCommerceServices } from '../services/serviceSetup';

/**
 * Social Commerce Global HQ Admin Application
 */
export class SocialCommerceAdmin extends BaseAdminApplication {
  constructor(props: AdminAppProps) {
    super({
      ...props,
      config: AppConfig
    });
    
    // Initialize domain-specific services
    setupSocialCommerceServices(this);
    
    // Register domain-specific regions
    this.registerRegions(socialCommerceRegions);
    
    // Register domain-specific reports
    this.registerReports(socialCommerceReports);
  }
  
  /**
   * Override to provide domain-specific dashboard
   */
  renderDashboard() {
    return <GlobalDashboard />;
  }
  
  /**
   * Override to add domain-specific authentication logic
   */
  authenticateUser(credentials) {
    // Implement Social Commerce specific authentication
    return super.authenticateUser(credentials);
  }
}

export default SocialCommerceAdmin;
