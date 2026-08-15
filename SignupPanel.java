package ui;

import service.AuthService;

import javax.swing.*;
import java.awt.*;

public class SignupPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final AuthFrame parent;
    private final AuthService authService;

    private JTextField nameField;
    private JPasswordField passwordField;

    public SignupPanel(AuthFrame parent, AuthService authService) {
        this.parent = parent;
        this.authService = authService;

        setBackground(Theme.appBackground());
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel title = new JLabel("Sign Up");
        title.setFont(Theme.font(Font.BOLD, 28));
        title.setForeground(Theme.textColor());
        add(title, gbc);

        gbc.gridy++;
        add(createFieldPanel("Username", nameField = new JTextField(20)), gbc);

        gbc.gridy++;
        add(createFieldPanel("Password", passwordField = new JPasswordField(20)), gbc);

        gbc.gridy++;
        JButton signupButton = new JButton("Create Account");
        signupButton.setFont(Theme.font(Font.BOLD, 14));
        signupButton.setBackground(Theme.primaryColor());
        signupButton.setForeground(Theme.buttonTextColor());
        signupButton.setFocusPainted(false);
        signupButton.setPreferredSize(new Dimension(180, 40));
        add(signupButton, gbc);

        gbc.gridy++;
        JButton goLoginButton = new JButton("Already have an account? Log In");
        goLoginButton.setFont(Theme.font(Font.PLAIN, 13));
        goLoginButton.setBorderPainted(false);
        goLoginButton.setContentAreaFilled(false);
        goLoginButton.setForeground(Theme.linkColor());
        add(goLoginButton, gbc);

        nameField.addActionListener(e -> handleSignup());
        passwordField.addActionListener(e -> handleSignup());

        signupButton.addActionListener(e -> handleSignup());
        goLoginButton.addActionListener(e -> parent.showLogin());

        SwingUtilities.invokeLater(() -> {
            JRootPane root = getRootPane();
            if (root != null) {
                root.setDefaultButton(signupButton);
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

    private void handleSignup() {
        String name = nameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (name.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Missing information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long.", "Invalid password", JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocusInWindow();
            return;
        }

        if (authService.signUp(name, password)) {
            JOptionPane.showMessageDialog(this, "Account created successfully!");
            nameField.setText("");
            passwordField.setText("");
            parent.openMainApp();
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists. Please choose another.", "Signup Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
