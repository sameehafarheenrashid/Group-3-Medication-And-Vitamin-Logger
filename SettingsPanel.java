package ui;

import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final MainFrame parentFrame;
    private final AuthService authService;
    private final JComboBox<String> themeComboBox;
    private final JLabel usernameValueLabel;
    private boolean initializingTheme = true;

    public SettingsPanel(MainFrame parentFrame, AuthService authService) {
        this.parentFrame = parentFrame;
        this.authService = authService;

        setLayout(new BorderLayout(15, 15));
        setBackground(Theme.appBackground());
        setBorder(new EmptyBorder(24, 24, 24, 24));

        themeComboBox = createThemeComboBox();
        usernameValueLabel = new JLabel(authService.getCurrentUsername());
        usernameValueLabel.setFont(Theme.font(Font.BOLD, 14));
        usernameValueLabel.setForeground(Theme.textColor());

        add(createHeader(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);

        syncThemeSelection();
        initializingTheme = false;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        header.setBackground(Theme.appBackground());
        header.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel title = new JLabel("Settings", SwingConstants.CENTER);
        title.setFont(Theme.font(Font.BOLD, 28));
        title.setForeground(Theme.textColor());

        header.add(title);

        return header;
    }

    private JPanel createCenterPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Theme.appBackground());

        // 1. Account Card
        JPanel accountCard = createCard("Account Settings");
        
        JLabel userTitleLabel = new JLabel("Logged in as:");
        userTitleLabel.setFont(Theme.font(Font.PLAIN, 14));
        userTitleLabel.setForeground(Theme.mutedTextColor());
        userTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonRow.setBackground(Theme.panelBackground());
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton changeUsernameButton = new JButton("Change Username");
        styleSecondaryButton(changeUsernameButton);
        changeUsernameButton.addActionListener(e -> openChangeUsernameDialog());

        JButton changePasswordButton = new JButton("Change Password");
        styleSecondaryButton(changePasswordButton);
        changePasswordButton.addActionListener(e -> openChangePasswordDialog());

        buttonRow.add(changeUsernameButton);
        buttonRow.add(Box.createRigidArea(new Dimension(12, 0)));
        buttonRow.add(changePasswordButton);

        accountCard.add(userTitleLabel);
        accountCard.add(Box.createVerticalStrut(4));
        accountCard.add(usernameValueLabel);
        accountCard.add(Box.createVerticalStrut(14));
        accountCard.add(buttonRow);

        // 2. Theme Card
        JPanel themeCard = createCard("Appearance Theme");

        themeComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        themeComboBox.setMaximumSize(new Dimension(240, 36));
        themeComboBox.setPreferredSize(new Dimension(240, 36));

        themeCard.add(themeComboBox);

        // 3. Direct Logout Button centered
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoutPanel.setBackground(Theme.appBackground());
        logoutPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton logoutButton = new JButton("Log Out");
        styleDangerButton(logoutButton);
        logoutButton.addActionListener(e -> parentFrame.logout());

        logoutPanel.add(logoutButton);

        container.add(accountCard);
        container.add(Box.createVerticalStrut(15));
        container.add(themeCard);
        container.add(Box.createVerticalStrut(22));
        container.add(logoutPanel);
        container.add(Box.createVerticalGlue());

        return container;
    }

    private JPanel createCard(String titleText) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.panelBackground());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.borderColor()),
                new EmptyBorder(16, 20, 16, 20)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JLabel title = new JLabel(titleText);
        title.setFont(Theme.font(Font.BOLD, 18));
        title.setForeground(Theme.textColor());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(10));

        return card;
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(Theme.font(Font.PLAIN, 13));
        button.setBackground(Theme.secondaryPanelBackground());
        button.setForeground(Theme.textColor());
        button.setFocusPainted(false);
        button.setMargin(new Insets(8, 16, 8, 16));
        button.setPreferredSize(new Dimension(180, 36));
    }

    private void styleDangerButton(JButton button) {
        button.setFont(Theme.font(Font.BOLD, 13));
        button.setBackground(Theme.dangerColor());
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setMargin(new Insets(8, 16, 8, 16));
        button.setPreferredSize(new Dimension(160, 38));
        button.setMaximumSize(new Dimension(160, 38));
    }

    private JComboBox<String> createThemeComboBox() {
        String[] themeNames = new String[Theme.ThemeMode.values().length];
        Theme.ThemeMode[] modes = Theme.ThemeMode.values();
        for (int i = 0; i < modes.length; i++) {
            themeNames[i] = modes[i].getDisplayName();
        }

        JComboBox<String> comboBox = new JComboBox<>(themeNames);
        Theme.styleComboBox(comboBox);

        comboBox.addActionListener(e -> {
            if (initializingTheme) return;

            Object selected = comboBox.getSelectedItem();
            if (selected != null) {
                Theme.ThemeMode mode = Theme.ThemeMode.fromDisplayName(selected.toString());
                Theme.setTheme(mode);
                parentFrame.refreshTheme();
            }
        });

        return comboBox;
    }

    private void syncThemeSelection() {
        themeComboBox.setSelectedItem(Theme.getTheme().getDisplayName());
    }

    private void openChangeUsernameDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        EditUsernameDialog dialog = new EditUsernameDialog(owner, authService);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            usernameValueLabel.setText(authService.getCurrentUsername());
            revalidate();
            repaint();
        }
    }

    private void openChangePasswordDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ChangePasswordDialog dialog = new ChangePasswordDialog(owner, authService);
        dialog.setVisible(true);
    }
}
