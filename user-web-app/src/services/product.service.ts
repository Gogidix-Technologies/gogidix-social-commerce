import { apiClient, API_CONFIG, PaginatedApiResponse } from './api.ts';
import { 
  Product, 
  ProductVariant, 
  ProductFilters, 
  SortOption, 
  PaginationParams,
  Review 
} from '../types/index';

/**
 * Product Service
 * Connects to the product-service backend using standardized com.exalt APIs
 */

export interface ProductSearchParams {
  search?: string;
  category?: string;
  filters?: ProductFilters;
  sort?: SortOption;
  page?: number;
  limit?: number;
}

export interface CategoryResponse {
  id: string;
  name: string;
  slug: string;
  description?: string;
  parentId?: string;
  children?: CategoryResponse[];
  productCount: number;
  image?: string;
}

export interface ProductRecommendations {
  relatedProducts: Product[];
  frequentlyBoughtTogether: Product[];
  recentlyViewed: Product[];
  trending: Product[];
}

export interface ProductPriceHistory {
  productId: string;
  priceHistory: Array<{
    price: number;
    date: string;
    type: 'regular' | 'discount' | 'sale';
  }>;
}

export interface WishlistItem {
  id: string;
  productId: string;
  product: Product;
  addedAt: string;
}

export interface CreateReviewRequest {
  productId: string;
  rating: number;
  title: string;
  comment: string;
  images?: string[];
}

/**
 * ProductService class for handling all product-related operations
 */
class ProductService {
  private readonly endpoint = API_CONFIG.ENDPOINTS.PRODUCTS;

  /**
   * Search and filter products with pagination
   */
  async searchProducts(params: ProductSearchParams = {}): Promise<PaginatedApiResponse<Product>> {
    try {
      const queryParams = {
        search: params.search,
        category: params.category,
        sort: params.sort || SortOption.POPULARITY,
        page: params.page || 1,
        limit: params.limit || 20,
        ...params.filters
      };

      const response = await apiClient.get<Product[]>(
        `${this.endpoint}/search`,
        { params: queryParams }
      );

      return response as PaginatedApiResponse<Product>;
    } catch (error) {
      console.error('Search products error:', error);
      throw error;
    }
  }

  /**
   * Get product by ID with detailed information
   */
  async getProductById(productId: string): Promise<Product> {
    try {
      const response = await apiClient.get<Product>(
        `${this.endpoint}/${productId}`
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Product not found');
    } catch (error) {
      console.error('Get product by ID error:', error);
      throw error;
    }
  }

  /**
   * Get multiple products by IDs
   */
  async getProductsByIds(productIds: string[]): Promise<Product[]> {
    try {
      const response = await apiClient.post<Product[]>(
        `${this.endpoint}/batch`,
        { productIds }
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to fetch products');
    } catch (error) {
      console.error('Get products by IDs error:', error);
      throw error;
    }
  }

  /**
   * Get product variants
   */
  async getProductVariants(productId: string): Promise<ProductVariant[]> {
    try {
      const response = await apiClient.get<ProductVariant[]>(
        `${this.endpoint}/${productId}/variants`
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to fetch product variants');
    } catch (error) {
      console.error('Get product variants error:', error);
      throw error;
    }
  }

  /**
   * Get all categories
   */
  async getCategories(parentId?: string): Promise<CategoryResponse[]> {
    try {
      const params = parentId ? { parentId } : {};
      const response = await apiClient.get<CategoryResponse[]>(
        `${this.endpoint}/categories`,
        { params }
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to fetch categories');
    } catch (error) {
      console.error('Get categories error:', error);
      throw error;
    }
  }

  /**
   * Get category by ID
   */
  async getCategoryById(categoryId: string): Promise<CategoryResponse> {
    try {
      const response = await apiClient.get<CategoryResponse>(
        `${this.endpoint}/categories/${categoryId}`
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Category not found');
    } catch (error) {
      console.error('Get category by ID error:', error);
      throw error;
    }
  }

  /**
   * Get featured products
   */
  async getFeaturedProducts(limit: number = 10): Promise<Product[]> {
    try {
      const response = await apiClient.get<Product[]>(
        `${this.endpoint}/featured`,
        { params: { limit } }
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to fetch featured products');
    } catch (error) {
      console.error('Get featured products error:', error);
      throw error;
    }
  }

  /**
   * Get trending products
   */
  async getTrendingProducts(limit: number = 10): Promise<Product[]> {
    try {
      const response = await apiClient.get<Product[]>(
        `${this.endpoint}/trending`,
        { params: { limit } }
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to fetch trending products');
    } catch (error) {
      console.error('Get trending products error:', error);
      throw error;
    }
  }

  /**
   * Get products on sale
   */
  async getSaleProducts(page: number = 1, limit: number = 20): Promise<PaginatedApiResponse<Product>> {
    try {
      const response = await apiClient.get<Product[]>(
        `${this.endpoint}/sale`,
        { params: { page, limit } }
      );

      return response as PaginatedApiResponse<Product>;
    } catch (error) {
      console.error('Get sale products error:', error);
      throw error;
    }
  }

  /**
   * Get product recommendations
   */
  async getProductRecommendations(productId: string): Promise<ProductRecommendations> {
    try {
      const response = await apiClient.get<ProductRecommendations>(
        `${this.endpoint}/${productId}/recommendations`
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to fetch recommendations');
    } catch (error) {
      console.error('Get product recommendations error:', error);
      throw error;
    }
  }

  /**
   * Get user-specific recommendations
   */
  async getUserRecommendations(limit: number = 20): Promise<Product[]> {
    try {
      const response = await apiClient.get<Product[]>(
        `${this.endpoint}/recommendations/for-you`,
        { params: { limit } }
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to fetch user recommendations');
    } catch (error) {
      console.error('Get user recommendations error:', error);
      throw error;
    }
  }

  /**
   * Get product price history
   */
  async getProductPriceHistory(productId: string, days: number = 30): Promise<ProductPriceHistory> {
    try {
      const response = await apiClient.get<ProductPriceHistory>(
        `${this.endpoint}/${productId}/price-history`,
        { params: { days } }
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to fetch price history');
    } catch (error) {
      console.error('Get product price history error:', error);
      throw error;
    }
  }

  /**
   * Track product view (for analytics and recommendations)
   */
  async trackProductView(productId: string): Promise<void> {
    try {
      await apiClient.post(
        `${this.endpoint}/${productId}/view`,
        { timestamp: new Date().toISOString() }
      );
    } catch (error) {
      // Don't throw error for tracking failures
      console.warn('Track product view failed:', error);
    }
  }

  /**
   * Get product reviews
   */
  async getProductReviews(
    productId: string, 
    page: number = 1, 
    limit: number = 10,
    sort: 'newest' | 'oldest' | 'highest' | 'lowest' = 'newest'
  ): Promise<PaginatedApiResponse<Review>> {
    try {
      const response = await apiClient.get<Review[]>(
        `${this.endpoint}/${productId}/reviews`,
        { params: { page, limit, sort } }
      );

      return response as PaginatedApiResponse<Review>;
    } catch (error) {
      console.error('Get product reviews error:', error);
      throw error;
    }
  }

  /**
   * Create product review
   */
  async createReview(reviewData: CreateReviewRequest): Promise<Review> {
    try {
      const response = await apiClient.post<Review>(
        `${this.endpoint}/${reviewData.productId}/reviews`,
        reviewData
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to create review');
    } catch (error) {
      console.error('Create review error:', error);
      throw error;
    }
  }

  /**
   * Get user's wishlist
   */
  async getWishlist(page: number = 1, limit: number = 20): Promise<PaginatedApiResponse<WishlistItem>> {
    try {
      const response = await apiClient.get<WishlistItem[]>(
        `${this.endpoint}/wishlist`,
        { params: { page, limit } }
      );

      return response as PaginatedApiResponse<WishlistItem>;
    } catch (error) {
      console.error('Get wishlist error:', error);
      throw error;
    }
  }

  /**
   * Add product to wishlist
   */
  async addToWishlist(productId: string): Promise<WishlistItem> {
    try {
      const response = await apiClient.post<WishlistItem>(
        `${this.endpoint}/wishlist`,
        { productId }
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to add to wishlist');
    } catch (error) {
      console.error('Add to wishlist error:', error);
      throw error;
    }
  }

  /**
   * Remove product from wishlist
   */
  async removeFromWishlist(productId: string): Promise<void> {
    try {
      const response = await apiClient.delete(
        `${this.endpoint}/wishlist/${productId}`
      );

      if (!response.success) {
        throw new Error(response.message || 'Failed to remove from wishlist');
      }
    } catch (error) {
      console.error('Remove from wishlist error:', error);
      throw error;
    }
  }

  /**
   * Check if product is in wishlist
   */
  async isInWishlist(productId: string): Promise<boolean> {
    try {
      const response = await apiClient.get<{ inWishlist: boolean }>(
        `${this.endpoint}/wishlist/${productId}/status`
      );

      if (response.success && response.data) {
        return response.data.inWishlist;
      }

      return false;
    } catch (error) {
      console.error('Check wishlist status error:', error);
      return false;
    }
  }

  /**
   * Get product availability and stock
   */
  async getProductAvailability(productId: string, variantId?: string): Promise<{
    inStock: boolean;
    stockQuantity: number;
    estimatedDelivery?: string;
  }> {
    try {
      const params = variantId ? { variantId } : {};
      const response = await apiClient.get<{
        inStock: boolean;
        stockQuantity: number;
        estimatedDelivery?: string;
      }>(`${this.endpoint}/${productId}/availability`, { params });

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to check availability');
    } catch (error) {
      console.error('Get product availability error:', error);
      throw error;
    }
  }

  /**
   * Get recently viewed products
   */
  async getRecentlyViewed(limit: number = 10): Promise<Product[]> {
    try {
      const response = await apiClient.get<Product[]>(
        `${this.endpoint}/recently-viewed`,
        { params: { limit } }
      );

      if (response.success && response.data) {
        return response.data;
      }

      return []; // Return empty array if no recently viewed products
    } catch (error) {
      console.error('Get recently viewed error:', error);
      return [];
    }
  }
}

// Export singleton instance
export const productService = new ProductService();
export default productService;