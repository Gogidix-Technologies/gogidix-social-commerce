// Week 11 Day 4: Multi-Currency Order History Component

import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  RefreshControl,
} from 'react-native';
import Icon from 'react-native-vector-icons/MaterialIcons';
import { useCurrency } from '../multi-currency/CurrencyContext';
import { useOrders } from '../hooks/useOrders';

interface Order {
  id: string;
  orderNumber: string;
  date: string;
  status: string;
  currency: {
    original: string;
    base: string;
    order_currency: string;
    total_display: number;
    total_base: number;
    exchange_rate: number;
  };
  items: OrderItem[];
}

export const MultiCurrencyOrderHistory: React.FC = () => {
  const { selectedCurrency, convertPrice, formatPrice } = useCurrency();
  const { orders, loading, error, refetch } = useOrders();
  const [refreshing, setRefreshing] = useState(false);
  const [displayCurrency, setDisplayCurrency] = useState(selectedCurrency);

  const handleRefresh = async () => {
    setRefreshing(true);
    await refetch();
    setRefreshing(false);
  };

  const renderOrderItem = ({ item: order }: { item: Order }) => (
    <TouchableOpacity 
      style={styles.orderCard}
      onPress={() => navigation.navigate('OrderDetails', { orderId: order.id })}
    >
      <View style={styles.orderHeader}>
        <View>
          <Text style={styles.orderNumber}>Order #{order.orderNumber}</Text>
          <Text style={styles.orderDate}>{formatDate(order.date)}</Text>
        </View>
        <OrderStatusBadge status={order.status} />
      </View>
      
      <View style={styles.currencyInfo}>
        <View style={styles.priceDisplay}>
          <Text style={styles.priceLabel}>Total Paid</Text>
          <Text style={styles.originalPrice}>
            {formatPrice(order.currency.total_display, order.currency.order_currency)}
          </Text>
          
          {order.currency.order_currency !== selectedCurrency && (
            <CurrencyConverter 
              amount={order.currency.total_display}
              fromCurrency={order.currency.order_currency}
              toCurrency={selectedCurrency}
            />
          )}
        </View>
        
        <View style={styles.rateInfo}>
          <Text style={styles.rateText}>
            Rate: 1 {order.currency.base} = {order.currency.exchange_rate.toFixed(4)} {order.currency.order_currency}
          </Text>
          <Text style={styles.rateDate}>
            at {formatDateTime(order.date)}
          </Text>
        </View>
      </View>
      
      <View style={styles.itemsPreview}>
        <Text style={styles.itemsCount}>
          {order.items.length} item{order.items.length !== 1 ? 's' : ''}
        </Text>
        <Icon name="chevron-right" size={20} color="#bdc3c7" />
      </View>
    </TouchableOpacity>
  );

  if (loading && !refreshing) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#3498db" />
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.errorContainer}>
        <Text style={styles.errorText}>Failed to load orders</Text>
        <TouchableOpacity onPress={refetch} style={styles.retryButton}>
          <Text style={styles.retryText}>Retry</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Order History</Text>
        <TouchableOpacity 
          onPress={() => setDisplayCurrency(displayCurrency === selectedCurrency ? 'USD' : selectedCurrency)}
          style={styles.toggleButton}
        >
          <Text style={styles.toggleText}>
            Show in {displayCurrency === selectedCurrency ? 'USD' : selectedCurrency}
          </Text>
        </TouchableOpacity>
      </View>
      
      <FlatList
        data={orders}
        renderItem={renderOrderItem}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.listContainer}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={handleRefresh}
            colors={['#3498db']}
          />
        }
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Icon name="receipt" size={48} color="#bdc3c7" />
            <Text style={styles.emptyText}>No orders yet</Text>
          </View>
        }
      />
    </View>
  );
};

const OrderStatusBadge: React.FC<{ status: string }> = ({ status }) => {
  const statusConfig = {
    pending: { color: '#f39c12', icon: 'schedule' },
    confirmed: { color: '#3498db', icon: 'check-circle' },
    shipping: { color: '#9b59b6', icon: 'local-shipping' },
    delivered: { color: '#27ae60', icon: 'done-all' },
    cancelled: { color: '#e74c3c', icon: 'cancel' },
  };

  const config = statusConfig[status] || statusConfig.pending;

  return (
    <View style={[styles.statusBadge, { backgroundColor: config.color + '20' }]}>
      <Icon name={config.icon} size={14} color={config.color} />
      <Text style={[styles.statusText, { color: config.color }]}>
        {status.charAt(0).toUpperCase() + status.slice(1)}
      </Text>
    </View>
  );
};

const CurrencyConverter: React.FC<{
  amount: number;
  fromCurrency: string;
  toCurrency: string;
}> = ({ amount, fromCurrency, toCurrency }) => {
  const [converted, setConverted] = useState<number | null>(null);

  useEffect(() => {
    const convert = async () => {
      const result = await currencyAPI.convert(amount, fromCurrency, toCurrency);
      setConverted(result.convertedAmount);
    };
    convert();
  }, [amount, fromCurrency, toCurrency]);

  if (converted === null) {
    return <ActivityIndicator size="small" color="#3498db" />;
  }

  return (
    <Text style={styles.convertedPrice}>
      ≈ {formatPrice(converted, toCurrency)}
    </Text>
  );
};

const formatDate = (date: string): string => {
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
};

const formatDateTime = (date: string): string => {
  return new Date(date).toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
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
  toggleButton: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    backgroundColor: '#ecf0f1',
    borderRadius: 6,
  },
  toggleText: {
    fontSize: 14,
    color: '#3498db',
  },
  listContainer: {
    paddingVertical: 10,
  },
  orderCard: {
    backgroundColor: '#ffffff',
    marginHorizontal: 15,
    marginVertical: 5,
    borderRadius: 10,
    padding: 15,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  orderHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  orderNumber: {
    fontSize: 16,
    fontWeight: '600',
    color: '#2c3e50',
  },
  orderDate: {
    fontSize: 12,
    color: '#95a5a6',
    marginTop: 2,
  },
  statusBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  statusText: {
    fontSize: 12,
    marginLeft: 4,
    fontWeight: '500',
  },
  currencyInfo: {
    marginVertical: 10,
  },
  priceDisplay: {
    alignItems: 'flex-end',
  },
  priceLabel: {
    fontSize: 12,
    color: '#95a5a6',
  },
  originalPrice: {
    fontSize: 18,
    fontWeight: '600',
    color: '#2c3e50',
    marginTop: 2,
  },
  convertedPrice: {
    fontSize: 14,
    color: '#95a5a6',
    marginTop: 4,
  },
  rateInfo: {
    marginTop: 8,
    alignItems: 'flex-end',
  },
  rateText: {
    fontSize: 12,
    color: '#7f8c8d',
  },
  rateDate: {
    fontSize: 10,
    color: '#bdc3c7',
  },
  itemsPreview: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 10,
    paddingTop: 10,
    borderTopWidth: 1,
    borderTopColor: '#ecf0f1',
  },
  itemsCount: {
    fontSize: 14,
    color: '#7f8c8d',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  errorText: {
    fontSize: 16,
    color: '#e74c3c',
    marginBottom: 20,
  },
  retryButton: {
    backgroundColor: '#3498db',
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 6,
  },
  retryText: {
    color: '#ffffff',
    fontWeight: '600',
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 100,
  },
  emptyText: {
    fontSize: 16,
    color: '#95a5a6',
    marginTop: 10,
  },
});