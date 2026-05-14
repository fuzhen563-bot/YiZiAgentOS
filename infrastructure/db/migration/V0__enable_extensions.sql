-- Enable pgvector extension for vector similarity search
CREATE EXTENSION IF NOT EXISTS vector;

-- Verify vector extension
SELECT extname, extversion FROM pg_extension WHERE extname = 'vector';

-- Note: VECTOR(1536) type will be available after extension is enabled
-- If using a managed PostgreSQL, ensure the extension is enabled:
-- ALTER DATABASE agentos SET extwstate.list = 'vector';
-- Or connect to the database and run: CREATE EXTENSION vector;