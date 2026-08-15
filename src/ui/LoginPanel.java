package ui;

import service.AuthService;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final AuthFrame parent;
    private final AuthService authService;

    private JTextField nameField;
    private JPasswordField passwordField;

    public LoginPanel(AuthFrame parent, AuthService authService) {
        this.parent = parent;
        this.authService = authService;

        setBackground(Theme.appBackground());
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel title = new JLabel("Log In");
        title.setFont(Theme.font(Font.BOLD, 28));
        title.setForeground(Theme.textColor());
        add(title, gbc);

        gbc.gridy++;
        add(createFieldPanel("Username", nameField = new JTextField(20)), gbc);

        gbc.gridy++;
        add(createFieldPanel("Password", passwordField = new JPasswordField(20)), gbc);

        gbc.gridy++;
        JButton loginButton = new JButton("Log In");
        loginButton.setFont(Theme.font(Font.BOLD, 14));
        loginButton.setBackground(Theme.primaryColor());
        loginButton.setForeground(Theme.buttonTextColor());
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(180, 40));
        add(loginButton, gbc);

        gbc.gridy++;
        JButton goSignupButton = new JButton("Don't have an account? Sign Up");
        goSignupButton.setFont(Theme.font(Font.PLAIN, 13));
        goSignupButton.setBorderPainted(false);
        goSignupButton.setContentAreaFilled(false);
        goSignupButton.setForeground(Theme.linkColor());
        add(goSignupButton, gbc);

        nameField.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());

        loginButton.addActionListener(e -> handleLogin());
        goSignupButton.addActionListener(e -> parent.showSignup());

        SwingUtilities.invokeLater(() -> {
            JRootPane root = getRootPane();
            if (root != null) {
                root.setDefaultButton(loginButton);
            }
            nameField.requestFocusInWindow();
        });
    }

    private JPanel createFieldPanel(String labelText, JTextField field) {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.appBackground());
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText);
        label.setFont(Theme.font(Font.PLAIN, 14));
        label.setForeground(Theme.textColor());

        Theme.styleTextField(field);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(field);

        return panel;
    }

    private void handleLogin() {
        String name = nameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (name.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        if (authService.login(name, password)) {
            parent.openMainApp();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password. Try again.");
            passwordField.setText("");
        }
    }
}
