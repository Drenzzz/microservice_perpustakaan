#!/bin/bash

# ============================================
# Kubernetes Deployment Script for Perpustakaan Microservices
# ============================================

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}🚀 Deploying Perpustakaan Microservices to Kubernetes...${NC}\n"

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo -e "${RED}❌ kubectl not found. Please install kubectl first.${NC}"
    exit 1
fi

# Check cluster connection
echo -e "${YELLOW}[1/5] Checking Kubernetes cluster connection...${NC}"
if ! kubectl cluster-info &> /dev/null; then
    echo -e "${RED}❌ Cannot connect to Kubernetes cluster. Please check your kubeconfig.${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Connected to Kubernetes cluster${NC}\n"

# Build Docker images (optional, uncomment if needed)
# echo -e "${YELLOW}[1.5/5] Building Docker images...${NC}"
# docker build -t anggota-service:latest ./anggota
# docker build -t buku-service:latest ./buku
# docker build -t peminjaman-service:latest ./peminjaman
# docker build -t pengembalian-service:latest ./pengembalian
# docker build -t api-gateway:latest ./api-gateway
# echo -e "${GREEN}✅ Docker images built${NC}\n"

# Deploy Infrastructure
echo -e "${YELLOW}[2/5] Deploying Infrastructure (ConfigMap, RabbitMQ, Eureka)...${NC}"
kubectl apply -f k8s/infrastructure/01-configmap.yaml
kubectl apply -f k8s/infrastructure/02-rabbitmq.yaml
kubectl apply -f k8s/infrastructure/03-eureka.yaml
echo -e "${GREEN}✅ Infrastructure deployed${NC}\n"

# Wait for infrastructure to be ready
echo -e "${YELLOW}[3/5] Waiting for infrastructure to be ready...${NC}"
kubectl wait --for=condition=ready pod -l app=rabbitmq --timeout=120s || true
kubectl wait --for=condition=ready pod -l app=eureka-server --timeout=120s || true
echo -e "${GREEN}✅ Infrastructure is ready${NC}\n"

# Deploy Services
echo -e "${YELLOW}[4/5] Deploying Microservices...${NC}"
kubectl apply -f k8s/services/anggota-deployment.yaml
kubectl apply -f k8s/services/buku-deployment.yaml
kubectl apply -f k8s/services/peminjaman-deployment.yaml
kubectl apply -f k8s/services/pengembalian-deployment.yaml
echo -e "${GREEN}✅ Microservices deployed${NC}\n"

# Deploy API Gateway
echo -e "${YELLOW}[5/5] Deploying API Gateway...${NC}"
kubectl apply -f k8s/gateway/api-gateway.yaml
echo -e "${GREEN}✅ API Gateway deployed${NC}\n"

# Summary
echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}🎉 Deployment Complete!${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""
echo -e "${YELLOW}Access Points:${NC}"
echo -e "  📊 Eureka Dashboard: http://localhost:30761"
echo -e "  🐰 RabbitMQ UI:      http://localhost:31672 (admin/password)"
echo -e "  🌐 API Gateway:      http://localhost:30090"
echo ""
echo -e "${YELLOW}Useful Commands:${NC}"
echo -e "  kubectl get pods                    # Check pod status"
echo -e "  kubectl get services                # Check service endpoints"
echo -e "  kubectl logs -f <pod-name>          # View pod logs"
echo -e "  kubectl describe pod <pod-name>     # Debug pod issues"
echo ""
echo -e "${YELLOW}To delete all resources:${NC}"
echo -e "  ./undeploy_k8s.sh"
