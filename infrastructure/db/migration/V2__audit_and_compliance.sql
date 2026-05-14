-- ============================================
-- Audit Log Schema
-- ============================================

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100),
    resource_id UUID,
    details JSONB DEFAULT '{}',
    ip_address INET,
    user_agent TEXT,
    trace_id VARCHAR(64),
    span_id VARCHAR(32),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_workspace ON audit_logs(workspace_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_resource ON audit_logs(resource_type, resource_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_trace ON audit_logs(trace_id);

-- ============================================
-- Audit Log Retention Policy
-- ============================================

CREATE OR REPLACE FUNCTION cleanup_old_audit_logs()
RETURNS void AS $$
BEGIN
    DELETE FROM audit_logs
    WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '90 days'
    AND workspace_id NOT IN (
        SELECT id FROM workspaces WHERE plan = 'enterprise'
    );
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- Audit Event Types
-- ============================================

CREATE TABLE audit_event_types (
    action VARCHAR(100) PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    risk_level VARCHAR(20) DEFAULT 'low',
    retention_days INTEGER DEFAULT 90
);

-- Authentication events
INSERT INTO audit_event_types (action, category, risk_level, retention_days) VALUES
('auth.login', 'authentication', 'low', 90),
('auth.logout', 'authentication', 'low', 90),
('auth.login.failed', 'authentication', 'medium', 90),
('auth.password_changed', 'authentication', 'medium', 365),
('auth.token_refreshed', 'authentication', 'low', 30),
('auth.sso_login', 'authentication', 'low', 90),

-- User management events
('user.created', 'user_management', 'medium', 365),
('user.updated', 'user_management', 'low', 365),
('user.deleted', 'user_management', 'high', 365),
('user.role_changed', 'user_management', 'high', 365),

-- Workspace events
('workspace.created', 'workspace', 'medium', 365),
('workspace.updated', 'workspace', 'low', 365),
('workspace.deleted', 'workspace', 'critical', 365),

-- Agent events
('agent.created', 'agent', 'medium', 180),
('agent.updated', 'agent', 'low', 180),
('agent.deleted', 'agent', 'high', 180),
('agent.started', 'agent', 'low', 90),
('agent.stopped', 'agent', 'low', 90),
('agent.config_changed', 'agent', 'medium', 180),

-- Conversation events
('conversation.created', 'conversation', 'low', 90),
('conversation.deleted', 'conversation', 'medium', 90),

-- Tool events
('tool.invoked', 'tool', 'medium', 180),
('tool.denied', 'tool', 'high', 365),
('tool.approval_requested', 'tool', 'high', 365),
('tool.config_changed', 'tool', 'medium', 180),

-- Skill events
('skill.installed', 'skill', 'medium', 180),
('skill.uninstalled', 'skill', 'medium', 180),
('skill.executed', 'skill', 'low', 90),
('skill.created', 'skill', 'medium', 365),
('skill.updated', 'skill', 'low', 365),

-- MCP events
('mcp.server.connected', 'mcp', 'medium', 90),
('mcp.server.disconnected', 'mcp', 'medium', 90),
('mcp.server.error', 'mcp', 'high', 365),
('mcp.tool.invoked', 'mcp', 'medium', 180),

-- Goal events
('goal.created', 'goal', 'medium', 365),
('goal.completed', 'goal', 'low', 365),
('goal.failed', 'goal', 'medium', 365),
('goal.cancelled', 'goal', 'medium', 365),

-- Knowledge base events
('kb.created', 'knowledge', 'medium', 365),
('kb.document.uploaded', 'knowledge', 'medium', 180),
('kb.document.processed', 'knowledge', 'low', 180),
('kb.document.deleted', 'knowledge', 'high', 365),
('kb.config_changed', 'knowledge', 'medium', 365),

-- Model/Provider events
('model.request', 'model', 'low', 30),
('model.error', 'model', 'medium', 180),
('provider.config_changed', 'provider', 'high', 365),

-- Admin events
('admin.approval.granted', 'admin', 'high', 365),
('admin.approval.rejected', 'admin', 'high', 365),
('admin.export.data', 'admin', 'critical', 365),
('admin.import.data', 'admin', 'critical', 365),
('admin.system_config_changed', 'admin', 'critical', 365);

-- ============================================
-- Compliance Reports View
-- ============================================

CREATE OR REPLACE VIEW compliance_report AS
SELECT
    workspace_id,
    date_trunc('day', created_at) as date,
    action,
    resource_type,
    resource_id,
    user_id,
    COUNT(*) as event_count,
    jsonb_agg(jsonb_build_object(
        'id', id,
        'ip_address', ip_address,
        'details', details
    )) as events
FROM audit_logs
WHERE created_at >= CURRENT_TIMESTAMP - INTERVAL '90 days'
GROUP BY workspace_id, date_trunc('day', created_at), action, resource_type, resource_id, user_id;

-- ============================================
-- Security Alert Rules
-- ============================================

CREATE TABLE audit_security_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    condition JSONB NOT NULL,
    action VARCHAR(50) DEFAULT 'alert',
    severity VARCHAR(20) DEFAULT 'medium',
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Example security rules
INSERT INTO audit_security_rules (name, condition, action, severity) VALUES
('Failed login attempts', '{"action": "auth.login.failed", "threshold": 5, "window": "1h"}', 'alert', 'high'),
('Admin action from new IP', '{"action": "admin.*", "new_ip": true}', 'alert', 'medium'),
('Bulk data export', '{"action": "admin.export.data", "volume": "high"}', 'block', 'critical'),
('Unauthorized tool access', '{"action": "tool.denied", "count": 3}', 'alert', 'high'),
('Suspicious pattern', '{"pattern": "sql_injection", "action": ".*"}', 'alert', 'critical');