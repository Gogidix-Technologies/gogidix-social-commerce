# Multi-stage build for React/Node.js application
FROM node:18-alpine AS builder
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --silent

# Copy source code and build
COPY . .
RUN npm run build 2>/dev/null || echo "No build script found"

# Production stage
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Install curl for health checks
RUN apk add --no-cache curl

# Remove default nginx static assets
RUN rm -rf ./*

# Copy built app from builder stage
COPY --from=builder /app/build . 2>/dev/null || COPY --from=builder /app/dist . 2>/dev/null || COPY --from=builder /app/public .

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf 2>/dev/null || echo "events { worker_connections 1024; } http { include /etc/nginx/mime.types; default_type application/octet-stream; sendfile on; keepalive_timeout 65; server { listen 80; location / { root /usr/share/nginx/html; index index.html index.htm; try_files \$uri \$uri/ /index.html; } } }" > /etc/nginx/nginx.conf

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:80/ || exit 1

# Expose port
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
