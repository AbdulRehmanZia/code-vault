package com.codevault.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * VersionPanel — the right sidebar that shows commit/version history.
 *
 * Contains:
 *   - A header with the panel title
 *   - A JList of VersionEntry objects (each representing one saved snapshot)
 *   - A custom cell renderer to style each entry like a Git commit row
 *
 * In Phase 1, entries are static placeholder data.
 * In a later phase, real snapshots with timestamps will be stored here (and in MySQL).
 */
public class VersionPanel extends JPanel {

    // The list model is the data source — add items here to update the UI
    DefaultListModel<VersionEntry> listModel;
    JList<VersionEntry> versionList;

    private static final Color PANEL_BG    = new Color(0x25, 0x25, 0x26);
    private static final Color HEADER_BG   = new Color(0x1E, 0x1E, 0x1E);
    private static final Color ITEM_BG     = new Color(0x2D, 0x2D, 0x2D);
    private static final Color ITEM_FG     = new Color(0xD4, 0xD4, 0xD4);
    private static final Color ITEM_SUB_FG = new Color(0x85, 0x85, 0x85);
    private static final Color SELECT_BG   = new Color(0x04, 0x4B, 0x7C);

    public VersionPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(0x3C, 0x3C, 0x3C)));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildList(),    BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
    }

    /** Title bar at the top of the sidebar. */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x3C, 0x3C, 0x3C)));

        JLabel title = new JLabel("⏱  VERSION HISTORY");
        title.setForeground(new Color(0xCC, 0xCC, 0xCC));
        title.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.add(title);

        return header;
    }

    /** The scrollable JList of version entries. */
    private JScrollPane buildList() {
        listModel = new DefaultListModel<>();

        // ── Seed with placeholder entries ─────────────────────────────────────
        listModel.addElement(new VersionEntry("v3 — Added main method", "Today, 10:42 AM"));
        listModel.addElement(new VersionEntry("v2 — Hello World draft",  "Today, 10:15 AM"));
        listModel.addElement(new VersionEntry("v1 — Initial commit",     "Today, 09:58 AM"));

        versionList = new JList<>(listModel);
        versionList.setBackground(PANEL_BG);
        versionList.setForeground(ITEM_FG);
        versionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        versionList.setFixedCellHeight(54);           // fixed row height for consistency
        versionList.setCellRenderer(new VersionCellRenderer());
        versionList.setSelectionBackground(SELECT_BG);
        versionList.setSelectionForeground(Color.WHITE);
        versionList.setBorder(new EmptyBorder(4, 0, 4, 0));

        JScrollPane scroll = new JScrollPane(versionList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PANEL_BG);

        return scroll;
    }

    /** A small footer with a commit count label. */
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(HEADER_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x3C, 0x3C, 0x3C)));

        JLabel countLabel = new JLabel(listModel.size() + " commits");
        countLabel.setForeground(ITEM_SUB_FG);
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.add(countLabel);

        return footer;
    }

    /**
     * Adds a new version entry at the top of the list.
     * Called by MainFrame when the user clicks Commit.
     */
    public void addVersion(String message, String timestamp) {
        listModel.add(0, new VersionEntry(message, timestamp));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Inner class: VersionEntry — the data model for one row in the list
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Holds the display data for a single version history item.
     * Keeping this as a plain data class is simpler than a full DTO.
     */
    static class VersionEntry {
        final String message;
        final String timestamp;

        VersionEntry(String message, String timestamp) {
            this.message   = message;
            this.timestamp = timestamp;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Inner class: VersionCellRenderer — controls how each row is drawn
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Custom renderer so each JList row looks like a commit card,
     * rather than the default plain-text row.
     */
    private class VersionCellRenderer implements ListCellRenderer<VersionEntry> {

        @Override
        public Component getListCellRendererComponent(
                JList<? extends VersionEntry> list,
                VersionEntry value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x3C, 0x3C, 0x3C)),
                new EmptyBorder(8, 12, 8, 12)
            ));
            card.setBackground(isSelected ? SELECT_BG : ITEM_BG);

            // Commit message (bold)
            JLabel msgLabel = new JLabel("● " + value.message);
            msgLabel.setForeground(isSelected ? Color.WHITE : ITEM_FG);
            msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            // Timestamp (muted, smaller)
            JLabel timeLabel = new JLabel("   " + value.timestamp);
            timeLabel.setForeground(isSelected ? new Color(0xCC, 0xCC, 0xCC) : ITEM_SUB_FG);
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            card.add(msgLabel);
            card.add(Box.createRigidArea(new Dimension(0, 3)));
            card.add(timeLabel);

            return card;
        }
    }
}
