# CI/CD Pipeline Guide

## Date: Sun Jun  8 16:57:14 IST 2025

## Overview

Complete CI/CD pipeline implementation using GitHub Actions for the social-commerce ecosystem.

## Pipeline Structure

### 1. Main CI/CD Pipeline (main-cicd.yml)
- **Triggers**: Push to main/develop, Pull requests
- **Stages**:
  1. Code Quality Check
  2. Build and Test (Parallel)
  3. Security Scan
  4. Docker Build
  5. Deploy (Dev/Prod)

### 2. Service-Specific Workflow
- **Triggers**: Changes to specific services
- **Benefits**: Faster feedback, resource optimization

### 3. Pull Request Checks
- **Triggers**: PR open/update
- **Checks**: Lint, Unit tests, Integration tests

### 4. Release Workflow
- **Triggers**: Version tags (v*)
- **Actions**: Create release, deploy to production

### 5. Monitoring Workflow
- **Triggers**: Scheduled (every 6 hours)
- **Checks**: Health, Performance

## Setup Instructions

### 1. GitHub Configuration

#### Secrets
```
GITHUB_TOKEN          # Automatically provided
SONAR_TOKEN          # SonarQube token
DOCKER_USERNAME      # Docker Hub username
DOCKER_PASSWORD      # Docker Hub password
KUBECONFIG          # Kubernetes config
```

#### Environments
- **development**: Auto-deploy from develop branch
- **production**: Manual approval required

### 2. Branch Protection

```bash
# Main branch
- Require PR reviews: 2
- Require status checks: build-test, security-scan
- Require branches up to date
- Include administrators

# Develop branch
- Require PR reviews: 1
- Require status checks: build-test
```

## Usage

### 1. Development Workflow
```bash
# Create feature branch
git checkout -b feature/new-feature

# Make changes and push
git add .
git commit -m "feat: add new feature"
git push origin feature/new-feature

# Create PR - triggers PR checks
# Merge to develop - triggers dev deployment
```

### 2. Release Workflow
```bash
# Create release branch
git checkout -b release/1.2.0

# Update version and push
mvn versions:set -DnewVersion=1.2.0
git commit -am "chore: bump version to 1.2.0"
git push origin release/1.2.0

# Merge to main and tag
git checkout main
git merge release/1.2.0
git tag v1.2.0
git push origin main --tags
```

### 3. Hotfix Workflow
```bash
# Create hotfix from main
git checkout -b hotfix/critical-fix main

# Fix and push
git commit -am "fix: critical issue"
git push origin hotfix/critical-fix

# Merge to main and develop
```

## Quality Gates

### 1. Code Coverage
- Minimum: 70%
- Target: 80%
- Enforcement: PR blocks

### 2. Security Scanning
- Tool: Trivy
- CVE threshold: High
- License check: Enabled

### 3. Performance
- Build time: < 10 minutes
- Test time: < 5 minutes
- Deploy time: < 5 minutes

## Monitoring

### 1. Build Status
- Dashboard: GitHub Actions tab
- Notifications: Email, Slack

### 2. Deployment Status
- Environments tab in GitHub
- Rollback capability

### 3. Service Health
- Automated health checks
- Alert on failure

## Best Practices

1. **Commit Messages**
   - Use conventional commits
   - Include ticket numbers
   - Clear descriptions

2. **Testing**
   - Write tests before merge
   - Maintain coverage
   - Include integration tests

3. **Security**
   - Never commit secrets
   - Use GitHub Secrets
   - Regular dependency updates

4. **Performance**
   - Cache dependencies
   - Parallel jobs
   - Optimize Docker layers

## Troubleshooting

### Build Failures
1. Check logs in Actions tab
2. Verify dependencies
3. Run locally first

### Deployment Issues
1. Check environment secrets
2. Verify permissions
3. Review deployment logs

### Performance Problems
1. Analyze job timings
2. Optimize parallelization
3. Review cache usage

## Next Steps

1. **Integrate SonarQube** for code quality
2. **Add E2E tests** with Cypress/Selenium
3. **Implement GitOps** with ArgoCD
4. **Add APM** with DataDog/NewRelic
5. **Create dashboards** for metrics

The CI/CD pipeline is now ready for production use!
