package calendar.views.gui;

import static calendar.views.gui.AppStyle.bgClr;
import static calendar.views.gui.AppStyle.btnClr;
import static calendar.views.gui.AppStyle.txtClr;

import calendar.models.ObservableCalendar;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * Represents the list of calendars.
 */
public class CalendarList extends JPanel {

  private final DefaultListModel<ObservableCalendar> calendarModels = new DefaultListModel<>();
  private final JButton createCalendarBtn;
  private JList<ObservableCalendar> calendars;
  private boolean updatingSelectedCalendar = false;

  /**
   * Construct a new calendar list.
   */
  public CalendarList() {
    setLayout(new BorderLayout());
    setBackground(bgClr);

    JPanel northPnl = new JPanel(new BorderLayout());
    northPnl.setBackground(bgClr);

    JLabel myCalendarsLbl = new JLabel("My Calendars");
    myCalendarsLbl.setForeground(txtClr);
    northPnl.add(myCalendarsLbl, BorderLayout.WEST);

    createCalendarBtn = createCalendarBtn();
    northPnl.add(createCalendarBtn, BorderLayout.EAST);

    add(northPnl, BorderLayout.NORTH);

    JScrollPane calendarsScrollPane = createCalendarListScrollPnl();
    calendarsScrollPane.setBorder(null);
    add(calendarsScrollPane, BorderLayout.CENTER);
  }

  /**
   * Add a listener for calendar selection changes.
   *
   * @param callback the callback to invoke when a calendar is selected
   */
  public void addListSelectionListener(Consumer<ObservableCalendar> callback) {
    calendars.addListSelectionListener(e -> {
      if (!(e.getValueIsAdjusting() || updatingSelectedCalendar)) {
        ObservableCalendar selected = calendars.getSelectedValue();
        if (selected != null) {
          callback.accept(selected);
        }
      }
    });
  }

  /**
   * Add a listener for create calendar button clicks.
   *
   * @param callback the callback to invoke when the button is clicked
   */
  public void addCreateCalendarActionListener(Runnable callback) {
    createCalendarBtn.addActionListener(e -> callback.run());
  }

  /**
   * Refresh the list of calendars.
   *
   * @param allCalendars     the list of all calendars
   * @param selectedCalendar the currently selected calendar
   */
  public void refreshList(List<ObservableCalendar> allCalendars,
      ObservableCalendar selectedCalendar) {
    calendarModels.clear();
    for (ObservableCalendar c : allCalendars) {
      calendarModels.addElement(c);
    }

    int index = allCalendars.indexOf(selectedCalendar);
    updatingSelectedCalendar = true;
    this.calendars.setSelectedIndex(index);
    updatingSelectedCalendar = false;
  }

  private JScrollPane createCalendarListScrollPnl() {
    calendars = new JList<>(calendarModels);
    calendars.setBackground(bgClr);
    calendars.setForeground(txtClr);
    calendars.setPreferredSize(new Dimension(140, 200));
    calendars.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    JScrollPane calendarsScrollPane = new JScrollPane(calendars);
    calendarsScrollPane.setPreferredSize(new Dimension(230, 300));
    return calendarsScrollPane;
  }

  private JButton createCalendarBtn() {
    JButton btn = new JButton("+");
    btn.setBackground(bgClr);
    btn.setForeground(txtClr);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setOpaque(true);
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
    return btn;
  }
}
