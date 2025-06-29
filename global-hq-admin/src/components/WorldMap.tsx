// Admin Framework Integration: WorldMap.tsx
// Refactored from original GlobalDashboard.tsx to use admin-framework

import React from 'react';
import { Box, Typography } from '@mui/material';
import { MapChart, MapTooltip, formatCurrency } from 'admin-framework';

interface WorldMapProps {
  data: any;
  currency: string;
}

/**
 * World Map component displaying global revenue distribution
 */
export const WorldMap: React.FC<WorldMapProps> = ({ data, currency }) => {
  if (!data) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <Typography>No map data available</Typography>
      </Box>
    );
  }

  const renderTooltip = (region) => (
    <MapTooltip>
      <Typography variant="subtitle1" fontWeight="bold">{region.name}</Typography>
      <Typography variant="body2">Revenue: {formatCurrency(region.revenue, currency)}</Typography>
      <Typography variant="body2">Growth: {region.growth}%</Typography>
      <Typography variant="body2">Users: {region.users.toLocaleString()}</Typography>
    </MapTooltip>
  );

  return (
    <MapChart
      data={data}
      valueField="revenue"
      tooltipRenderer={renderTooltip}
      colorScale={{
        min: data.minRevenue,
        max: data.maxRevenue,
        minColor: '#E3F2FD',
        maxColor: '#1565C0'
      }}
    />
  );
};

export default WorldMap;
