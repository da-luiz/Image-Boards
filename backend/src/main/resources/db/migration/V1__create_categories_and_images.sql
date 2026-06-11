-- V1: initial schema. Flyway runs this once, automatically, on startup.
-- Never edit a migration after it has run — write a new V2__ file instead.

-- categories: the "one" side. The category name lives here exactly once.
-- (DDIA Ch.2 normalization — rename a category in one row, not thousands.)
CREATE TABLE categories (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        TEXT        NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- images: the "many" side. Many images belong to one category (many-to-one).
-- We store storage_key (the object's key in Cloudflare R2), NOT the image bytes.
CREATE TABLE images (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    storage_key   TEXT        NOT NULL,
    category_id   BIGINT      NOT NULL REFERENCES categories(id),
    uploaded_by   BIGINT,                 -- becomes a FK to admins(id) in Phase 2.5
    content_type  TEXT        NOT NULL,
    size_bytes    BIGINT      NOT NULL,
    width         INTEGER,
    height        INTEGER,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Index A → serves "latest images overall" (GET /images/latest):
--   SELECT ... FROM images ORDER BY created_at DESC LIMIT 20;
CREATE INDEX idx_images_created_at ON images (created_at DESC);

-- Index B → serves "latest images in ONE category":
--   SELECT ... FROM images WHERE category_id = ? ORDER BY created_at DESC LIMIT 20;
CREATE INDEX idx_images_category_created_at ON images (category_id, created_at DESC);