import React from 'react';
import { Typography, Box } from '@mui/material';

const ContentManager = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Content Manager
      </Typography>
      <Typography variant="body1">
        Create and manage your social media content here.
      </Typography>
    </Box>
  );
};

export default ContentManager;