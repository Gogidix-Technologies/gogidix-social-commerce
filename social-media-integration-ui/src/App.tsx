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
import SocialPlatforms from './pages/platforms/SocialPlatforms';
import ContentManager from './pages/content/ContentManager';
import CampaignManager from './pages/campaigns/CampaignManager';
import Analytics from './pages/analytics/Analytics';
import Automation from './pages/automation/Automation';
import Settings from './pages/settings/Settings';

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
    info: {
      main: '#1DA1F2', // Twitter blue
    },
    success: {
      main: '#1877F2', // Facebook blue
    },
    warning: {
      main: '#E4405F', // Instagram pink
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
          textTransform: 'none',
          fontWeight: 600,
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
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
                  
                  {/* Protected Routes */}
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
                  
                  <Route path="/platforms" element={
                    <ProtectedRoute>
                      <Layout>
                        <SocialPlatforms />
                      </Layout>
                    </ProtectedRoute>
                  } />
                  
                  <Route path="/content" element={
                    <ProtectedRoute>
                      <Layout>
                        <ContentManager />
                      </Layout>
                    </ProtectedRoute>
                  } />
                  
                  <Route path="/campaigns" element={
                    <ProtectedRoute>
                      <Layout>
                        <CampaignManager />
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
                  
                  <Route path="/automation" element={
                    <ProtectedRoute>
                      <Layout>
                        <Automation />
                      </Layout>
                    </ProtectedRoute>
                  } />
                  
                  <Route path="/settings" element={
                    <ProtectedRoute>
                      <Layout>
                        <Settings />
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