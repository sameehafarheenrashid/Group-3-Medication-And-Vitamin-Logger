package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Theme {

    public static final String FONT_FAMILY = "Segoe UI";
    private static Boolean hasSegoeSemibold = null;

    public static Font font(int style, int size) {
        if ((style & Font.BOLD) != 0) {
            if (hasSegoeSemibold == null) {
                hasSegoeSemibold = checkFontExists("Segoe UI Semibold");
            }
            if (hasSegoeSemibold) {
                return new Font("Segoe UI Semibold", Font.PLAIN, size);
            }
        }
        return new Font(FONT_FAMILY, style, size);
    }

    private static boolean checkFontExists(String fontName) {
        try {
            for (String family : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
                if (family.equalsIgnoreCase(fontName)) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public enum ThemeMode {
        DEFAULT_THEME("Default Theme"),
        DARK_MODERN("Dark Modern"),
        SUNSET_WARMTH("Sunset Warmth");

        private final String displayName;

        ThemeMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static ThemeMode fromDisplayName(String name) {
            for (ThemeMode mode : values()) {
                if (mode.displayName.equalsIgnoreCase(name)) {
                    return mode;
                }
            }
            return DEFAULT_THEME;
        }
    }

    private static ThemeMode currentTheme = ThemeMode.DEFAULT_THEME;

    // Palette Definitions
    // 1. Default Theme (Mint Slate & Emerald Green)
    public static final Color DEFAULT_BG = hex("#0F2027");
    public static final Color DEFAULT_PANEL = hex("#1C333D");
    public static final Color DEFAULT_PANEL_2 = hex("#274856");
    public static final Color DEFAULT_PRIMARY = hex("#10B981");
    public static final Color DEFAULT_ACCENT = hex("#34D399");
    public static final Color DEFAULT_TEXT = hex("#ECFDF5");
    public static final Color DEFAULT_MUTED = hex("#A7F3D0");
    public static final Color DEFAULT_BORDER = hex("#376878");
    public static final Color DEFAULT_BTN_TEXT = hex("#064E3B");
    public static final Color DEFAULT_INPUT_BG = hex("#1C333D");
    public static final Color DEFAULT_INPUT_TEXT = hex("#FFFFFF");

    // 2. Dark Modern (OLED Night & Cyan)
    public static final Color DARK_BG = hex("#0B0F19");
    public static final Color DARK_PANEL = hex("#151E32");
    public static final Color DARK_PANEL_2 = hex("#1E2B47");
    public static final Color DARK_PRIMARY = hex("#38BDF8");
    public static final Color DARK_ACCENT = hex("#818CF8");
    public static final Color DARK_TEXT = hex("#F8FAFC");
    public static final Color DARK_MUTED = hex("#94A3B8");
    public static final Color DARK_BORDER = hex("#2A3B5C");
    public static final Color DARK_BTN_TEXT = hex("#0F172A");
    public static final Color DARK_INPUT_BG = hex("#151E32");
    public static final Color DARK_INPUT_TEXT = hex("#FFFFFF");

    // 3. Sunset Warmth (Terracotta Dark & Amber)
    public static final Color SUNSET_BG = hex("#1C1917");
    public static final Color SUNSET_PANEL = hex("#292524");
    public static final Color SUNSET_PANEL_2 = hex("#383533");
    public static final Color SUNSET_PRIMARY = hex("#F59E0B");
    public static final Color SUNSET_ACCENT = hex("#F97316");
    public static final Color SUNSET_TEXT = hex("#FAFAF9");
    public static final Color SUNSET_MUTED = hex("#D6D3D1");
    public static final Color SUNSET_BORDER = hex("#57534E");
    public static final Color SUNSET_BTN_TEXT = hex("#1C1917");
    public static final Color SUNSET_INPUT_BG = hex("#292524");
    public static final Color SUNSET_INPUT_TEXT = hex("#FFFFFF");

    private Theme() {}

    public static void setTheme(ThemeMode mode) {
        if (mode != null) {
            currentTheme = mode;
            applyToUIManager();
        }
    }

    public static ThemeMode getTheme() {
        return currentTheme;
    }

    public static boolean isDefault() {
        return currentTheme == ThemeMode.DEFAULT_THEME;
    }

    public static Color appBackground() {
        switch (currentTheme) {
            case DARK_MODERN: return DARK_BG;
            case SUNSET_WARMTH: return SUNSET_BG;
            default: return DEFAULT_BG;
        }
    }

    public static Color panelBackground() {
        switch (currentTheme) {
            case DARK_MODERN: return DARK_PANEL;
            case SUNSET_WARMTH: return SUNSET_PANEL;
            default: return DEFAULT_PANEL;
        }
    }

    public static Color secondaryPanelBackground() {
        switch (currentTheme) {
            case DARK_MODERN: return DARK_PANEL_2;
            case SUNSET_WARMTH: return SUNSET_PANEL_2;
            default: return DEFAULT_PANEL_2;
        }
    }

    public static Color primaryColor() {
        switch (currentTheme) {
            case DARK_MODERN: return DARK_PRIMARY;
            case SUNSET_WARMTH: return SUNSET_PRIMARY;
            default: return DEFAULT_PRIMARY;
        }
    }

    public static Color accentColor() {
        switch (currentTheme) {
            case DARK_MODERN: return DARK_ACCENT;
            case SUNSET_WARMTH: return SUNSET_ACCENT;
            default: return DEFAULT_ACCENT;
        }
    }

    public static Color textColor() {
        switch (currentTheme) {
            case DARK_MODERN: return DARK_TEXT;
            case SUNSET_WARMTH: return SUNSET_TEXT;
            default: return DEFAULT_TEXT;
        }
    }

    public static Color mutedTextColor() {
        switch (currentTheme) {
            case DARK_MODERN: return DARK_MUTED;
            case SUNSET_WARMTH: return SUNSET_MUTED;
            default: return DEFAULT_MUTED;
        }
    }

    public static Color borderColor() {
        switch (currentTheme) {
            case DARK_MODERN: return DARK_BORDER;
            case SUNSET_WARMTH: return SUNSET_BORDER;
            default: return DEFAULT_BORDER;
        }
    }

    public static Color linkColor() {
        return primaryColor();
    }

    public static Color buttonTextColor() {
        switch (currentTheme) {
            case DARK_MODERN: return DARK_BTN_TEXT;
            case SUNSET_WARMTH: return SUNSET_BTN_TEXT;
            default: return DEFAULT_BTN_TEXT;
        }
    }

    public static Color inputBackground() {
        switch (currentTheme) {
            case DARK_MODERN: return DARK_INPUT_BG;
            case SUNSET_WARMTH: return SUNSET_INPUT_BG;
            default: return DEFAULT_INPUT_BG;
        }
    }

    public static Color inputTextColor() {
        switch (currentTheme) {
            case DARK_MODERN: return DARK_INPUT_TEXT;
            case SUNSET_WARMTH: return SUNSET_INPUT_TEXT;
            default: return DEFAULT_INPUT_TEXT;
        }
    }

    public static Color successColor() {
        return hex("#34D399");
    }

    public static Color warningColor() {
        return hex("#FBBF24");
    }

    public static Color dangerColor() {
        return hex("#DB0B1E");
    }

    public static Color prescriptionColor() {
        return hex("#E899DC");
    }

    public static Color selectionTextColor() {
        return buttonTextColor();
    }

    public static String textColorToHex() {
        Color c = textColor();
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    public static String mutedTextColorToHex() {
        Color c = mutedTextColor();
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    public static Color hex(String hex) {
        return Color.decode(hex);
    }

    public static void applyToUIManager() {
        Color bg = appBackground();
        Color panelBg = panelBackground();
        Color text = textColor();
        Color border = borderColor();
        Color primary = primaryColor();
        Color btnText = buttonTextColor();
        Color inputBg = inputBackground();
        Color inputText = inputTextColor();

        UIManager.put("Panel.background", bg);
        UIManager.put("OptionPane.background", panelBg);
        UIManager.put("OptionPane.messageForeground", text);
        UIManager.put("Label.foreground", text);
        UIManager.put("Label.font", font(Font.PLAIN, 14));
        UIManager.put("Button.background", primary);
        UIManager.put("Button.foreground", btnText);
        UIManager.put("Button.font", font(Font.BOLD, 13));
        UIManager.put("TextField.background", inputBg);
        UIManager.put("TextField.foreground", inputText);
        UIManager.put("TextField.caretForeground", inputText);
        UIManager.put("PasswordField.background", inputBg);
        UIManager.put("PasswordField.foreground", inputText);
        UIManager.put("PasswordField.caretForeground", inputText);
        UIManager.put("TextArea.background", inputBg);
        UIManager.put("TextArea.foreground", inputText);
        UIManager.put("TextArea.caretForeground", inputText);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.foreground", hex("#1E293B"));
        UIManager.put("ComboBox.selectionBackground", primary);
        UIManager.put("ComboBox.selectionForeground", btnText);
        UIManager.put("Table.background", panelBg);
        UIManager.put("Table.foreground", text);
        UIManager.put("Table.gridColor", border);
        UIManager.put("Table.selectionBackground", primary);
        UIManager.put("Table.selectionForeground", btnText);
        UIManager.put("TableHeader.background", panelBg);
        UIManager.put("TableHeader.foreground", text);
        UIManager.put("ScrollPane.background", panelBg);
        UIManager.put("Viewport.background", panelBg);
        UIManager.put("ToolTip.background", secondaryPanelBackground());
        UIManager.put("ToolTip.foreground", text);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(border));
        UIManager.put("TitledBorder.titleColor", text);
        UIManager.put("TitledBorder.border", BorderFactory.createLineBorder(border));
        UIManager.put("TitledBorder.font", font(Font.BOLD, 15));
    }

    public static void styleTextField(JTextField field) {
        field.setBackground(inputBackground());
        field.setForeground(inputTextColor());
        field.setCaretColor(inputTextColor());
        field.setFont(font(Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor()),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    public static void styleTextArea(JTextArea area) {
        area.setBackground(inputBackground());
        area.setForeground(inputTextColor());
        area.setCaretColor(inputTextColor());
        area.setFont(font(Font.PLAIN, 14));
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor()),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    public static <T> void styleComboBox(JComboBox<T> comboBox) {
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(hex("#1E293B"));
        comboBox.setFont(font(Font.PLAIN, 13));
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor()),
                new EmptyBorder(3, 6, 3, 6)
        ));
        comboBox.setRenderer(createCustomComboBoxRenderer());
    }

    @SuppressWarnings("unchecked")
    public static <T> ListCellRenderer<T> createCustomComboBoxRenderer() {
        return (ListCellRenderer<T>) new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (list != null) {
                    list.setBackground(Color.WHITE);
                    list.setSelectionBackground(primaryColor());
                    list.setSelectionForeground(buttonTextColor());
                }
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(font(Font.PLAIN, 13));

                if (index == -1) {
                    // Closed JComboBox main display box
                    label.setOpaque(false);
                    label.setBackground(Color.WHITE);
                    label.setForeground(hex("#1E293B"));
                    label.setBorder(new EmptyBorder(2, 4, 2, 4));
                } else {
                    // Popup dropdown menu list item
                    label.setOpaque(true);
                    label.setBorder(new EmptyBorder(6, 10, 6, 10));
                    if (isSelected) {
                        label.setBackground(primaryColor());
                        label.setForeground(buttonTextColor());
                    } else {
                        label.setBackground(Color.WHITE);
                        label.setForeground(hex("#1E293B"));
                    }
                }
                return label;
            }
        };
    }
}
