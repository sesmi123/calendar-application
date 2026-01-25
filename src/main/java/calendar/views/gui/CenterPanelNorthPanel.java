package calendar.views.gui;

import static calendar.views.gui.AppStyle.fontName;
import static calendar.views.gui.AppStyle.green;
import static calendar.views.gui.AppStyle.mainBgClr;
import static calendar.views.gui.AppStyle.txtClr;

import java.awt.BorderLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.BiConsumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Represents the center north panel of the calendar.
 */
public class CenterPanelNorthPanel extends JPanel {

  private final Runnable updateCalendar;
  private YearMonth currentMonth;
  private JButton prevMonthBtn;
  private JButton nextMonthBtn;
  private JLabel monthYearLbl;
  private BiConsumer<LocalDateTime, LocalDateTime> queryEventsOfMonth;

  /**
   * Constructs a new center-panel-north-panel with month navigation controls.
   *
   * @param updateCalendar callback to refresh the calendar grid when navigating months
   */
  public CenterPanelNorthPanel(Runnable updateCalendar) {
    this.updateCalendar = updateCalendar;
    currentMonth = YearMonth.now();
    setLayout(new BorderLayout());
    setBackground(mainBgClr);
    setForeground(txtClr);

    createPrevMonthBtn();
    add(prevMonthBtn, BorderLayout.WEST);

    createMonthYearLbl();
    add(monthYearLbl, BorderLayout.CENTER);

    createNextMonthBtn();
    add(nextMonthBtn, BorderLayout.EAST);

  }

  public YearMonth getCurrYearMonth() {
    return this.currentMonth;
  }

  /**
   * Sets the current month and year; and queries the events for the month.
   *
   * @param ym the month of the year to set
   */
  public void setCurrYearMonth(YearMonth ym) {
    this.currentMonth = ym;
    queryEventsOfMonth();
    updateMonthYearLabel();
  }

  public void setQueryEventsConsumer(BiConsumer<LocalDateTime, LocalDateTime> callback) {
    queryEventsOfMonth = callback;
  }

  /**
   * Query all the events for the month.
   */
  public void queryEventsOfMonth() {
    LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
    LocalDateTime end = currentMonth.atEndOfMonth().plusDays(1).atStartOfDay();
    if (queryEventsOfMonth != null) {
      queryEventsOfMonth.accept(start, end);
    }
  }

  private void styleBtn(JButton btn) {
    btn.setBackground(mainBgClr);
    btn.setForeground(txtClr);
    btn.setFocusPainted(false);
    btn.setFont(new Font(fontName, Font.BOLD, 12));
    btn.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(green, 1),
        BorderFactory.createEmptyBorder(5, 15, 5, 15)
    ));
  }

  private void createPrevMonthBtn() {
    prevMonthBtn = new JButton("<");
    styleBtn(prevMonthBtn);
    prevMonthBtn.addActionListener(e -> {
      currentMonth = currentMonth.minusMonths(1);
      updateCalendar.run();
      updateMonthYearLabel();
      queryEventsOfMonth();
    });
  }

  private void createNextMonthBtn() {
    nextMonthBtn = new JButton(">");
    styleBtn(nextMonthBtn);
    nextMonthBtn.addActionListener(e -> {
      currentMonth = currentMonth.plusMonths(1);
      updateCalendar.run();
      updateMonthYearLabel();
      queryEventsOfMonth();
    });
  }

  private void updateMonthYearLabel() {
    String monthName = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
    monthYearLbl.setText(monthName + " " + currentMonth.getYear());
  }

  private void createMonthYearLbl() {
    monthYearLbl = new JLabel("", SwingConstants.CENTER);
    monthYearLbl.setFont(new Font(fontName, Font.BOLD, 18));
    monthYearLbl.setForeground(green);
    updateMonthYearLabel();
  }
}
