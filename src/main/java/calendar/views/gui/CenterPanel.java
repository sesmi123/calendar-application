package calendar.views.gui;

import static calendar.views.gui.AppStyle.fontName;
import static calendar.views.gui.AppStyle.mainBgClr;
import static calendar.views.gui.AppStyle.txtClr;

import calendar.models.Event;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Center panel displaying the monthly calendar view with events. Manages the calendar grid and
 * handles day selection.
 */
public class CenterPanel extends JPanel {

  private static final int CALENDAR_ROWS = 6;
  private static final int CALENDAR_COLS = 7;
  private static final int TOTAL_CELLS = CALENDAR_ROWS * CALENDAR_COLS;
  private static final String[] DAY_NAMES = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

  private final Map<LocalDate, List<Event>> eventsByDate;
  private final CenterPanelNorthPanel northPanel;
  private final CalendarDayCell[] dayCells;
  private final JPanel calendarGridPanel;

  private LocalDate selectedDate;
  private BiConsumer<LocalDate, Set<Event>> onDaySelected;

  /**
   * Creates the center panel with calendar view.
   */
  public CenterPanel() {
    this.eventsByDate = new HashMap<>();
    this.dayCells = new CalendarDayCell[TOTAL_CELLS];

    setBackground(mainBgClr);
    setBorder(new EmptyBorder(10, 10, 10, 10));
    setLayout(new BorderLayout());

    northPanel = new CenterPanelNorthPanel(this::updateCalendarGrid);
    add(northPanel, BorderLayout.NORTH);

    calendarGridPanel = createCalendarPanel();
    add(calendarGridPanel, BorderLayout.CENTER);

    updateCalendarGrid();
  }

  /**
   * Sets the callback invoked when a day is selected.
   *
   * @param callback receives the selected date and its events
   */
  public void setOnDaySelected(BiConsumer<LocalDate, Set<Event>> callback) {
    this.onDaySelected = callback;
  }

  /**
   * Gets the north panel containing navigation controls.
   *
   * @return the north panel
   */
  public CenterPanelNorthPanel getNorthPanel() {
    return northPanel;
  }

  /**
   * Refreshes the calendar view by requerying events.
   */
  public void refreshCalendar() {
    northPanel.queryEventsOfMonth();
  }

  /**
   * Loads and displays events in the calendar.
   *
   * @param events the set of events to display
   */
  public void loadEvents(Set<Event> events) {
    if (events == null) {
      return;
    }

    populateEventsByDate(events);
    updateCalendarGrid();
  }

  private void populateEventsByDate(Set<Event> events) {
    eventsByDate.clear();

    for (Event event : events) {
      addEventToDateRange(event);
    }
  }

  private void addEventToDateRange(Event event) {
    LocalDate start = event.getStartDate();
    LocalDate end = event.getEndDate();

    for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
      eventsByDate.computeIfAbsent(date, d -> new ArrayList<>()).add(event);
    }
  }

  private List<Event> getEventsForDate(LocalDate date) {
    return eventsByDate.getOrDefault(date, new ArrayList<>());
  }

  private JPanel createCalendarPanel() {
    JPanel container = new JPanel(new BorderLayout());
    container.setBackground(mainBgClr);

    container.add(createDayHeaderPanel(), BorderLayout.NORTH);
    container.add(createCalendarGrid(), BorderLayout.CENTER);

    return container;
  }

  private JPanel createDayHeaderPanel() {
    JPanel headerPanel = new JPanel(new GridLayout(1, CALENDAR_COLS));
    headerPanel.setBackground(mainBgClr);

    for (String dayName : DAY_NAMES) {
      headerPanel.add(createDayHeaderLabel(dayName));
    }

    return headerPanel;
  }

  private JLabel createDayHeaderLabel(String dayName) {
    JLabel label = new JLabel(dayName, SwingConstants.CENTER);
    label.setFont(new Font(fontName, Font.BOLD, 12));
    label.setForeground(txtClr);
    return label;
  }

  private JPanel createCalendarGrid() {
    JPanel grid = new JPanel(new GridLayout(CALENDAR_ROWS, CALENDAR_COLS));
    grid.setBackground(mainBgClr);

    for (int i = 0; i < TOTAL_CELLS; i++) {
      dayCells[i] = new CalendarDayCell();
      final int cellIndex = i;
      dayCells[i].addClickListener(() -> handleDayClick(cellIndex));
      grid.add(dayCells[i]);
    }

    return grid;
  }

  private void updateCalendarGrid() {
    YearMonth currentMonth = northPanel.getCurrYearMonth();
    clearAllCells();
    populateMonthCells(currentMonth);
  }

  private void clearAllCells() {
    for (CalendarDayCell cell : dayCells) {
      cell.clear();
    }
  }

  private void populateMonthCells(YearMonth month) {
    int firstDayOfWeek = month.atDay(1).getDayOfWeek().getValue() % 7;
    int daysInMonth = month.lengthOfMonth();

    for (int day = 1; day <= daysInMonth; day++) {
      int cellIndex = firstDayOfWeek + day - 1;
      LocalDate date = month.atDay(day);

      configureDayCell(dayCells[cellIndex], day, date);
    }
  }

  private void configureDayCell(CalendarDayCell cell, int day, LocalDate date) {
    cell.setDay(day);
    cell.setDate(date);
    cell.setEvents(getEventsForDate(date));
    cell.setEnabled(true);

    if (date.equals(LocalDate.now())) {
      cell.setToday(true);
    }

    if (date.equals(selectedDate)) {
      cell.setSelected(true);
    }
  }

  private void handleDayClick(int cellIndex) {
    CalendarDayCell clickedCell = dayCells[cellIndex];

    if (!clickedCell.isEnabled()) {
      return;
    }

    LocalDate clickedDate = clickedCell.getDate();
    updateSelection(clickedDate, cellIndex);
    notifyDaySelected(clickedDate);
  }

  private void updateSelection(LocalDate date, int selectedIndex) {
    selectedDate = date;

    for (CalendarDayCell cell : dayCells) {
      cell.setSelected(false);
    }

    dayCells[selectedIndex].setSelected(true);
  }

  private void notifyDaySelected(LocalDate date) {
    if (onDaySelected == null) {
      return;
    }

    List<Event> dayEvents = getEventsForDate(date);
    dayEvents.sort(new EventComparator(date));
    onDaySelected.accept(date, Set.copyOf(dayEvents));
  }

  /**
   * Comparator for sorting events by their relevance to a specific date. Multi-day events that
   * started before the date are prioritized, followed by events starting on that date sorted by
   * time.
   */
  private static class EventComparator implements Comparator<Event> {

    private final LocalDate referenceDate;

    public EventComparator(LocalDate referenceDate) {
      this.referenceDate = referenceDate;
    }

    @Override
    public int compare(Event e1, Event e2) {
      boolean e1Continues = e1.getStartDate().isBefore(referenceDate);
      boolean e2Continues = e2.getStartDate().isBefore(referenceDate);

      if (e1Continues && !e2Continues) {
        return -1;
      }
      if (!e1Continues && e2Continues) {
        return 1;
      }
      if (e1Continues && e2Continues) {
        return 0;
      }

      return e1.getStartDateTime().compareTo(e2.getStartDateTime());
    }
  }
}