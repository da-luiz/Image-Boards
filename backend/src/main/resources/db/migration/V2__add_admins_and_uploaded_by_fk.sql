-- V2: add the admins table, and link images.uploaded_by to it.
-- This runs AFTER V1, on top of existing data. Flyway applies it once.

-- admins: the people allowed to log in and upload. Each row is one admin.
-- We store a bcrypt HASH of the password, never the password itself.
CREATE TABLE admins (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email          TEXT        NOT NULL UNIQUE,
    password_hash  TEXT        NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Now make images.uploaded_by a real foreign key pointing at admins(id).
-- Until now it was just a plain BIGINT column with no link enforced.
-- This tells the database: any value here MUST match a real admin's id
-- (or be NULL). The DB will now refuse to store an uploaded_by that
-- doesn't correspond to an actual admin — integrity enforced for you.
ALTER TABLE images
    ADD CONSTRAINT fk_images_uploaded_by
    FOREIGN KEY (uploaded_by) REFERENCES admins(id);