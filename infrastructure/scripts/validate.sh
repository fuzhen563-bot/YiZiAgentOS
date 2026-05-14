#!/bin/bash
set -euo pipefail

echo "=== AgentOS Phase 1 & 2 Validation ==="
echo ""

PASS=0
FAIL=0

check() {
    local desc="$1"
    local cmd="$2"
    if eval "$cmd" &>/dev/null; then
        echo "  ✅ $desc"
        PASS=$((PASS + 1))
    else
        echo "  ❌ $desc"
        FAIL=$((FAIL + 1))
    fi
}

echo "--- Kubernetes ---"
check "K8s cluster reachable" "kubectl cluster-info"
check "Namespace agentos exists" "kubectl get ns agentos"
check "Namespace agentos-infra exists" "kubectl get ns agentos-infra"

echo ""
echo "--- Infrastructure Pods ---"
for app in postgres redis minio qdrant loki prometheus grafana; do
    check "Pod $app is Running" "kubectl get pod -n agentos-infra -l app=$app -o jsonpath='{.items[0].status.phase}' 2>/dev/null | grep -q Running"
done

echo ""
echo "--- PostgreSQL ---"
check "PostgreSQL is ready" "kubectl exec -n agentos-infra deploy/postgres -- pg_isready -U agentos 2>/dev/null"

echo ""
echo "--- Redis ---"
check "Redis is ready" "kubectl exec -n agentos-infra deploy/redis -- redis-cli ping 2>/dev/null | grep -q PONG"

echo ""
echo "--- Prometheus ---"
check "Prometheus endpoint responds" "kubectl run -n agentos-infra test-pm --image=curlimages/curl --restart=Never -- curl -s http://prometheus:9090/-/healthy 2>/dev/null"

echo ""
echo "--- Grafana ---"
check "Grafana API responds" "kubectl run -n agentos-infra test-gf --image=curlimages/curl --restart=Never -- curl -s http://grafana:3000/api/health 2>/dev/null"

echo ""
echo "--- Jaeger ---"
check "Jaeger collector is Running" "kubectl get pod -n agentos-infra -l app=jaeger-collector -o jsonpath='{.items[0].status.phase}' 2>/dev/null | grep -q Running"
check "Jaeger query is Running" "kubectl get pod -n agentos-infra -l app=jaeger-query -o jsonpath='{.items[0].status.phase}' 2>/dev/null | grep -q Running"

echo ""
echo "--- OTel Collector ---"
check "OTel Collector is Running" "kubectl get pod -n agentos-infra -l app=otel-collector -o jsonpath='{.items[0].status.phase}' 2>/dev/null | grep -q Running"

echo ""
echo "--- Network Policy ---"
check "NetworkPolicy agentos-isolation exists" "kubectl get networkpolicy -n agentos agentos-isolation &>/dev/null"
check "NetworkPolicy agentos-infra-isolation exists" "kubectl get networkpolicy -n agentos-infra agentos-infra-isolation &>/dev/null"

echo ""
echo "--- Database Migrations ---"
check "V0__enable_extensions.sql exists" "test -f infrastructure/db/migration/V0__enable_extensions.sql"
check "V1__initial_schema.sql exists" "test -f infrastructure/db/migration/V1__initial_schema.sql"
check "V2__audit_and_compliance.sql exists" "test -f infrastructure/db/migration/V2__audit_and_compliance.sql"
check "V3__performance_monitoring.sql exists" "test -f infrastructure/db/migration/V3__performance_monitoring.sql"

echo ""
echo "--- Monitoring Config ---"
check "logback-spring.xml exists" "test -f infrastructure/monitoring/logback-spring.xml"
check "prometheus-alerts.yml exists" "test -f infrastructure/monitoring/prometheus-alerts.yml"
check "agentos-overview.json exists" "test -f infrastructure/monitoring/grafana-dashboards/agentos-overview.json"
check "SIEM_INTEGRATION.md exists" "test -f infrastructure/monitoring/SIEM_INTEGRATION.md"

echo ""
echo "=========================================="
echo "Results: $PASS passed, $FAIL failed"
echo "=========================================="

if [ $FAIL -gt 0 ]; then
    exit 1
fi