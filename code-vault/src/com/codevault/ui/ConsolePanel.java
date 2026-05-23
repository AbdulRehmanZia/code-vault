package com.codevault.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * ConsolePanel — the bottom output area, like a terminal panel in VS Code.
 *
 * Contains:
 *   - A header tab strip ("CONSOLE" label + a clear button)
 *   - A read-only JTextArea that displays program output
 *
 * In Phase 1 the output is static placeholder text.
 * In a later phase, System.out will be redirected here.
 */
public class ConsolePanel extends JPanel {

    // Package-private so MainFrame can write output to it later
    JTextArea outputArea;

    private static final Color CONSOLE_BG  = new Color(0xF4, 0xF4, 0xF4);
    private static final Color CONSOLE_FG  = new Color(0x2A, 0x2A, 0x2A);
    private static final Color HEADER_BG   = new Color(0xDC, 0xDC, 0xDC);
    private static final Color HEADER_FG   = new Color(0x60, 0x60, 0x60);

    public ConsolePanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(CONSOLE_BG);
        // A top border to visually separate the console from the editor
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xC4, 0xC4, 0xC4)));

        // ── Header strip ─────────────────────────────────────────────────────
        add(buildHeader(), BorderLayout.NORTH);

        // ── Output area ──────────────────────────────────────────────────────
        outputArea = new JTextArea();
        outputArea.setBackground(CONSOLE_BG);
        outputArea.setForeground(CONSOLE_FG);
        outputArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        outputArea.setEditable(false);      // console is output-only
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setBorder(new EmptyBorder(4, 10, 4, 10));
        outputArea.setText("> CodeVault console ready. Press Run to execute your code.\n");

        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CONSOLE_BG);

        add(scroll, BorderLayout.CENTER);
    }

    /** Tab-strip at the top of the console (CONSOLE | PROBLEMS tabs in VS Code). */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setPreferredSize(new Dimension(0, 28));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xC4, 0xC4, 0xC4)));

        JLabel consoleTab = new JLabel("  CONSOLE");
        consoleTab.setForeground(new Color(0x2A, 0x2A, 0x2A));
        consoleTab.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.add(consoleTab, BorderLayout.WEST);

        // Right: clear button
        JButton clearBtn = new JButton("✕ Clear");
        clearBtn.setBackground(HEADER_BG);
        clearBtn.setForeground(HEADER_FG);
        clearBtn.setFocusPainted(false);
        clearBtn.setBorderPainted(false);
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.setOpaque(true);
        clearBtn.addActionListener(e -> clearConsole());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 3));
        rightPanel.setBackground(HEADER_BG);
        rightPanel.add(clearBtn);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    /** Appends a line of text to the console output. Called by MainFrame. */
    public void appendOutput(String text) {
        outputArea.append(text + "\n");
        // Auto-scroll to the latest output
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    /** Wipes the console clean. */
    public void clearConsole() {
        outputArea.setText("");
    }
}
