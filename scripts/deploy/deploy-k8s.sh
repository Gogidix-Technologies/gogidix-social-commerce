#!/bin/bash

ENVIRONMENT=$1
NAMESPACE="socialcommerce-$ENVIRONMENT"
VERSION=$2

echo "Deploying to Kubernetes - Environment: $ENVIRONMENT, Version: $VERSION"

# Create namespace if not exists
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Apply configurations
kubectl apply -f k8s/configmaps/ -n $NAMESPACE
kubectl apply -f k8s/secrets/ -n $NAMESPACE

# Deploy services
for service in k8s/deployments/*.yaml; do
    sed "s/latest/$VERSION/g" $service | kubectl apply -f - -n $NAMESPACE
done

# Wait for rollout
kubectl rollout status deployment --timeout=10m -n $NAMESPACE

echo "Deployment complete!"
