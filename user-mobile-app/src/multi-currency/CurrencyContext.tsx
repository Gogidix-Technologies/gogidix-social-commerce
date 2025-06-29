// Week 11: Multi-Currency Context for Social Commerce App

import React, { createContext, useContext, useState, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { currencyAPI } from '../api/currency';

interface CurrencyContextType {
  selectedCurrency: string;
  setSelectedCurrency: (currency: string) => Promise<void>;
  convertPrice: (amount: number, fromCurrency: string) => Promise<number>;
  formatPrice: (amount: number, currency?: string) => string;
  supportedCurrencies: Currency[];
  isLoading: boolean;
  error: string | null;
}

interface Currency {
  code: string;
  symbol: string;
  name: string;
  flag: string;
}

const CurrencyContext = createContext<CurrencyContextType | undefined>(undefined);

export const CurrencyProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [selectedCurrency, setSelectedCurrencyState] = useState('USD');
  const [supportedCurrencies] = useState<Currency[]>([
    { code: 'USD', symbol: '$', name: 'US Dollar', flag: '🇺🇸' },
    { code: 'EUR', symbol: '€', name: 'Euro', flag: '🇪🇺' },
    { code: 'GBP', symbol: '£', name: 'British Pound', flag: '🇬🇧' },
    { code: 'JPY', symbol: '¥', name: 'Japanese Yen', flag: '🇯🇵' },
    { code: 'CAD', symbol: 'C$', name: 'Canadian Dollar', flag: '🇨🇦' },
    { code: 'AUD', symbol: 'A$', name: 'Australian Dollar', flag: '🇦🇺' },
    { code: 'CHF', symbol: 'CHF', name: 'Swiss Franc', flag: '🇨🇭' },
    { code: 'CNY', symbol: '¥', name: 'Chinese Yuan', flag: '🇨🇳' },
    { code: 'SGD', symbol: 'S$', name: 'Singapore Dollar', flag: '🇸🇬' },
    { code: 'HKD', symbol: 'HK$', name: 'Hong Kong Dollar', flag: '🇭🇰' },
  ]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadUserCurrency();
  }, []);

  const loadUserCurrency = async () => {
    try {
      const saved = await AsyncStorage.getItem('user_currency');
      if (saved && supportedCurrencies.find(c => c.code === saved)) {
        setSelectedCurrencyState(saved);
      }
    } catch (err) {
      console.error('Failed to load currency preference:', err);
    }
  };

  const setSelectedCurrency = async (currency: string) => {
    try {
      setIsLoading(true);
      setError(null);
      
      await AsyncStorage.setItem('user_currency', currency);
      await currencyAPI.setUserPreference(currency);
      
      setSelectedCurrencyState(currency);
      
      // Analytics
      analytics.track('currency_changed', {
        from: selectedCurrency,
        to: currency,
      });
    } catch (err) {
      setError('Failed to update currency');
      console.error('Currency update error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const convertPrice = async (amount: number, fromCurrency: string): Promise<number> => {
    if (fromCurrency === selectedCurrency) return amount;
    
    try {
      const response = await currencyAPI.convert(amount, fromCurrency, selectedCurrency);
      return response.convertedAmount;
    } catch (err) {
      console.error('Conversion error:', err);
      return amount; // Fallback to original amount
    }
  };

  const formatPrice = (amount: number, currency?: string): string => {
    const curr = currency || selectedCurrency;
    const currencyInfo = supportedCurrencies.find(c => c.code === curr);
    
    if (!currencyInfo) return `${amount.toFixed(2)} ${curr}`;
    
    // Use Intl for proper formatting
    return new Intl.NumberFormat('en', {
      style: 'currency',
      currency: curr,
      currencyDisplay: 'symbol',
    }).format(amount);
  };

  const value: CurrencyContextType = {
    selectedCurrency,
    setSelectedCurrency,
    convertPrice,
    formatPrice,
    supportedCurrencies,
    isLoading,
    error,
  };

  return <CurrencyContext.Provider value={value}>{children}</CurrencyContext.Provider>;
};

export const useCurrency = (): CurrencyContextType => {
  const context = useContext(CurrencyContext);
  if (!context) {
    throw new Error('useCurrency must be used within CurrencyProvider');
  }
  return context;
};

// HOC for currency-aware components
export const withCurrency = <P extends object>(
  Component: React.ComponentType<P & { currency: CurrencyContextType }>
) => {
  return React.memo((props: P) => {
    const currency = useCurrency();
    return <Component {...props} currency={currency} />;
  });
};