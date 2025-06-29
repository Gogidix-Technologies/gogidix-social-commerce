// Admin Framework Integration: GlobalMetrics.tsx
// Refactored from original GlobalDashboard.tsx to use admin-framework

import React from 'react';
import { 
  Grid, 
  Box, 
  Typography 
} from '@mui/material';
import { 
  MetricCard, 
  formatCurrency, 
  formatNumber, 
  TrendIndicator 
} from 'admin-framework';
import {
  TrendingUp as TrendingUpIcon,
  People as PeopleIcon,
  Store as StoreIcon,
  AttachMoney as RevenueIcon
} from '@mui/icons-material';

interface GlobalMetricsProps {
  data: any;
  currency: string;
  loading?: boolean;
}

/**
 * Global Metrics component displaying key performance indicators
 */
export const GlobalMetrics: React.FC<GlobalMetricsProps> = ({ 
  data, 
  currency, 
  loading = false 
}) => {
  if (!data || loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <Typography>Loading metrics...</Typography>
      </Box>
    );
  }

  const metrics = data.metrics || {};

  return (
    <Grid container spacing={2}>
      <Grid item xs={12} sm={6} md={3}>
        <MetricCard
          title="Global Revenue"
          value={formatCurrency(metrics.totalRevenue, currency)}
          change={metrics.revenueGrowth}
          icon={<RevenueIcon />}
          color="primary"
        />
      </Grid>
      
      <Grid item xs={12} sm={6} md={3}>
        <MetricCard
          title="Total Users"
          value={formatNumber(metrics.totalUsers)}
          change={metrics.userGrowth}
          icon={<PeopleIcon />}
          color="success"
        />
      </Grid>
      
      <Grid item xs={12} sm={6} md={3}>
        <MetricCard
          title="Active Vendors"
          value={formatNumber(metrics.totalVendors)}
          change={metrics.vendorGrowth}
          icon={<StoreIcon />}
          color="info"
        />
      </Grid>
      
      <Grid item xs={12} sm={6} md={3}>
        <MetricCard
          title="Global Conversion"
          value={\\%\}
          change={metrics.conversionChange}
          icon={<TrendingUpIcon />}
          color="warning"
        />
      </Grid>
    </Grid>
  );
};

export default GlobalMetrics;
