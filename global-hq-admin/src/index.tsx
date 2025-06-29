// Admin Framework Integration: index.tsx
// Entry point for the Social Commerce Global HQ Admin Dashboard

import React from 'react';
import ReactDOM from 'react-dom/client';
import { AdminProvider } from 'admin-framework';
import AppConfig from './core/AppConfig';
import SocialCommerceAdmin from './core/SocialCommerceAdmin';
import './styles/index.css';

// Initialize the application
const root = ReactDOM.createRoot(document.getElementById('root') as HTMLElement);

root.render(
  <React.StrictMode>
    <AdminProvider config={AppConfig}>
      <SocialCommerceAdmin />
    </AdminProvider>
  </React.StrictMode>
);
