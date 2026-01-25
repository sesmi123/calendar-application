package calendar.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.controllers.commands.Command;
import calendar.controllers.commands.CopyEventsOnCommand;
import calendar.controllers.mocks.MockView;
import calendar.models.Calendar;
import calendar.models.CalendarImpl;
import calendar.models.Event;
import calendar.models.EventImpl;
import calendar.models.enums.Location;
import calendar.models.enums.Status;
import calendar.views.ObservableView;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CopyEventsOnCommand.
 */
public class CopyEventsOnCommandTest {

  private StringBuilder log;
  private ObservableView view;
  private Calendar sourceCalendar;
  private Calendar targetCalendar;
  private Event event1;
  private Event event2;
  private Event event3;
  private LocalDate date;
  private LocalTime startTime;
  private LocalTime endTime;

  /**
   * Test set up.
   */
  @Before
  public void setUp() {
    log = new StringBuilder();
    date = LocalDate.of(2025, 11, 26);
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

    event3 = EventImpl.getBuilder()
        .subject("Workshop")
        .from(date.plusDays(1), LocalTime.of(14, 0))
        .to(date.plusDays(1), LocalTime.of(15, 0))
        .build();

    sourceCalendar.addEvent(event1);
    sourceCalendar.addEvent(event2);
    sourceCalendar.addEvent(event3);
  }

  @Test
  public void testCopyEventsOnSuccessDifferentCalendar() {
    assertEquals(0, targetCalendar.getAllEvents().size());

    LocalDate targetDate = date.plusDays(5);
    Command c = new CopyEventsOnCommand(view, sourceCalendar, targetCalendar, date, targetDate);
    c.execute();

    assertEquals(2, targetCalendar.getAllEvents().size());
    assertTrue(log.toString().contains("Success: Copied events on " + date));
    assertTrue(log.toString().contains("to " + targetCalendar.getTitle()));
    assertTrue(log.toString().contains("on " + targetDate));
  }

  @Test
  public void testCopyEventsOnSuccessSameCalendar() {
    int originalSize = sourceCalendar.getAllEvents().size();

    LocalDate targetDate = date.plusDays(5);
    Command c = new CopyEventsOnCommand(view, sourceCalendar, sourceCalendar, date, targetDate);
    c.execute();

    assertEquals(originalSize + 2, sourceCalendar.getAllEvents().size());
    assertTrue(log.toString().contains("Success"));
  }

  @Test
  public void testCopyEventsOnMultipleEventsDifferentCalendar() {
    Event event4 = EventImpl.getBuilder()
        .subject("Sprint Planning")
        .from(date, LocalTime.of(15, 0))
        .to(date, LocalTime.of(16, 30))
        .build();
    sourceCalendar.addEvent(event4);

    LocalDate targetDate = date.plusDays(5);
    Command c = new CopyEventsOnCommand(view, sourceCalendar, targetCalendar, date, targetDate);
    c.execute();

    assertEquals(3, targetCalendar.getAllEvents().size());
  }

  @Test
  public void testCopyEventsOnPreservesProperties() {
    LocalDate targetDate = date.plusDays(5);
    Command c = new CopyEventsOnCommand(view, sourceCalendar, targetCalendar, date, targetDate);
    c.execute();

    Event copiedMeeting = targetCalendar.findSingleEvent("Meeting",
        java.time.LocalDateTime.of(targetDate, LocalTime.of(20, 30)),
        java.time.LocalDateTime.of(targetDate, LocalTime.of(21, 30)));

    assertNotNull(copiedMeeting);
    assertEquals("Meeting", copiedMeeting.getSubject());
    assertEquals("Project discussion", copiedMeeting.getDescription());
    assertEquals(Location.PHYSICAL, copiedMeeting.getLocation());
    assertEquals(Status.PRIVATE, copiedMeeting.getStatus());
  }

  @Test
  public void testCopyEventsOnPreservesDuration() {
    LocalDate targetDate = date.plusDays(5);
    Command c = new CopyEventsOnCommand(view, sourceCalendar, targetCalendar, date, targetDate);
    c.execute();

    List<Event> copiedEvents = new ArrayList<>(targetCalendar.getAllEvents());
    copiedEvents.sort((e1, e2) -> e1.getStartTime().compareTo(e2.getStartTime()));

    Event firstEvent = copiedEvents.get(0);
    Event secondEvent = copiedEvents.get(1);

    assertEquals(LocalTime.of(20, 30), firstEvent.getStartTime());
    assertEquals(LocalTime.of(21, 30), firstEvent.getEndTime());

    assertEquals(LocalTime.of(22, 30), secondEvent.getStartTime());
    assertEquals(LocalTime.of(23, 0), secondEvent.getEndTime());
  }

  @Test
  public void testCopyEventsOnNoEventsOnDate() {
    LocalDate dateWithNoEvents = date.plusDays(10);
    LocalDate targetDate = date.plusDays(15);

    Command c = new CopyEventsOnCommand(view, sourceCalendar, targetCalendar,
        dateWithNoEvents, targetDate);
    c.execute();

    assertEquals(0, targetCalendar.getAllEvents().size());
    assertTrue(log.toString().contains("Error: Events with startDate " + dateWithNoEvents
        + " not found"));
  }

  @Test
  public void testCopyEventsOnConvertsTimezoneDifferentCalendar() {
    LocalDate targetDate = date.plusDays(5);
    Command c = new CopyEventsOnCommand(view, sourceCalendar, targetCalendar, date, targetDate);
    c.execute();

    assertEquals(2, targetCalendar.getAllEvents().size());
    List<Event> events = new ArrayList<>(targetCalendar.getAllEvents());
    for (Event e : events) {
      assertNotNull(e.getStartTime());
      assertNotNull(e.getEndTime());
    }
  }

  @Test
  public void testCopyEventsOnConvertsTimezoneSameCalendar() {
    int originalSize = sourceCalendar.getAllEvents().size();

    LocalDate targetDate = date.plusDays(5);
    Command c = new CopyEventsOnCommand(view, sourceCalendar, sourceCalendar, date, targetDate);
    c.execute();

    assertEquals(originalSize + 2, sourceCalendar.getAllEvents().size());
  }

  @Test
  public void testCopyEventsOnCopiesAsStandaloneEvents() {
    LocalDate targetDate = date.plusDays(5);
    Command c = new CopyEventsOnCommand(view, sourceCalendar, targetCalendar, date, targetDate);
    c.execute();

    for (Event e : targetCalendar.getAllEvents()) {
      assertFalse(e.isPartOfSeries());
    }
  }

  @Test
  public void testCopyEventsOnSuccessMessageFormat() {
    LocalDate targetDate = date.plusDays(5);
    Command c = new CopyEventsOnCommand(view, sourceCalendar, targetCalendar, date, targetDate);
    c.execute();

    String expectedMessage = "Success: Copied events on " + date + " to "
        + targetCalendar.getTitle() + " on " + targetDate + System.lineSeparator();
    assertEquals(expectedMessage, log.toString());
  }

  @Test
  public void testCopyEventsOnSingleEventOnDate() {
    LocalDate singleEventDate = date.plusDays(2);
    Event singleEvent = EventImpl.getBuilder()
        .subject("One Time Meeting")
        .from(singleEventDate, LocalTime.of(9, 0))
        .to(singleEventDate, LocalTime.of(10, 0))
        .build();

    Calendar singleEventCalendar = new CalendarImpl("Single Event Calendar",
        ZoneId.of("America/New_York"));
    singleEventCalendar.addEvent(singleEvent);

    LocalDate targetDate = singleEventDate.plusDays(3);
    Command c = new CopyEventsOnCommand(view, singleEventCalendar, targetCalendar,
        singleEventDate, targetDate);
    c.execute();

    assertEquals(1, targetCalendar.getAllEvents().size());
    Event copiedEvent = targetCalendar.getAllEvents().iterator().next();
    assertEquals("One Time Meeting", copiedEvent.getSubject());
    assertEquals(targetDate, copiedEvent.getStartDate());
  }

  @Test
  public void testCopyEventsOnDateOffsetCalculation() {
    LocalDate sourceDate = date;
    LocalDate targetDate = date.plusDays(10);

    Command c = new CopyEventsOnCommand(view, sourceCalendar,
        targetCalendar, sourceDate, targetDate);
    c.execute();

    List<Event> copiedEvents = new ArrayList<>(targetCalendar.getAllEvents());
    for (Event e : copiedEvents) {
      assertEquals(targetDate, e.getStartDate());
    }
  }

  @Test
  public void testCopyEventsOnExceptionHandling() {
    LocalDate targetDate = date.plusDays(5);
    Command c = new CopyEventsOnCommand(view, sourceCalendar, targetCalendar, date, targetDate);

    try {
      c.execute();
      assertEquals(2, targetCalendar.getAllEvents().size());
    } catch (Exception e) {
      assertTrue(log.toString().contains("Error"));
    }
  }

  @Test
  public void testCopyEventsOnFromOneCalendarToMultiple() {
    Calendar thirdCalendar = new CalendarImpl("Third Calendar", ZoneId.of("Europe/Paris"));

    LocalDate targetDate = date.plusDays(5);

    Command c1 = new CopyEventsOnCommand(view, sourceCalendar, targetCalendar, date, targetDate);
    c1.execute();

    log.setLength(0);
    Command c2 = new CopyEventsOnCommand(view, sourceCalendar, thirdCalendar, date, targetDate);
    c2.execute();

    assertEquals(2, targetCalendar.getAllEvents().size());
    assertEquals(2, thirdCalendar.getAllEvents().size());
    assertEquals(3, sourceCalendar.getAllEvents().size()); // Original unchanged
  }

  @Test
  public void testCopyEventsOnWithEventSpanningMultipleDays() {
    Event multiDayEvent = EventImpl.getBuilder()
        .subject("Multi-day Event")
        .from(date, LocalTime.of(22, 0))
        .to(date.plusDays(1), LocalTime.of(2, 0))
        .build();

    Calendar testCalendar = new CalendarImpl("Test Calendar", ZoneId.of("America/New_York"));
    testCalendar.addEvent(multiDayEvent);

    LocalDate targetDate = date.plusDays(5);
    Command c = new CopyEventsOnCommand(view, testCalendar, targetCalendar, date, targetDate);
    c.execute();

    assertEquals(1, targetCalendar.getAllEvents().size());
    Event copiedEvent = targetCalendar.getAllEvents().iterator().next();
    assertEquals(targetDate.plusDays(1), copiedEvent.getStartDate());
  }


  @Test
  public void testCopyEventsOnNullParameterHandling() {
    try {
      new CopyEventsOnCommand(view, sourceCalendar, null, date, date.plusDays(5));
      fail();
    } catch (NullPointerException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testCopyEventsOnDuplicateEvent() {

    LocalDate targetDate = date.plusDays(5);

    Command c1 = new CopyEventsOnCommand(view, sourceCalendar, targetCalendar, date, targetDate);
    c1.execute();

    assertEquals(2, targetCalendar.getAllEvents().size());

    log.setLength(0);

    Command c2 = new CopyEventsOnCommand(view, sourceCalendar, targetCalendar, date, targetDate);
    c2.execute();

    assertTrue(log.toString().contains("Error: Failed to copy event:"));
  }
}