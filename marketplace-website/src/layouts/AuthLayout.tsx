import React from 'react';
import { Outlet, Link } from 'react-router-dom';
import { Box, Container, Typography, Paper } from '@mui/material';
import { styled } from '@mui/material/styles';

const AuthContainer = styled(Box)(({ theme }) => ({
  minHeight: '100vh',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  background: `linear-gradient(135deg, ${theme.palette.primary.light} 0%, ${theme.palette.primary.main} 100%)`,
  padding: theme.spacing(2),
}));

const AuthPaper = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(4),
  width: '100%',
  maxWidth: 480,
  borderRadius: theme.shape.borderRadius * 2,
  boxShadow: '0 8px 32px rgba(0, 0, 0, 0.1)',
  [theme.breakpoints.down('sm')]: {
    padding: theme.spacing(3),
  },
}));

const Logo = styled(Box)(({ theme }) => ({
  textAlign: 'center',
  marginBottom: theme.spacing(4),
  '& img': {
    height: 60,
    width: 'auto',
  },
}));

const AuthLayout: React.FC = () => {
  return (
    <AuthContainer>
      <Container maxWidth="sm">
        <AuthPaper elevation={0}>
          <Logo>
            <Link to="/">
              <Typography variant="h4" color="primary" fontWeight={700}>
                Exalt Marketplace
              </Typography>
            </Link>
          </Logo>
          
          <Outlet />
          
          <Box mt={4} textAlign="center">
            <Typography variant="caption" color="text.secondary">
              © {new Date().getFullYear()} Exalt Application Limited. All rights reserved.
            </Typography>
          </Box>
        </AuthPaper>
      </Container>
    </AuthContainer>
  );
};

export default AuthLayout;