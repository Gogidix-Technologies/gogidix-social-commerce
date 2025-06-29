import React from 'react';
import { StatusBar } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { Provider } from 'react-redux';
import { store } from './src/store';
import { CurrencyProvider } from './src/multi-currency/CurrencyContext';

// Screens (will be implemented)
import HomeScreen from './src/screens/HomeScreen';
import ProductsScreen from './src/screens/ProductsScreen';
import CartScreen from './src/screens/CartScreen';
import ProfileScreen from './src/screens/ProfileScreen';
import OrdersScreen from './src/screens/OrdersScreen';

// Multi-currency components (already implemented)
import MultiCurrencyCheckout from './src/checkout/MultiCurrencyCheckout';
import MultiCurrencyOrderHistory from './src/orders/MultiCurrencyOrderHistory';

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

const TabNavigator = () => {
  return (
    <Tab.Navigator
      screenOptions={{
        tabBarActiveTintColor: '#2196f3',
        tabBarInactiveTintColor: 'gray',
        tabBarStyle: {
          backgroundColor: 'white',
          borderTopWidth: 1,
          borderTopColor: '#e0e0e0',
        },
      }}
    >
      <Tab.Screen 
        name="Home" 
        component={HomeScreen}
        options={{
          tabBarLabel: 'Home',
          // tabBarIcon: ({ color, size }) => <HomeIcon color={color} size={size} />
        }}
      />
      <Tab.Screen 
        name="Products" 
        component={ProductsScreen}
        options={{
          tabBarLabel: 'Products',
          // tabBarIcon: ({ color, size }) => <ProductsIcon color={color} size={size} />
        }}
      />
      <Tab.Screen 
        name="Cart" 
        component={CartScreen}
        options={{
          tabBarLabel: 'Cart',
          // tabBarIcon: ({ color, size }) => <CartIcon color={color} size={size} />
        }}
      />
      <Tab.Screen 
        name="Profile" 
        component={ProfileScreen}
        options={{
          tabBarLabel: 'Profile',
          // tabBarIcon: ({ color, size }) => <ProfileIcon color={color} size={size} />
        }}
      />
    </Tab.Navigator>
  );
};

const App: React.FC = () => {
  return (
    <SafeAreaProvider>
      <Provider store={store}>
        <CurrencyProvider>
          <NavigationContainer>
            <StatusBar barStyle="dark-content" backgroundColor="#fff" />
            <Stack.Navigator>
              <Stack.Screen 
                name="Main" 
                component={TabNavigator}
                options={{ headerShown: false }}
              />
              <Stack.Screen 
                name="Orders" 
                component={OrdersScreen}
                options={{ title: 'My Orders' }}
              />
              <Stack.Screen 
                name="MultiCurrencyCheckout" 
                component={MultiCurrencyCheckout}
                options={{ title: 'Checkout' }}
              />
              <Stack.Screen 
                name="MultiCurrencyOrderHistory" 
                component={MultiCurrencyOrderHistory}
                options={{ title: 'Order History' }}
              />
            </Stack.Navigator>
          </NavigationContainer>
        </CurrencyProvider>
      </Provider>
    </SafeAreaProvider>
  );
};

export default App;