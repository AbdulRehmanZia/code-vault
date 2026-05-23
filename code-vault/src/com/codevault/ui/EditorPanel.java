package com.codevault.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * EditorPanel — the main code-writing area.
 *
 * Contains:
 *   - A line-number gutter on the left (a read-only JTextArea kept in sync)
 *   - A JTextArea for the actual code (center)
 *
 * Both are placed inside a JScrollPane so they scroll together.
 * Using JTextArea (not JTextPane) keeps things simple for Phase 1.
 * Syntax highlighting can be added in a later phase.
 */
public class EditorPanel extends JPanel {

    // The main code editor — package-private so MainFrame can read its content
    JTextArea codeArea;

    // Gutter showing line numbers
    private JTextArea lineNumbers;

    private static final Color EDITOR_BG     = new Color(0xF7, 0xF7, 0xF7);
    private static final Color EDITOR_FG     = new Color(0x1E, 0x1E, 0x1E);
    private static final Color GUTTER_BG     = new Color(0xED, 0xED, 0xEE);
    private static final Color GUTTER_FG     = new Color(0xA0, 0xA0, 0xA0);
    private static final Color CARET_COLOR   = new Color(0x1E, 0x1E, 0x1E);
    private static final Color SELECTION_BG  = new Color(0xC4, 0xD8, 0xF0);

    public EditorPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(EDITOR_BG);

        // ── Code area ────────────────────────────────────────────────────────
        codeArea = new JTextArea();
        codeArea.setBackground(EDITOR_BG);
        codeArea.setForeground(EDITOR_FG);
        codeArea.setCaretColor(CARET_COLOR);
        codeArea.setSelectionColor(SELECTION_BG);
        codeArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 14));  // falls back to monospaced
        codeArea.setTabSize(4);
        codeArea.setLineWrap(false);       // horizontal scroll instead of wrapping
        codeArea.setWrapStyleWord(false);
        codeArea.setBorder(new EmptyBorder(4, 8, 4, 8));

        // Seed with a welcome comment so the editor isn't empty on launch
        codeArea.setText(
            "// Welcome to CodeVault\n" +
            "// Write your Java code here...\n\n" +
            "public class Main {\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"Hello, CodeVault!\");\n" +
            "    }\n" +
            "}\n"
        );

        // ── Line-number gutter ────────────────────────────────────────────────
        lineNumbers = new JTextArea("1");
        lineNumbers.setBackground(GUTTER_BG);
        lineNumbers.setForeground(GUTTER_FG);
        lineNumbers.setFont(codeArea.getFont());
        lineNumbers.setEditable(false);
        lineNumbers.setFocusable(false);
        lineNumbers.setBorder(new EmptyBorder(4, 8, 4, 8));

        // Update gutter whenever text changes
        codeArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { updateLineNumbers(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { updateLineNumbers(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateLineNumbers(); }
        });
        updateLineNumbers(); // initial render

        // ── Scroll pane wraps both gutter + code area ─────────────────────────
        JScrollPane scrollPane = new JScrollPane(codeArea);
        scrollPane.setRowHeaderView(lineNumbers);   // gutter sits left of the scroll viewport
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(EDITOR_BG);

        styleScrollBar(scrollPane.getVerticalScrollBar());
        styleScrollBar(scrollPane.getHorizontalScrollBar());

        // ── Title tab bar ─────────────────────────────────────────────────────
        JPanel tabBar = buildTabBar();

        add(tabBar,    BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    /** Recount lines and refresh the gutter text. */
    private void updateLineNumbers() {
        int lines = codeArea.getLineCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) {
            sb.append(i);
            if (i < lines) sb.append('\n');
        }
        lineNumbers.setText(sb.toString());
    }

    /** A minimal "open tab" bar at the top of the editor, like VS Code's file tabs. */
    private JPanel buildTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setBackground(new Color(0xE0, 0xE0, 0xE0));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xC4, 0xC4, 0xC4)));

        JLabel tab = new JLabel("  Main.java  ");
        tab.setForeground(new Color(0x1E, 0x1E, 0x1E));
        tab.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tab.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 1, new Color(0x15, 0x65, 0xC0)));
        tab.setOpaque(true);
        tab.setBackground(new Color(0xF7, 0xF7, 0xF7));

        Dimension tabSize = new Dimension(120, 30);
        tab.setPreferredSize(tabSize);
        tab.setHorizontalAlignment(SwingConstants.CENTER);

        bar.add(tab);
        return bar;
    }

    private void styleScrollBar(JScrollBar bar) {
        bar.setBackground(new Color(0xED, 0xED, 0xEE));
        bar.setForeground(new Color(0xB4, 0xB4, 0xB4));
        bar.setPreferredSize(new Dimension(8, 8));
    }

    /** Returns the full text currently in the editor. Called by MainFrame. */
    public String getCode() {
        return codeArea.getText();
    }

    /**
     * setCode() — replaces the editor content with the given code.
     * Called by MainFrame when the user clicks a version in the history sidebar
     * to restore a previous snapshot.
     */
    public void setCode(String code) {
        codeArea.setText(code);
        codeArea.setCaretPosition(0);   // scroll back to the top
    }
}
