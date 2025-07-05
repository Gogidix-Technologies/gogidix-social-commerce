import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline, Box } from '@mui/material';
import { SnackbarProvider } from 'notistack';
import { Provider } from 'react-redux';

import { store } from './store';
import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/auth/ProtectedRoute';
import Layout from './components/layout/Layout';

// Pages
import Login from './pages/auth/Login';
import Dashboard from './pages/dashboard/Dashboard';
import UserManagement from './pages/users/UserManagement';
import VendorManagement from './pages/vendors/VendorManagement';
import ProductModeration from './pages/products/ProductModeration';
import OrderManagement from './pages/orders/OrderManagement';
import ContentModeration from './pages/content/ContentModeration';
import SystemSettings from './pages/settings/SystemSettings';
import Reports from './pages/reports/Reports';
import Analytics from './pages/analytics/Analytics';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
    background: {
      default: '#f5f5f5',
    },
    error: {
      main: '#f44336',
    },
    warning: {
      main: '#ff9800',
    },
    success: {
      main: '#4caf50',
    },
  },
  typography: {
    h4: {
      fontWeight: 600,
    },
    h5: {
      fontWeight: 600,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 12,
        },
      },
    },
  },
});

function App() {
  return (
    <Provider store={store}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <SnackbarProvider maxSnack={3}>
          <AuthProvider>
            <Router>
              <Box sx={{ display: 'flex' }}>
                <Routes>
                  {/* Public Routes */}
                  <Route path="/login" element={<Login />} />
                  
                  {/* Protected Admin Routes */}
                  <Route path="/" element={
                    <ProtectedRoute>
                      <Layout>
                        <Dashboard />
                      </Layout>
                    </ProtectedRoute>
                  } />
                  
                  <Route path="/dashboard" element={
                    <ProtectedRoute>
                      <Layout>
                        <Dashboard />
                      </Layout>
                    </ProtectedRoute>
                  } />
                  
                  <Route path="/users" element={
                    <ProtectedRoute>
                      <Layout>
                        <UserManagement />
                      </Layout>
                    </ProtectedRoute>
                  } />
                  
                  <Route path="/vendors" element={
                    <ProtectedRoute>
                      <Layout>
                        <VendorManagement />
                      </Layout>
                    </ProtectedRoute>
                  } />
                  
                  <Route path="/products" element={
                    <ProtectedRoute>
                      <Layout>
                        <ProductModeration />
                      </Layout>
                    </ProtectedRoute>
                  } />
                  
                  <Route path="/orders" element={
                    <ProtectedRoute>
                      <Layout>
                        <OrderManagement />
                      </Layout>
                    </ProtectedRoute>
                  } />
                  
                  <Route path="/content" element={
                    <ProtectedRoute>
                      <Layout>
                        <ContentModeration />
                      </Layout>
                    </ProtectedRoute>
                  } />
                  
                  <Route path="/analytics" element={
                    <ProtectedRoute>
                      <Layout>
                        <Analytics />
                      </Layout>
                    </ProtectedRoute>
                  } />
                  
                  <Route path="/reports" element={
                    <ProtectedRoute>
                      <Layout>
                        <Reports />
                      </Layout>
                    </ProtectedRoute>
                  } />
                  
                  <Route path="/settings" element={
                    <ProtectedRoute>
                      <Layout>
                        <SystemSettings />
                      </Layout>
                    </ProtectedRoute>
                  } />
                </Routes>
              </Box>
            </Router>
          </AuthProvider>
        </SnackbarProvider>
      </ThemeProvider>
    </Provider>
  );
}

export default App;