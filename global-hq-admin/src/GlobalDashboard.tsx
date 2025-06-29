// Admin Framework Integration: GlobalDashboard.tsx
// Refactored to use admin-framework

import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  Tabs,
  Tab,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  CircularProgress,
} from '@mui/material';
import {
  Public as GlobalIcon,
  TrendingUp as TrendingIcon,
  Analytics as AnalyticsIcon,
  Settings as SystemIcon,
  Security as ComplianceIcon,
  Insights as StrategicIcon,
} from '@mui/icons-material';
import { 
  TabPanel, 
  useAdminContext,
  PageHeader
} from 'admin-framework';

// Import components
import { WorldMap } from './components/WorldMap';
import { GlobalMetrics } from './components/GlobalMetrics';
import { RegionalPerformance } from './components/RegionalPerformance';
import { StrategicAnalytics } from './components/StrategicAnalytics';
import { SystemMonitoring } from './components/SystemMonitoring';
import { GlobalCompliance } from './components/GlobalCompliance';
import { StrategicPlanning } from './components/StrategicPlanning';

// Import API service
import { socialCommerceApi } from './services/api.service';

/**
 * Global HQ Dashboard for Social Commerce Domain
 * Refactored to use admin-framework components and patterns
 */
export const GlobalDashboard: React.FC = () => {
  const { appConfig } = useAdminContext();
  const [activeTab, setActiveTab] = useState(0);
  const [timeRange, setTimeRange] = useState(appConfig.defaultTimeRange);
  const [currency, setCurrency] = useState(appConfig.defaultCurrency);
  const [globalData, setGlobalData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchGlobalData();
  }, [timeRange, currency]);

  const fetchGlobalData = async () => {
    setLoading(true);
    try {
      const data = await socialCommerceApi.getDashboardData({
        timeRange,
        currency,
      });
      setGlobalData(data);
    } catch (error) {
      console.error('Failed to fetch global data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue);
  };

  return (
    <Box sx={{ flexGrow: 1, bgcolor: 'background.default', minHeight: '100vh' }}>
      {/* Global Header */}
      <PageHeader
        title="Global HQ Dashboard"
        icon={<GlobalIcon />}
        actions={
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <FormControl size="small" sx={{ minWidth: 120 }}>
              <InputLabel>Time Range</InputLabel>
              <Select 
                value={timeRange} 
                label="Time Range" 
                onChange={(e) => setTimeRange(e.target.value)}
              >
                {appConfig.supportedTimeRanges.map(range => (
                  <MenuItem key={range.value} value={range.value}>
                    {range.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            
            <FormControl size="small" sx={{ minWidth: 100 }}>
              <InputLabel>Currency</InputLabel>
              <Select 
                value={currency} 
                label="Currency" 
                onChange={(e) => setCurrency(e.target.value)}
              >
                {appConfig.supportedCurrencies.map(curr => (
                  <MenuItem key={curr} value={curr}>
                    {curr}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>
        }
      />
      
      {/* Navigation Tabs */}
      <Paper elevation={1} sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs 
          value={activeTab} 
          onChange={handleTabChange} 
          variant="scrollable" 
          scrollButtons="auto"
        >
          <Tab icon={<GlobalIcon />} label="Global Overview" />
          <Tab icon={<AnalyticsIcon />} label="Regional Performance" />
          <Tab icon={<TrendingIcon />} label="Strategic Analytics" />
          <Tab icon={<SystemIcon />} label="System Health" />
          <Tab icon={<ComplianceIcon />} label="Compliance" />
          <Tab icon={<StrategicIcon />} label="Strategic Planning" />
        </Tabs>
      </Paper>

      {/* Tab Content */}
      <TabPanel value={activeTab} index={0}>
        <GlobalOverview data={globalData} loading={loading} currency={currency} />
      </TabPanel>
      
      <TabPanel value={activeTab} index={1}>
        <RegionalPerformance data={globalData?.regions} currency={currency} />
      </TabPanel>
      
      <TabPanel value={activeTab} index={2}>
        <StrategicAnalytics data={globalData?.analytics} timeRange={timeRange} />
      </TabPanel>
      
      <TabPanel value={activeTab} index={3}>
        <SystemMonitoring data={globalData?.system} />
      </TabPanel>
      
      <TabPanel value={activeTab} index={4}>
        <GlobalCompliance data={globalData?.compliance} />
      </TabPanel>
      
      <TabPanel value={activeTab} index={5}>
        <StrategicPlanning data={globalData?.strategic} />
      </TabPanel>
    </Box>
  );
};

// Global Overview Component
const GlobalOverview: React.FC<{
  data: any;
  loading: boolean;
  currency: string;
}> = ({ data, loading, currency }) => {
  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Grid container spacing={3}>
      {/* Key Global Metrics */}
      <Grid item xs={12}>
        <GlobalMetrics data={data} currency={currency} />
      </Grid>

      {/* World Map with Regional Data */}
      <Grid item xs={12}>
        <Paper sx={{ p: 2, height: 500 }}>
          <Typography variant="h6" gutterBottom>
            Global Revenue Distribution
          </Typography>
          <WorldMap data={data?.worldMapData} currency={currency} />
        </Paper>
      </Grid>

      {/* Regional Performance Summary */}
      <Grid item xs={12} md={8}>
        <Paper sx={{ p: 2 }}>
          <Typography variant="h6" gutterBottom>
            Regional Performance Overview
          </Typography>
          <RegionalPerformance data={data?.regionalPerformance} currency={currency} />
        </Paper>
      </Grid>
      
      {/* Strategic Highlights */}
      <Grid item xs={12} md={4}>
        <Paper sx={{ p: 2 }}>
          <Typography variant="h6" gutterBottom>
            Strategic Highlights
          </Typography>
          <StrategicHighlights data={data?.strategicHighlights} />
        </Paper>
      </Grid>
    </Grid>
  );
};

// Strategic Highlights Component (placeholder - needs implementation)
const StrategicHighlights: React.FC<{ data: any }> = ({ data }) => {
  return (
    <Box>
      <Typography>Strategic Highlights Component</Typography>
    </Box>
  );
};

export default GlobalDashboard;
