CREATE TABLE IF NOT EXISTS reports (
  id TEXT PRIMARY KEY,
  latitude REAL NOT NULL,
  longitude REAL NOT NULL,
  description TEXT NOT NULL,
  severity TEXT NOT NULL,
  created_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reports_created_at ON reports (created_at);
