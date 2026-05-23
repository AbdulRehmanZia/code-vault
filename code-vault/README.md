# CodeVault

A lightweight Java IDE desktop app built with Java Swing. Inspired by VS Code's dark UI — write Java code, run it, commit snapshots, and browse version history. Designed as a semester project to learn OOP, Swing, JDBC, and basic compiler integration.

---

## Built With

- **Java 17** — core language
- **Java Swing** — desktop GUI framework
- **JDBC + MySQL** — database persistence (Phase 4)
- **SwingWorker** — background threads for non-blocking code execution
- **Runtime.exec()** — spawns `javac` and `java` as child processes

---

## Features

| Feature | Status |
|---|---|
| Dark-themed code editor with line numbers | Done |
| Run Java code (compile + execute) | Done |
| Capture stdout and stderr in console | Done |
| Commit code snapshots with messages | Done |
| Version history sidebar | Done |
| Click any version to restore code | Done |
| MySQL persistence across sessions | Done (Phase 4) |

---

## Project Structure

```
code-vault/
├── src/
│   └── com/codevault/
│       ├── Main.java               App entry point
│       ├── ui/
│       │   ├── MainFrame.java      Root window, wires everything together
│       │   ├── EditorPanel.java    Code editor with line number gutter
│       │   ├── ConsolePanel.java   Output console at the bottom
│       │   ├── ToolbarPanel.java   Open / Save / Run / Commit buttons
│       │   └── VersionPanel.java   Right sidebar — version history list
│       ├── services/
│       │   ├── CodeRunnerService.java   Compiles and runs Java code
│       │   └── VersionService.java      Manages version snapshots
│       ├── models/
│       │   ├── Version.java        Data model for one commit snapshot
│       │   └── Project.java        Data model for a project
│       └── database/
│           └── DatabaseManager.java     MySQL JDBC operations
├── lib/                            Place mysql-connector-j.jar here
├── out/                            Compiled .class files (auto-generated)
├── schema.sql                      MySQL setup script
└── .gitignore
```

---

## Prerequisites

- **Java 17+** — check with `java -version`
- **MySQL 8+** — only needed for Phase 4 (database persistence)
- **MySQL Connector/J** — only needed for Phase 4

---

## Setup & Run

### Phase 3 — No Database (runs out of the box)

```bash
# 1. Compile
find src -name "*.java" | xargs javac -d out

# 2. Run  (open a regular terminal, not VS Code's integrated terminal)
java -cp out com.codevault.Main
```

> If you get a GTK/snap error in the VS Code terminal, run this instead:
> ```bash
> unset GTK_PATH GTK_IM_MODULE_FILE GDK_PIXBUF_MODULEDIR GDK_PIXBUF_MODULE_FILE
> java -cp out com.codevault.Main
> ```

---

### Phase 4 — With MySQL Persistence

**Step 1 — Set up the database**

```bash
mysql -u root -p < schema.sql
```

**Step 2 — Download MySQL Connector/J**

1. Go to: https://dev.mysql.com/downloads/connector/j/
2. Select **Platform Independent** → download the ZIP
3. Extract it and copy `mysql-connector-j-x.x.x.jar` into the `lib/` folder

**Step 3 — Update credentials**

Open [src/com/codevault/database/DatabaseManager.java](src/com/codevault/database/DatabaseManager.java) and update:

```java
private static final String DB_USER     = "root";
private static final String DB_PASSWORD = "your_password";   // ← change this
```

**Step 4 — Enable Phase 4 in MainFrame**

Open [src/com/codevault/ui/MainFrame.java](src/com/codevault/ui/MainFrame.java), find `initServices()`, and:
- Comment out the Phase 3 line
- Uncomment the Phase 4 block

**Step 5 — Compile and run with the JDBC driver**

```bash
# Compile
find src -name "*.java" | xargs javac -cp lib/mysql-connector-j-*.jar -d out

# Run
java -cp "out:lib/mysql-connector-j-*.jar" com.codevault.Main
```

> On Windows, replace `:` with `;` in the classpath:
> ```
> java -cp "out;lib\mysql-connector-j-*.jar" com.codevault.Main
> ```

---

## How to Use

1. **Write code** in the editor (must have a `Main` class with `main()`)
2. **Click Run** — output appears in the console at the bottom
3. **Click Commit** — enter a message to save a snapshot
4. **Click any version** in the sidebar to restore that snapshot

---

## Database Schema

```sql
projects  (id, name, created_at)
versions  (id, project_id, message, code, timestamp, created_at)
```

See [schema.sql](schema.sql) for the full setup script.
