package com.codevault;

import com.codevault.ui.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main — the application entry point.
 *
 * Two things happen here:
 *   1. We try to set the OS Look & Feel so native dialogs look correct.
 *   2. We launch MainFrame on the Event Dispatch Thread (EDT).
 *
 * WHY the EDT?
 *   Swing is NOT thread-safe. All UI creation and updates must happen on
 *   the EDT (a dedicated thread Swing manages internally). SwingUtilities.invokeLater
 *   schedules our startup code to run there — this is the correct pattern
 *   even when nothing else is running yet.
 */
public class Main {

    public static void main(String[] args) {

        // Try to match the host OS's native appearance for dialogs/fonts
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Falls back to default Java (Metal) L&F — not a fatal error
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }

        // Schedule UI creation on the Event Dispatch Thread
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
