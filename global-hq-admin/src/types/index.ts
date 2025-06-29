// Admin Framework Integration: types/index.ts
// TypeScript interfaces for Social Commerce Global HQ Admin Dashboard

/**
 * Global Dashboard Data interface
 */
export interface GlobalDashboardData {
  metrics: GlobalMetrics;
  worldMapData: WorldMapData;
  regionalPerformance: RegionalPerformance[];
  strategicHighlights: StrategicHighlight[];
  regions: any;
  analytics: any;
  system: any;
  compliance: any;
  strategic: any;
}

/**
 * Global Metrics interface
 */
export interface GlobalMetrics {
  totalRevenue: number;
  revenueGrowth: number;
  totalUsers: number;
  userGrowth: number;
  totalVendors: number;
  vendorGrowth: number;
  globalConversion: number;
  conversionChange: number;
}

/**
 * World Map Data interface
 */
export interface WorldMapData {
  minRevenue: number;
  maxRevenue: number;
  regions: WorldMapRegion[];
}

/**
 * World Map Region interface
 */
export interface WorldMapRegion {
  code: string;
  name: string;
  revenue: number;
  growth: number;
  users: number;
}

/**
 * Regional Performance interface
 */
export interface RegionalPerformance {
  code: string;
  name: string;
  flag: string;
  revenue: number;
  growth: number;
  users: number;
  conversion: number;
  status: 'healthy' | 'warning' | 'critical';
}

/**
 * Strategic Highlight interface
 */
export interface StrategicHighlight {
  id: string;
  title: string;
  description: string;
  impact: 'high' | 'medium' | 'low';
  category: 'opportunity' | 'risk' | 'strength' | 'weakness';
}

/**
 * Export the interfaces for admin-framework integration
 */
export default {
  GlobalDashboardData,
  GlobalMetrics,
  WorldMapData,
  WorldMapRegion,
  RegionalPerformance,
  StrategicHighlight
};
