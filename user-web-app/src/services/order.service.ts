import { apiClient, API_CONFIG, PaginatedApiResponse } from './api.ts';
import { 
  Order, 
  OrderItem, 
  OrderStatus, 
  Address, 
  PaymentMethod,
  PaymentType 
} from '../types/index';

/**
 * Order Service
 * Connects to the order-service backend using standardized com.exalt APIs
 */

export interface CreateOrderRequest {
  items: Array<{
    productId: string;
    variantId?: string;
    quantity: number;
    unitPrice: number;
  }>;
  shippingAddress: Omit<Address, 'id' | 'userId'>;
  billingAddress: Omit<Address, 'id' | 'userId'>;
  paymentMethod: {
    type: PaymentType;
    cardToken?: string;
    paypalToken?: string;
  };
  couponCode?: string;
  notes?: string;
  subscribeNewsletter?: boolean;
}

export interface CreateOrderResponse {
  order: Order;
  paymentIntentId?: string;
  paymentUrl?: string;
  requiresAction?: boolean;
}

export interface OrderFilters {
  status?: OrderStatus[];
  dateFrom?: string;
  dateTo?: string;
  minAmount?: number;
  maxAmount?: number;
}

export interface OrderTrackingInfo {
  orderId: string;
  trackingNumber?: string;
  carrier?: string;
  status: OrderStatus;
  estimatedDelivery?: string;
  trackingHistory: Array<{
    date: string;
    status: string;
    location?: string;
    description: string;
  }>;
}

export interface ShippingCalculationRequest {
  items: Array<{
    productId: string;
    variantId?: string;
    quantity: number;
  }>;
  shippingAddress: Omit<Address, 'id' | 'userId'>;
}

export interface ShippingCalculationResponse {
  options: Array<{
    id: string;
    name: string;
    description: string;
    cost: number;
    estimatedDays: number;
    carrier: string;
  }>;
}

export interface OrderInvoice {
  orderId: string;
  invoiceNumber: string;
  invoiceUrl: string;
  generatedAt: string;
}

export interface ReturnRequest {
  orderId: string;
  items: Array<{
    orderItemId: string;
    quantity: number;
    reason: string;
  }>;
  reason: string;
  description?: string;
  images?: string[];
}

export interface ReturnResponse {
  returnId: string;
  returnNumber: string;
  status: 'pending' | 'approved' | 'rejected' | 'processed';
  estimatedRefund: number;
}

export interface CancelOrderRequest {
  orderId: string;
  reason: string;
  description?: string;
}

/**
 * OrderService class for handling all order-related operations
 */
class OrderService {
  private readonly endpoint = API_CONFIG.ENDPOINTS.ORDERS;

  /**
   * Create a new order
   */
  async createOrder(orderData: CreateOrderRequest): Promise<CreateOrderResponse> {
    try {
      const response = await apiClient.post<CreateOrderResponse>(
        `${this.endpoint}`,
        orderData
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to create order');
    } catch (error) {
      console.error('Create order error:', error);
      throw error;
    }
  }

  /**
   * Get user's orders with pagination and filtering
   */
  async getOrders(
    page: number = 1,
    limit: number = 10,
    filters?: OrderFilters
  ): Promise<PaginatedApiResponse<Order>> {
    try {
      const params = {
        page,
        limit,
        ...filters
      };

      const response = await apiClient.get<Order[]>(
        `${this.endpoint}`,
        { params }
      );

      return response as PaginatedApiResponse<Order>;
    } catch (error) {
      console.error('Get orders error:', error);
      throw error;
    }
  }

  /**
   * Get order by ID
   */
  async getOrderById(orderId: string): Promise<Order> {
    try {
      const response = await apiClient.get<Order>(
        `${this.endpoint}/${orderId}`
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Order not found');
    } catch (error) {
      console.error('Get order by ID error:', error);
      throw error;
    }
  }

  /**
   * Get order by order number
   */
  async getOrderByNumber(orderNumber: string): Promise<Order> {
    try {
      const response = await apiClient.get<Order>(
        `${this.endpoint}/number/${orderNumber}`
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Order not found');
    } catch (error) {
      console.error('Get order by number error:', error);
      throw error;
    }
  }

  /**
   * Calculate shipping costs for items
   */
  async calculateShipping(data: ShippingCalculationRequest): Promise<ShippingCalculationResponse> {
    try {
      const response = await apiClient.post<ShippingCalculationResponse>(
        `${this.endpoint}/shipping/calculate`,
        data
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to calculate shipping');
    } catch (error) {
      console.error('Calculate shipping error:', error);
      throw error;
    }
  }

  /**
   * Apply coupon code to cart
   */
  async applyCoupon(couponCode: string, cartTotal: number): Promise<{
    valid: boolean;
    discountAmount: number;
    discountPercentage?: number;
    message?: string;
  }> {
    try {
      const response = await apiClient.post<{
        valid: boolean;
        discountAmount: number;
        discountPercentage?: number;
        message?: string;
      }>(`${this.endpoint}/coupons/validate`, {
        couponCode,
        cartTotal
      });

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to validate coupon');
    } catch (error) {
      console.error('Apply coupon error:', error);
      throw error;
    }
  }

  /**
   * Get order tracking information
   */
  async getOrderTracking(orderId: string): Promise<OrderTrackingInfo> {
    try {
      const response = await apiClient.get<OrderTrackingInfo>(
        `${this.endpoint}/${orderId}/tracking`
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Tracking information not available');
    } catch (error) {
      console.error('Get order tracking error:', error);
      throw error;
    }
  }

  /**
   * Cancel an order
   */
  async cancelOrder(data: CancelOrderRequest): Promise<void> {
    try {
      const response = await apiClient.post(
        `${this.endpoint}/${data.orderId}/cancel`,
        {
          reason: data.reason,
          description: data.description
        }
      );

      if (!response.success) {
        throw new Error(response.message || 'Failed to cancel order');
      }
    } catch (error) {
      console.error('Cancel order error:', error);
      throw error;
    }
  }

  /**
   * Request order return/refund
   */
  async requestReturn(data: ReturnRequest): Promise<ReturnResponse> {
    try {
      const response = await apiClient.post<ReturnResponse>(
        `${this.endpoint}/${data.orderId}/returns`,
        data
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to request return');
    } catch (error) {
      console.error('Request return error:', error);
      throw error;
    }
  }

  /**
   * Get order invoice
   */
  async getOrderInvoice(orderId: string): Promise<OrderInvoice> {
    try {
      const response = await apiClient.get<OrderInvoice>(
        `${this.endpoint}/${orderId}/invoice`
      );

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Invoice not available');
    } catch (error) {
      console.error('Get order invoice error:', error);
      throw error;
    }
  }

  /**
   * Download order invoice as PDF
   */
  async downloadInvoice(orderId: string): Promise<Blob> {
    try {
      const response = await apiClient.get(
        `${this.endpoint}/${orderId}/invoice/download`,
        {
          responseType: 'blob'
        }
      );

      return response.data;
    } catch (error) {
      console.error('Download invoice error:', error);
      throw error;
    }
  }

  /**
   * Confirm order delivery (for cash on delivery)
   */
  async confirmDelivery(orderId: string): Promise<void> {
    try {
      const response = await apiClient.post(
        `${this.endpoint}/${orderId}/confirm-delivery`
      );

      if (!response.success) {
        throw new Error(response.message || 'Failed to confirm delivery');
      }
    } catch (error) {
      console.error('Confirm delivery error:', error);
      throw error;
    }
  }

  /**
   * Reorder items from a previous order
   */
  async reorder(orderId: string): Promise<{
    availableItems: OrderItem[];
    unavailableItems: OrderItem[];
    cartId?: string;
  }> {
    try {
      const response = await apiClient.post<{
        availableItems: OrderItem[];
        unavailableItems: OrderItem[];
        cartId?: string;
      }>(`${this.endpoint}/${orderId}/reorder`);

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to reorder items');
    } catch (error) {
      console.error('Reorder error:', error);
      throw error;
    }
  }

  /**
   * Get order status history
   */
  async getOrderStatusHistory(orderId: string): Promise<Array<{
    status: OrderStatus;
    timestamp: string;
    note?: string;
    updatedBy?: string;
  }>> {
    try {
      const response = await apiClient.get<Array<{
        status: OrderStatus;
        timestamp: string;
        note?: string;
        updatedBy?: string;
      }>>(`${this.endpoint}/${orderId}/status-history`);

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to get status history');
    } catch (error) {
      console.error('Get order status history error:', error);
      throw error;
    }
  }

  /**
   * Update order shipping address (if order is still processing)
   */
  async updateShippingAddress(
    orderId: string, 
    shippingAddress: Omit<Address, 'id' | 'userId'>
  ): Promise<void> {
    try {
      const response = await apiClient.put(
        `${this.endpoint}/${orderId}/shipping-address`,
        { shippingAddress }
      );

      if (!response.success) {
        throw new Error(response.message || 'Failed to update shipping address');
      }
    } catch (error) {
      console.error('Update shipping address error:', error);
      throw error;
    }
  }

  /**
   * Get order summary for checkout
   */
  async getOrderSummary(items: Array<{
    productId: string;
    variantId?: string;
    quantity: number;
  }>, shippingAddress?: Omit<Address, 'id' | 'userId'>, couponCode?: string): Promise<{
    subtotal: number;
    shippingCost: number;
    tax: number;
    discountAmount: number;
    total: number;
    estimatedDelivery?: string;
  }> {
    try {
      const response = await apiClient.post<{
        subtotal: number;
        shippingCost: number;
        tax: number;
        discountAmount: number;
        total: number;
        estimatedDelivery?: string;
      }>(`${this.endpoint}/summary`, {
        items,
        shippingAddress,
        couponCode
      });

      if (response.success && response.data) {
        return response.data;
      }

      throw new Error(response.message || 'Failed to calculate order summary');
    } catch (error) {
      console.error('Get order summary error:', error);
      throw error;
    }
  }

  /**
   * Get recent orders for quick reorder
   */
  async getRecentOrders(limit: number = 5): Promise<Order[]> {
    try {
      const response = await apiClient.get<Order[]>(
        `${this.endpoint}/recent`,
        { params: { limit } }
      );

      if (response.success && response.data) {
        return response.data;
      }

      return [];
    } catch (error) {
      console.error('Get recent orders error:', error);
      return [];
    }
  }

  /**
   * Check if order can be cancelled
   */
  async canCancelOrder(orderId: string): Promise<{
    canCancel: boolean;
    reason?: string;
  }> {
    try {
      const response = await apiClient.get<{
        canCancel: boolean;
        reason?: string;
      }>(`${this.endpoint}/${orderId}/can-cancel`);

      if (response.success && response.data) {
        return response.data;
      }

      return { canCancel: false, reason: 'Unable to check cancellation status' };
    } catch (error) {
      console.error('Check can cancel order error:', error);
      return { canCancel: false, reason: 'Error checking cancellation status' };
    }
  }
}

// Export singleton instance
export const orderService = new OrderService();
export default orderService;