package com.codevault.database;

import com.codevault.models.Version;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseManager — handles all MySQL database operations for CodeVault.
 *
 * ─── HOW JDBC WORKS ──────────────────────────────────────────────────────────
 *
 *  JDBC (Java Database Connectivity) is Java's standard way to talk to databases.
 *  The steps are always:
 *
 *  1. Load the driver:
 *       Class.forName("com.mysql.cj.jdbc.Driver")
 *       Tells Java which JDBC driver to use (MySQL's in our case).
 *       The driver class lives inside the .jar file you download.
 *
 *  2. Get a Connection:
 *       DriverManager.getConnection(url, user, password)
 *       Opens a live connection to the database. Think of it like
 *       opening a phone call to MySQL.
 *
 *  3. Create a PreparedStatement:
 *       connection.prepareStatement("INSERT INTO versions VALUES (?, ?, ?)")
 *       A PreparedStatement is a pre-compiled SQL query with ? placeholders.
 *       WHY use it instead of String concat?
 *         → Prevents SQL Injection attacks (e.g. user types: '; DROP TABLE ...)
 *         → Handles special characters in strings automatically
 *
 *  4. Execute it:
 *       stmt.executeUpdate()  → for INSERT / UPDATE / DELETE
 *       stmt.executeQuery()   → for SELECT (returns a ResultSet)
 *
 *  5. Read results with ResultSet:
 *       while (rs.next()) {
 *           rs.getInt("id");
 *           rs.getString("message");
 *       }
 *       rs.next() moves to the next row; returns false when no more rows.
 *
 *  6. Always close resources (Connection, Statement, ResultSet).
 *       We use try-with-resources so Java closes them automatically.
 *
 * ─── SETUP ───────────────────────────────────────────────────────────────────
 *  1. Install MySQL and create the database (see schema.sql)
 *  2. Download MySQL Connector/J from:
 *       https://dev.mysql.com/downloads/connector/j/
 *  3. Place the .jar in the lib/ folder
 *  4. Update DB_USER and DB_PASSWORD below to match your MySQL setup
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class DatabaseManager {

    // ── Connection details — update these to match your MySQL setup ───────────
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/codevault_db"
                                              + "?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "your_password";   // ← change this

    // The live connection object (null if connection failed)
    private Connection connection;

    // ── Constructor: connects to MySQL when DatabaseManager is created ─────────
    public DatabaseManager() {
        connect();
    }

    /**
     * connect() — establishes the MySQL connection.
     * Called once in the constructor. If it fails, the app still runs
     * but without database persistence (falls back to ArrayList in VersionService).
     */
    private void connect() {
        try {
            // Step 1: tell Java to load the MySQL JDBC driver from the .jar
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Step 2: open the connection using our URL, username, password
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            System.out.println("[DB] Connected to MySQL: codevault_db");

        } catch (ClassNotFoundException e) {
            // The mysql-connector .jar is missing from the classpath
            System.err.println("[DB] MySQL driver not found. Add mysql-connector-j.jar to lib/");
            System.err.println("[DB] Running without database (in-memory only).");
        } catch (SQLException e) {
            // Wrong credentials, MySQL not running, wrong port, etc.
            System.err.println("[DB] Connection failed: " + e.getMessage());
            System.err.println("[DB] Running without database (in-memory only).");
        }
    }

    /** Returns true if the database connection is open and usable. */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VERSIONS TABLE OPERATIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * saveVersion() — inserts a new version row into the `versions` table.
     *
     * SQL: INSERT INTO versions (project_id, message, code, timestamp) VALUES (?, ?, ?, ?)
     *
     * PreparedStatement ?s are filled in order using setInt/setString.
     * executeUpdate() runs the INSERT and returns the number of rows affected.
     */
    public void saveVersion(Version version, int projectId) {
        String sql = "INSERT INTO versions (project_id, message, code, timestamp) "
                   + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, projectId);              // fills first  ?
            stmt.setString(2, version.getMessage()); // fills second ?
            stmt.setString(3, version.getCode());    // fills third  ?
            stmt.setString(4, version.getTimestamp()); // fills fourth ?
            stmt.executeUpdate();                    // run the INSERT

            System.out.println("[DB] Version saved: \"" + version.getMessage() + "\"");

        } catch (SQLException e) {
            System.err.println("[DB] Error saving version: " + e.getMessage());
        }
    }

    /**
     * loadVersions() — fetches all versions for a project, newest first.
     *
     * SQL: SELECT * FROM versions WHERE project_id = ? ORDER BY id DESC
     *
     * executeQuery() returns a ResultSet — a cursor over rows.
     * rs.next() advances to the next row (returns false when done).
     * rs.getInt("id") / rs.getString("code") read column values.
     */
    public List<Version> loadVersions(int projectId) {
        List<Version> list = new ArrayList<>();

        String sql = "SELECT id, message, code, timestamp "
                   + "FROM versions "
                   + "WHERE project_id = ? "
                   + "ORDER BY id DESC";    // newest first (highest id)

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, projectId);

            // executeQuery() returns a ResultSet — the table of results
            ResultSet rs = stmt.executeQuery();

            // Loop through each row in the result
            while (rs.next()) {
                Version v = new Version(
                    rs.getInt("id"),            // column "id"
                    rs.getString("message"),    // column "message"
                    rs.getString("code"),       // column "code"
                    rs.getString("timestamp")   // column "timestamp"
                );
                list.add(v);
            }
            System.out.println("[DB] Loaded " + list.size() + " versions from database.");

        } catch (SQLException e) {
            System.err.println("[DB] Error loading versions: " + e.getMessage());
        }

        return list;
    }

    /**
     * ensureDefaultProject() — creates the default project (id=1) if it doesn't exist.
     * Called once on startup. IGNORE_ERROR handles the case where it already exists.
     */
    public void ensureDefaultProject() {
        String sql = "INSERT IGNORE INTO projects (id, name) VALUES (1, 'Default Project')";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("[DB] Error ensuring default project: " + e.getMessage());
        }
    }

    /** Closes the database connection — call when the app shuts down. */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }
}
