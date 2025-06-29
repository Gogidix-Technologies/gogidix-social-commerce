const express = require('express');
const router = express.Router();
const config = require('../config/config');

/**
 * Initialize Health Check routes
 */
module.exports = (app) => {
  // Basic health check endpoint
  router.get('/', (req, res) => {
    res.json({
      status: 'ok',
      service: config.app.name,
      version: process.env.npm_package_version || '1.0.0',
      environment: config.app.environment,
      timestamp: new Date().toISOString()
    });
  });

  // More detailed health check including system status
  router.get('/details', (req, res) => {
    // Memory usage statistics
    const memUsage = process.memoryUsage();
    
    res.json({
      status: 'ok',
      service: config.app.name,
      version: process.env.npm_package_version || '1.0.0',
      environment: config.app.environment,
      uptime: process.uptime(),
      timestamp: new Date().toISOString(),
      system: {
        memoryUsage: {
          rss: `${Math.round(memUsage.rss / (1024 * 1024))} MB`,
          heapTotal: `${Math.round(memUsage.heapTotal / (1024 * 1024))} MB`,
          heapUsed: `${Math.round(memUsage.heapUsed / (1024 * 1024))} MB`,
          external: `${Math.round(memUsage.external / (1024 * 1024))} MB`
        },
        platform: process.platform,
        nodeVersion: process.version
      },
      components: {
        database: 'connected',
        auth: 'connected',
        socialPlatforms: {
          facebook: config.oauth.facebook.clientId ? 'configured' : 'not_configured',
          instagram: config.oauth.instagram.clientId ? 'configured' : 'not_configured',
          twitter: config.oauth.twitter.clientId ? 'configured' : 'not_configured',
          pinterest: config.oauth.pinterest.clientId ? 'configured' : 'not_configured' 
        }
      }
    });
  });

  // Register routes
  app.use('/health', router);
};