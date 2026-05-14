#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$SCRIPT_DIR/../k8s/base"
NAMESPACE="agentos"
INFRA_NAMESPACE="agentos-infra"

echo "=== Installing AgentOS Infrastructure Layer ==="

if ! kubectl cluster-info &>/dev/null; then
    echo "ERROR: Cannot connect to Kubernetes cluster"
    exit 1
fi

echo "Creating namespaces..."
kubectl apply -f "$BASE_DIR/namespace.yaml"

echo "Applying infrastructure components..."
for manifest in postgres redis minio qdrant loki prometheus grafana jaeger otel-collector; do
    if [ -f "$BASE_DIR/$manifest.yaml" ]; then
        echo "  - $manifest"
        kubectl apply -f "$BASE_DIR/$manifest.yaml" -n $INFRA_NAMESPACE
    else
        echo "  WARNING: $manifest.yaml not found, skipping"
    fi
done

echo "Waiting for infrastructure pods..."
for app in postgres redis minio qdrant loki prometheus grafana; do
    echo "  Waiting for $app..."
    kubectl wait --for=condition=ready pod -l app=$app -n $INFRA_NAMESPACE --timeout=180s 2>/dev/null || echo "  WARNING: $app not ready within timeout"
done

echo "Applying agentos config and secrets..."
kubectl apply -f "$BASE_DIR/config.yaml" -n $NAMESPACE

echo "Applying agentos server deployment..."
kubectl apply -f "$BASE_DIR/server.yaml" -n $NAMESPACE

echo "Waiting for agentos-server..."
kubectl wait --for=condition=ready pod -l app=agentos-server -n $NAMESPACE --timeout=300s 2>/dev/null || echo "  WARNING: agentos-server not ready within timeout"

echo ""
echo "=== AgentOS Namespace ==="
kubectl get pods -n $NAMESPACE
echo ""
echo "=== AgentOS-Infra Namespace ==="
kubectl get pods -n $INFRA_NAMESPACE
echo ""
echo "=== Installation complete ==="