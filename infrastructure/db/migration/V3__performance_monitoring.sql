-- ============================================
-- PostgreSQL Performance Monitoring
-- Slow Query Log Configuration
-- ============================================

-- Enable slow query log (queries taking > 1 second)
ALTER SYSTEM SET log_min_duration_statement = '1s';

-- Enable query statistics collection
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- Create indexes for monitoring tables
CREATE INDEX IF NOT EXISTS idx_token_usage_created ON token_usage(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created ON audit_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_conversation_created ON messages(conversation_id, created_at);

-- ============================================
-- Slow Query Log Table
-- ============================================

CREATE TABLE IF NOT EXISTS slow_query_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    query_text TEXT NOT NULL,
    execution_time_ms INTEGER NOT NULL,
    rows_examined BIGINT,
    rows_returned BIGINT,
    user_id UUID,
    workspace_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_slow_query_log_created ON slow_query_log(created_at DESC);
CREATE INDEX idx_slow_query_log_execution_time ON slow_query_log(execution_time_ms DESC);

-- ============================================
-- Query Performance View
-- ============================================

CREATE OR REPLACE VIEW query_performance AS
SELECT
    substring(query, 1, 100) as query_prefix,
    calls,
    total_exec_time / 1000 as total_seconds,
    mean_exec_time as mean_ms,
    max_exec_time as max_ms,
    rows,
    shared_blks_hit,
    shared_blks_read,
    local_blks_hit,
    local_blks_read,
    temp_blks_read,
    temp_blks_written
FROM pg_stat_statements
WHERE calls > 10
ORDER BY total_exec_time DESC
LIMIT 50;

-- ============================================
-- Connection Monitoring
-- ============================================

CREATE OR REPLACE VIEW active_connections AS
SELECT
    pid,
    usename,
    application_name,
    client_addr,
    backend_start,
    state,
    wait_event_type,
    wait_event,
    query
FROM pg_stat_activity
WHERE state != 'idle'
ORDER BY backend_start DESC;

-- ============================================
-- Database Size Monitoring
-- ============================================

CREATE OR REPLACE VIEW database_size AS
SELECT
    table_schema as schema_name,
    table_name,
    pg_size_pretty(pg_total_relation_size(table_schema||'.'||table_name)) as total_size,
    pg_size_pretty(pg_relation_size(table_schema||'.'||table_name)) as table_size,
    pg_size_pretty(pg_indexes_size(table_schema||'.'||table_name)) as index_size
FROM information_schema.tables
WHERE table_schema NOT IN ('pg_catalog', 'information_schema')
ORDER BY pg_total_relation_size(table_schema||'.'||table_name) DESC
LIMIT 20;

-- ============================================
-- Replication Status (for HA setups)
-- ============================================

CREATE OR REPLACE VIEW replication_status AS
SELECT
    pid,
    usesysid,
    usename,
    application_name,
    client_addr,
    client_hostname,
    client_port,
    backend_start,
    backend_xmin,
    state,
    sent_lsn,
    write_lsn,
    flush_lsn,
    replay_lsn,
    write_lag,
    flush_lag,
    replay_lag,
    sync_priority,
    sync_state
FROM pg_stat_replication;

-- ============================================
-- Cache Hit Ratio
-- ============================================

CREATE OR REPLACE VIEW cache_hit_ratio AS
SELECT
    datname,
    blks_hit,
    blks_read,
    CASE WHEN blks_read + blks_hit > 0
         THEN round(100.0 * blks_hit / (blks_read + blks_hit), 2)
         ELSE 0 END as cache_hit_percentage
FROM pg_stat_database
WHERE datname = current_database();