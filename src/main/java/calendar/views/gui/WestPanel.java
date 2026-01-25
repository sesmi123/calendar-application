package calendar.views.gui;

import static calendar.views.gui.AppStyle.bgClr;
import static calendar.views.gui.AppStyle.fontName;
import static calendar.views.gui.AppStyle.green;
import static calendar.views.gui.AppStyle.txtClr;

import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * West panel of the main application window.
 */
public class WestPanel extends JPanel {

  private final MiniDatePicker datePickerPnl;
  private final CalendarList myCalendarsPnl;
  private JButton createEvent;

  /**
   * Constructs the west panel.
   */
  public WestPanel() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBackground(bgClr);
    setBorder(new EmptyBorder(10, 10, 10, 10));

    JPanel createButtonPnl = createEventBtnPnl();
    add(createButtonPnl);

    datePickerPnl = new MiniDatePicker();
    add(datePickerPnl);

    myCalendarsPnl = new CalendarList();
    add(myCalendarsPnl);
  }

  public CalendarList getMyCalendarsPnl() {
    return myCalendarsPnl;
  }

  public MiniDatePicker getMiniDatePicker() {
    return this.datePickerPnl;
  }

  public JButton getCreateEventButton() {
    return createEvent;
  }

  private JPanel createEventBtnPnl() {
    createEvent = new JButton("+ Create Event");
    createEvent.setBackground(green);
    createEvent.setForeground(txtClr);
    createEvent.setFocusPainted(false);
    createEvent.setBorderPainted(false);
    createEvent.setOpaque(true);
    createEvent.setFont(new Font(fontName, Font.BOLD, 14));

    JPanel createButtonPnl = new JPanel(new GridLayout(1, 1));

    createButtonPnl.add(createEvent);
    return createButtonPnl;
  }

}
