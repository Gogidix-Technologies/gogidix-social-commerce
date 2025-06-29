/**
 * Social Media Integration Service
 * 
 * Main application entry point for the Social Media Integration service
 * that connects the Social E-commerce platform with various social media platforms.
 */

const express = require('express');
const morgan = require('morgan');
const helmet = require('helmet');
const cors = require('cors');
const mongoose = require('mongoose');
const dotenv = require('dotenv');

// Load environment variables
dotenv.config();

// Initialize Express app
const app = express();
const port = process.env.PORT || 8080;

// Connect to MongoDB
mongoose.connect(process.env.MONGODB_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true,
  user: process.env.MONGODB_USERNAME,
  pass: process.env.MONGODB_PASSWORD,
  authSource: process.env.MONGODB_AUTH_SOURCE || 'admin',
})
.then(() => console.log('Connected to MongoDB'))
.catch(err => {
  console.error('MongoDB connection error:', err);
  process.exit(1);
});

// Middleware
app.use(morgan('dev'));
app.use(helmet());
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Routes
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'ok', service: 'social-media-integration' });
});

// Import route modules
const facebookRoutes = require('./routes/facebook');
const instagramRoutes = require('./routes/instagram');
const twitterRoutes = require('./routes/twitter');
const pinterestRoutes = require('./routes/pinterest');
const tiktokRoutes = require('./routes/tiktok');
const whatsappRoutes = require('./routes/whatsapp');

// Apply routes
app.use('/api/facebook', facebookRoutes);
app.use('/api/instagram', instagramRoutes);
app.use('/api/twitter', twitterRoutes);
app.use('/api/pinterest', pinterestRoutes);
app.use('/api/tiktok', tiktokRoutes);
app.use('/api/whatsapp', whatsappRoutes);

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({
    status: 'error',
    message: 'An internal server error occurred',
    error: process.env.NODE_ENV === 'development' ? err.message : undefined
  });
});

// Start server
if (process.env.NODE_ENV !== 'test') {
  app.listen(port, () => {
    console.log(`Social Media Integration Service listening on port ${port}`);
    console.log(`Environment: ${process.env.NODE_ENV}`);
  });
}

module.exports = app;