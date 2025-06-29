#!/bin/bash

echo "🐙 GITHUB REPOSITORY PREPARATION"
echo "================================"

# This script prepares clean code for pushing to GitHub
# Run this after the staging deployment preparation

BASE_PATH="/mnt/c/Users/frich/Desktop/Exalt-Application-Limited"

# Function to clean and prepare repository
clean_repo() {
    local repo_path="$1"
    local repo_name="$2"
    
    echo "🧹 Cleaning $repo_name repository..."
    
    cd "$repo_path" 2>/dev/null || return 1
    
    # Remove build artifacts and temporary files
    find . -name "target" -type d -exec rm -rf {} + 2>/dev/null
    find . -name "node_modules" -type d -exec rm -rf {} + 2>/dev/null
    find . -name "*.log" -type f -delete 2>/dev/null
    find . -name "*.class" -type f -delete 2>/dev/null
    find . -name ".DS_Store" -type f -delete 2>/dev/null
    
    # Keep only essential files for deployment
    echo "✅ Cleaned $repo_name"
}

# Clean existing repositories
clean_repo "$BASE_PATH/shared-infrastructure" "shared-infrastructure"
clean_repo "$BASE_PATH/shared-libraries" "shared-libraries" 
clean_repo "$BASE_PATH/centralized-dashboard" "centralized-dashboard"
clean_repo "$BASE_PATH/Exalt-Application-Limited/social-ecommerce-ecosystem/central-configuration" "central-configuration"
clean_repo "$BASE_PATH/Exalt-Application-Limited/social-ecommerce-ecosystem/social-commerce" "social-commerce"
clean_repo "$BASE_PATH/Exalt-Application-Limited/social-ecommerce-ecosystem/warehousing" "warehousing"

echo ""
echo "🎯 ALL REPOSITORIES CLEANED AND READY FOR PUSH"
echo "Next: Push to GitHub with green CI/CD pipelines"
