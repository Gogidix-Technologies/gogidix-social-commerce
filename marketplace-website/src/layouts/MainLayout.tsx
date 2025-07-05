import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Box, Container } from '@mui/material';
import Header from '../components/layout/Header';
import Footer from '../components/layout/Footer';
import MobileNavigation from '../components/layout/MobileNavigation';
import CartDrawer from '../components/cart/CartDrawer';
import SearchDrawer from '../components/search/SearchDrawer';
import { useScrollTrigger } from '../hooks/useScrollTrigger';

const MainLayout: React.FC = () => {
  const [cartOpen, setCartOpen] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);
  const isScrolled = useScrollTrigger(100);

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Header 
        onCartOpen={() => setCartOpen(true)}
        onSearchOpen={() => setSearchOpen(true)}
        elevated={isScrolled}
      />
      
      <Box component="main" sx={{ flexGrow: 1, pt: { xs: 7, sm: 8 }, pb: { xs: 7, sm: 0 } }}>
        <Outlet />
      </Box>
      
      <Footer />
      
      {/* Mobile Bottom Navigation */}
      <MobileNavigation />
      
      {/* Cart Drawer */}
      <CartDrawer open={cartOpen} onClose={() => setCartOpen(false)} />
      
      {/* Search Drawer */}
      <SearchDrawer open={searchOpen} onClose={() => setSearchOpen(false)} />
    </Box>
  );
};

export default MainLayout;