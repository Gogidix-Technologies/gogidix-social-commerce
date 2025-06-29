// Admin Framework Integration: data/reports.ts
// Social Commerce reports for admin-framework integration

import { Report } from 'admin-framework';

/**
 * Social Commerce specific reports for the Global HQ Admin Dashboard
 */
export const socialCommerceReports: Report[] = [
  {
    id: 'global-revenue',
    name: 'Global Revenue Report',
    description: 'Comprehensive revenue analysis across all regions',
    category: 'FINANCIAL',
    format: 'PDF',
    schedulable: true,
    parameters: [
      { name: 'timeRange', type: 'STRING', required: true, defaultValue: '30d' },
      { name: 'currency', type: 'STRING', required: true, defaultValue: 'USD' },
      { name: 'includeSubsidiaries', type: 'BOOLEAN', required: false, defaultValue: true }
    ]
  },
  {
    id: 'user-engagement',
    name: 'User Engagement Analysis',
    description: 'Detailed analysis of user engagement metrics',
    category: 'ANALYTICS',
    format: 'PDF',
    schedulable: true,
    parameters: [
      { name: 'timeRange', type: 'STRING', required: true, defaultValue: '30d' },
      { name: 'segmentBy', type: 'STRING', required: false, defaultValue: 'region' },
      { name: 'includeInactive', type: 'BOOLEAN', required: false, defaultValue: false }
    ]
  },
  {
    id: 'vendor-performance',
    name: 'Vendor Performance Report',
    description: 'Analysis of vendor performance and metrics',
    category: 'OPERATIONS',
    format: 'PDF',
    schedulable: true,
    parameters: [
      { name: 'timeRange', type: 'STRING', required: true, defaultValue: '30d' },
      { name: 'region', type: 'STRING', required: false },
      { name: 'vendorCategory', type: 'STRING', required: false },
      { name: 'minTransactions', type: 'NUMBER', required: false, defaultValue: 10 }
    ]
  },
  {
    id: 'social-integration',
    name: 'Social Media Integration Metrics',
    description: 'Analysis of social media platform integration performance',
    category: 'MARKETING',
    format: 'PDF',
    schedulable: true,
    parameters: [
      { name: 'timeRange', type: 'STRING', required: true, defaultValue: '30d' },
      { name: 'platforms', type: 'ARRAY', required: false, defaultValue: ['facebook', 'instagram', 'twitter'] },
      { name: 'engagementType', type: 'STRING', required: false, defaultValue: 'all' }
    ]
  },
  {
    id: 'compliance-audit',
    name: 'Regulatory Compliance Audit',
    description: 'Audit report for regulatory compliance across regions',
    category: 'COMPLIANCE',
    format: 'PDF',
    schedulable: true,
    parameters: [
      { name: 'timeRange', type: 'STRING', required: true, defaultValue: '90d' },
      { name: 'regulations', type: 'ARRAY', required: false },
      { name: 'region', type: 'STRING', required: false },
      { name: 'includeRemediation', type: 'BOOLEAN', required: false, defaultValue: true }
    ]
  }
];

export default socialCommerceReports;
