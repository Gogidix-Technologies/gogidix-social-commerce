// Week 11 Day 3: Multi-Currency Checkout Implementation

import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { useCurrency } from '../multi-currency/CurrencyContext';
import { useCart } from '../context/CartContext';
import { CurrencySelector } from '../multi-currency/CurrencySelector';
import { MultiCurrencyPrice } from '../multi-currency/MultiCurrencyPrice';

interface CheckoutSummary {
  subtotal: number;
  shipping: number;
  tax: number;
  total: number;
  originalCurrency: string;
  displayCurrency: string;
  exchangeRate: number;
}

export const MultiCurrencyCheckout: React.FC = () => {
  const { selectedCurrency, convertPrice, formatPrice } = useCurrency();
  const { items, subtotal, getCartTotal } = useCart();
  const [checkoutSummary, setCheckoutSummary] = useState<CheckoutSummary | null>(null);
  const [isProcessing, setIsProcessing] = useState(false);
  const [isCalculating, setIsCalculating] = useState(false);

  useEffect(() => {
    calculateCheckoutSummary();
  }, [selectedCurrency, items]);

  const calculateCheckoutSummary = async () => {
    setIsCalculating(true);
    try {
      // Base calculations in USD
      const baseSubtotal = getCartTotal();
      const baseShipping = calculateShipping(baseSubtotal);
      const baseTax = calculateTax(baseSubtotal + baseShipping);
      const baseTotal = baseSubtotal + baseShipping + baseTax;
      
      // Convert to display currency
      const [convertedSubtotal, convertedShipping, convertedTax, convertedTotal] = 
        await Promise.all([
          convertPrice(baseSubtotal, 'USD'),
          convertPrice(baseShipping, 'USD'),
          convertPrice(baseTax, 'USD'),
          convertPrice(baseTotal, 'USD'),
        ]);

      setCheckoutSummary({
        subtotal: convertedSubtotal,
        shipping: convertedShipping,
        tax: convertedTax,
        total: convertedTotal,
        originalCurrency: 'USD',
        displayCurrency: selectedCurrency,
        exchangeRate: convertedTotal / baseTotal,
      });
    } catch (error) {
      console.error('Failed to calculate checkout summary:', error);
      Alert.alert('Error', 'Failed to calculate prices. Please try again.');
    } finally {
      setIsCalculating(false);
    }
  };

  const calculateShipping = (subtotal: number): number => {
    // Example shipping calculation
    if (subtotal > 100) return 0;
    if (subtotal > 50) return 5;
    return 10;
  };

  const calculateTax = (amount: number): number => {
    // Example tax calculation (8%)
    return amount * 0.08;
  };

  const handleCheckout = async () => {
    if (!checkoutSummary) return;
    
    setIsProcessing(true);
    try {
      // Prepare order data with currency information
      const orderData = {
        items: items.map(item => ({
          ...item,
          price_original: item.price,
          price_display: convertPrice(item.price, item.currency || 'USD'),
          currency_original: item.currency || 'USD',
        })),
        currency: {
          order_currency: selectedCurrency,
          base_currency: 'USD',
          exchange_rate: checkoutSummary.exchangeRate,
          rate_timestamp: new Date().toISOString(),
        },
        totals: {
          subtotal_display: checkoutSummary.subtotal,
          shipping_display: checkoutSummary.shipping,
          tax_display: checkoutSummary.tax,
          total_display: checkoutSummary.total,
          total_base: checkoutSummary.total / checkoutSummary.exchangeRate,
        },
      };

      // Process order
      const result = await checkoutAPI.processOrder(orderData);
      
      if (result.success) {
        // Navigate to confirmation
        navigation.navigate('OrderConfirmation', { orderId: result.orderId });
      } else {
        Alert.alert('Checkout Failed', result.message);
      }
    } catch (error) {
      console.error('Checkout error:', error);
      Alert.alert('Error', 'Failed to process order. Please try again.');
    } finally {
      setIsProcessing(false);
    }
  };

  if (isCalculating || !checkoutSummary) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#3498db" />
        <Text style={styles.loadingText}>Calculating prices...</Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Checkout</Text>
        <CurrencySelector />
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Order Summary</Text>
        
        {items.map((item) => (
          <View key={item.id} style={styles.itemRow}>
            <View style={styles.itemInfo}>
              <Text style={styles.itemName}>{item.name}</Text>
              <Text style={styles.itemQuantity}>Qty: {item.quantity}</Text>
            </View>
            <MultiCurrencyPrice
              amount={item.price}
              baseCurrency={item.currency || 'USD'}
              showComparison={false}
              fontSize="medium"
            />
          </View>
        ))}
      </View>

      <View style={styles.section}>
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>Subtotal</Text>
          <Text style={styles.summaryValue}>
            {formatPrice(checkoutSummary.subtotal)}
          </Text>
        </View>
        
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>Shipping</Text>
          <Text style={styles.summaryValue}>
            {formatPrice(checkoutSummary.shipping)}
          </Text>
        </View>
        
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>Tax</Text>
          <Text style={styles.summaryValue}>
            {formatPrice(checkoutSummary.tax)}
          </Text>
        </View>
        
        <View style={[styles.summaryRow, styles.totalRow]}>
          <Text style={styles.totalLabel}>Total</Text>
          <Text style={styles.totalValue}>
            {formatPrice(checkoutSummary.total)}
          </Text>
        </View>
      </View>

      <View style={styles.rateInfo}>
        <Text style={styles.rateText}>
          Exchange rate: 1 {checkoutSummary.originalCurrency} = {checkoutSummary.exchangeRate.toFixed(4)} {checkoutSummary.displayCurrency}
        </Text>
        <Text style={styles.rateNote}>
          Final amount will be charged in {selectedCurrency}
        </Text>
      </View>

      <TouchableOpacity
        style={[styles.checkoutButton, isProcessing && styles.disabledButton]}
        onPress={handleCheckout}
        disabled={isProcessing}
      >
        {isProcessing ? (
          <ActivityIndicator color="#ffffff" />
        ) : (
          <Text style={styles.checkoutButtonText}>
            Pay {formatPrice(checkoutSummary.total)}
          </Text>
        )}
      </TouchableOpacity>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 10,
    color: '#95a5a6',
    fontSize: 16,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 20,
    backgroundColor: '#ffffff',
    borderBottomWidth: 1,
    borderBottomColor: '#e0e0e0',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#2c3e50',
  },
  section: {
    backgroundColor: '#ffffff',
    marginTop: 10,
    paddingVertical: 15,
    borderBottomWidth: 1,
    borderBottomColor: '#e0e0e0',
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#2c3e50',
    paddingHorizontal: 20,
    marginBottom: 10,
  },
  itemRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 10,
  },
  itemInfo: {
    flex: 1,
  },
  itemName: {
    fontSize: 16,
    color: '#2c3e50',
  },
  itemQuantity: {
    fontSize: 14,
    color: '#95a5a6',
    marginTop: 2,
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingVertical: 8,
  },
  summaryLabel: {
    fontSize: 16,
    color: '#7f8c8d',
  },
  summaryValue: {
    fontSize: 16,
    color: '#2c3e50',
  },
  totalRow: {
    borderTopWidth: 1,
    borderTopColor: '#e0e0e0',
    marginTop: 10,
    paddingTop: 15,
  },
  totalLabel: {
    fontSize: 18,
    fontWeight: '600',
    color: '#2c3e50',
  },
  totalValue: {
    fontSize: 18,
    fontWeight: '600',
    color: '#27ae60',
  },
  rateInfo: {
    backgroundColor: '#f8f9fa',
    padding: 15,
    margin: 10,
    borderRadius: 8,
  },
  rateText: {
    fontSize: 14,
    color: '#7f8c8d',
    textAlign: 'center',
  },
  rateNote: {
    fontSize: 12,
    color: '#95a5a6',
    textAlign: 'center',
    marginTop: 5,
  },
  checkoutButton: {
    backgroundColor: '#3498db',
    margin: 20,
    padding: 16,
    borderRadius: 10,
    alignItems: 'center',
  },
  disabledButton: {
    backgroundColor: '#95a5a6',
  },
  checkoutButtonText: {
    color: '#ffffff',
    fontSize: 18,
    fontWeight: '600',
  },
});