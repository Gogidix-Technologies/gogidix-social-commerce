import { configureStore } from '@reduxjs/toolkit';
import { useDispatch, useSelector, TypedUseSelectorHook } from 'react-redux';

// Placeholder reducers - these would be implemented as needed
const initialState = {
  user: {
    isAuthenticated: false,
    profile: null,
    preferences: {
      currency: 'USD',
      language: 'en'
    }
  },
  cart: {
    items: [],
    total: 0
  },
  products: {
    items: [],
    filters: {},
    loading: false
  },
  orders: {
    items: [],
    loading: false
  }
};

// Simple reducer for now - would be split into separate slices in production
const rootReducer = (state = initialState, action: any) => {
  switch (action.type) {
    case 'SET_USER':
      return {
        ...state,
        user: {
          ...state.user,
          ...action.payload
        }
      };
    case 'ADD_TO_CART':
      return {
        ...state,
        cart: {
          ...state.cart,
          items: [...state.cart.items, action.payload]
        }
      };
    case 'SET_CURRENCY':
      return {
        ...state,
        user: {
          ...state.user,
          preferences: {
            ...state.user.preferences,
            currency: action.payload
          }
        }
      };
    default:
      return state;
  }
};

export const store = configureStore({
  reducer: rootReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST', 'persist/REHYDRATE'],
      },
    }),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

// Typed hooks
export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;