#!/bin/bash

echo "=== Social Commerce Deployment ==="
echo ""

# Check for .env file
if [ ! -f .env ]; then
    echo "❌ .env file not found. Creating from template..."
    cp .env.example .env
    echo "Please edit .env file with your configuration"
    exit 1
fi

# Load environment
export $(cat .env | xargs)

# Select environment
ENV=${1:-dev}

case $ENV in
    dev)
        echo "🚀 Starting development environment..."
        docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
        ;;
    prod)
        echo "🚀 Starting production environment..."
        docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
        ;;
    *)
        echo "Usage: ./deploy.sh [dev|prod]"
        exit 1
        ;;
esac

# Wait for services
echo ""
echo "⏳ Waiting for services to start..."
sleep 10

# Check health
echo ""
echo "🏥 Checking service health..."
docker-compose ps

echo ""
echo "✅ Deployment complete!"
echo ""
echo "Access points:"
echo "  - Eureka Dashboard: http://localhost:8761"
echo "  - API Gateway: http://localhost:8080"
echo "  - PostgreSQL: localhost:5432"
echo "  - Redis: localhost:6379"
