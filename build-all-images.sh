#!/bin/bash

echo "=== Building Docker Images ==="
echo "Date: $(date)"
echo ""

SERVICES=(
    "service-registry"
    "api-gateway"
    "analytics-service"
    "commission-service"
    "marketplace"
    "multi-currency-service"
    "subscription-service"
)

for service in "${SERVICES[@]}"; do
    if [ -d "$service" ] && [ -f "$service/Dockerfile" ]; then
        echo "Building $service..."
        docker build -t socialcommerce/$service:latest ./$service
        if [ $? -eq 0 ]; then
            echo "✅ Successfully built $service"
        else
            echo "❌ Failed to build $service"
        fi
        echo ""
    fi
done

echo "=== Build Complete ==="
docker images | grep socialcommerce
