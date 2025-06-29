/**
 * Social Media Integration Service
 * 
 * This service enables the synchronization of product listings between 
 * the Social E-commerce platform and various social media platforms.
 */

const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const { createProxyMiddleware } = require('http-proxy-middleware');

// Import platform-specific adapters
const facebookAdapter = require('./adapters/facebook');
const instagramAdapter = require('./adapters/instagram');
const tiktokAdapter = require('./adapters/tiktok');
const whatsappAdapter = require('./adapters/whatsapp');
const twitterAdapter = require('./adapters/twitter');
const pinterestAdapter = require('./adapters/pinterest');

// Import utilities
const logger = require('./utils/logger');
const metrics = require('./utils/metrics');
const validation = require('./utils/validation');
const errorHandler = require('./middleware/errorHandler');

// Create Express application
const app = express();

// Apply middleware
app.use(helmet()); // Security headers
app.use(cors()); // Cross-origin resource sharing
app.use(express.json()); // Parse JSON bodies
app.use(morgan('combined')); // HTTP request logging
app.use(metrics.requestCounter); // Prometheus metrics

// Import routes
const productRoutes = require('./routes/product');
const publishRoutes = require('./routes/publish');
const platformRoutes = require('./routes/platform');
const webhookRoutes = require('./routes/webhook');
const accountRoutes = require('./routes/account');
const analyticsRoutes = require('./routes/analytics');

// Apply routes
app.use('/api/products', productRoutes);
app.use('/api/publish', publishRoutes);
app.use('/api/platforms', platformRoutes);
app.use('/api/webhooks', webhookRoutes);
app.use('/api/accounts', accountRoutes);
app.use('/api/analytics', analyticsRoutes);

// Health check endpoint
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'ok', version: process.env.SERVICE_VERSION || '1.0.0' });
});

// Apply error handler middleware
app.use(errorHandler);

// Start server
const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
  logger.info(`Social Media Integration Service running on port ${PORT}`);
});