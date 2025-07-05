import React from 'react';
import { Box, Container, Typography, Grid, Button } from '@mui/material';
import { styled } from '@mui/material/styles';
import { ArrowForward } from '@mui/icons-material';
import { Link } from 'react-router-dom';
import HeroSection from '../components/home/HeroSection';
import CategoryGrid from '../components/home/CategoryGrid';
import FeaturedProducts from '../components/home/FeaturedProducts';
import PromotionalBanner from '../components/home/PromotionalBanner';
import TrendingProducts from '../components/home/TrendingProducts';
import VendorShowcase from '../components/home/VendorShowcase';
import NewsletterSignup from '../components/home/NewsletterSignup';

const Section = styled(Box)(({ theme }) => ({
  padding: theme.spacing(8, 0),
  [theme.breakpoints.down('sm')]: {
    padding: theme.spacing(4, 0),
  },
}));

const SectionTitle = styled(Typography)(({ theme }) => ({
  marginBottom: theme.spacing(4),
  fontWeight: 600,
  textAlign: 'center',
}));

const HomePage: React.FC = () => {
  return (
    <Box>
      {/* Hero Section */}
      <HeroSection />
      
      {/* Categories */}
      <Section sx={{ bgcolor: 'background.paper' }}>
        <Container>
          <SectionTitle variant="h4">Shop by Category</SectionTitle>
          <CategoryGrid />
        </Container>
      </Section>
      
      {/* Featured Products */}
      <Section>
        <Container>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
            <Typography variant="h4" fontWeight={600}>
              Featured Products
            </Typography>
            <Button 
              component={Link} 
              to="/products" 
              endIcon={<ArrowForward />}
              color="primary"
            >
              View All
            </Button>
          </Box>
          <FeaturedProducts />
        </Container>
      </Section>
      
      {/* Promotional Banner */}
      <PromotionalBanner />
      
      {/* Trending Products */}
      <Section>
        <Container>
          <SectionTitle variant="h4">Trending Now</SectionTitle>
          <TrendingProducts />
        </Container>
      </Section>
      
      {/* Top Vendors */}
      <Section sx={{ bgcolor: 'background.paper' }}>
        <Container>
          <SectionTitle variant="h4">Top Vendors</SectionTitle>
          <VendorShowcase />
        </Container>
      </Section>
      
      {/* Newsletter */}
      <Section>
        <Container>
          <NewsletterSignup />
        </Container>
      </Section>
    </Box>
  );
};

export default HomePage;