package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DatePickerButton extends JButton {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US);

    private static final int MIN_YEAR = 2021;
    private static final int MAX_YEAR = 2035;

    private LocalDate selectedDate;
    private YearMonth viewYearMonth;

    private JPopupMenu calendarPopup;
    private JLabel monthYearLabel;
    private JButton prevMonthBtn;
    private JButton nextMonthBtn;
    private JButton prevYearBtn;
    private JButton nextYearBtn;
    private final JButton[] dayButtons = new JButton[42];

    public DatePickerButton() {
        this(LocalDate.now());
    }

    public DatePickerButton(LocalDate initialDate) {
        this.selectedDate = clampDate(initialDate != null ? initialDate : LocalDate.now());
        this.viewYearMonth = YearMonth.from(this.selectedDate);

        setText("📅  " + this.selectedDate.format(DISPLAY_FORMATTER));
        setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        setBackground(Theme.inputBackground());
        setForeground(Theme.inputTextColor());
        setFocusPainted(false);
        setHorizontalAlignment(SwingConstants.LEFT);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.borderColor()),
                new EmptyBorder(6, 12, 6, 12)
        ));

        addActionListener(e -> showCalendarPopup());
    }

    private LocalDate clampDate(LocalDate date) {
        if (date.getYear() < MIN_YEAR) return LocalDate.of(MIN_YEAR, 1, 1);
        if (date.getYear() > MAX_YEAR) return LocalDate.of(MAX_YEAR, 12, 31);
        return date;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate date) {
        if (date == null) return;
        this.selectedDate = clampDate(date);
        this.viewYearMonth = YearMonth.from(this.selectedDate);
        setText("📅  " + this.selectedDate.format(DISPLAY_FORMATTER));
    }

    private void showCalendarPopup() {
        if (calendarPopup != null && calendarPopup.isVisible()) {
            calendarPopup.setVisible(false);
            return;
        }

        calendarPopup = new JPopupMenu();
        calendarPopup.setBackground(Theme.panelBackground());
        calendarPopup.setBorder(BorderFactory.createLineBorder(Theme.borderColor(), 2));

        JPanel calendarPanel = buildCalendarPanel();
        calendarPopup.add(calendarPanel);

        updateCalendarGrid();

        calendarPopup.show(this, 0, getHeight() + 2);
    }

    private JPanel buildCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Theme.panelBackground());
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Header Panel with Year and Month controls
        JPanel header = new JPanel(new BorderLayout(4, 0));
        header.setBackground(Theme.panelBackground());

        prevYearBtn = new JButton("◄Y");
        styleNavButton(prevYearBtn);
        prevYearBtn.setToolTipText("Previous Year (Min 2021)");
        prevYearBtn.addActionListener(e -> {
            if (viewYearMonth.getYear() > MIN_YEAR) {
                viewYearMonth = viewYearMonth.minusYears(1);
                updateCalendarGrid();
            }
        });

        prevMonthBtn = new JButton("◄");
        styleNavButton(prevMonthBtn);
        prevMonthBtn.setToolTipText("Previous Month");
        prevMonthBtn.addActionListener(e -> {
            YearMonth prev = viewYearMonth.minusMonths(1);
            if (prev.getYear() >= MIN_YEAR) {
                viewYearMonth = prev;
                updateCalendarGrid();
            }
        });

        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        leftNav.setBackground(Theme.panelBackground());
        leftNav.add(prevYearBtn);
        leftNav.add(prevMonthBtn);

        nextMonthBtn = new JButton("►");
        styleNavButton(nextMonthBtn);
        nextMonthBtn.setToolTipText("Next Month");
        nextMonthBtn.addActionListener(e -> {
            YearMonth next = viewYearMonth.plusMonths(1);
            if (next.getYear() <= MAX_YEAR) {
                viewYearMonth = next;
                updateCalendarGrid();
            }
        });

        nextYearBtn = new JButton("Y►");
        styleNavButton(nextYearBtn);
        nextYearBtn.setToolTipText("Next Year (Max 2035)");
        nextYearBtn.addActionListener(e -> {
            if (viewYearMonth.getYear() < MAX_YEAR) {
                viewYearMonth = viewYearMonth.plusYears(1);
                updateCalendarGrid();
            }
        });

        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        rightNav.setBackground(Theme.panelBackground());
        rightNav.add(nextMonthBtn);
        rightNav.add(nextYearBtn);

        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setFont(Theme.font(Font.BOLD, 15));
        monthYearLabel.setForeground(Theme.textColor());

        header.add(leftNav, BorderLayout.WEST);
        header.add(monthYearLabel, BorderLayout.CENTER);
        header.add(rightNav, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        // Grid Container (Fixed 7 columns x 7 rows)
        JPanel gridContainer = new JPanel(new GridLayout(7, 7, 4, 4));
        gridContainer.setBackground(Theme.panelBackground());
        gridContainer.setPreferredSize(new Dimension(300, 220));

        String[] weekDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String dayName : weekDays) {
            JLabel lbl = new JLabel(dayName, SwingConstants.CENTER);
            lbl.setFont(Theme.font(Font.BOLD, 12));
            lbl.setForeground(Theme.mutedTextColor());
            gridContainer.add(lbl);
        }

        // Initialize 42 fixed day buttons
        for (int i = 0; i < 42; i++) {
            JButton dayBtn = new JButton();
            dayBtn.setFont(Theme.font(Font.PLAIN, 13));
            dayBtn.setFocusPainted(false);
            dayBtn.setPreferredSize(new Dimension(38, 28));

            dayBtn.addActionListener(e -> {
                String text = dayBtn.getText();
                if (!text.isEmpty()) {
                    int dayNum = Integer.parseInt(text);
                    LocalDate btnDate = viewYearMonth.atDay(dayNum);
                    setSelectedDate(btnDate);
                    if (calendarPopup != null) {
                        calendarPopup.setVisible(false);
                    }
                }
            });

            dayButtons[i] = dayBtn;
            gridContainer.add(dayBtn);
        }

        panel.add(gridContainer, BorderLayout.CENTER);

        return panel;
    }

    private void styleNavButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setFont(Theme.font(Font.BOLD, 11));
        btn.setBackground(Theme.secondaryPanelBackground());
        btn.setForeground(Theme.textColor());
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.borderColor()),
                new EmptyBorder(3, 6, 3, 6)
        ));
    }

    private void updateCalendarGrid() {
        if (monthYearLabel == null) return;

        // 1. Update Month Year Label
        String monthTitle = viewYearMonth.getMonth().name().charAt(0)
                + viewYearMonth.getMonth().name().substring(1).toLowerCase()
                + " " + viewYearMonth.getYear();
        monthYearLabel.setText(monthTitle);

        // 2. Enable/disable boundary navigation
        prevYearBtn.setEnabled(viewYearMonth.getYear() > MIN_YEAR);
        nextYearBtn.setEnabled(viewYearMonth.getYear() < MAX_YEAR);
        prevMonthBtn.setEnabled(viewYearMonth.minusMonths(1).getYear() >= MIN_YEAR);
        nextMonthBtn.setEnabled(viewYearMonth.plusMonths(1).getYear() <= MAX_YEAR);

        // 3. Update day buttons in-place
        LocalDate firstOfMonth = viewYearMonth.atDay(1);
        int dayOfWeekVal = firstOfMonth.getDayOfWeek().getValue() % 7; // 0=Sunday
        int daysInMonth = viewYearMonth.lengthOfMonth();

        LocalDate today = LocalDate.now();

        for (int i = 0; i < 42; i++) {
            JButton btn = dayButtons[i];

            if (i < dayOfWeekVal || i >= dayOfWeekVal + daysInMonth) {
                btn.setText("");
                btn.setEnabled(false);
                btn.setBackground(Theme.panelBackground());
                btn.setBorder(BorderFactory.createEmptyBorder());
            } else {
                int dayNum = i - dayOfWeekVal + 1;
                LocalDate btnDate = viewYearMonth.atDay(dayNum);

                btn.setText(String.valueOf(dayNum));
                btn.setEnabled(true);

                if (btnDate.equals(selectedDate)) {
                    btn.setBackground(Theme.primaryColor());
                    btn.setForeground(Theme.buttonTextColor());
                    btn.setBorder(BorderFactory.createLineBorder(Theme.primaryColor(), 2));
                } else if (btnDate.equals(today)) {
                    btn.setBackground(Theme.secondaryPanelBackground());
                    btn.setForeground(Theme.textColor());
                    btn.setBorder(BorderFactory.createLineBorder(Theme.primaryColor(), 1));
                } else {
                    btn.setBackground(Theme.panelBackground());
                    btn.setForeground(Theme.textColor());
                    btn.setBorder(BorderFactory.createLineBorder(Theme.borderColor(), 1));
                }
            }
        }
    }
}
