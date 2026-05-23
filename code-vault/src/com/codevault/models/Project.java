package com.codevault.models;

/**
 * Project — represents a coding project in CodeVault.
 *
 * In Phase 4 (MySQL), each project maps to a row in the `projects` table.
 * Versions belong to a project via project_id (foreign key).
 *
 * For now we only ever have one project (id = 1, name = "Default Project"),
 * but the model is here so the database structure makes sense.
 */
public class Project {

    private int    id;
    private String name;
    private String createdAt;

    public Project(int id, String name, String createdAt) {
        this.id        = id;
        this.name      = name;
        this.createdAt = createdAt;
    }

    public int    getId()        { return id; }
    public String getName()      { return name; }
    public String getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return name;
    }
}
