package calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.controllers.CalendarManager;
import calendar.controllers.CalendarManagerImpl;
import calendar.controllers.CommandFactory;
import calendar.controllers.CommandFactoryImpl;
import calendar.controllers.CommandParser;
import calendar.controllers.CommandParserImpl;
import calendar.controllers.ControllerImpl;
import calendar.models.Calendar;
import calendar.models.CalendarImpl;
import calendar.models.Event;
import calendar.views.ConsoleView;
import calendar.views.ObservableView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;

/**
 * Calendar integration tests.
 */
public class CalendarTest {

  private static final Pattern ANSI_PATTERN = Pattern.compile("\u001B\\[[;\\d]*m");
  private ByteArrayOutputStream outContent;
  private Calendar model;
  private Calendar targetCalendar;
  private ObservableView view;
  private CommandParser parser;

  /**
   * Test set up.
   */
  @Before
  public void setUp() {
    model = new CalendarImpl("MyCalendar", ZoneId.of("America/New_York"));
    targetCalendar = new CalendarImpl("CopyCalendar", ZoneId.of("Asia/Kolkata"));
    outContent = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(outContent);
    view = new ConsoleView(out);
    CalendarManager db = new CalendarManagerImpl();
    db.set(model);
    db.set(targetCalendar);
    db.activate(model.getTitle());
    CommandFactory factory = new CommandFactoryImpl(db, view);
    parser = new CommandParserImpl(factory);
  }

  private String stripAnsi(String s) {
    return ANSI_PATTERN.matcher(s).replaceAll("");
  }

  @Test
  public void testCreateCalendar() {
    String input = "create calendar --name College --timezone America/New_York"
        + System.lineSeparator() + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    assertEquals("Created new calendar College with timezone America/New_York", output);
  }

  @Test
  public void testEditCalendar() {
    String input = "create calendar --name College --timezone America/New_York"
        + System.lineSeparator()
        + "edit calendar --name College --property timezone Canada/Pacific"
        + System.lineSeparator()
        + "edit calendar --name College --property name School"
        + System.lineSeparator()
        + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expected = "Created new calendar College with timezone America/New_York"
        + System.lineSeparator()
        + "Edited timezone of calendar College to Canada/Pacific"
        + System.lineSeparator()
        + "Edited name of calendar College to School";
    assertEquals(expected, output);
  }

  @Test
  public void testEditCalendarChangesEventsTimeZone() {
    String input = "create calendar --name College --timezone America/New_York"
        + System.lineSeparator()
        + "use calendar --name College"
        + System.lineSeparator()
        + "create event DoctorAppointment from 2025-11-02T15:00 to 2025-11-02T16:00"
        + System.lineSeparator()
        + "edit calendar --name College --property timezone Canada/Pacific"
        + System.lineSeparator()
        + "print events on 2025-11-02"
        + System.lineSeparator()
        + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expected = "Created new calendar College with timezone America/New_York"
        + System.lineSeparator()
        + "Activated College calendar for use"
        + System.lineSeparator()
        + "Event created: DoctorAppointment"
        + System.lineSeparator()
        + "Edited timezone of calendar College to Canada/Pacific"
        + System.lineSeparator()
        + "- DoctorAppointment starting on 2025-11-02 at 12:00, ending on 2025-11-02 at 13:00";
    assertEquals(expected, output);
  }

  @Test
  public void testCreateCalendarInvalidTimezone() {
    String input = "create calendar --name College --timezone area/location"
        + System.lineSeparator() + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    assertEquals("Error parsing command: Invalid time zone: area/location", output);
  }

  @Test
  public void testUseCalendar() {
    String input = "use calendar --name MyCalendar"
        + System.lineSeparator() + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    assertEquals("Activated MyCalendar calendar for use", output);
  }

  @Test
  public void testCreateEvent() {
    String input = "create event DoctorAppointment from 2025-11-02T15:00 to 2025-11-02T16:00"
        + System.lineSeparator() + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    assertEquals("Event created: DoctorAppointment", output);
  }

  @Test
  public void testCreateDuplicateEvent() {
    String input = "create event DoctorAppointment from 2025-11-02T15:00 to 2025-11-02T16:00"
        + System.lineSeparator()
        + "create event DoctorAppointment from 2025-11-02T15:00 to 2025-11-02T16:00"
        + System.lineSeparator()
        + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String expectedOutput = "Event created: DoctorAppointment"
        + System.lineSeparator()
        + "Event already exists: DoctorAppointment";
    String output = stripAnsi(outContent.toString()).trim();
    assertEquals(expectedOutput, output);
  }

  @Test
  public void testCreateAllDayEvent() {
    String input = "create event TeamsMeeting on 2025-11-03"
        + System.lineSeparator() + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    assertEquals("Event created: TeamsMeeting", output);
  }

  @Test
  public void testCreateDuplicateAllDayEvent() {
    String input = "create event TeamsMeeting on 2025-11-03" + System.lineSeparator();
    input += input + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event created: TeamsMeeting" + System.lineSeparator()
        + "Event already exists: TeamsMeeting";
    assertEquals(expectedOutput, output);
  }

  @Test
  public void testCreateAllDayEventSeries() {
    String input =
        "create event TeamsMeeting2 from 2025-11-03T15:00 to 2025-11-03T16:00 "
            + "repeats MTF for 3 times"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    assertEquals("Event series created: TeamsMeeting2", output);
  }

  @Test
  public void testPrintEvents() {
    String input =
        "create event TeamsMeeting2 from 2025-11-03T15:00 to 2025-11-03T16:00 "
            + "repeats MTF for 3 times"
            + System.lineSeparator()
            + "print events on 2025-11-03"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String expectedOutput = "Event series created: TeamsMeeting2" + System.lineSeparator()
        + "- TeamsMeeting2 starting on 2025-11-03 at 15:00, ending on 2025-11-03 at 16:00";
    String output = stripAnsi(outContent.toString()).trim();
    assertEquals(expectedOutput, output);
  }

  @Test
  public void testPrintEventsInDateRange() {
    String input =
        "create event TeamsMeeting2 from 2025-11-03T15:00 to 2025-11-03T16:00 "
            + "repeats MTF for 1 times"
            + System.lineSeparator()
            + "print events from 2025-11-03T01:00 to 2025-11-30T11:00"
            + System.lineSeparator() + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String expectedOutput = "Event series created: TeamsMeeting2" + System.lineSeparator()
        + "- TeamsMeeting2 starting on 2025-11-03 at 15:00, ending on 2025-11-03 at 16:00";
    String output = stripAnsi(outContent.toString()).trim();
    assertEquals(expectedOutput, output);
  }

  @Test
  public void testExportCsvCommand() throws IOException {
    File tempFile = File.createTempFile("calendar_export", ".csv");
    tempFile.deleteOnExit();
    String input =
        "create event TeamsMeeting2 from 2025-11-03T15:00 to 2025-11-03T16:00 "
            + "repeats MTF for 1 times"
            + System.lineSeparator()
            + "export cal " + tempFile.getAbsolutePath()
            + System.lineSeparator() + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String content = new String(Files.readAllBytes(tempFile.toPath()));
    assertEquals(content, "Subject,Start Date,Start Time,End Date,End Time,"
        + "All Day Event,Description,Location,Private"
        + System.lineSeparator()
        + "\"TeamsMeeting2\",11/03/2025,03:00 PM,11/03/2025,04:00 PM,False,,,False"
        + System.lineSeparator());

    String expectedOutput = "Event series created: TeamsMeeting2" + System.lineSeparator()
        + "Calendar MyCalendar exported successfully to " + tempFile.getAbsolutePath();
    String output = stripAnsi(outContent.toString()).trim();
    assertEquals(expectedOutput, output);
  }

  @Test
  public void testExportUnsupportedFormat() throws IOException {
    File tempFile = File.createTempFile("calendar_export", ".txt");
    tempFile.deleteOnExit();
    String input =
        "create event TeamsMeeting2 from 2025-11-03T15:00 to 2025-11-03T16:00 "
            + "repeats MTF for 1 times"
            + System.lineSeparator()
            + "export cal " + tempFile.getAbsolutePath()
            + System.lineSeparator() + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String expectedOutput = "Event series created: TeamsMeeting2" + System.lineSeparator()
        + "Error parsing command: Unknown export file type: " + tempFile.getAbsolutePath();
    String output = stripAnsi(outContent.toString()).trim();
    assertEquals(expectedOutput, output);
  }

  @Test
  public void testExportIcalCommand() throws IOException {
    File tempFile = File.createTempFile("calendar_export", ".ical");
    tempFile.deleteOnExit();
    String input =
        "create event TeamsMeeting2 from 2025-11-03T15:00 to 2025-11-03T16:00 "
            + "repeats MTF for 1 times"
            + System.lineSeparator()
            + "export cal " + tempFile.getAbsolutePath()
            + System.lineSeparator() + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String content = new String(Files.readAllBytes(tempFile.toPath()));
    assertTrue(content.contains("BEGIN:VCALENDAR"));
    assertTrue(content.contains("VERSION:2.0"));
    assertTrue(content.contains("BEGIN:VEVENT"));
    assertTrue(content.contains("UID:"));
    assertTrue(content.contains("DTSTAMP:"));
    assertTrue(content.contains("SUMMARY:TeamsMeeting2"));
    assertTrue(content.contains("DTSTART:20251103T200000Z"));
    assertTrue(content.contains("DTEND:20251103T210000Z"));
    assertTrue(content.contains("END:VEVENT"));
    assertTrue(content.contains("END:VCALENDAR"));

    String expectedOutput = "Event series created: TeamsMeeting2" + System.lineSeparator()
        + "Calendar MyCalendar exported successfully to " + tempFile.getAbsolutePath();
    String output = stripAnsi(outContent.toString()).trim();
    assertEquals(expectedOutput, output);
  }

  @Test
  public void testShowStatus() {
    String input =
        "create event TeamsMeeting2 from 2025-11-03T15:00 to 2025-11-03T16:00 "
            + "repeats MTF for 3 times"
            + System.lineSeparator()
            + "show status on 2025-11-03T16:00"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String expectedOutput = "Event series created: TeamsMeeting2" + System.lineSeparator()
        + "Status: Busy";
    String output = stripAnsi(outContent.toString()).trim();
    assertEquals(expectedOutput, output);
  }

  @Test
  public void testEditEvents() {
    String input =
        "create event FootballMatch from 2025-12-06T09:00 to 2025-12-06T23:00 repeats RF "
            + "until 2025-12-30"
            + System.lineSeparator()
            + "edit events subject FootballMatch from 2025-12-18T09:00 with BasketballMatch"
            + System.lineSeparator()
            + "edit events location BasketballMatch from 2025-12-18T09:00 with PHYSICAL"
            + System.lineSeparator()
            + "edit events description BasketballMatch from 2025-12-18T09:00 with \"bball match\""
            + System.lineSeparator()
            + "edit events status BasketballMatch from 2025-12-18T09:00 with PRIVATE"
            + System.lineSeparator()
            + "edit events subject FootballMatch from 2025-12-11T09:00 with VolleyBallMatch"
            + System.lineSeparator()
            + "edit events start VolleyBallMatch from 2025-12-19T09:00 with 2025-12-19T20:00"
            + System.lineSeparator()
            + "edit events subject VolleyBallMatch from 2025-12-11T09:00 with Chess"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event series created: FootballMatch" + System.lineSeparator()
        + "Event/Series edited successfully" + System.lineSeparator()
        + "Event/Series edited successfully" + System.lineSeparator()
        + "Event/Series edited successfully" + System.lineSeparator()
        + "Event/Series edited successfully" + System.lineSeparator()
        + "Event/Series edited successfully" + System.lineSeparator()
        + "Event/Series edited successfully" + System.lineSeparator()
        + "Event/Series edited successfully";
    assertEquals(expectedOutput, output);
  }

  @Test
  public void testEditEvent() {
    String input =
        "create event DoctorAppointment from 2025-11-02T15:00 to 2025-11-02T16:00"
            + System.lineSeparator()
            + "edit event subject DoctorAppointment from 2025-11-02T15:00 to "
            + "2025-11-02T16:00 with Doctor_Appointment"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event created: DoctorAppointment" + System.lineSeparator()
        + "Event edited successfully";
    assertEquals(expectedOutput, output);
  }

  @Test
  public void testEditEventSeriesSubject() {
    String input =
        "create event DoctorAppointment from 2025-11-02T15:00 to 2025-11-02T16:00 "
            + "repeats MT for 2 times"
            + System.lineSeparator()
            + "edit series subject DoctorAppointment from 2025-11-03T15:00 with Doctor_Appointment"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event series created: DoctorAppointment" + System.lineSeparator()
        + "Event/Series edited successfully";
    assertEquals(expectedOutput, output);
  }

  @Test
  public void testEditEventSeriesDescription() {
    String input =
        "create event DoctorAppointment from 2025-11-02T15:00 to 2025-11-02T16:00 "
            + "repeats MT for 2 times"
            + System.lineSeparator()
            + "create event DoctorAppointment2 from 2025-11-04T15:00 to 2025-11-04T16:00 "
            + "repeats WF for 2 times"
            + System.lineSeparator()
            + "edit series description DoctorAppointment from 2025-11-03T15:00 with "
            + "Doctor_Appointment"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event series created: DoctorAppointment" + System.lineSeparator()
        + "Event series created: DoctorAppointment2" + System.lineSeparator()
        + "Event/Series edited successfully";
    assertEquals(expectedOutput, output);
  }

  @Test
  public void testEditEventSeriesLocation() {
    String input =
        "create event DoctorAppointment from 2025-11-02T15:00 to 2025-11-02T16:00 "
            + "repeats MT for 2 times"
            + System.lineSeparator()
            + "edit series location DoctorAppointment from 2025-11-03T15:00 with online"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event series created: DoctorAppointment" + System.lineSeparator()
        + "Event/Series edited successfully";
    assertEquals(expectedOutput, output);
  }

  @Test
  public void testEditEventSeriesStatus() {
    String input =
        "create event DoctorAppointment from 2025-11-02T15:00 to 2025-11-02T16:00 "
            + "repeats MT for 2 times"
            + System.lineSeparator()
            + "edit series status DoctorAppointment from 2025-11-03T15:00 with public"
            + System.lineSeparator() + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event series created: DoctorAppointment" + System.lineSeparator()
        + "Event/Series edited successfully";
    assertEquals(expectedOutput, output);
  }

  @Test
  public void testEditEventSeriesStart() {
    String input =
        "create event DoctorAppointment from 2025-11-02T15:00 to 2025-11-02T16:00 "
            + "repeats MT for 2 times"
            + System.lineSeparator()
            + "edit series start DoctorAppointment from 2025-11-03T15:00 with 2025-11-03T12:00"
            + System.lineSeparator() + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event series created: DoctorAppointment" + System.lineSeparator()
        + "Event/Series edited successfully";
    assertEquals(expectedOutput, output);
  }

  @Test
  public void testEditEventSeriesEnd() {
    String input =
        "create event DoctorAppointment from 2025-11-02T15:00 to 2025-11-02T16:00 "
            + "repeats MT for 2 times"
            + System.lineSeparator()
            + "edit series end DoctorAppointment from 2025-11-03T15:00 with 2025-11-03T17:00"
            + System.lineSeparator() + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event series created: DoctorAppointment" + System.lineSeparator()
        + "Event/Series edited successfully";
    assertEquals(expectedOutput, output);
  }

  @Test
  public void testEditEventDoesntExist() {
    String input =
        "edit event subject DoctorAppointment from 2025-11-02T15:00 to "
            + "2025-11-02T16:00 with Doctor_Appointment"
            + System.lineSeparator() + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    controller.go();

    String expectedOutput = "Event with subject DoctorAppointment startDateTime 2025-11-02T15:00 "
        + "endDateTime 2025-11-02T16:00 not found";
    String output = stripAnsi(outContent.toString()).trim();
    assertEquals(expectedOutput, output);
  }

  @Test
  public void testCopyNonExistentSingleEventr() {
    String input =
        "copy event DoctorAppointment on 2025-11-02T15:00 --target CopyCalendar "
            + "to 2025-11-04T12:00"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    Set<Event> events = targetCalendar.getAllEvents();
    assertEquals(0, events.size());

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event not found: DoctorAppointment at 2025-11-02T15:00";
    assertEquals(expectedOutput, output);
    events = targetCalendar.getAllEvents();
    assertEquals(0, events.size());
  }

  @Test
  public void testCopySingleEventToDifferentCalendar() {
    String input =
        "create event DoctorAppointment from 2025-11-02T15:00 to 2025-11-02T16:00"
            + System.lineSeparator()
            + "copy event DoctorAppointment on 2025-11-02T15:00 --target CopyCalendar "
            + "to 2025-11-04T12:00"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    Set<Event> events = targetCalendar.getAllEvents();
    assertEquals(0, events.size());

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event created: DoctorAppointment"
        + System.lineSeparator()
        + "Successfully copied event to calendar CopyCalendar";
    assertEquals(expectedOutput, output);
    events = targetCalendar.getAllEvents();
    assertEquals(1, events.size());
    assertEquals(LocalDateTime.of(2025, 11, 4, 12, 0),
        events.iterator().next().getStartDateTime());
  }

  @Test
  public void testCopySingleEventToSameCalendar() {
    String input =
        "create event DoctorAppointment from 2025-11-02T15:00 to 2025-11-02T16:00"
            + System.lineSeparator()
            + "copy event DoctorAppointment on 2025-11-02T15:00 --target MyCalendar "
            + "to 2025-11-04T12:00"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    Set<Event> events = model.getAllEvents();
    assertEquals(0, events.size());

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event created: DoctorAppointment"
        + System.lineSeparator()
        + "Successfully copied event to calendar MyCalendar";
    assertEquals(expectedOutput, output);

    events = model.getAllEvents();
    assertEquals(2, events.size());
    assertTrue(events.stream().anyMatch(e -> e.getStartDateTime().isEqual(
        LocalDateTime.of(2025, 11, 4, 12, 0))));
  }

  @Test
  public void testCopySingleEventFromSeriesFailsWhenSpansAcrossDay() {
    String input =
        "create event TeamsMeeting2 from 2025-11-01T15:00 to 2025-11-03T16:00 "
            + "repeats MTWRFSU for 3 times"
            + System.lineSeparator()
            + "copy event TeamsMeeting2 on 2025-11-02T15:00 --target CopyCalendar "
            + "to 2025-11-04T12:00"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    Set<Event> events = targetCalendar.getAllEvents();
    assertEquals(0, events.size());

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event series created: TeamsMeeting2"
        + System.lineSeparator()
        + "Failed to create event series: Events in a series must start and end on the same day.";

    events = targetCalendar.getAllEvents();
    assertEquals(0, events.size());
  }

  @Test
  public void testCopySingleEventFromSeries() {
    String input =
        "create event TeamsMeeting2 from 2025-11-01T22:00 to 2025-11-01T23:00 "
            + "repeats MTWRFSU for 3 times"
            + System.lineSeparator()
            + "copy event TeamsMeeting2 on 2025-11-02T22:00 --target CopyCalendar "
            + "to 2025-11-04T12:00"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    Set<Event> events = targetCalendar.getAllEvents();
    assertEquals(0, events.size());

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event series created: TeamsMeeting2"
        + System.lineSeparator()
        + "Successfully copied event to calendar CopyCalendar";
    assertEquals(expectedOutput, output);
    events = targetCalendar.getAllEvents();
    assertEquals(1, events.size());
    assertEquals(LocalDateTime.of(2025, 11, 4, 12, 0),
        events.iterator().next().getStartDateTime());
  }

  @Test
  public void testCopySingleEventFromSeriesTargetDateNotInRecurrenceRuleDays() {
    String input =
        "create event TeamsMeeting2 from 2025-11-01T22:00 to 2025-11-01T23:00 "
            + "repeats M for 3 times"
            + System.lineSeparator()
            + "copy event TeamsMeeting2 on 2025-11-03T22:00 --target CopyCalendar "
            + "to 2025-11-04T12:00"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    Set<Event> events = targetCalendar.getAllEvents();
    assertEquals(0, events.size());

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event series created: TeamsMeeting2"
        + System.lineSeparator()
        + "Successfully copied event to calendar CopyCalendar";
    assertEquals(expectedOutput, output);
    events = targetCalendar.getAllEvents();
    assertEquals(1, events.size()); // Event should be copied even though Nov 4 is not Monday
    assertEquals(LocalDateTime.of(2025, 11, 4, 12, 0),
        events.iterator().next().getStartDateTime());
  }


  @Test
  public void testCopyEventsOnToDifferentCalendar() {
    String input =
        "create event DoctorAppointment from 2025-11-01T15:00 to 2025-11-02T16:00"
            + System.lineSeparator()
            + "create event DoctorAppointment2 from 2025-11-02T11:00 to 2025-11-02T12:00"
            + System.lineSeparator()
            + "create event DoctorAppointment3 from 2025-11-02T17:00 to 2025-11-03T12:00"
            + System.lineSeparator()
            + "copy events on 2025-11-02 --target CopyCalendar to 2025-11-05"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    Set<Event> events = targetCalendar.getAllEvents();
    assertEquals(0, events.size());

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event created: DoctorAppointment"
        + System.lineSeparator()
        + "Event created: DoctorAppointment2"
        + System.lineSeparator()
        + "Event created: DoctorAppointment3"
        + System.lineSeparator()
        + "Copied events on 2025-11-02 to CopyCalendar on 2025-11-05";
    assertEquals(expectedOutput, output);
    events = targetCalendar.getAllEvents();
    assertEquals(3, events.size());
    assertNotNull(targetCalendar.findSingleEvent("DoctorAppointment",
        LocalDateTime.of(2025, 11, 5, 1, 30),
        LocalDateTime.of(2025, 11, 6, 2, 30)));
    assertNotNull(targetCalendar.findSingleEvent("DoctorAppointment2",
        LocalDateTime.of(2025, 11, 5, 21, 30),
        LocalDateTime.of(2025, 11, 5, 22, 30)));
    assertNotNull(targetCalendar.findSingleEvent("DoctorAppointment3",
        LocalDateTime.of(2025, 11, 6, 3, 30),
        LocalDateTime.of(2025, 11, 6, 22, 30)));
  }

  @Test
  public void testCopyEventsOnToSameCalendar() {
    String input =
        "create event DoctorAppointment from 2025-11-01T15:00 to 2025-11-02T16:00"
            + System.lineSeparator()
            + "create event DoctorAppointment2 from 2025-11-02T11:00 to 2025-11-02T12:00"
            + System.lineSeparator()
            + "create event DoctorAppointment3 from 2025-11-02T17:00 to 2025-11-03T12:00"
            + System.lineSeparator()
            + "copy events on 2025-11-02 --target MyCalendar to 2025-11-05"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    Set<Event> events = model.getAllEvents();
    assertEquals(0, events.size());

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event created: DoctorAppointment"
        + System.lineSeparator()
        + "Event created: DoctorAppointment2"
        + System.lineSeparator()
        + "Event created: DoctorAppointment3"
        + System.lineSeparator()
        + "Copied events on 2025-11-02 to MyCalendar on 2025-11-05";
    assertEquals(expectedOutput, output);
    events = model.getAllEvents();
    assertEquals(6, events.size());
    assertNotNull(model.findSingleEvent("DoctorAppointment",
        LocalDateTime.of(2025, 11, 4, 15, 0),
        LocalDateTime.of(2025, 11, 5, 16, 0)));
    assertNotNull(model.findSingleEvent("DoctorAppointment2",
        LocalDateTime.of(2025, 11, 5, 11, 0),
        LocalDateTime.of(2025, 11, 5, 12, 0)));
    assertNotNull(model.findSingleEvent("DoctorAppointment3",
        LocalDateTime.of(2025, 11, 5, 17, 0),
        LocalDateTime.of(2025, 11, 6, 12, 0)));
  }

  @Test
  public void testCopyEventsOnFromSeries() {
    String input =
        "create event TeamsMeeting2 from 2025-11-01T22:00 to 2025-11-01T23:00 "
            + "repeats MTWRFSU for 3 times"
            + System.lineSeparator()
            + "copy events on 2025-11-02 --target CopyCalendar to 2025-11-03"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    Set<Event> events = targetCalendar.getAllEvents();
    assertEquals(0, events.size());

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event series created: TeamsMeeting2"
        + System.lineSeparator()
        + "Copied events on 2025-11-02 to CopyCalendar on 2025-11-03";
    assertEquals(expectedOutput, output);
    events = targetCalendar.getAllEvents();
    assertEquals(1, events.size());
    assertEquals(LocalDateTime.of(2025, 11, 3, 8, 30),
        events.iterator().next().getStartDateTime());
  }

  @Test
  public void testCopyEventsBetweenToDifferentCalendar() {
    String input =
        "create event DoctorAppointment from 2025-11-01T15:00 to 2025-11-02T16:00"
            + System.lineSeparator()
            + "create event DoctorAppointment2 from 2025-11-02T11:00 to 2025-11-02T12:00"
            + System.lineSeparator()
            + "create event DoctorAppointment3 from 2025-11-02T17:00 to 2025-11-03T12:00"
            + System.lineSeparator()
            + "create event DoctorAppointment4 from 2025-11-03T17:00 to 2025-11-04T12:00"
            + System.lineSeparator()
            + "create event DoctorAppointment5 from 2025-11-04T17:00 to 2025-11-04T19:00"
            + System.lineSeparator()
            + "copy events between 2025-11-02 and 2025-11-03 --target CopyCalendar to 2025-11-05"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    Set<Event> events = targetCalendar.getAllEvents();
    assertEquals(0, events.size());

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event created: DoctorAppointment"
        + System.lineSeparator()
        + "Event created: DoctorAppointment2"
        + System.lineSeparator()
        + "Event created: DoctorAppointment3"
        + System.lineSeparator()
        + "Event created: DoctorAppointment4"
        + System.lineSeparator()
        + "Event created: DoctorAppointment5"
        + System.lineSeparator()
        + "Successfully copied events to CopyCalendar";
    assertEquals(expectedOutput, output);
    events = targetCalendar.getAllEvents();
    assertEquals(4, events.size());
    assertNotNull(targetCalendar.findSingleEvent("DoctorAppointment",
        LocalDateTime.of(2025, 11, 5, 1, 30),
        LocalDateTime.of(2025, 11, 6, 2, 30)));
    assertNotNull(targetCalendar.findSingleEvent("DoctorAppointment2",
        LocalDateTime.of(2025, 11, 5, 21, 30),
        LocalDateTime.of(2025, 11, 5, 22, 30)));
    assertNotNull(targetCalendar.findSingleEvent("DoctorAppointment3",
        LocalDateTime.of(2025, 11, 6, 3, 30),
        LocalDateTime.of(2025, 11, 6, 22, 30)));
    assertNotNull(targetCalendar.findSingleEvent("DoctorAppointment4",
        LocalDateTime.of(2025, 11, 7, 3, 30),
        LocalDateTime.of(2025, 11, 7, 22, 30)));
  }

  @Test
  public void testCopyEventsBetweenToSameCalendar() {
    String input =
        "create event DoctorAppointment from 2025-11-01T15:00 to 2025-11-02T16:00"
            + System.lineSeparator()
            + "create event DoctorAppointment2 from 2025-11-02T11:00 to 2025-11-02T12:00"
            + System.lineSeparator()
            + "create event DoctorAppointment3 from 2025-11-02T17:00 to 2025-11-03T12:00"
            + System.lineSeparator()
            + "create event DoctorAppointment4 from 2025-11-03T17:00 to 2025-11-04T12:00"
            + System.lineSeparator()
            + "create event DoctorAppointment5 from 2025-11-04T17:00 to 2025-11-04T19:00"
            + System.lineSeparator()
            + "copy events between 2025-11-02 and 2025-11-03 --target MyCalendar to 2025-11-05"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    Set<Event> events = model.getAllEvents();
    assertEquals(0, events.size());

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event created: DoctorAppointment"
        + System.lineSeparator()
        + "Event created: DoctorAppointment2"
        + System.lineSeparator()
        + "Event created: DoctorAppointment3"
        + System.lineSeparator()
        + "Event created: DoctorAppointment4"
        + System.lineSeparator()
        + "Event created: DoctorAppointment5"
        + System.lineSeparator()
        + "Successfully copied events to MyCalendar";
    assertEquals(expectedOutput, output);
    events = model.getAllEvents();
    assertEquals(9, events.size());
    assertNotNull(model.findSingleEvent("DoctorAppointment",
        LocalDateTime.of(2025, 11, 4, 15, 0),
        LocalDateTime.of(2025, 11, 5, 16, 0)));
    assertNotNull(model.findSingleEvent("DoctorAppointment2",
        LocalDateTime.of(2025, 11, 5, 11, 0),
        LocalDateTime.of(2025, 11, 5, 12, 0)));
    assertNotNull(model.findSingleEvent("DoctorAppointment3",
        LocalDateTime.of(2025, 11, 5, 17, 0),
        LocalDateTime.of(2025, 11, 6, 12, 0)));
    assertNotNull(model.findSingleEvent("DoctorAppointment4",
        LocalDateTime.of(2025, 11, 6, 17, 0),
        LocalDateTime.of(2025, 11, 7, 12, 0)));
  }


  @Test
  public void testCopyEventsBetweenFromSeries() {
    String input =
        "create event TeamsMeeting2 from 2025-11-01T22:00 to 2025-11-01T23:00 "
            + "repeats MTWRFSU for 5 times"
            + System.lineSeparator()
            + "copy events between 2025-11-02 and 2025-11-03 --target CopyCalendar to 2025-11-05"
            + System.lineSeparator()
            + "exit";
    Readable readable = new StringReader(input);
    ControllerImpl controller = new ControllerImpl(view, readable, parser);

    Set<Event> events = targetCalendar.getAllEvents();
    assertEquals(0, events.size());

    controller.go();

    String output = stripAnsi(outContent.toString()).trim();
    String expectedOutput = "Event series created: TeamsMeeting2"
        + System.lineSeparator()
        + "Successfully copied events to CopyCalendar";
    assertEquals(expectedOutput, output);
    events = targetCalendar.getAllEvents();
    assertEquals(2, events.size());
    assertTrue(events.stream().anyMatch(e -> e.getStartDateTime().isEqual(
        LocalDateTime.of(2025, 11, 6, 8, 30))));
    assertTrue(events.stream().anyMatch(e -> e.getStartDateTime().isEqual(
        LocalDateTime.of(2025, 11, 7, 8, 30))));
  }
}