import service.AuthService;
import service.SingleInstanceLock;
import ui.AuthFrame;
import ui.MainFrame;
import ui.Theme;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        if (!SingleInstanceLock.acquireLock()) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        null,
                        "The program is already running in the background.\nPlease access it through the system tray.",
                        "Already Running",
                        JOptionPane.WARNING_MESSAGE
                );
                System.exit(0);
            });
            return;
        }

        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.put("Button.focus", new java.awt.Color(0, 0, 0, 0));
                UIManager.put("Button.focusPainted", Boolean.FALSE);
                UIManager.put("CheckBox.focus", new java.awt.Color(0, 0, 0, 0));
                UIManager.put("CheckBox.focusPainted", Boolean.FALSE);
                UIManager.put("RadioButton.focus", new java.awt.Color(0, 0, 0, 0));
                UIManager.put("RadioButton.focusPainted", Boolean.FALSE);
                UIManager.put("ToggleButton.focus", new java.awt.Color(0, 0, 0, 0));
            } catch (Exception ignored) {}

            Theme.applyToUIManager();

            AuthService authService = new AuthService();
            if (authService.getCurrentPatient() != null) {
                new MainFrame(authService).setVisible(true);
            } else {
                new AuthFrame(authService).setVisible(true);
            }
        });
    }
}
