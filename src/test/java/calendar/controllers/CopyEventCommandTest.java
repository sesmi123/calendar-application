package calendar.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.controllers.commands.Command;
import calendar.controllers.commands.CopyEventCommand;
import calendar.controllers.mocks.MockView;
import calendar.models.Calendar;
import calendar.models.CalendarImpl;
import calendar.models.Event;
import calendar.models.EventImpl;
import calendar.models.enums.Location;
import calendar.models.enums.Status;
import calendar.views.ObservableView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CopyEventCommand.
 */
public class CopyEventCommandTest {

  private StringBuilder log;
  private ObservableView view;
  private Calendar sourceCalendar;
  private Calendar targetCalendar;
  private Event event1;
  private Event event2;
  private LocalDate date;
  private LocalTime startTime;
  private LocalTime endTime;

  /**
   * Test set up.
   */
  @Before
  public void setUp() {
    log = new StringBuilder();
    date = LocalDate.of(2025, 10, 26);
    startTime = LocalTime.of(10, 0);
    endTime = LocalTime.of(11, 0);

    sourceCalendar = new CalendarImpl("Source Calendar", ZoneId.of("America/New_York"));
    targetCalendar = new CalendarImpl("Target Calendar", ZoneId.of("Asia/Kolkata"));
    view = new MockView(log, 234567);

    event1 = EventImpl.getBuilder()
        .subject("Meeting")
        .from(date, startTime)
        .to(date, endTime)
        .description("Project discussion")
        .location(Location.PHYSICAL)
        .status(Status.PRIVATE)
        .build();

    event2 = EventImpl.getBuilder()
        .subject("Standup")
        .from(date, LocalTime.of(12, 0))
        .to(date, LocalTime.of(12, 30))
        .build();

    sourceCalendar.addEvent(event1);
    sourceCalendar.addEvent(event2);
  }

  @Test
  public void testCopyEventSuccessDifferentCalendar() {
    LocalDateTime sourceStart = LocalDateTime.of(date, startTime);
    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 14, 0);

    assertEquals(0, targetCalendar.getAllEvents().size());

    Command c = new CopyEventCommand(view, sourceCalendar, targetCalendar, "Meeting",
        sourceStart, targetStart);
    c.execute();

    assertEquals(1, targetCalendar.getAllEvents().size());
    Event copiedEvent = targetCalendar.getAllEvents().iterator().next();
    assertEquals("Meeting", copiedEvent.getSubject());
    assertEquals(LocalDate.of(2025, 11, 1), copiedEvent.getStartDate());
    assertEquals(LocalTime.of(14, 0), copiedEvent.getStartTime());
    assertEquals("Success: Successfully copied event to calendar Target Calendar"
        + System.lineSeparator(), log.toString());
  }

  @Test
  public void testCopyEventSuccessSameCalendar() {
    LocalDateTime sourceStart = LocalDateTime.of(date, startTime);
    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 14, 0);

    int originalSize = sourceCalendar.getAllEvents().size();

    Command c = new CopyEventCommand(view, sourceCalendar, sourceCalendar, "Meeting",
        sourceStart, targetStart);
    c.execute();

    assertEquals(originalSize + 1, sourceCalendar.getAllEvents().size());
    assertEquals("Success: Successfully copied event to calendar Source Calendar"
        + System.lineSeparator(), log.toString());
  }

  @Test
  public void testCopyEventPreservesProperties() {
    LocalDateTime sourceStart = LocalDateTime.of(date, startTime);
    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 14, 0);

    Command c = new CopyEventCommand(view, sourceCalendar, targetCalendar, "Meeting",
        sourceStart, targetStart);
    c.execute();

    Event copiedEvent = targetCalendar.getAllEvents().iterator().next();
    assertEquals("Meeting", copiedEvent.getSubject());
    assertEquals("Project discussion", copiedEvent.getDescription());
    assertEquals(Location.PHYSICAL, copiedEvent.getLocation());
    assertEquals(Status.PRIVATE, copiedEvent.getStatus());
  }

  @Test
  public void testCopyEventPreservesDuration() {
    LocalDateTime sourceStart = LocalDateTime.of(date, startTime);
    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 14, 0);

    Command c = new CopyEventCommand(view, sourceCalendar, targetCalendar, "Meeting",
        sourceStart, targetStart);
    c.execute();

    Event copiedEvent = targetCalendar.getAllEvents().iterator().next();
    assertEquals(LocalTime.of(14, 0), copiedEvent.getStartTime());
    assertEquals(LocalTime.of(15, 0), copiedEvent.getEndTime());
  }

  @Test
  public void testCopyEventNotFound() {
    LocalDateTime sourceStart = LocalDateTime.of(date, startTime);
    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 14, 0);

    Command c = new CopyEventCommand(view, sourceCalendar, targetCalendar,
        "Nonexistent Event", sourceStart, targetStart);
    c.execute();

    assertEquals(0, targetCalendar.getAllEvents().size());
    assertTrue(log.toString().contains("Error: Event not found: Nonexistent Event"));
  }

  @Test
  public void testCopyEventWithWrongStartDateTime() {
    LocalDateTime sourceStart = LocalDateTime.of(date, LocalTime.of(15, 0)); // Wrong time
    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 14, 0);

    Command c = new CopyEventCommand(view, sourceCalendar, targetCalendar, "Meeting",
        sourceStart, targetStart);
    c.execute();

    assertEquals(0, targetCalendar.getAllEvents().size());
    assertTrue(log.toString().contains("Error: Event not found: Meeting"));
  }

  @Test
  public void testCopyEventTimezoneConversion() {
    LocalDateTime sourceStart = LocalDateTime.of(date, startTime);
    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 10, 0);

    Command c = new CopyEventCommand(view, sourceCalendar, targetCalendar, "Meeting",
        sourceStart, targetStart);
    c.execute();

    Event copiedEvent = targetCalendar.getAllEvents().iterator().next();
    assertNotNull(copiedEvent);
    assertEquals("Meeting", copiedEvent.getSubject());
  }

  @Test
  public void testCopyEventRemovesSeriesId() {
    Event seriesEventPrototype = EventImpl.getBuilder()
        .subject("Series Event")
        .from(date, startTime)
        .to(date, endTime)
        .seriesId("some-series-id")
        .build();
    sourceCalendar.addEvent(seriesEventPrototype);

    LocalDateTime sourceStart = LocalDateTime.of(date, startTime);
    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 14, 0);

    Command c = new CopyEventCommand(view, sourceCalendar, targetCalendar, "Series Event",
        sourceStart, targetStart);
    c.execute();

    Event copiedEvent = targetCalendar.getAllEvents().iterator().next();
    assertEquals("Series Event", copiedEvent.getSubject());
    assertFalse(copiedEvent.isPartOfSeries());
  }

  @Test
  public void testCopyEventDuplicate() {
    LocalDateTime sourceStart = LocalDateTime.of(date, startTime);
    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 14, 0);

    Command c1 = new CopyEventCommand(view, sourceCalendar, targetCalendar, "Meeting",
        sourceStart, targetStart);
    c1.execute();

    assertEquals(1, targetCalendar.getAllEvents().size());

    log.setLength(0);
    Command c2 = new CopyEventCommand(view, sourceCalendar, targetCalendar, "Meeting",
        sourceStart, targetStart);
    c2.execute();

    assertEquals(1, targetCalendar.getAllEvents().size());
    assertTrue(log.toString().contains("Error: Failed to copy event"));
  }

  @Test
  public void testCopyEventSpansMidnight() {
    Event eveningEvent = EventImpl.getBuilder()
        .subject("Evening Event")
        .from(date, LocalTime.of(22, 0))
        .to(date, LocalTime.of(23, 30))
        .build();
    sourceCalendar.addEvent(eveningEvent);

    LocalDateTime sourceStart = LocalDateTime.of(date, LocalTime.of(22, 0));
    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 23, 0);

    Command c = new CopyEventCommand(view, sourceCalendar, targetCalendar, "Evening Event",
        sourceStart, targetStart);
    c.execute();

    Event copiedEvent = targetCalendar.getAllEvents().iterator().next();
    assertEquals(LocalDate.of(2025, 11, 1), copiedEvent.getStartDate());
    assertEquals(LocalTime.of(23, 0), copiedEvent.getStartTime());
    assertEquals(LocalDate.of(2025, 11, 2), copiedEvent.getEndDate());
    assertEquals(LocalTime.of(0, 30), copiedEvent.getEndTime());
  }

  @Test
  public void testCopyEventMultipleMatches() {
    Event duplicateEvent = EventImpl.getBuilder()
        .subject("Meeting")
        .from(date, startTime)
        .to(date, endTime)
        .build();

    LocalDateTime sourceStart = LocalDateTime.of(date, startTime);
    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 14, 0);

    Command c = new CopyEventCommand(view, sourceCalendar, targetCalendar, "Meeting",
        sourceStart, targetStart);
    c.execute();

    assertEquals(1, targetCalendar.getAllEvents().size());
    assertTrue(log.toString().contains("Success"));
  }

  @Test
  public void testCopyEventSuccessMessageFormat() {
    LocalDateTime sourceStart = LocalDateTime.of(date, startTime);
    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 14, 0);

    Command c = new CopyEventCommand(view, sourceCalendar, targetCalendar, "Meeting",
        sourceStart, targetStart);
    c.execute();

    String expectedMessage = "Success: Successfully copied event to calendar Target Calendar"
        + System.lineSeparator();
    assertEquals(expectedMessage, log.toString());
  }

  @Test
  public void testCopyEventFromOneCalendarToMultiple() {
    Calendar thirdCalendar = new CalendarImpl("Third Calendar", ZoneId.of("Europe/Paris"));

    LocalDateTime sourceStart = LocalDateTime.of(date, startTime);
    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 14, 0);

    Command c1 = new CopyEventCommand(view, sourceCalendar, targetCalendar, "Meeting",
        sourceStart, targetStart);
    c1.execute();

    Command c2 = new CopyEventCommand(view, sourceCalendar, thirdCalendar, "Meeting",
        sourceStart, targetStart);
    c2.execute();

    assertEquals(1, targetCalendar.getAllEvents().size());
    assertEquals(1, thirdCalendar.getAllEvents().size());
    assertEquals(2, sourceCalendar.getAllEvents().size()); // Original unchanged
  }

  @Test
  public void testCopyEventMultipleEventsFound() {
    Event event3 = EventImpl.getBuilder()
        .subject("Meeting")
        .from(date, startTime)
        .to(date, LocalTime.of(11, 30))
        .description("Different description")
        .build();

    sourceCalendar.addEvent(event3);

    LocalDateTime sourceStart = LocalDateTime.of(date, startTime);
    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 14, 0);

    Command c = new CopyEventCommand(view, sourceCalendar, targetCalendar, "Meeting",
        sourceStart, targetStart);
    c.execute();

    assertEquals(0, targetCalendar.getAllEvents().size());

    assertTrue(log.toString().contains("Error: Multiple events found: Meeting at"));
    assertTrue(log.toString().contains(sourceStart.toString()));
  }
}