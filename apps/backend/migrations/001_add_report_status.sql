ALTER TABLE reports ADD COLUMN status TEXT NOT NULL DEFAULT 'active';
CREATE INDEX IF NOT EXISTS idx_reports_status ON reports (status);
