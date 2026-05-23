package com.codevault.services;

import com.codevault.database.DatabaseManager;
import com.codevault.models.Version;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * VersionService — manages the list of code snapshots (versions/commits).
 *
 * ─── PHASE 3 (No Database) ───────────────────────────────────────────────────
 *   Versions are stored in a plain ArrayList in memory.
 *   They are LOST when the app closes.
 *   Use constructor:  new VersionService()
 *
 * ─── PHASE 4 (With MySQL) ────────────────────────────────────────────────────
 *   Versions are saved to and loaded from MySQL via DatabaseManager.
 *   They PERSIST across app restarts.
 *   Use constructor:  new VersionService(databaseManager)
 *
 * The rest of the app (MainFrame, VersionPanel) doesn't care which phase
 * is active — they just call saveVersion() and getAllVersions(). This is
 * an example of "separation of concerns."
 */
public class VersionService {

    // In-memory list — always used, even in Phase 4 (as a cache)
    private final ArrayList<Version> versions = new ArrayList<>();

    // Optional database — null in Phase 3, set in Phase 4
    private final DatabaseManager db;

    // Auto-incrementing ID counter (only used when there's no DB)
    private int nextId = 1;

    // ── PHASE 3 Constructor (no database) ────────────────────────────────────
    public VersionService() {
        this.db = null;
    }

    // ── PHASE 4 Constructor (with database) ──────────────────────────────────
    public VersionService(DatabaseManager db) {
        this.db = db;

        // Load all existing versions from MySQL into memory on startup
        if (db != null && db.isConnected()) {
            List<Version> saved = db.loadVersions(1);   // project_id = 1
            versions.addAll(saved);

            // Set nextId to one more than the highest existing id
            for (Version v : saved) {
                if (v.getId() >= nextId) {
                    nextId = v.getId() + 1;
                }
            }
        }
    }

    /**
     * saveVersion() — creates a new version snapshot.
     *
     * @param message  the commit message typed by the user
     * @param code     the current code in the editor
     * @return the newly created Version (so MainFrame can add it to the panel)
     */
    public Version saveVersion(String message, String code) {
        // Format current date/time as a readable string
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("MMM d, hh:mm a"));

        Version version = new Version(nextId++, message, code, timestamp);

        // Add to front of list so newest appears first in the sidebar
        versions.add(0, version);

        // Phase 4: also persist to MySQL
        if (db != null && db.isConnected()) {
            db.saveVersion(version, 1);     // project_id = 1
        }

        return version;
    }

    /**
     * getAllVersions() — returns a copy of the version list.
     * We return a copy so the caller can't accidentally modify our internal list.
     */
    public List<Version> getAllVersions() {
        return new ArrayList<>(versions);
    }

    /** Returns how many versions have been saved so far. */
    public int getCount() {
        return versions.size();
    }
}
