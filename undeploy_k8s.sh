#!/bin/bash
#!/bin/bash

# ============================================
# Kubernetes Undeploy Script for Perpustakaan Microservices
# ============================================

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}🧹 Removing Perpustakaan Microservices from Kubernetes...${NC}\n"

# Delete in reverse order
echo -e "${YELLOW}[1/3] Removing API Gateway...${NC}"
kubectl delete -f k8s/gateway/api-gateway.yaml --ignore-not-found=true

echo -e "${YELLOW}[2/3] Removing Microservices...${NC}"
kubectl delete -f k8s/services/ --ignore-not-found=true

echo -e "${YELLOW}[3/3] Removing Infrastructure...${NC}"
kubectl delete -f k8s/infrastructure/ --ignore-not-found=true

echo -e "\n${GREEN}✅ All resources removed successfully!${NC}"
