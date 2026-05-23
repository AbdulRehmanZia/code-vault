-- ============================================================
-- CodeVault Database Schema
-- Run this once in MySQL to set up the database.
--
-- HOW TO RUN:
--   mysql -u root -p < schema.sql
-- OR paste it into MySQL Workbench / phpMyAdmin.
-- ============================================================

-- Create the database
CREATE DATABASE IF NOT EXISTS codevault_db;
USE codevault_db;

-- ── projects table ────────────────────────────────────────────
-- Each row is one project. For now CodeVault always uses id = 1.
CREATE TABLE IF NOT EXISTS projects (
    id         INT          AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ── versions table ────────────────────────────────────────────
-- Each row is one committed code snapshot.
-- project_id links back to the projects table (foreign key).
-- code is LONGTEXT because source files can be arbitrarily large.
CREATE TABLE IF NOT EXISTS versions (
    id         INT          AUTO_INCREMENT PRIMARY KEY,
    project_id INT          NOT NULL,
    message    VARCHAR(500) NOT NULL,
    code       LONGTEXT     NOT NULL,
    timestamp  VARCHAR(50)  NOT NULL,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id)
);

-- ── Insert default project ────────────────────────────────────
-- INSERT IGNORE skips the insert if a row with id=1 already exists.
INSERT IGNORE INTO projects (id, name) VALUES (1, 'Default Project');
