
CREATE TABLE projects (
    id UUID PRIMARY KEY,

    name VARCHAR(255) NOT NULL,
    description TEXT,

    github_url VARCHAR(500) NOT NULL UNIQUE,
    owner VARCHAR(255) NOT NULL,

    language VARCHAR(100),
    license VARCHAR(100),

    stars INTEGER NOT NULL DEFAULT 0,
    forks INTEGER NOT NULL DEFAULT 0,

    topics JSONB NOT NULL DEFAULT '[]'::jsonb,

    last_updated TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_projects_name ON projects(name);
CREATE INDEX idx_projects_language ON projects(language);
CREATE INDEX idx_projects_license ON projects(license);
CREATE INDEX idx_projects_stars ON projects(stars);
