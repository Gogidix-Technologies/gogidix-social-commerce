import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { CartItem, CartSummary } from '../../types/cart';

interface CartState {
  items: CartItem[];
  totalItems: number;
  subtotal: number;
  tax: number;
  shipping: number;
  total: number;
  loading: boolean;
  error: string | null;
}

const initialState: CartState = {
  items: [],
  totalItems: 0,
  subtotal: 0,
  tax: 0,
  shipping: 0,
  total: 0,
  loading: false,
  error: null,
};

const calculateTotals = (items: CartItem[]): CartSummary => {
  const subtotal = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  const tax = subtotal * 0.08; // 8% tax rate
  const shipping = subtotal > 50 ? 0 : 10; // Free shipping over $50
  const total = subtotal + tax + shipping;
  const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);

  return {
    subtotal,
    tax,
    shipping,
    total,
    totalItems,
  };
};

const cartSlice = createSlice({
  name: 'cart',
  initialState,
  reducers: {
    addToCart: (state, action: PayloadAction<CartItem>) => {
      const existingItem = state.items.find(item => 
        item.productId === action.payload.productId && 
        item.variantId === action.payload.variantId
      );

      if (existingItem) {
        existingItem.quantity += action.payload.quantity;
      } else {
        state.items.push(action.payload);
      }

      const totals = calculateTotals(state.items);
      Object.assign(state, totals);
    },
    updateQuantity: (state, action: PayloadAction<{ productId: string; variantId?: string; quantity: number }>) => {
      const item = state.items.find(item => 
        item.productId === action.payload.productId && 
        item.variantId === action.payload.variantId
      );

      if (item) {
        item.quantity = action.payload.quantity;
        const totals = calculateTotals(state.items);
        Object.assign(state, totals);
      }
    },
    removeFromCart: (state, action: PayloadAction<{ productId: string; variantId?: string }>) => {
      state.items = state.items.filter(item => 
        !(item.productId === action.payload.productId && 
          item.variantId === action.payload.variantId)
      );

      const totals = calculateTotals(state.items);
      Object.assign(state, totals);
    },
    clearCart: (state) => {
      state.items = [];
      state.totalItems = 0;
      state.subtotal = 0;
      state.tax = 0;
      state.shipping = 0;
      state.total = 0;
    },
    setCartLoading: (state, action: PayloadAction<boolean>) => {
      state.loading = action.payload;
    },
    setCartError: (state, action: PayloadAction<string | null>) => {
      state.error = action.payload;
    },
  },
});

export const {
  addToCart,
  updateQuantity,
  removeFromCart,
  clearCart,
  setCartLoading,
  setCartError,
} = cartSlice.actions;

export default cartSlice.reducer;