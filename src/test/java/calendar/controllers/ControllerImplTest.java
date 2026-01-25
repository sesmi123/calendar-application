package calendar.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.controllers.mocks.MockCalendar;
import calendar.controllers.mocks.MockCommandParser;
import calendar.controllers.mocks.MockView;
import calendar.models.Calendar;
import calendar.models.ObservableCalendar;
import calendar.views.GuiView;
import java.io.StringReader;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ControllerImpl}.
 */
public class ControllerImplTest {

  private StringBuilder log;
  private CommandParser mockParser;
  private Calendar mockModel;
  private GuiView mockView;
  private CommandFactory factory;
  private CalendarManager db;

  /**
   * Setting up the mock components to create the controller before every test. Setting up the
   * command factory to return actual command objects which invoke mock model, to test the callback
   * methods.
   */
  @Before
  public void setUp() {
    log = new StringBuilder();
    mockParser = new MockCommandParser(log, 45789);
    mockModel = new MockCalendar(log, 123456);
    mockView = new MockView(log, 234567);

    db = new CalendarManagerImpl();
    factory = new CommandFactoryImpl(db, mockView);
    db.set(mockModel);
    db.activate(mockModel.getTitle());
    log.setLength(0);
  }

  /**
   * Helper method to create a controller with given input commands.
   *
   * @param commands variable number of command strings
   * @return Controller instance configured with the input
   */
  private Controller createController(String... commands) {
    String input = String.join(System.lineSeparator(), commands) + System.lineSeparator();
    Readable in = new StringReader(input);
    return new ControllerImpl(mockView, in, mockParser);
  }

  /**
   * Helper method to create a controller in GUI mode using the factory from setUp().
   *
   * @return Controller instance configured for GUI mode
   */
  private Controller createGuiController() {
    Controller controller = new ControllerImpl(factory, db);
    controller.setView(mockView);
    log.setLength(0);
    return controller;
  }

  @Test
  public void testGoReadsInputAndExecutesCommands() {
    String createCommand =
        "create event DoctorAppointment from 2025-11-02T15:00 to " + "2025-11-02T16:00";

    Controller controller = createController(createCommand, "exit");
    controller.go();

    assertEquals(createCommand + "Execute", log.toString());
  }

  @Test
  public void testGoExportCal() {
    String createCommand =
        "create event DoctorAppointment from 2025-11-02T15:00 to " + "2025-11-02T16:00";
    String exportCsvCommand = "export cal calendar.csv";
    String exportIcalCommand = "export cal calendar.ical";

    Controller controller =
        createController(createCommand, exportCsvCommand, exportIcalCommand, "exit");
    controller.go();

    assertTrue(log.toString().contains(createCommand + "Execute"));
    assertTrue(log.toString().contains(exportCsvCommand + "Execute"));
    assertTrue(log.toString().contains(exportIcalCommand + "Execute"));
  }

  @Test
  public void testGoWithoutExit() {
    String createCommand =
        "create event DoctorAppointment from 2025-11-02T15:00 to " + "2025-11-02T16:00";

    Controller controller = createController(createCommand, "");
    controller.go();

    String expected =
        createCommand + "Execute" + "Execute" + "Error: Input ended without an 'exit' command. "
            + "Terminating program..." + System.lineSeparator();
    assertEquals(expected, log.toString());
  }

  @Test
  public void testGoHandlesIllegalArgumentException() {
    String invalidCommand = "invalid command";

    Controller controller = createController(invalidCommand, "exit");
    controller.go();

    String expected =
        invalidCommand + "Error: Error parsing command: " + invalidCommand + System.lineSeparator();
    assertEquals(expected, log.toString());
  }

  @Test
  public void testCreateEvent() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.createEvent("Team Meeting", "2024-01-15T10:00", "2024-01-15T11:00");

    String expectedLog = "Add event: Team MeetingSuccess: Event created: Team Meeting";
    assertEquals(expectedLog, log.toString().trim());
  }

  @Test
  public void testCreateEventWithInvalidDateTimeFormat() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.createEvent("Meeting", "invalid-date", "2024-01-15T11:00");

    assertTrue("Log should contain invalid datetime format error",
        log.toString().contains("Invalid datetime format"));
  }

  @Test
  public void testCreateEventEndBeforeStart() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.createEvent("Meeting", "2024-01-15T11:00", "2024-01-15T10:00");

    assertTrue("Log should contain error message",
        log.toString().contains("Error: Failed to create event"));
  }

  @Test
  public void testCreateAllDayEvent() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.createAllDayEvent("Birthday", "2024-01-15");

    String expectedLog = "Add event: BirthdaySuccess: Event created: Birthday";
    assertEquals(expectedLog, log.toString().trim());
  }

  @Test
  public void testCreateAllDayEventWithInvalidDateFormat() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.createAllDayEvent("Birthday", "01-15-2024");

    assertTrue("Log should contain invalid date format error",
        log.toString().contains("Invalid date format"));
  }

  @Test
  public void testCreateSeriesEventWithCount() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.createEventSeries("Standup", "2024-01-15", "09:00", "09:30",
        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), 10, null);

    assertTrue("Log should contain add event series", log.toString().contains("Add event series:"));
  }

  @Test
  public void testCreateSeriesEventWithUntil() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.createEventSeries("Standup", "2024-01-15", "09:00", "09:30",
        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), 0, "2024-03-01");

    assertTrue("Log should contain add event series", log.toString().contains("Add event series:"));
  }

  @Test
  public void testCreateSeriesEventAllDayWithCount() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.createEventSeries("Team Lunch", "2024-01-15", null, null, Set.of(DayOfWeek.FRIDAY), 10,
        null);

    assertTrue("Log should contain add event series", log.toString().contains("Add event series:"));
  }

  @Test
  public void testCreateSeriesEventAllDayWithUntil() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.createEventSeries("Team Lunch", "2024-01-15", null, null, Set.of(DayOfWeek.FRIDAY), 0,
        "2024-03-01");

    assertTrue("Log should contain add event series", log.toString().contains("Add event series:"));
  }

  @Test
  public void testCreateSeriesEventInvalidMixedTimes() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.createEventSeries("Meeting", "2024-01-15", "09:00", null, Set.of(DayOfWeek.MONDAY), 10,
        null);
    assertTrue("Error message should mention time requirement", log.toString()
        .contains("Either start time and end time should be set or both should not be set."));
  }

  @Test
  public void testCreateSeriesEventInvalidDateFormat() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.createEventSeries("Meeting", "01-15-2024", "09:00", "09:30", Set.of(DayOfWeek.MONDAY),
        10, null);
    assertTrue("Error message should mention invalid date format",
        log.toString().contains("Invalid date format"));
  }

  @Test
  public void testCreateSeriesEventInvalidTimeFormat() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.createEventSeries("Meeting", "2024-01-15", "9:00", "09:30", Set.of(DayOfWeek.MONDAY),
        10, null);
    assertTrue("Error message should mention invalid time format",
        log.toString().contains("Invalid time format"));
  }

  @Test
  public void testEditEvent() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.editEvent("subject", "Meeting", "2024-01-15T10:00", "2024-01-15T11:00", "Meeting2.0");

    assertTrue("Log should contain edit single event", log.toString()
        .contains("Edit single event: Meeting property subject with value Meeting2.0"));
  }

  @Test
  public void testEditEventInvalidProperty() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.editEvent("invalid", "Meeting", "2024-01-15T10:00", "2024-01-15T11:00", "value");
    assertTrue("Error message should mention invalid property",
        log.toString().contains("Invalid property"));
  }

  @Test
  public void testEditEventInvalidNewValueForProperty() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.editEvent("location", "Meeting", "2024-01-15T10:00", "2024-01-15T11:00", "HYBRID");
    assertTrue("Error message should mention invalid value for property",
        log.toString().contains("Invalid location: HYBRID"));
  }

  @Test
  public void testEditEventWithQuotes() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.editEvent("subject", "Meeting", "2024-01-15T10:00", "2024-01-15T11:00",
        "\"Updated Meeting\"");

    assertTrue("Log should contain edit single event with unquoted value", log.toString()
        .contains("Edit single event: Meeting property subject with value Updated Meeting"));
  }

  @Test
  public void testEditEvents() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.editEvents("subject", "Meeting", "2024-01-15T10:00", "Meeting2.0");

    assertTrue("Log should contain edit this and following events", log.toString().contains(
        "Edit this and following events: Meeting property subject with value Meeting2.0"));
  }

  @Test
  public void testEditEventsInvalidProperty() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.editEvents("invalid", "Meeting", "2024-01-15T10:00", "value");
    assertTrue("Error message should mention invalid property",
        log.toString().contains("Invalid property"));
  }

  @Test
  public void testEditSeries() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.editSeries("subject", "Meeting", "2024-01-15T10:00", "Meeting2.0");

    assertTrue("Log should contain edit series event", log.toString()
        .contains("Edit series event: Meeting property subject with value Meeting2.0"));
  }

  @Test
  public void testEditSeriesInvalidProperty() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.editSeries("invalid", "Meeting", "2024-01-15T10:00", "value");
    assertTrue("Error message should mention invalid property",
        log.toString().contains("Invalid property"));
  }

  @Test
  public void testCreateCalendar() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.createCalendar("Work", java.time.ZoneId.of("America/New_York"));

    Calendar createdCalendar = db.get("Work");

    assertEquals("Work", createdCalendar.getTitle());
    assertTrue("Log should contain calendar created success message",
        log.toString().contains("Created new calendar Work with timezone America/New_York"));

    db.remove("Work");
  }

  @Test
  public void testEditCalendarTimezone() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.createCalendar("Work", java.time.ZoneId.of("America/New_York"));
    features.editCalendar("Work", "timezone", "Canada/Pacific");

    Calendar calendar = db.get("Work");

    assertEquals("Canada/Pacific", calendar.getTimezone().getId());
    assertTrue("Log should contain calendar edited success message",
        log.toString().contains("Edited timezone of calendar Work to Canada/Pacific"));

    db.remove("Work");
  }

  @Test
  public void testEditCalendarName() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.createCalendar("Work", java.time.ZoneId.of("America/New_York"));
    features.editCalendar("Work", "name", "Play");

    Calendar calendar = db.get("Play");

    assertEquals("Play", calendar.getTitle());
    assertTrue("Log should contain calendar edited success message",
        log.toString().contains("Edited name of calendar Work to Play"));

    db.remove("Play");
  }

  @Test
  public void testUseCalendar() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.createCalendar("Work", java.time.ZoneId.of("America/New_York"));
    log.setLength(0);

    features.useCalendar("Work");

    Calendar activeCalendar = db.getActive();
    assertEquals("Work", activeCalendar.getTitle());

    assertTrue("Log should contain calendar activated message",
        log.toString().contains("Activated Work calendar for use"));

    db.remove("Work");
  }

  @Test
  public void testUseCalendarNotFound() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.useCalendar("NonExistent");

    assertTrue("Log should contain calendar not found error",
        log.toString().contains("Error: Calendar not found"));
  }

  @Test
  public void testGetActiveCalendar() {
    Controller controller = createGuiController();
    Features features = (Features) controller;

    features.createCalendar("Work", java.time.ZoneId.of("America/New_York"));
    features.useCalendar("Work");

    calendar.models.ObservableCalendar activeCalendar = features.getActiveCalendar();

    assertEquals("Work", activeCalendar.getTitle());
    assertEquals(java.time.ZoneId.of("America/New_York"), activeCalendar.getTimezone());

    db.remove("Work");
  }

  @Test
  public void testGetActiveCalendarWhenNoneActive() {
    db = new CalendarManagerImpl();
    factory = new CommandFactoryImpl(db, mockView);
    Controller controller = new ControllerImpl(factory, db);
    controller.setView(mockView);
    Features features = (Features) controller;

    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> features.getActiveCalendar());
    assertEquals("An active calendar is not set", e.getMessage());
  }

  @Test
  public void testListCalendars() {
    Controller controller = createGuiController();
    Features features = (Features) controller;

    List<ObservableCalendar> calendars = features.listCalendars();
    assertEquals(1, calendars.size());
    assertEquals("title", calendars.get(0).getTitle());

    features.createCalendar("Work", java.time.ZoneId.of("America/New_York"));
    features.createCalendar("Personal", java.time.ZoneId.of("America/Los_Angeles"));

    calendars = features.listCalendars();
    assertEquals(3, calendars.size());

    java.util.Set<String> calendarNames = new java.util.HashSet<>();
    for (calendar.models.ObservableCalendar cal : calendars) {
      calendarNames.add(cal.getTitle());
    }
    assertTrue(calendarNames.contains("title"));
    assertTrue(calendarNames.contains("Work"));
    assertTrue(calendarNames.contains("Personal"));

    db.remove("Work");
    db.remove("Personal");
  }

  @Test
  public void testListCalendarsEmpty() {
    db = new CalendarManagerImpl();
    factory = new CommandFactoryImpl(db, mockView);
    Controller controller = new ControllerImpl(factory, db);
    controller.setView(mockView);
    Features features = (Features) controller;

    List<ObservableCalendar> calendars = features.listCalendars();
    assertEquals(0, calendars.size());
  }

  @Test
  public void testQueryByDate() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.queryByDate(java.time.LocalDate.of(2024, 1, 15));

    assertTrue("Log should contain Display Events On",
        log.toString().contains("Display Events On"));
  }

  @Test
  public void testQueryByDateWithNullDate() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    assertThrows(NullPointerException.class, () -> features.queryByDate(null));
  }

  @Test
  public void testQueryByDateRange() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.queryByDateRange(java.time.LocalDateTime.of(2024, 1, 15, 9, 0),
        java.time.LocalDateTime.of(2024, 1, 15, 17, 0));

    assertTrue("Log should contain Display Events In Range",
        log.toString().contains("Display Events In Range"));
  }

  @Test
  public void testQueryByDateRangeWithNullDates() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    assertThrows(NullPointerException.class,
        () -> features.queryByDateRange(null, java.time.LocalDateTime.of(2024, 1, 15, 17, 0)));
  }

  @Test
  public void testShowStatus() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    features.showStatus(java.time.LocalDateTime.of(2024, 1, 15, 10, 0));

    assertTrue("Log should contain Display Status", log.toString().contains("Display Status"));
  }

  @Test
  public void testShowStatusWithNullDateTime() {
    Controller controller = createGuiController();
    log.setLength(0);
    Features features = (Features) controller;

    assertThrows(NullPointerException.class, () -> features.showStatus(null));
  }

  /**
   * Remove the created calendars.
   */
  @After
  public void tearDown() {
    db.remove("title");
  }

}