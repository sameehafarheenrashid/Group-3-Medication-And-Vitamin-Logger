package ui;

import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EditUsernameDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JTextField newUsernameField;
    private boolean saved = false;

    public EditUsernameDialog(Window owner, AuthService authService) {
        super(owner, "Change Username", ModalityType.APPLICATION_MODAL);
        IconUtil.applyAppIcon(this);
        setSize(450, 240);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        newUsernameField = new JTextField(authService.getCurrentUsername(), 20);

        setContentPane(createUI(authService));
    }

    private JPanel createUI(AuthService authService) {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBackground(Theme.appBackground());
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Change Username");
        title.setFont(Theme.font(Font.BOLD, 22));
        title.setForeground(Theme.textColor());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.appBackground());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel currentLabel = new JLabel("Current Username:");
        currentLabel.setForeground(Theme.mutedTextColor());
        form.add(currentLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JLabel currentValueLabel = new JLabel(authService.getCurrentUsername());
        currentValueLabel.setFont(Theme.font(Font.BOLD, 14));
        currentValueLabel.setForeground(Theme.textColor());
        form.add(currentValueLabel, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel newLabel = new JLabel("New Username:");
        newLabel.setForeground(Theme.textColor());
        form.add(newLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        Theme.styleTextField(newUsernameField);
        form.add(newUsernameField, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setBackground(Theme.appBackground());

        JButton cancel = new JButton("Cancel");
        JButton save = new JButton("Save Username");

        cancel.setBackground(Theme.panelBackground());
        cancel.setForeground(Theme.textColor());
        cancel.addActionListener(e -> dispose());

        save.setBackground(Theme.primaryColor());
        save.setForeground(Theme.buttonTextColor());
        save.addActionListener(e -> {
            String newUsername = newUsernameField.getText().trim();

            if (newUsername.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username cannot be empty.", "Invalid Username", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (authService.updateUsername(newUsername)) {
                saved = true;
                JOptionPane.showMessageDialog(this, "Username updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Username already taken by another account.", "Username Error", JOptionPane.ERROR_MESSAGE);
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

        SwingUtilities.invokeLater(newUsernameField::requestFocusInWindow);

        buttons.add(cancel);
        buttons.add(save);

        main.add(title, BorderLayout.NORTH);
        main.add(form, BorderLayout.CENTER);
        main.add(buttons, BorderLayout.SOUTH);
        return main;
    }

    public boolean isSaved() {
        return saved;
    }
}
