-- Lantern Tool Library schema (Week 5). Tables are created parent first, so every foreign key can point at a
-- table that already exists. DatabaseManager runs this file on every reset, statement by statement: end each
-- statement with ";" and never put ";" or "--" inside a string.

-- Worked example: one row per tool category. The fee rules live in the Java enum ToolCategory.
CREATE TABLE categories (
    code VARCHAR(20) PRIMARY KEY,
    label VARCHAR(60) NOT NULL UNIQUE
);

-- TODO LEARNER 5A: create the members, tools and loans tables described in docs/milestones/5-database.md.

-- TODO LEARNER 5B: add the index described in docs/milestones/5-database.md.
