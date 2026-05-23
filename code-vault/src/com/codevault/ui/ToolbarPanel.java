package com.codevault.ui;

import javax.swing.*;
import java.awt.*;

/**
 * ToolbarPanel — the top bar of the IDE.
 *
 * Contains four action buttons:
 *   Open   → will later open a file from disk
 *   Save   → will later save the current file
 *   Run    → will later compile and run the code
 *   Commit → will later snapshot the code into version history
 *
 * Layout: FlowLayout (left-aligned) so buttons sit side by side.
 */
public class ToolbarPanel extends JPanel {

    // package-private so MainFrame can attach listeners
    JButton saveButton;
    JButton runButton;
    JButton commitButton;

    private static final Color TOOLBAR_BG   = new Color(0xE0, 0xE0, 0xE0);
    private static final Color BUTTON_BG    = new Color(0xD0, 0xD0, 0xD0);
    private static final Color BUTTON_FG    = new Color(0x2A, 0x2A, 0x2A);
    private static final Color RUN_COLOR    = new Color(0x2E, 0x7D, 0x32); // forest green
    private static final Color COMMIT_COLOR = new Color(0x15, 0x65, 0xC0); // deep blue

    public ToolbarPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 6));
        setBackground(TOOLBAR_BG);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xC4, 0xC4, 0xC4)));

        saveButton   = makeButton("💾 Save",   BUTTON_BG, BUTTON_FG);
        runButton    = makeButton("▶  Run",    BUTTON_BG, RUN_COLOR);
        commitButton = makeButton("✔  Commit", BUTTON_BG, COMMIT_COLOR);

        add(saveButton);
        add(makeSeparator());
        add(runButton);
        add(makeSeparator());
        add(commitButton);
    }

    /** Builds a styled button with consistent padding and no border focus ring. */
    private JButton makeButton(String label, Color bg, Color fg) {
        JButton btn = new JButton(label);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);          // removes the dotted focus rectangle
        btn.setBorderPainted(false);         // flat, borderless look
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        // Hover effect: slightly lighter on mouse-over
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            final Color original = bg;
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(original.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(original);
            }
        });

        return btn;
    }

    /** A thin vertical separator between button groups. */
    private JSeparator makeSeparator() {
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 22));
        sep.setForeground(new Color(0xB4, 0xB4, 0xB4));
        return sep;
    }
}
