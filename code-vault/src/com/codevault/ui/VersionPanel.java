package com.codevault.ui;

import com.codevault.models.Version;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * VersionPanel — the right sidebar showing the list of saved code snapshots.
 *
 * Changes from Phase 1:
 *   - Now uses the real Version model (instead of the old inner VersionEntry class)
 *   - Starts EMPTY — versions are added by MainFrame via addVersion() or loadVersions()
 *   - Clicking a version triggers a callback so MainFrame can restore the code
 *   - Footer commit count updates dynamically
 *
 * Layout (BorderLayout):
 *   ┌─────────────────────┐
 *   │  Header             │  ← "VERSION HISTORY" title
 *   ├─────────────────────┤
 *   │  Scrollable JList   │  ← one row per Version (commit card style)
 *   ├─────────────────────┤
 *   │  Footer             │  ← "N commits" count
 *   └─────────────────────┘
 */
public class VersionPanel extends JPanel {

    // The data source for the JList — add/remove items here to update the UI
    private DefaultListModel<Version> listModel;
    private JList<Version>            versionList;

    // Footer label — stored as a field so we can update the count dynamically
    private JLabel countLabel;

    // Callback triggered when the user clicks a version row
    // Consumer<Version> is a functional interface: it takes a Version and returns nothing
    private Consumer<Version> onVersionSelected;

    private static final Color PANEL_BG    = new Color(0xEB, 0xEB, 0xEB);
    private static final Color HEADER_BG   = new Color(0xDC, 0xDC, 0xDC);
    private static final Color ITEM_BG     = new Color(0xE4, 0xE4, 0xE4);
    private static final Color ITEM_FG     = new Color(0x2A, 0x2A, 0x2A);
    private static final Color ITEM_SUB_FG = new Color(0x88, 0x88, 0x88);
    private static final Color SELECT_BG   = new Color(0xC4, 0xD8, 0xF0);

    public VersionPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(0xC4, 0xC4, 0xC4)));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildList(),    BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
    }

    /** Title bar at the top of the sidebar. */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xC4, 0xC4, 0xC4)));

        JLabel title = new JLabel("⏱  VERSION HISTORY");
        title.setForeground(new Color(0x5A, 0x5A, 0x5A));
        title.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.add(title);

        return header;
    }

    /** The scrollable JList of version entries (starts empty). */
    private JScrollPane buildList() {
        listModel = new DefaultListModel<>();   // empty — populated by MainFrame

        versionList = new JList<>(listModel);
        versionList.setBackground(PANEL_BG);
        versionList.setForeground(ITEM_FG);
        versionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        versionList.setFixedCellHeight(54);
        versionList.setCellRenderer(new VersionCellRenderer());
        versionList.setSelectionBackground(SELECT_BG);
        versionList.setSelectionForeground(Color.WHITE);
        versionList.setBorder(new EmptyBorder(4, 0, 4, 0));

        // ── Click-to-restore listener ─────────────────────────────────────────
        // ListSelectionListener fires when selection changes.
        // getValueIsAdjusting() is true while the user is still dragging — we
        // only act when they've made a final selection (false).
        versionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Version selected = versionList.getSelectedValue();
                if (selected != null && onVersionSelected != null) {
                    onVersionSelected.accept(selected);   // trigger the callback
                }
            }
        });

        JScrollPane scroll = new JScrollPane(versionList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PANEL_BG);

        return scroll;
    }

    /** Footer showing the total number of commits. */
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(HEADER_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xC4, 0xC4, 0xC4)));

        countLabel = new JLabel("0 commits");
        countLabel.setForeground(ITEM_SUB_FG);
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.add(countLabel);

        return footer;
    }

    // ── Public API called by MainFrame ────────────────────────────────────────

    /**
     * addVersion() — inserts a single new Version at the top of the list.
     * Called by MainFrame each time the user clicks Commit.
     */
    public void addVersion(Version version) {
        listModel.add(0, version);              // add at front (newest first)
        countLabel.setText(listModel.size() + " commits");
    }

    /**
     * loadVersions() — replaces the entire list with the given versions.
     * Called by MainFrame on startup to populate from the database (Phase 4).
     */
    public void loadVersions(List<Version> versions) {
        listModel.clear();
        for (Version v : versions) {
            listModel.addElement(v);
        }
        countLabel.setText(listModel.size() + " commits");
    }

    /**
     * setOnVersionSelected() — registers the callback for click-to-restore.
     *
     * Usage in MainFrame:
     *   versionPanel.setOnVersionSelected(version -> {
     *       editorPanel.setCode(version.getCode());
     *   });
     *
     * Consumer<Version> is from java.util.function — it's a lambda-friendly
     * interface that accepts one argument and returns nothing.
     */
    public void setOnVersionSelected(Consumer<Version> callback) {
        this.onVersionSelected = callback;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner class: VersionCellRenderer
    // Controls how each row in the JList is drawn (commit card style).
    // ─────────────────────────────────────────────────────────────────────────

    private class VersionCellRenderer implements ListCellRenderer<Version> {

        @Override
        public Component getListCellRendererComponent(
                JList<? extends Version> list,
                Version value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            // Build a small card panel for each row
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xD0, 0xD0, 0xD0)),
                new EmptyBorder(8, 12, 8, 12)
            ));
            card.setBackground(isSelected ? SELECT_BG : ITEM_BG);

            // Top line: commit message (bold dot + message text)
            JLabel msgLabel = new JLabel("● " + value.getMessage());
            msgLabel.setForeground(isSelected ? new Color(0x15, 0x65, 0xC0) : ITEM_FG);
            msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            // Bottom line: timestamp (smaller, muted color)
            JLabel timeLabel = new JLabel("   " + value.getTimestamp());
            timeLabel.setForeground(isSelected ? new Color(0x88, 0x88, 0x88) : ITEM_SUB_FG);
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            card.add(msgLabel);
            card.add(Box.createRigidArea(new Dimension(0, 3)));
            card.add(timeLabel);

            return card;
        }
    }
}
