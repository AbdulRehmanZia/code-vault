package com.codevault.ui;

import com.codevault.database.DatabaseManager;
import com.codevault.models.Version;
import com.codevault.services.CodeRunnerService;
import com.codevault.services.VersionService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * MainFrame — the root JFrame that owns and assembles every panel.
 *
 * Layout (BorderLayout):
 *
 *   ┌─────────────────────────────────────────────────────┐
 *   │  ToolbarPanel  (NORTH)                              │
 *   ├──────────────────────────────────┬──────────────────┤
 *   │                                  │                  │
 *   │   EditorPanel  (CENTER of        │  VersionPanel    │
 *   │   JSplitPane)                    │  (RIGHT of       │
 *   │                                  │   JSplitPane)    │
 *   ├──────────────────────────────────┴──────────────────┤
 *   │  ConsolePanel  (SOUTH)                              │
 *   └─────────────────────────────────────────────────────┘
 *
 * ─── HOW THE PHASES CONNECT ──────────────────────────────────────────────────
 *
 *  Phase 2 (Run):
 *    Run button → CodeRunnerService.runCode() → compiles & runs → ConsolePanel
 *
 *  Phase 3 (Version history, in-memory):
 *    Commit button → VersionService.saveVersion() → Version object
 *                 → VersionPanel.addVersion()     → sidebar updates
 *    Click version → VersionPanel callback        → EditorPanel.setCode()
 *
 *  Phase 4 (MySQL):
 *    Same as Phase 3 but VersionService also calls DatabaseManager.saveVersion()
 *    On startup: VersionService loads existing versions → VersionPanel.loadVersions()
 *
 * ─── TO SWITCH BETWEEN PHASE 3 AND PHASE 4 ──────────────────────────────────
 *
 *  Phase 3 (no DB):
 *    versionService = new VersionService();
 *
 *  Phase 4 (with MySQL):
 *    db             = new DatabaseManager();
 *    versionService = new VersionService(db);
 *
 *  Just change those two lines in initServices() below.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class MainFrame extends JFrame {

    // ── UI Panels ─────────────────────────────────────────────────────────────
    private ToolbarPanel toolbarPanel;
    private EditorPanel  editorPanel;
    private ConsolePanel consolePanel;
    private VersionPanel versionPanel;

    // ── Services ──────────────────────────────────────────────────────────────
    private CodeRunnerService codeRunnerService;
    private VersionService    versionService;
    private DatabaseManager   db;           // null in Phase 3, set in Phase 4
    private String            currentFilePath = null;  // null until first Save

    private static final int WINDOW_WIDTH  = 1200;
    private static final int WINDOW_HEIGHT = 750;
    private static final int SIDEBAR_WIDTH = 240;

    public MainFrame() {
        initFrame();
        initPanels();
        initServices();     // ← NEW: create service objects
        wireButtons();      // ← connects buttons and callbacks to services
        setVisible(true);
    }

    /** Sets basic JFrame properties. */
    private void initFrame() {
        setTitle("CodeVault — Lightweight Java IDE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        getContentPane().setBackground(new Color(0xEB, 0xEB, 0xEB));
        setLayout(new BorderLayout());

        // Close DB connection cleanly when the user closes the window
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (db != null) {
                    db.close();
                }
            }
        });
    }

    /** Constructs all panels and assembles them into the frame. */
    private void initPanels() {
        toolbarPanel = new ToolbarPanel();
        editorPanel  = new EditorPanel();
        consolePanel = new ConsolePanel();
        versionPanel = new VersionPanel();

        JSplitPane horizontalSplit = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            editorPanel,
            versionPanel
        );
        horizontalSplit.setResizeWeight(1.0);
        horizontalSplit.setDividerLocation(WINDOW_WIDTH - SIDEBAR_WIDTH);
        horizontalSplit.setDividerSize(3);
        horizontalSplit.setBorder(null);

        JSplitPane verticalSplit = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            horizontalSplit,
            consolePanel
        );
        verticalSplit.setResizeWeight(0.72);
        verticalSplit.setDividerSize(4);
        verticalSplit.setBorder(null);

        add(buildTitleBar(), BorderLayout.NORTH);
        add(verticalSplit,   BorderLayout.CENTER);
    }

    /**
     * initServices() — creates the service objects.
     *
     * ── PHASE 3 (ArrayList, no database) — currently active ──────────────────
     *   Just create VersionService with no arguments.
     *
     * ── PHASE 4 (MySQL database) — uncomment block below when ready ──────────
     *   1. Install MySQL, run schema.sql
     *   2. Update DB_USER / DB_PASSWORD in DatabaseManager.java
     *   3. Add mysql-connector-j.jar to lib/
     *   4. Uncomment the Phase 4 block and comment out the Phase 3 line
     */
    private void initServices() {

        // ── Phase 3: in-memory only ───────────────────────────────────────────
        versionService    = new VersionService();
        codeRunnerService = new CodeRunnerService(consolePanel);

        /*
         * ── Phase 4: uncomment this block (and remove the two lines above) ───
         *
         * db = new DatabaseManager();
         * if (db.isConnected()) {
         *     db.ensureDefaultProject();
         *     versionService = new VersionService(db);
         *     versionPanel.loadVersions(versionService.getAllVersions());
         *     consolePanel.appendOutput("[DB] Loaded " + versionService.getCount()
         *                               + " versions from database.");
         * } else {
         *     versionService = new VersionService();   // fallback
         *     consolePanel.appendOutput("[DB] No database — running in-memory.");
         * }
         * codeRunnerService = new CodeRunnerService(consolePanel);
         */
    }

    /**
     * wireButtons() — attaches ActionListeners to every toolbar button
     * and registers the version-selection callback on the sidebar.
     *
     * All logic lives here in MainFrame so each panel stays "dumb."
     */
    private void wireButtons() {

        // ── Save ─────────────────────────────────────────────────────────────
        // First click opens a JFileChooser so the user picks where to save.
        // Every subsequent click saves to the same path without asking again.
        toolbarPanel.saveButton.addActionListener((ActionEvent e) -> {
            if (currentFilePath == null) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Save Java File");
                chooser.setSelectedFile(new java.io.File("Main.java"));
                chooser.setFileFilter(new FileNameExtensionFilter("Java files (*.java)", "java"));
                if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

                String path = chooser.getSelectedFile().getAbsolutePath();
                if (!path.endsWith(".java")) path += ".java";
                currentFilePath = path;
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFilePath))) {
                writer.write(editorPanel.getCode());
                consolePanel.appendOutput("[Save] Saved → " + currentFilePath);
            } catch (IOException ex) {
                consolePanel.appendOutput("[Save] Error: " + ex.getMessage());
                currentFilePath = null;   // reset so user can pick a new path
            }
        });

        // ── Run ───────────────────────────────────────────────────────────────
        // Phase 2: CodeRunnerService handles compilation and execution.
        // We disable the Run button while running to prevent double-clicks,
        // then re-enable it via the onComplete callback when execution finishes.
        toolbarPanel.runButton.addActionListener((ActionEvent e) -> {
            String code = editorPanel.getCode();
            toolbarPanel.runButton.setEnabled(false);   // disable while running

            // runCode() returns immediately — work happens on a background thread.
            // The lambda () -> ... is called back on the EDT when done.
            codeRunnerService.runCode(code, () -> {
                toolbarPanel.runButton.setEnabled(true);
            });
        });

        // ── Commit ────────────────────────────────────────────────────────────
        // Phase 3: VersionService saves the snapshot; VersionPanel shows it.
        toolbarPanel.commitButton.addActionListener((ActionEvent e) -> {
            String message = JOptionPane.showInputDialog(
                this,
                "Enter commit message:",
                "New Commit",
                JOptionPane.PLAIN_MESSAGE
            );

            if (message != null && !message.trim().isEmpty()) {
                String  code    = editorPanel.getCode();
                Version version = versionService.saveVersion(message.trim(), code);

                versionPanel.addVersion(version);   // update the sidebar

                consolePanel.appendOutput("[Commit] Snapshot saved: \""
                        + version.getMessage() + "\" — " + version.getTimestamp());
            }
        });

        // ── Version click → restore code ──────────────────────────────────────
        // When the user clicks any row in the version sidebar, load that
        // snapshot's code back into the editor.
        versionPanel.setOnVersionSelected(version -> {
            editorPanel.setCode(version.getCode());
            consolePanel.appendOutput("[Restore] Loaded: \"" + version.getMessage() + "\"");
        });
    }

    // ── Title bar builder (unchanged from Phase 1) ────────────────────────────

    private JPanel buildTitleBar() {
        JPanel northArea = new JPanel(new BorderLayout());
        northArea.setBackground(new Color(0xEB, 0xEB, 0xEB));
        northArea.add(buildMenuBar(), BorderLayout.NORTH);
        northArea.add(toolbarPanel,   BorderLayout.CENTER);
        return northArea;
    }

    private JPanel buildMenuBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        bar.setBackground(new Color(0xDC, 0xDC, 0xDC));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xC4, 0xC4, 0xC4)));

        for (String item : new String[]{"File", "Edit", "View", "Run", "Help"}) {
            JLabel lbl = new JLabel(item);
            lbl.setForeground(new Color(0x3C, 0x3C, 0x3C));
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            bar.add(lbl);
        }

        JLabel appName = new JLabel("CodeVault  ");
        appName.setForeground(new Color(0x15, 0x65, 0xC0));
        appName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bar.add(Box.createHorizontalGlue());
        bar.add(appName);

        return bar;
    }
}
