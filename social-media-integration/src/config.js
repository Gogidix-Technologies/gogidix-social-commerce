/**
 * Configuration settings for the Social Media Integration service
 */

module.exports = {
  // Service configuration
  service: {
    name: 'social-media-integration',
    port: process.env.PORT || 8080,
    logLevel: process.env.LOG_LEVEL || 'info',
    environment: process.env.NODE_ENV || 'development'
  },
  
  // Website configuration
  website: {
    baseUrl: process.env.WEBSITE_BASE_URL || 'https://marketplace.social-ecommerce-ecosystem.com',
    productPath: '/products',
    vendorPath: '/vendors'
  },
  
  // API endpoints for internal service communication
  services: {
    product: process.env.PRODUCT_SERVICE_URL || 'http://product-service:8080',
    order: process.env.ORDER_SERVICE_URL || 'http://order-service:8080',
    vendor: process.env.VENDOR_SERVICE_URL || 'http://vendor-app:8080',
    marketplace: process.env.MARKETPLACE_SERVICE_URL || 'http://marketplace:8080'
  },
  
  // Social media platform API configurations
  platforms: {
    facebook: {
      enabled: process.env.FACEBOOK_ENABLED === 'true' || true,
      apiVersion: 'v18.0',
      webhookVerifyToken: process.env.FACEBOOK_WEBHOOK_VERIFY_TOKEN || 'your-webhook-verify-token'
    },
    instagram: {
      enabled: process.env.INSTAGRAM_ENABLED === 'true' || true,
      apiVersion: 'v18.0'
    },
    twitter: {
      enabled: process.env.TWITTER_ENABLED === 'true' || true,
      apiVersion: '2'
    },
    pinterest: {
      enabled: process.env.PINTEREST_ENABLED === 'true' || true,
      apiVersion: 'v5'
    },
    tiktok: {
      enabled: process.env.TIKTOK_ENABLED === 'true' || true,
      apiVersion: 'v2'
    },
    whatsapp: {
      enabled: process.env.WHATSAPP_ENABLED === 'true' || true,
      apiVersion: 'v18.0',
      webhookVerifyToken: process.env.WHATSAPP_WEBHOOK_VERIFY_TOKEN || 'your-whatsapp-webhook-verify-token'
    }
  },
  
  // Redis configuration for caching
  redis: {
    host: process.env.REDIS_HOST || 'redis',
    port: process.env.REDIS_PORT || 6379,
    password: process.env.REDIS_PASSWORD || '',
    ttl: 3600 // Cache TTL in seconds
  },
  
  // Authentication and authorization
  auth: {
    jwtSecret: process.env.JWT_SECRET || 'your-jwt-secret',
    jwtExpiresIn: process.env.JWT_EXPIRES_IN || '1h',
    apiKeyHeader: 'x-api-key'
  },
  
  // Metrics and monitoring
  metrics: {
    enabled: process.env.METRICS_ENABLED === 'true' || true,
    path: '/metrics'
  },
  
  // Rate limiting
  rateLimit: {
    window: 15 * 60 * 1000, // 15 minutes
    max: 100, // max requests per window
    standardHeaders: true,
    legacyHeaders: false
  }
};