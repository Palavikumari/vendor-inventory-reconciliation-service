ALTER TABLE batch_execution
ADD COLUMN IF NOT EXISTS source_batch_id UUID;

CREATE INDEX IF NOT EXISTS idx_batch_source
ON batch_execution(source_batch_id);