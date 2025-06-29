/**
 * Facebook Integration Adapter
 * 
 * Handles the integration with Facebook's API for product listings and catalog management.
 * This module implements the Facebook Marketing API for catalog and product management.
 */

const axios = require('axios');
const crypto = require('crypto');
const logger = require('../utils/logger');
const config = require('../config');

class FacebookAdapter {
  constructor() {
    this.baseUrl = 'https://graph.facebook.com/v18.0';
    this.apiVersion = 'v18.0';
  }

  /**
   * Initialize the adapter with authentication credentials
   * @param {Object} credentials - Facebook API credentials
   */
  initialize(credentials) {
    this.pageId = credentials.pageId;
    this.accessToken = credentials.accessToken;
    this.appSecret = credentials.appSecret;
    this.catalogId = credentials.catalogId;
    
    logger.info(`Facebook adapter initialized for page ID: ${this.pageId}`);
    return this;
  }

  /**
   * Authenticate with the Facebook API
   * @returns {Promise<boolean>} - Success status
   */
  async authenticate() {
    try {
      const response = await axios.get(`${this.baseUrl}/me`, {
        params: {
          access_token: this.accessToken,
          fields: 'id,name'
        }
      });
      
      logger.info(`Facebook authentication successful for page: ${response.data.name}`);
      return true;
    } catch (error) {
      logger.error('Facebook authentication failed', { error: error.message });
      throw new Error(`Facebook authentication failed: ${error.message}`);
    }
  }

  /**
   * Create or update a product in the Facebook catalog
   * @param {Object} product - Product data
   * @returns {Promise<Object>} - Created/updated product data
   */
  async publishProduct(product) {
    try {
      // Transform product to Facebook catalog format
      const fbProduct = this._transformProductToFbFormat(product);
      
      // Check if product already exists in Facebook
      const existingProduct = await this._findExistingProduct(product.sku);
      
      if (existingProduct) {
        // Update existing product
        return this._updateProduct(existingProduct.id, fbProduct);
      } else {
        // Create new product
        return this._createProduct(fbProduct);
      }
    } catch (error) {
      logger.error('Failed to publish product to Facebook', { 
        productId: product.id,
        error: error.message 
      });
      throw error;
    }
  }

  /**
   * Create a new product in the Facebook catalog
   * @param {Object} fbProduct - Facebook formatted product data
   * @returns {Promise<Object>} - Created product data
   * @private
   */
  async _createProduct(fbProduct) {
    try {
      const response = await axios.post(
        `${this.baseUrl}/${this.catalogId}/products`,
        fbProduct,
        {
          params: { access_token: this.accessToken }
        }
      );
      
      logger.info(`Product created in Facebook catalog`, {
        catalogId: this.catalogId,
        productId: response.data.id
      });
      
      return response.data;
    } catch (error) {
      logger.error('Failed to create product in Facebook catalog', { error: error.message });
      throw new Error(`Facebook product creation failed: ${error.message}`);
    }
  }

  /**
   * Update an existing product in the Facebook catalog
   * @param {string} fbProductId - Facebook product ID
   * @param {Object} fbProduct - Facebook formatted product data
   * @returns {Promise<Object>} - Updated product data
   * @private
   */
  async _updateProduct(fbProductId, fbProduct) {
    try {
      const response = await axios.post(
        `${this.baseUrl}/${fbProductId}`,
        fbProduct,
        {
          params: { access_token: this.accessToken }
        }
      );
      
      logger.info(`Product updated in Facebook catalog`, {
        catalogId: this.catalogId,
        productId: fbProductId
      });
      
      return response.data;
    } catch (error) {
      logger.error('Failed to update product in Facebook catalog', { error: error.message });
      throw new Error(`Facebook product update failed: ${error.message}`);
    }
  }

  /**
   * Find an existing product in the Facebook catalog by SKU
   * @param {string} sku - Product SKU
   * @returns {Promise<Object|null>} - Existing product or null
   * @private
   */
  async _findExistingProduct(sku) {
    try {
      const response = await axios.get(
        `${this.baseUrl}/${this.catalogId}/products`,
        {
          params: {
            access_token: this.accessToken,
            filter: JSON.stringify({
              retailer_id: { eq: sku }
            })
          }
        }
      );
      
      if (response.data.data && response.data.data.length > 0) {
        return response.data.data[0];
      }
      
      return null;
    } catch (error) {
      logger.error('Failed to find product in Facebook catalog', { error: error.message });
      return null;
    }
  }

  /**
   * Transform product data to Facebook catalog format
   * @param {Object} product - Platform product data
   * @returns {Object} - Facebook formatted product data
   * @private
   */
  _transformProductToFbFormat(product) {
    return {
      retailer_id: product.sku,
      name: product.name,
      description: product.description,
      availability: this._mapAvailability(product.inventory_status),
      condition: 'new',
      price: product.price.amount + ' ' + product.price.currency,
      link: `${config.website.baseUrl}/products/${product.slug}`,
      image_url: product.images[0].url,
      brand: product.brand,
      category: product.category,
      additional_image_urls: product.images.slice(1).map(img => img.url),
      custom_data: {
        vendor_id: product.vendor_id,
        vendor_name: product.vendor_name
      }
    };
  }

  /**
   * Map platform inventory status to Facebook availability
   * @param {string} inventoryStatus - Platform inventory status
   * @returns {string} - Facebook availability status
   * @private
   */
  _mapAvailability(inventoryStatus) {
    const map = {
      'in_stock': 'in stock',
      'out_of_stock': 'out of stock',
      'preorder': 'preorder',
      'backorder': 'available for order'
    };
    
    return map[inventoryStatus] || 'in stock';
  }

  /**
   * Share a product post on Facebook page
   * @param {Object} product - Product data
   * @param {string} message - Post message
   * @returns {Promise<Object>} - Created post data
   */
  async shareProductPost(product, message) {
    try {
      const response = await axios.post(
        `${this.baseUrl}/${this.pageId}/feed`,
        {
          message: message,
          link: `${config.website.baseUrl}/products/${product.slug}`
        },
        {
          params: { access_token: this.accessToken }
        }
      );
      
      logger.info(`Product shared on Facebook page`, {
        pageId: this.pageId,
        productId: product.id,
        postId: response.data.id
      });
      
      return response.data;
    } catch (error) {
      logger.error('Failed to share product on Facebook', { error: error.message });
      throw new Error(`Facebook post creation failed: ${error.message}`);
    }
  }

  /**
   * Delete a product from the Facebook catalog
   * @param {string} sku - Product SKU
   * @returns {Promise<boolean>} - Success status
   */
  async deleteProduct(sku) {
    try {
      const existingProduct = await this._findExistingProduct(sku);
      
      if (!existingProduct) {
        logger.warn(`Product not found in Facebook catalog`, { sku });
        return false;
      }
      
      await axios.delete(
        `${this.baseUrl}/${existingProduct.id}`,
        {
          params: { access_token: this.accessToken }
        }
      );
      
      logger.info(`Product deleted from Facebook catalog`, {
        catalogId: this.catalogId,
        sku
      });
      
      return true;
    } catch (error) {
      logger.error('Failed to delete product from Facebook catalog', { error: error.message });
      throw new Error(`Facebook product deletion failed: ${error.message}`);
    }
  }

  /**
   * Get Facebook insights for a product
   * @param {string} sku - Product SKU
   * @returns {Promise<Object>} - Product insights data
   */
  async getProductInsights(sku) {
    try {
      const existingProduct = await this._findExistingProduct(sku);
      
      if (!existingProduct) {
        logger.warn(`Product not found in Facebook catalog`, { sku });
        return null;
      }
      
      const response = await axios.get(
        `${this.baseUrl}/${existingProduct.id}/insights`,
        {
          params: {
            access_token: this.accessToken,
            metric: 'impressions,clicks,conversions'
          }
        }
      );
      
      return response.data;
    } catch (error) {
      logger.error('Failed to get product insights from Facebook', { error: error.message });
      throw new Error(`Facebook insights retrieval failed: ${error.message}`);
    }
  }
}

module.exports = new FacebookAdapter();