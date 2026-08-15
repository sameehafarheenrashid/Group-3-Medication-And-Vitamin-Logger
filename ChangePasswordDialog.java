package ui;

import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JPasswordField currentPasswordField;
    private final JPasswordField newPasswordField;
    private final JPasswordField confirmPasswordField;
    private boolean saved = false;

    public ChangePasswordDialog(Window owner, AuthService authService) {
        super(owner, "Change Password", ModalityType.APPLICATION_MODAL);
        IconUtil.applyAppIcon(this);
        setSize(480, 320);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        currentPasswordField = new JPasswordField(20);
        newPasswordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);

        setContentPane(createUI(authService));
    }

    private JPanel createUI(AuthService authService) {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBackground(Theme.appBackground());
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Change Password");
        title.setFont(Theme.font(Font.BOLD, 22));
        title.setForeground(Theme.textColor());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.appBackground());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        addRow(form, gbc, 0, "Current Password", currentPasswordField);
        addRow(form, gbc, 1, "New Password", newPasswordField);
        addRow(form, gbc, 2, "Confirm New Password", confirmPasswordField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setBackground(Theme.appBackground());

        JButton cancel = new JButton("Cancel");
        JButton save = new JButton("Save Password");

        cancel.setBackground(Theme.panelBackground());
        cancel.setForeground(Theme.textColor());
        cancel.addActionListener(e -> dispose());

        save.setBackground(Theme.primaryColor());
        save.setForeground(Theme.buttonTextColor());
        save.addActionListener(e -> {
            String currentPw = new String(currentPasswordField.getPassword());
            String newPw = new String(newPasswordField.getPassword());
            String confirmPw = new String(confirmPasswordField.getPassword());

            if (currentPw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your current password.", "Current Password Required", JOptionPane.WARNING_MESSAGE);
                currentPasswordField.requestFocusInWindow();
                return;
            }

            if (!authService.verifyCurrentPassword(currentPw)) {
                JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Verification Failed", JOptionPane.ERROR_MESSAGE);
                currentPasswordField.setText("");
                currentPasswordField.requestFocusInWindow();
                return;
            }

            if (newPw.length() < 4) {
                JOptionPane.showMessageDialog(this, "New password must be at least 4 characters long.", "Invalid Password", JOptionPane.WARNING_MESSAGE);
                newPasswordField.requestFocusInWindow();
                return;
            }

            if (!newPw.equals(confirmPw)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.", "Password Mismatch", JOptionPane.WARNING_MESSAGE);
                confirmPasswordField.requestFocusInWindow();
                return;
            }

            if (authService.updatePassword(currentPw, newPw)) {
                saved = true;
                JOptionPane.showMessageDialog(this, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Could not update password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        getRootPane().setDefaultButton(save);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "ENTER_SAVE"
        );
        getRootPane().getActionMap().put("ENTER_SAVE", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                save.doClick();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "ESCAPE_CANCEL"
        );
        getRootPane().getActionMap().put("ESCAPE_CANCEL", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cancel.doClick();
            }
        });

        SwingUtilities.invokeLater(currentPasswordField::requestFocusInWindow);

        buttons.add(cancel);
        buttons.add(save);

        main.add(title, BorderLayout.NORTH);
        main.add(form, BorderLayout.CENTER);
        main.add(buttons, BorderLayout.SOUTH);
        return main;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel label = new JLabel(labelText);
        label.setForeground(Theme.textColor());
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        if (field instanceof JTextField tf) {
            Theme.styleTextField(tf);
        }
        panel.add(field, gbc);
    }

    public boolean isSaved() {
        return saved;
    }
}
