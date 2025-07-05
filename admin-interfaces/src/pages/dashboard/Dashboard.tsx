import React, { useEffect, useState } from 'react';
import {
  Grid,
  Paper,
  Typography,
  Box,
  Card,
  CardContent,
  Alert,
  Chip,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Avatar,
} from '@mui/material';
import {
  People,
  Store,
  ShoppingCart,
  TrendingUp,
  Warning,
  CheckCircle,
  Error,
  Schedule,
} from '@mui/icons-material';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, PieChart, Pie, Cell } from 'recharts';

interface AdminStats {
  totalUsers: number;
  totalVendors: number;
  totalOrders: number;
  pendingApprovals: number;
  reportedContent: number;
  systemHealth: string;
}

interface ChartData {
  name: string;
  users: number;
  vendors: number;
  orders: number;
}

const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<AdminStats>({
    totalUsers: 0,
    totalVendors: 0,
    totalOrders: 0,
    pendingApprovals: 0,
    reportedContent: 0,
    systemHealth: 'excellent',
  });

  const [chartData] = useState<ChartData[]>([
    { name: 'Jan', users: 1200, vendors: 45, orders: 890 },
    { name: 'Feb', users: 1350, vendors: 52, orders: 1020 },
    { name: 'Mar', users: 1580, vendors: 48, orders: 1150 },
    { name: 'Apr', users: 1720, vendors: 63, orders: 1380 },
    { name: 'May', users: 1950, vendors: 71, orders: 1520 },
    { name: 'Jun', users: 2100, vendors: 78, orders: 1690 },
  ]);

  const systemHealthData = [
    { name: 'Operational', value: 85, color: '#4caf50' },
    { name: 'Warning', value: 12, color: '#ff9800' },
    { name: 'Critical', value: 3, color: '#f44336' },
  ];

  useEffect(() => {
    // Simulate API call
    setTimeout(() => {
      setStats({
        totalUsers: 89247,
        totalVendors: 1247,
        totalOrders: 45689,
        pendingApprovals: 23,
        reportedContent: 8,
        systemHealth: 'excellent',
      });
    }, 1000);
  }, []);

  const StatCard = ({ title, value, icon: Icon, color, alert = false }: any) => (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Box>
            <Typography color="textSecondary" gutterBottom variant="overline">
              {title}
            </Typography>
            <Typography variant="h4" component="div" color={alert ? 'error' : 'inherit'}>
              {value.toLocaleString()}
            </Typography>
            {alert && (
              <Chip 
                label="Requires Attention" 
                color="error" 
                size="small" 
                sx={{ mt: 1 }}
              />
            )}
          </Box>
          <Icon sx={{ fontSize: 40, color: alert ? 'error.main' : color }} />
        </Box>
      </CardContent>
    </Card>
  );

  return (
    <Box sx={{ flexGrow: 1, p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Admin Dashboard
      </Typography>
      
      {/* System Alerts */}
      <Box sx={{ mb: 3 }}>
        <Alert severity="info" sx={{ mb: 1 }}>
          System maintenance scheduled for tomorrow 2:00 AM - 4:00 AM EST
        </Alert>
        {stats.pendingApprovals > 0 && (
          <Alert severity="warning">
            {stats.pendingApprovals} vendor applications pending approval
          </Alert>
        )}
      </Box>
      
      <Grid container spacing={3}>
        {/* Stats Cards */}
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Users"
            value={stats.totalUsers}
            icon={People}
            color="primary.main"
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Active Vendors"
            value={stats.totalVendors}
            icon={Store}
            color="success.main"
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Orders"
            value={stats.totalOrders}
            icon={ShoppingCart}
            color="info.main"
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Pending Approvals"
            value={stats.pendingApprovals}
            icon={Schedule}
            color="warning.main"
            alert={stats.pendingApprovals > 20}
          />
        </Grid>

        {/* Growth Chart */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 2, height: 400 }}>
            <Typography variant="h6" gutterBottom>
              Platform Growth Overview
            </Typography>
            <ResponsiveContainer width="100%" height="90%">
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Line type="monotone" dataKey="users" stroke="#1976d2" strokeWidth={2} name="Users" />
                <Line type="monotone" dataKey="vendors" stroke="#4caf50" strokeWidth={2} name="Vendors" />
                <Line type="monotone" dataKey="orders" stroke="#ff9800" strokeWidth={2} name="Orders" />
              </LineChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* System Health */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 2, height: 400 }}>
            <Typography variant="h6" gutterBottom>
              System Health
            </Typography>
            <ResponsiveContainer width="100%" height="70%">
              <PieChart>
                <Pie
                  data={systemHealthData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={80}
                  dataKey="value"
                >
                  {systemHealthData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
            <Box sx={{ mt: 2 }}>
              {systemHealthData.map((item, index) => (
                <Box key={index} sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                  <Box 
                    sx={{ 
                      width: 12, 
                      height: 12, 
                      backgroundColor: item.color, 
                      borderRadius: '50%',
                      mr: 1 
                    }} 
                  />
                  <Typography variant="body2">
                    {item.name}: {item.value}%
                  </Typography>
                </Box>
              ))}
            </Box>
          </Paper>
        </Grid>

        {/* Recent Activities */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, height: 400 }}>
            <Typography variant="h6" gutterBottom>
              Recent Admin Activities
            </Typography>
            <List>
              <ListItem>
                <ListItemIcon>
                  <CheckCircle color="success" />
                </ListItemIcon>
                <ListItemText 
                  primary="Vendor Application Approved"
                  secondary="TechGear Pro - Electronics category"
                />
              </ListItem>
              <ListItem>
                <ListItemIcon>
                  <Warning color="warning" />
                </ListItemIcon>
                <ListItemText 
                  primary="Content Flagged for Review"
                  secondary="Product listing requires moderation"
                />
              </ListItem>
              <ListItem>
                <ListItemIcon>
                  <Error color="error" />
                </ListItemIcon>
                <ListItemText 
                  primary="User Account Suspended"
                  secondary="Violation of terms of service"
                />
              </ListItem>
              <ListItem>
                <ListItemIcon>
                  <CheckCircle color="success" />
                </ListItemIcon>
                <ListItemText 
                  primary="System Backup Completed"
                  secondary="Daily backup finished successfully"
                />
              </ListItem>
            </List>
          </Paper>
        </Grid>

        {/* Quick Actions */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, height: 400 }}>
            <Typography variant="h6" gutterBottom>
              Pending Reviews
            </Typography>
            <List>
              <ListItem>
                <ListItemIcon>
                  <Avatar sx={{ bgcolor: 'warning.light' }}>V</Avatar>
                </ListItemIcon>
                <ListItemText 
                  primary="Vendor Applications"
                  secondary={`${stats.pendingApprovals} applications waiting for review`}
                />
                <Chip label="Review" color="warning" size="small" />
              </ListItem>
              <ListItem>
                <ListItemIcon>
                  <Avatar sx={{ bgcolor: 'error.light' }}>C</Avatar>
                </ListItemIcon>
                <ListItemText 
                  primary="Content Reports"
                  secondary={`${stats.reportedContent} items flagged by users`}
                />
                <Chip label="Moderate" color="error" size="small" />
              </ListItem>
              <ListItem>
                <ListItemIcon>
                  <Avatar sx={{ bgcolor: 'info.light' }}>P</Avatar>
                </ListItemIcon>
                <ListItemText 
                  primary="Product Reviews"
                  secondary="15 product reviews need approval"
                />
                <Chip label="Approve" color="info" size="small" />
              </ListItem>
              <ListItem>
                <ListItemIcon>
                  <Avatar sx={{ bgcolor: 'success.light' }}>S</Avatar>
                </ListItemIcon>
                <ListItemText 
                  primary="System Updates"
                  secondary="2 security patches available"
                />
                <Chip label="Install" color="success" size="small" />
              </ListItem>
            </List>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;