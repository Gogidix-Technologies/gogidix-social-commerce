export interface CartItem {
  id: string;
  productId: string;
  productName: string;
  productImage: string;
  variantId?: string;
  variantName?: string;
  price: number;
  salePrice?: number;
  quantity: number;
  vendorId: string;
  vendorName: string;
  attributes?: Record<string, string>;
}

export interface Cart {
  id: string;
  userId?: string;
  items: CartItem[];
  subtotal: number;
  tax: number;
  shipping: number;
  discount: number;
  total: number;
  couponCode?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CartSummary {
  subtotal: number;
  tax: number;
  shipping: number;
  total: number;
  totalItems: number;
}

export interface AddToCartRequest {
  productId: string;
  variantId?: string;
  quantity: number;
}

export interface UpdateCartItemRequest {
  itemId: string;
  quantity: number;
}

export interface ApplyCouponRequest {
  couponCode: string;
}

export interface CouponResponse {
  valid: boolean;
  discount: number;
  discountType: 'percentage' | 'fixed';
  message?: string;
}