package ui;

import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EditAccountDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private boolean saved = false;

    public EditAccountDialog(Window owner, AuthService authService) {
        super(owner, "Edit Account Credentials", ModalityType.APPLICATION_MODAL);
        IconUtil.applyAppIcon(this);
        setSize(420, 240);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setContentPane(createUI(owner, authService));
    }

    private JPanel createUI(Window owner, AuthService authService) {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(Theme.appBackground());
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Edit Account Credentials");
        title.setFont(Theme.font(Font.BOLD, 22));
        title.setForeground(Theme.textColor());

        JLabel subtitle = new JLabel("Select an option below:");
        subtitle.setFont(Theme.font(Font.PLAIN, 14));
        subtitle.setForeground(Theme.mutedTextColor());

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Theme.appBackground());
        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitle);

        JPanel optionsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        optionsPanel.setBackground(Theme.appBackground());

        JButton changeUsernameBtn = new JButton("1. Change Username");
        styleOptionButton(changeUsernameBtn);

        JButton changePasswordBtn = new JButton("2. Change Password");
        styleOptionButton(changePasswordBtn);

        changeUsernameBtn.addActionListener(e -> {
            EditUsernameDialog dialog = new EditUsernameDialog(owner, authService);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                saved = true;
                dispose();
            }
        });

        changePasswordBtn.addActionListener(e -> {
            ChangePasswordDialog dialog = new ChangePasswordDialog(owner, authService);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                saved = true;
                dispose();
            }
        });

        optionsPanel.add(changeUsernameBtn);
        optionsPanel.add(changePasswordBtn);

        java.awt.event.KeyAdapter keyNav = new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int code = e.getKeyCode();
                if (code == java.awt.event.KeyEvent.VK_UP || code == java.awt.event.KeyEvent.VK_LEFT) {
                    changeUsernameBtn.requestFocusInWindow();
                } else if (code == java.awt.event.KeyEvent.VK_DOWN || code == java.awt.event.KeyEvent.VK_RIGHT) {
                    changePasswordBtn.requestFocusInWindow();
                } else if (code == java.awt.event.KeyEvent.VK_ENTER) {
                    Component focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                    if (focus == changeUsernameBtn) {
                        changeUsernameBtn.doClick();
                    } else if (focus == changePasswordBtn) {
                        changePasswordBtn.doClick();
                    }
                }
            }
        };

        changeUsernameBtn.addKeyListener(keyNav);
        changePasswordBtn.addKeyListener(keyNav);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "ESCAPE_CANCEL"
        );
        getRootPane().getActionMap().put("ESCAPE_CANCEL", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        main.add(headerPanel, BorderLayout.NORTH);
        main.add(optionsPanel, BorderLayout.CENTER);

        SwingUtilities.invokeLater(changeUsernameBtn::requestFocusInWindow);

        return main;
    }

    private void styleOptionButton(JButton button) {
        button.setBackground(Theme.panelBackground());
        button.setForeground(Theme.textColor());
        button.setFont(Theme.font(Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Theme.borderColor()));
    }

    public boolean isSaved() {
        return saved;
    }
}
