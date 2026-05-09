package com.codevault.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
 * This class also wires up the button action listeners —
 * keeping all logic in one place makes it easy to find for beginners.
 */
public class MainFrame extends JFrame {

    private ToolbarPanel toolbarPanel;
    private EditorPanel  editorPanel;
    private ConsolePanel consolePanel;
    private VersionPanel versionPanel;

    private static final int WINDOW_WIDTH  = 1200;
    private static final int WINDOW_HEIGHT = 750;

    // How wide the version history sidebar starts (pixels)
    private static final int SIDEBAR_WIDTH = 240;

    public MainFrame() {
        initFrame();
        initPanels();
        wireButtons();
        setVisible(true);
    }

    /** Sets basic JFrame properties (title, size, close operation, etc.). */
    private void initFrame() {
        setTitle("CodeVault — Lightweight Java IDE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);          // center on screen

        // Dark window background prevents white flash during startup
        getContentPane().setBackground(new Color(0x1E, 0x1E, 0x1E));
        setLayout(new BorderLayout());

        // Optional: set application icon (placeholder, no file needed right now)
        // setIconImage(new ImageIcon("resources/icon.png").getImage());
    }

    /** Constructs all panels and assembles them into the frame. */
    private void initPanels() {
        toolbarPanel = new ToolbarPanel();
        editorPanel  = new EditorPanel();
        consolePanel = new ConsolePanel();
        versionPanel = new VersionPanel();

        // ── JSplitPane: editor (left) + version sidebar (right) ───────────────
        JSplitPane horizontalSplit = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            editorPanel,
            versionPanel
        );
        horizontalSplit.setResizeWeight(1.0);       // editor takes all extra space on resize
        horizontalSplit.setDividerLocation(WINDOW_WIDTH - SIDEBAR_WIDTH);
        horizontalSplit.setDividerSize(3);
        horizontalSplit.setBorder(null);             // no default border

        // ── Vertical split: code area (top) + console (bottom) ────────────────
        // Wrap the horizontal split + console in another JSplitPane
        JSplitPane verticalSplit = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            horizontalSplit,
            consolePanel
        );
        verticalSplit.setResizeWeight(0.72);        // 72% editor, 28% console at start
        verticalSplit.setDividerSize(4);
        verticalSplit.setBorder(null);

        // ── Assemble into the frame ────────────────────────────────────────────
        add(buildTitleBar(), BorderLayout.NORTH);
        add(verticalSplit,   BorderLayout.CENTER);
    }

    /**
     * A VS Code-style custom title bar area: project name on the left,
     * plus the toolbar below it.
     * We use a sub-panel so both can live in NORTH.
     */
    private JPanel buildTitleBar() {
        JPanel northArea = new JPanel(new BorderLayout());
        northArea.setBackground(new Color(0x1E, 0x1E, 0x1E));

        // Window menu bar (File | Edit | View — placeholder labels)
        JPanel menuArea = buildMenuBar();
        northArea.add(menuArea,    BorderLayout.NORTH);
        northArea.add(toolbarPanel, BorderLayout.CENTER);

        return northArea;
    }

    /** Minimal fake menu bar for visual completeness (no actual menus yet). */
    private JPanel buildMenuBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        bar.setBackground(new Color(0x32, 0x32, 0x32));

        for (String item : new String[]{"File", "Edit", "View", "Run", "Help"}) {
            JLabel lbl = new JLabel(item);
            lbl.setForeground(new Color(0xCC, 0xCC, 0xCC));
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            bar.add(lbl);
        }

        // App name on the far right
        JLabel appName = new JLabel("CodeVault  ");
        appName.setForeground(new Color(0x00, 0x78, 0xD4));
        appName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bar.add(Box.createHorizontalGlue());
        bar.add(appName);

        return bar;
    }

    /**
     * Attaches ActionListeners to every toolbar button.
     * All logic is here in MainFrame so each panel stays "dumb" (it only knows its own UI).
     */
    private void wireButtons() {
        // Open — placeholder only (file chooser added in Phase 2)
        toolbarPanel.openButton.addActionListener((ActionEvent e) -> {
            consolePanel.appendOutput("[Open] File chooser will be implemented in Phase 2.");
        });

        // Save — placeholder only (actual file I/O added in Phase 2)
        toolbarPanel.saveButton.addActionListener((ActionEvent e) -> {
            consolePanel.appendOutput("[Save] File save will be implemented in Phase 2.");
        });

        // Run — placeholder; real execution comes in Phase 3
        toolbarPanel.runButton.addActionListener((ActionEvent e) -> {
            consolePanel.appendOutput("[Run] Code execution will be implemented in Phase 3.");
            consolePanel.appendOutput("> Simulating run...");
            consolePanel.appendOutput("  Hello, CodeVault!\n");
        });

        // Commit — captures a snapshot label and adds it to the version list
        toolbarPanel.commitButton.addActionListener((ActionEvent e) -> {
            String message = JOptionPane.showInputDialog(
                this,
                "Enter commit message:",
                "New Commit",
                JOptionPane.PLAIN_MESSAGE
            );
            if (message != null && !message.trim().isEmpty()) {
                String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("MMM d, hh:mm a"));
                versionPanel.addVersion(message.trim(), timestamp);
                consolePanel.appendOutput("[Commit] Snapshot saved: \"" + message.trim() + "\"");
            }
        });
    }
}
