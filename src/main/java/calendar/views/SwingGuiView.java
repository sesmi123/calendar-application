package calendar.views;

import static calendar.views.gui.AppStyle.appIconPath;
import static calendar.views.gui.AppStyle.green;
import static calendar.views.gui.AppStyle.red;

import calendar.controllers.Features;
import calendar.models.Event;
import calendar.models.ObservableCalendar;
import calendar.views.gui.CenterPanel;
import calendar.views.gui.CenterPanelNorthPanel;
import calendar.views.gui.CreateCalendarDialog;
import calendar.views.gui.EastPanel;
import calendar.views.gui.EventDialog;
import calendar.views.gui.NorthPanel;
import calendar.views.gui.SouthPanel;
import calendar.views.gui.WestPanel;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * GUI view implementation for the calendar using Java Swing.
 */
public final class SwingGuiView extends JFrame implements GuiView {

  private static final String APP_ICON = appIconPath.toString();

  private final WestPanel westPanel;
  private final CenterPanel centerPanel;
  private final NorthPanel northPanel;
  private final SouthPanel southPanel;
  private final EastPanel eastPanel;
  private boolean showWestPanel = true;

  /**
   * Constructs a {@code SwingGuiView}.
   *
   * @param title the title to be set for the JFrame
   */
  public SwingGuiView(String title) {
    super(title);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1200, 700);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout());

    northPanel = new NorthPanel();
    add(northPanel, BorderLayout.NORTH);

    westPanel = new WestPanel();
    add(westPanel, BorderLayout.WEST);

    centerPanel = new CenterPanel();
    add(centerPanel, BorderLayout.CENTER);

    eastPanel = new EastPanel();
    add(eastPanel, BorderLayout.EAST);

    southPanel = new SouthPanel();
    add(southPanel, BorderLayout.SOUTH);

    setVisible(true);

    ImageIcon icon = new ImageIcon(APP_ICON);
    setIconImage(icon.getImage());
  }

  /**
   * Toggles visibility of west side panel.
   */
  public void toggleWestPanel() {
    showWestPanel = !showWestPanel;
    westPanel.setVisible(showWestPanel);
  }

  @Override
  public void displaySuccess(String message) {
    southPanel.showMessage(message, green, 3000);
  }

  @Override
  public void displayError(String message) {
    southPanel.showMessage(message, red, 3000);
  }

  @Override
  public void displayEventsOn(Set<Event> events) {
    eastPanel.loadEventsForDate(eastPanel.getDate(), events);
  }

  @Override
  public void displayEventsInRange(Set<Event> events) {
    centerPanel.loadEvents(events);
  }

  @Override
  public void displayStatus(boolean status) {
    throw new UnsupportedOperationException("Not supported yet");
  }

  @Override
  public void addFeatures(Features features) {
    centerPanel.getNorthPanel().setQueryEventsConsumer(features::queryByDateRange);

    northPanel.getWestPnlToggleBtn().addActionListener(e -> toggleWestPanel());

    westPanel.getMiniDatePicker().setOnDateSelected((LocalDate date) -> {
      CenterPanelNorthPanel np = centerPanel.getNorthPanel();
      np.setCurrYearMonth(YearMonth.from(date));
    });

    centerPanel.setOnDaySelected(eastPanel::loadEventsForDate);

    westPanel.getMyCalendarsPnl().addListSelectionListener(calendar -> {
      northPanel.setTimeZone(calendar.getTimezone().toString());
      features.useCalendar(calendar.getTitle());
      centerPanel.refreshCalendar();
      features.queryByDate(eastPanel.getDate());
    });

    refreshCalendars(features);

    westPanel.getMyCalendarsPnl().addCreateCalendarActionListener(() -> {
      CreateCalendarDialog calendarDialog = new CreateCalendarDialog(this);
      calendarDialog.addCreateCalendarActionListener((name, timezone) -> {
        features.createCalendar(name, timezone);
        refreshCalendars(features);
        calendarDialog.dispose();
      });
      calendarDialog.setVisible(true);
    });

    westPanel.getCreateEventButton().addActionListener(e -> {
      EventDialog createDialog = new EventDialog(this,
          centerPanel::refreshCalendar,
          () -> features.queryByDate(eastPanel.getDate()));
      createDialog.setFeatures(features);
      createDialog.setVisible(true);
    });

    eastPanel.getCreateEventButton().addActionListener(e -> {
      EventDialog createDialog = new EventDialog(this,
          centerPanel::refreshCalendar,
          () -> features.queryByDate(eastPanel.getDate()));
      createDialog.setFeatures(features);
      createDialog.setStartDate(eastPanel.getDate().toString());
      createDialog.setEndDate(eastPanel.getDate().toString());
      createDialog.setStartTime("08:00");
      createDialog.setEndTime("17:00");
      createDialog.setVisible(true);
    });

    eastPanel.setEditEventBtnCallback(event -> {
      EventDialog editDialog = new EventDialog(this,
          event,
          centerPanel::refreshCalendar,
          () -> features.queryByDate(eastPanel.getDate()));
      editDialog.setFeatures(features);
      editDialog.setVisible(true);
    });

  }

  private void refreshCalendars(Features features) {
    List<ObservableCalendar> all = features.listCalendars();
    ObservableCalendar active = features.getActiveCalendar();
    westPanel.getMyCalendarsPnl().refreshList(all, active);
    northPanel.setTimeZone(active.getTimezone().toString());
  }
}