package ui;

import service.AuthService;
import service.MedicationSchedulerService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final AuthService authService;
    private final MedicationSchedulerService schedulerService;
    private JTabbedPane tabbedPane;
    private Timer autoRefreshTimer;
    private TrayIcon trayIcon;

    public MainFrame(AuthService authService) {
        this.authService = authService;
        this.schedulerService = new MedicationSchedulerService();
        this.schedulerService.setAuthService(authService);

        if (authService.getCurrentPatient() != null) {
            this.schedulerService.setPatient(authService.getCurrentPatient());
        }
        
        initializeUI();
        setupSystemTray();
        schedulerService.startScheduler(this);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
                if (SystemTray.isSupported() && trayIcon != null) {
                    setVisible(false);
                    trayIcon.displayMessage(
                            "Medicine and Vitamin Logger",
                            "App is running in the background. Double-click tray icon to restore.",
                            TrayIcon.MessageType.INFO
                    );
                }
            }

            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowCloseRequest();
            }
        });
    }

    private void initializeUI() {
        setTitle("Medicine and Vitamin Logger");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.appBackground());
        IconUtil.applyAppIcon(this);

        tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        tabbedPane.setFont(Theme.font(Font.BOLD, 14));
        tabbedPane.setBackground(Theme.appBackground());
        tabbedPane.setForeground(Theme.textColor());

        tabbedPane.addChangeListener(e -> {
            updateTabComponentStyles();
            refreshActiveTab();
        });

        refreshTabs();

        add(tabbedPane, BorderLayout.CENTER);

        // Auto-refresh active view every 15 seconds
        autoRefreshTimer = new Timer(15000, e -> refreshActiveTab());
        autoRefreshTimer.start();
    }

    private void handleWindowCloseRequest() {
        boolean trayAvailable = SystemTray.isSupported() && trayIcon != null;
        CloseAppDialog dialog = new CloseAppDialog(this, trayAvailable);
        dialog.setVisible(true);

        int choice = dialog.getChoice();
        if (choice == 0) {
            performFullExit();
        } else if (choice == 1) {
            setVisible(false);
            if (trayIcon != null) {
                trayIcon.displayMessage(
                        "Medicine and Vitamin Logger",
                        "App is running in the background. Double-click tray icon to restore.",
                        TrayIcon.MessageType.INFO
                );
            }
        }
    }

    private static class CloseAppDialog extends JDialog {
        private int choice = -1;

        CloseAppDialog(Frame owner, boolean trayAvailable) {
            super(owner, "Close Application", ModalityType.APPLICATION_MODAL);
            IconUtil.applyAppIcon(this);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setSize(520, 175);
            setResizable(false);
            setLocationRelativeTo(owner);

            JPanel main = new JPanel(new BorderLayout(15, 15));
            main.setBackground(Theme.appBackground());
            main.setBorder(new javax.swing.border.EmptyBorder(18, 20, 18, 20));

            JLabel subtitle = new JLabel(trayAvailable ? "What would you like to do?" : "Are you sure you want to exit?");
            subtitle.setFont(Theme.font(Font.BOLD, 20));
            subtitle.setForeground(Theme.textColor());

            JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            header.setBackground(Theme.appBackground());
            header.add(subtitle);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            buttons.setBackground(Theme.appBackground());

            JButton exitBtn = new JButton("Exit Program");
            exitBtn.setFont(Theme.font(Font.BOLD, 13));
            exitBtn.setBackground(Theme.dangerColor());
            exitBtn.setForeground(Color.WHITE);
            exitBtn.setFocusPainted(false);
            exitBtn.setPreferredSize(new Dimension(170, 42));

            JButton minimizeBtn = new JButton("Minimize to System Tray");
            minimizeBtn.setFont(Theme.font(Font.BOLD, 13));
            minimizeBtn.setBackground(Theme.primaryColor());
            minimizeBtn.setForeground(Theme.buttonTextColor());
            minimizeBtn.setFocusPainted(false);
            minimizeBtn.setPreferredSize(new Dimension(230, 42));

            exitBtn.addActionListener(e -> {
                choice = 0;
                dispose();
            });

            minimizeBtn.addActionListener(e -> {
                choice = 1;
                dispose();
            });

            java.awt.event.KeyAdapter keyNav = new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent e) {
                    int code = e.getKeyCode();
                    if (code == java.awt.event.KeyEvent.VK_LEFT || code == java.awt.event.KeyEvent.VK_UP) {
                        exitBtn.requestFocusInWindow();
                    } else if (code == java.awt.event.KeyEvent.VK_RIGHT || code == java.awt.event.KeyEvent.VK_DOWN) {
                        if (trayAvailable) {
                            minimizeBtn.requestFocusInWindow();
                        }
                    } else if (code == java.awt.event.KeyEvent.VK_ENTER) {
                        Component focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                        if (focus == exitBtn) {
                            exitBtn.doClick();
                        } else if (focus == minimizeBtn) {
                            minimizeBtn.doClick();
                        }
                    } else if (code == java.awt.event.KeyEvent.VK_ESCAPE) {
                        choice = -1;
                        dispose();
                    }
                }
            };

            exitBtn.addKeyListener(keyNav);
            minimizeBtn.addKeyListener(keyNav);

            buttons.add(exitBtn);
            if (trayAvailable) {
                buttons.add(minimizeBtn);
            }

            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "ESCAPE_CANCEL"
            );
            getRootPane().getActionMap().put("ESCAPE_CANCEL", new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    choice = -1;
                    dispose();
                }
            });

            main.add(header, BorderLayout.NORTH);
            main.add(buttons, BorderLayout.CENTER);

            setContentPane(main);

            SwingUtilities.invokeLater(() -> {
                if (trayAvailable) {
                    minimizeBtn.requestFocusInWindow();
                } else {
                    exitBtn.requestFocusInWindow();
                }
            });
        }

        int getChoice() {
            return choice;
        }
    }

    private void performFullExit() {
        if (authService != null) {
            authService.saveData();
        }
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }
        if (schedulerService != null) {
            schedulerService.stopScheduler();
        }
        dispose();
        System.exit(0);
    }

    private void setupSystemTray() {
        if (!SystemTray.isSupported()) return;

        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image iconImage = createTrayIconImage();

            PopupMenu popup = new PopupMenu();

            MenuItem openItem = new MenuItem("Open Medicine and Vitamin Logger");
            openItem.setFont(Theme.font(Font.BOLD, 12));
            openItem.addActionListener(e -> restoreFromTray());

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> performFullExit());

            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);

            trayIcon = new TrayIcon(iconImage, "Medicine and Vitamin Logger", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> restoreFromTray());

            tray.add(trayIcon);
            schedulerService.setTrayIcon(trayIcon);
        } catch (Exception e) {
            System.err.println("Could not setup system tray: " + e.getMessage());
        }
    }

    private Image createTrayIconImage() {
        return IconUtil.getAppIconImage(32, 32);
    }

    private void restoreFromTray() {
        setVisible(true);
        setState(Frame.NORMAL);
        toFront();
    }

    private void refreshActiveTab() {
        if (tabbedPane == null) return;

        // Do not auto-refresh if a popup menu (e.g. JComboBox dropdown or JPopupMenu) is open
        MenuSelectionManager msm = MenuSelectionManager.defaultManager();
        if (msm.getSelectedPath() != null && msm.getSelectedPath().length > 0) {
            return;
        }

        // Do not auto-refresh if a modal dialog is currently active
        Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (activeWindow instanceof JDialog && ((JDialog) activeWindow).isModal()) {
            return;
        }

        Component selected = tabbedPane.getSelectedComponent();
        if (selected instanceof DashboardPanel) {
            ((DashboardPanel) selected).refreshDashboard();
        } else if (selected instanceof HistoryPanel) {
            ((HistoryPanel) selected).refreshHistory();
        } else if (selected instanceof MedicationManagerPanel) {
            ((MedicationManagerPanel) selected).refreshTableData();
        }
    }

    public void refreshTabs() {
        getContentPane().setBackground(Theme.appBackground());
        tabbedPane.setBackground(Theme.appBackground());
        tabbedPane.setForeground(Theme.textColor());

        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex < 0) selectedIndex = 0;

        tabbedPane.removeAll();
        tabbedPane.addTab("Dashboard", new DashboardPanel(authService));
        tabbedPane.addTab("Medications", new MedicationManagerPanel(authService, schedulerService));
        tabbedPane.addTab("History", new HistoryPanel(authService));
        tabbedPane.addTab("Settings", new SettingsPanel(this, authService));

        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setTabComponentAt(i, createCustomTabComponent(tabbedPane.getTitleAt(i)));
        }

        if (selectedIndex < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(selectedIndex);
        }

        updateTabComponentStyles();
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    private Component createCustomTabComponent(String title) {
        JPanel floppyDisk = new JPanel(new BorderLayout());
        floppyDisk.setOpaque(true);
        floppyDisk.setPreferredSize(new Dimension(155, 46));
        floppyDisk.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.borderColor(), 2),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));

        // Metal Shutter Slider Bar at Top
        JPanel shutter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 1));
        shutter.setOpaque(true);
        shutter.setPreferredSize(new Dimension(150, 10));

        JPanel notch = new JPanel();
        notch.setPreferredSize(new Dimension(12, 6));
        notch.setBackground(new Color(30, 41, 59));
        shutter.add(notch);

        // Label Area (The floppy disk paper label)
        JPanel labelArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        labelArea.setOpaque(true);

        JLabel iconLabel;
        if ("Settings".equals(title)) {
            iconLabel = new JLabel(IconUtil.createGearIcon(16, Theme.textColor()));
        } else {
            iconLabel = new JLabel(getTabEmoji(title));
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        }

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.font(Font.BOLD, 13));

        labelArea.add(iconLabel);
        labelArea.add(titleLabel);

        floppyDisk.add(shutter, BorderLayout.NORTH);
        floppyDisk.add(labelArea, BorderLayout.CENTER);

        return floppyDisk;
    }

    private String getTabEmoji(String title) {
        if (title == null) return "📌";
        switch (title) {
            case "Dashboard":
                return "📊";
            case "Medications":
                return "💊";
            case "History":
                return "📜";
            case "Settings":
                return "⚙️";
            default:
                return "📌";
        }
    }

    private void updateTabComponentStyles() {
        if (tabbedPane == null) return;
        int selectedIndex = tabbedPane.getSelectedIndex();
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component c = tabbedPane.getTabComponentAt(i);
            if (c instanceof JPanel) {
                JPanel floppyDisk = (JPanel) c;
                boolean isSelected = (i == selectedIndex);

                if (floppyDisk.getComponentCount() >= 2) {
                    JPanel shutter = (JPanel) floppyDisk.getComponent(0);
                    JPanel labelArea = (JPanel) floppyDisk.getComponent(1);

                    if (labelArea.getComponentCount() >= 2) {
                        JLabel iconLabel = (JLabel) labelArea.getComponent(0);
                        JLabel titleLabel = (JLabel) labelArea.getComponent(1);

                        if ("Settings".equals(titleLabel.getText())) {
                            Color gearColor = isSelected ? Theme.buttonTextColor() : Theme.textColor();
                            iconLabel.setIcon(IconUtil.createGearIcon(16, gearColor));
                            iconLabel.setText("");
                        }

                        if (isSelected) {
                            floppyDisk.setBackground(Theme.primaryColor());
                            floppyDisk.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(Theme.accentColor(), 2),
                                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
                            ));
                            shutter.setBackground(new Color(148, 163, 184));
                            labelArea.setBackground(Theme.primaryColor());
                            titleLabel.setForeground(Theme.buttonTextColor());
                            iconLabel.setForeground(Theme.buttonTextColor());
                        } else {
                            floppyDisk.setBackground(Theme.panelBackground());
                            floppyDisk.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(Theme.borderColor(), 2),
                                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
                            ));
                            shutter.setBackground(new Color(71, 85, 105));
                            labelArea.setBackground(Theme.secondaryPanelBackground());
                            titleLabel.setForeground(Theme.textColor());
                            iconLabel.setForeground(Theme.textColor());
                        }
                    }
                }
            }
        }
    }

    public void refreshTheme() {
        Theme.applyToUIManager();
        refreshTabs();
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }

    public void logout() {
        if (authService != null) {
            authService.logout();
            authService.saveData();
        }
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }
        schedulerService.stopScheduler();
        dispose();
        new AuthFrame(authService).setVisible(true);
    }
}
