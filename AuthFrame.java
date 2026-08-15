package ui;

import service.AuthService;

import javax.swing.*;
import java.awt.*;

public class AuthFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final AuthService authService;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    public AuthFrame() {
        this(new AuthService());
    }

    public AuthFrame(AuthService authService) {
        this.authService = authService;

        setTitle("Medicine and Vitamin Logger - Authentication");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(null);
        setResizable(false);
        IconUtil.applyAppIcon(this);

        JPanel leftPanel = createLeftPanel();
        add(leftPanel, BorderLayout.WEST);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        LoginPanel loginPanel = new LoginPanel(this, authService);
        SignupPanel signupPanel = new SignupPanel(this, authService);

        cardPanel.add(loginPanel, "LOGIN");
        cardPanel.add(signupPanel, "SIGNUP");

        add(cardPanel, BorderLayout.CENTER);

        cardLayout.show(cardPanel, "LOGIN");
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(320, 550));
        panel.setBackground(Theme.panelBackground());
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(Box.createVerticalGlue());

        JLabel iconLabel = new JLabel(IconUtil.getAppIcon(100, 100));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("WELCOME");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Theme.textColor());
        title.setFont(Theme.font(Font.BOLD, 32));

        JLabel subtitle = new JLabel("Medicine and Vitamin Logger");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(Theme.mutedTextColor());
        subtitle.setFont(Theme.font(Font.ITALIC, 15));

        panel.add(iconLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtitle);

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    public void showLogin() {
        cardLayout.show(cardPanel, "LOGIN");
    }

    public void showSignup() {
        cardLayout.show(cardPanel, "SIGNUP");
    }

    public void openMainApp() {
        dispose();
        new MainFrame(authService).setVisible(true);
    }
}
