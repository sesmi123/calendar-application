package calendar.views.gui;

import static calendar.views.gui.AppStyle.bgClr;
import static calendar.views.gui.AppStyle.blue;
import static calendar.views.gui.AppStyle.btnClr;
import static calendar.views.gui.AppStyle.fontName;
import static calendar.views.gui.AppStyle.txtClr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Date picker component for selecting dates in a compact view.
 */
public class MiniDatePicker extends JPanel {

  private YearMonth currentMonthDtPicker = YearMonth.now();
  private Consumer<LocalDate> onDateSelected;

  /**
   * Constructs the mini date picker.
   */
  public MiniDatePicker() {
    setLayout(new BorderLayout());
    setBackground(bgClr);
    setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    JLabel monthYearLbl = createMonthYearLabel();
    JPanel monthPanel = createMonthPanel(monthYearLbl);
    monthPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

    JPanel dateGrid = new JPanel(new GridLayout(0, 7, 0, 0));
    dateGrid.setBackground(bgClr);

    add(monthPanel, BorderLayout.NORTH);
    add(dateGrid, BorderLayout.CENTER);

    Runnable updateCalendar = () -> populateDateGrid(dateGrid, monthYearLbl);

    addMonthNavigationListeners(monthPanel, updateCalendar);

    updateCalendar.run();

  }

  public void setOnDateSelected(Consumer<LocalDate> listener) {
    this.onDateSelected = listener;
  }

  private JLabel createMonthYearLabel() {
    JLabel lbl = new JLabel("", JLabel.CENTER);
    lbl.setForeground(txtClr);
    lbl.setFont(new Font(fontName, Font.BOLD, 12));
    return lbl;
  }

  private JPanel createMonthPanel(JLabel monthYearLbl) {
    JPanel monthPanel = new JPanel(new BorderLayout());
    monthPanel.setBackground(bgClr);

    JButton prevBtn = createMonthNavButton("<");
    JButton nextBtn = createMonthNavButton(">");

    monthPanel.add(prevBtn, BorderLayout.WEST);
    monthPanel.add(monthYearLbl, BorderLayout.CENTER);
    monthPanel.add(nextBtn, BorderLayout.EAST);

    return monthPanel;
  }

  private JButton createMonthNavButton(String text) {
    JButton btn = new JButton(text);
    styleMonthBtn(btn);
    return btn;
  }

  private void populateDateGrid(JPanel dateGrid, JLabel monthYearLbl) {
    dateGrid.removeAll();
    monthYearLbl.setText(currentMonthDtPicker.getMonth() + " " + currentMonthDtPicker.getYear());

    String[] days = {"S", "M", "T", "W", "T", "F", "S"};
    for (String d : days) {
      JLabel lbl = new JLabel(d, JLabel.CENTER);
      lbl.setForeground(txtClr);
      lbl.setFont(new Font(fontName, Font.BOLD, 10));
      dateGrid.add(lbl);
    }

    LocalDate first = currentMonthDtPicker.atDay(1);
    int startDay = first.getDayOfWeek().getValue() % 7;
    int daysInMonth = currentMonthDtPicker.lengthOfMonth();

    for (int i = 0; i < startDay; i++) {
      dateGrid.add(new JLabel(""));
    }

    LocalDate today = LocalDate.now();
    for (int i = 1; i <= daysInMonth; i++) {
      JButton dayBtn = new JButton(String.valueOf(i));
      styleDayBtn(dayBtn, i,
          i == today.getDayOfMonth() && currentMonthDtPicker.equals(YearMonth.now()));
      dateGrid.add(dayBtn);
    }

    dateGrid.revalidate();
    dateGrid.repaint();
  }

  private void addMonthNavigationListeners(JPanel monthPanel, Runnable updateCalendar) {
    JButton prevBtn = (JButton) ((BorderLayout) monthPanel.getLayout()).getLayoutComponent(
        BorderLayout.WEST);
    JButton nextBtn = (JButton) ((BorderLayout) monthPanel.getLayout()).getLayoutComponent(
        BorderLayout.EAST);

    prevBtn.addActionListener(e -> {
      currentMonthDtPicker = currentMonthDtPicker.minusMonths(1);
      updateCalendar.run();
    });

    nextBtn.addActionListener(e -> {
      currentMonthDtPicker = currentMonthDtPicker.plusMonths(1);
      updateCalendar.run();
    });
  }


  private void styleMonthBtn(JButton btn) {
    btn.setFont(new Font(fontName, Font.BOLD, 12));
    btn.setBackground(bgClr);
    btn.setForeground(txtClr);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setOpaque(true);
  }


  private void styleDayBtn(JButton btn, int day, boolean isToday) {
    btn.setFont(new Font(fontName, Font.PLAIN, 9));
    btn.setForeground(txtClr);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setOpaque(true);
    btn.addActionListener(e -> {
      LocalDate selected = currentMonthDtPicker.atDay(day);
      if (onDateSelected != null) {
        onDateSelected.accept(selected);
      }
    });
    btn.setMargin(new Insets(0, 0, 0, 0));
    if (isToday) {
      btn.setBackground(blue);
    } else {
      btn.setBackground(bgClr);
      btn.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
          btn.setBackground(btnClr);
        }

        @Override
        public void mouseExited(MouseEvent e) {
          btn.setBackground(bgClr);
        }
      });
    }
  }

  @Override
  public Dimension getPreferredSize() {
    int width = (7 * 50) + 10;
    int height = 200;
    return new Dimension(width, height);
  }

  @Override
  public Dimension getMaximumSize() {
    return getPreferredSize();
  }
}
