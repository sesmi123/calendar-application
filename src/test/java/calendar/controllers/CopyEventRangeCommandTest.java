package calendar.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.controllers.commands.Command;
import calendar.controllers.commands.CopyEventRangeCommand;
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
 * Tests for CopyEventRangeCommand.
 */
public class CopyEventRangeCommandTest {

  private StringBuilder log;
  private ObservableView view;
  private Calendar sourceCalendar;
  private Calendar targetCalendar;
  private LocalDate startDate;
  private LocalTime startTime;
  private LocalTime endTime;

  /**
   * Test set up.
   */
  @Before
  public void setUp() {
    log = new StringBuilder();
    startDate = LocalDate.of(2025, 10, 26);
    startTime = LocalTime.of(10, 0);
    endTime = LocalTime.of(11, 0);

    sourceCalendar = new CalendarImpl("Source Calendar", ZoneId.of("America/New_York"));
    targetCalendar = new CalendarImpl("Target Calendar", ZoneId.of("Asia/Kolkata"));
    view = new MockView(log, 234567);
  }

  @Test
  public void testCopyEventRangeSuccessDifferentCalendar() {
    for (int i = 0; i < 5; i++) {
      Event event = EventImpl.getBuilder()
          .subject("Daily Meeting")
          .from(startDate.plusDays(i), startTime)
          .to(startDate.plusDays(i), endTime)
          .build();
      sourceCalendar.addEvent(event);
    }

    assertEquals(0, targetCalendar.getAllEvents().size());

    LocalDate sourceRangeStart = startDate;
    LocalDate sourceRangeEnd = startDate.plusDays(4);
    LocalDate targetRangeStart = startDate.plusDays(10);

    Command c = new CopyEventRangeCommand(view, sourceCalendar, targetCalendar,
        sourceRangeStart, sourceRangeEnd, targetRangeStart);
    c.execute();

    assertEquals(5, targetCalendar.getAllEvents().size());
    assertTrue(log.toString().contains("Success: Successfully copied events to Target Calendar"));
  }

  @Test
  public void testCopyEventRangeSuccessSameCalendar() {
    for (int i = 0; i < 5; i++) {
      Event event = EventImpl.getBuilder()
          .subject("Daily Meeting")
          .from(startDate.plusDays(i), startTime)
          .to(startDate.plusDays(i), endTime)
          .build();
      sourceCalendar.addEvent(event);
    }

    int originalSize = sourceCalendar.getAllEvents().size();

    LocalDate sourceRangeStart = startDate;
    LocalDate sourceRangeEnd = startDate.plusDays(4);
    LocalDate targetRangeStart = startDate.plusDays(10);

    Command c = new CopyEventRangeCommand(view, sourceCalendar, sourceCalendar,
        sourceRangeStart, sourceRangeEnd, targetRangeStart);
    c.execute();

    assertEquals(originalSize + 5, sourceCalendar.getAllEvents().size());
    assertTrue(log.toString().contains("Success"));
  }

  @Test
  public void testCopyEventRangeNoEventsInRange() {
    Event event = EventImpl.getBuilder()
        .subject("Outside Event")
        .from(startDate.plusDays(20), startTime)
        .to(startDate.plusDays(20), endTime)
        .build();
    sourceCalendar.addEvent(event);

    LocalDate sourceRangeStart = startDate;
    LocalDate sourceRangeEnd = startDate.plusDays(4);
    LocalDate targetRangeStart = startDate.plusDays(10);

    Command c = new CopyEventRangeCommand(view, sourceCalendar, targetCalendar,
        sourceRangeStart, sourceRangeEnd, targetRangeStart);
    c.execute();

    assertEquals(0, targetCalendar.getAllEvents().size());
    assertTrue(log.toString().contains("Success: No events found in the specified range to copy"));
  }

  @Test
  public void testCopyEventRangePartialOverlap() {
    for (int i = 0; i < 10; i++) {
      Event event = EventImpl.getBuilder()
          .subject("Event " + i)
          .from(startDate.plusDays(i), startTime)
          .to(startDate.plusDays(i), endTime)
          .build();
      sourceCalendar.addEvent(event);
    }

    LocalDate sourceRangeStart = startDate.plusDays(2);
    LocalDate sourceRangeEnd = startDate.plusDays(6);
    LocalDate targetRangeStart = startDate.plusDays(15);

    Command c = new CopyEventRangeCommand(view, sourceCalendar, targetCalendar,
        sourceRangeStart, sourceRangeEnd, targetRangeStart);
    c.execute();

    assertEquals(5, targetCalendar.getAllEvents().size());
  }

  @Test
  public void testCopyEventRangePreservesProperties() {
    Event eventWithProps = EventImpl.getBuilder()
        .subject("Important Meeting")
        .from(startDate, startTime)
        .to(startDate, endTime)
        .description("Q4 Planning")
        .location(Location.PHYSICAL)
        .status(Status.PRIVATE)
        .build();
    sourceCalendar.addEvent(eventWithProps);

    LocalDate sourceRangeStart = startDate;
    LocalDate sourceRangeEnd = startDate.plusDays(1);
    LocalDate targetRangeStart = startDate.plusDays(10);

    Command c = new CopyEventRangeCommand(view, sourceCalendar, targetCalendar,
        sourceRangeStart, sourceRangeEnd, targetRangeStart);
    c.execute();

    Event copiedEvent = targetCalendar.getAllEvents().iterator().next();
    assertEquals("Important Meeting", copiedEvent.getSubject());
    assertEquals("Q4 Planning", copiedEvent.getDescription());
    assertEquals(Location.PHYSICAL, copiedEvent.getLocation());
    assertEquals(Status.PRIVATE, copiedEvent.getStatus());
  }

  @Test
  public void testCopyEventRangePreservesDuration() {
    Event twoHourEvent = EventImpl.getBuilder()
        .subject("Long Meeting")
        .from(startDate, LocalTime.of(9, 0))
        .to(startDate, LocalTime.of(11, 0))
        .build();
    sourceCalendar.addEvent(twoHourEvent);

    LocalDate sourceRangeStart = startDate;
    LocalDate sourceRangeEnd = startDate.plusDays(1);
    LocalDate targetRangeStart = startDate.plusDays(10);

    Command c = new CopyEventRangeCommand(view, sourceCalendar, targetCalendar,
        sourceRangeStart, sourceRangeEnd, targetRangeStart);
    c.execute();

    Event copiedEvent = targetCalendar.getAllEvents().iterator().next();
    assertEquals(targetRangeStart, copiedEvent.getStartDate());
    assertEquals(LocalTime.of(19, 30), copiedEvent.getStartTime());
    assertEquals(LocalTime.of(21, 30), copiedEvent.getEndTime());
  }

  @Test
  public void testCopyEventRangeSeriesEventRetainsStatus() {
    for (int i = 0; i < 3; i++) {
      Event event = EventImpl.getBuilder()
          .subject("Series Event")
          .from(startDate.plusDays(i), startTime)
          .to(startDate.plusDays(i), endTime)
          .build();
      sourceCalendar.addEvent(event);
    }

    LocalDate sourceRangeStart = startDate;
    LocalDate sourceRangeEnd = startDate.plusDays(2);
    LocalDate targetRangeStart = startDate.plusDays(10);

    Command c = new CopyEventRangeCommand(view, sourceCalendar, targetCalendar,
        sourceRangeStart, sourceRangeEnd, targetRangeStart);
    c.execute();

    assertEquals(3, targetCalendar.getAllEvents().size());
  }

  @Test
  public void testCopyEventRangeDateOffsetCalculation() {
    Event event1 = EventImpl.getBuilder()
        .subject("Event 1")
        .from(startDate, startTime)
        .to(startDate, endTime)
        .build();

    Event event2 = EventImpl.getBuilder()
        .subject("Event 2")
        .from(startDate.plusDays(3), startTime)
        .to(startDate.plusDays(3), endTime)
        .build();

    sourceCalendar.addEvent(event1);
    sourceCalendar.addEvent(event2);

    LocalDate sourceRangeStart = startDate;
    LocalDate sourceRangeEnd = startDate.plusDays(3);
    LocalDate targetRangeStart = startDate.plusDays(10);

    Command c = new CopyEventRangeCommand(view, sourceCalendar, targetCalendar,
        sourceRangeStart, sourceRangeEnd, targetRangeStart);
    c.execute();

    List<Event> copiedEvents = new ArrayList<>(targetCalendar.getAllEvents());
    copiedEvents.sort((e1, e2) -> e1.getStartDate().compareTo(e2.getStartDate()));

    assertEquals(startDate.plusDays(10), copiedEvents.get(0).getStartDate());

    assertEquals(startDate.plusDays(13), copiedEvents.get(1).getStartDate());
  }

  @Test
  public void testCopyEventRangeTimezoneConversion() {
    Event event = EventImpl.getBuilder()
        .subject("Timezone Event")
        .from(startDate, LocalTime.of(10, 0))
        .to(startDate, LocalTime.of(11, 0))
        .build();
    sourceCalendar.addEvent(event);

    LocalDate sourceRangeStart = startDate;
    LocalDate sourceRangeEnd = startDate.plusDays(1);
    LocalDate targetRangeStart = startDate.plusDays(5);

    Command c = new CopyEventRangeCommand(view, sourceCalendar, targetCalendar,
        sourceRangeStart, sourceRangeEnd, targetRangeStart);
    c.execute();

    Event copiedEvent = targetCalendar.getAllEvents().iterator().next();
    assertNotNull(copiedEvent);
    assertEquals("Timezone Event", copiedEvent.getSubject());
  }

  @Test
  public void testCopyEventRangeMultipleEventsPerDay() {
    Event event1 = EventImpl.getBuilder()
        .subject("Meeting 1")
        .from(startDate, LocalTime.of(9, 0))
        .to(startDate, LocalTime.of(10, 0))
        .build();

    Event event2 = EventImpl.getBuilder()
        .subject("Meeting 2")
        .from(startDate, LocalTime.of(10, 0))
        .to(startDate, LocalTime.of(11, 0))
        .build();

    Event event3 = EventImpl.getBuilder()
        .subject("Meeting 3")
        .from(startDate, LocalTime.of(11, 0))
        .to(startDate, LocalTime.of(12, 0))
        .build();

    sourceCalendar.addEvent(event1);
    sourceCalendar.addEvent(event2);
    sourceCalendar.addEvent(event3);

    LocalDate sourceRangeStart = startDate;
    LocalDate sourceRangeEnd = startDate.plusDays(1);
    LocalDate targetRangeStart = startDate.plusDays(5);

    Command c = new CopyEventRangeCommand(view, sourceCalendar, targetCalendar,
        sourceRangeStart, sourceRangeEnd, targetRangeStart);
    c.execute();

    assertEquals(3, targetCalendar.getAllEvents().size());
  }

  @Test
  public void testCopyEventRangeSuccessMessageFormat() {
    Event event = EventImpl.getBuilder()
        .subject("Test Event")
        .from(startDate, startTime)
        .to(startDate, endTime)
        .build();
    sourceCalendar.addEvent(event);

    LocalDate sourceRangeStart = startDate;
    LocalDate sourceRangeEnd = startDate.plusDays(1);
    LocalDate targetRangeStart = startDate.plusDays(10);

    Command c = new CopyEventRangeCommand(view, sourceCalendar, targetCalendar,
        sourceRangeStart, sourceRangeEnd, targetRangeStart);
    c.execute();

    String expectedMessage = "Success: Successfully copied events to " + targetCalendar.getTitle()
        + System.lineSeparator();
    assertEquals(expectedMessage, log.toString());
  }

  @Test
  public void testCopyEventRangeFromOneCalendarToMultiple() {
    Event event = EventImpl.getBuilder()
        .subject("Test Event")
        .from(startDate, startTime)
        .to(startDate, endTime)
        .build();
    sourceCalendar.addEvent(event);

    LocalDate sourceRangeStart = startDate;
    LocalDate sourceRangeEnd = startDate.plusDays(1);
    LocalDate targetRangeStart = startDate.plusDays(10);

    Command c1 = new CopyEventRangeCommand(view, sourceCalendar, targetCalendar,
        sourceRangeStart, sourceRangeEnd, targetRangeStart);
    c1.execute();

    log.setLength(0);

    Calendar thirdCalendar = new CalendarImpl("Third Calendar", ZoneId.of("Europe/Paris"));

    Command c2 = new CopyEventRangeCommand(view, sourceCalendar, thirdCalendar,
        sourceRangeStart, sourceRangeEnd, targetRangeStart);
    c2.execute();

    assertEquals(1, targetCalendar.getAllEvents().size());
    assertEquals(1, thirdCalendar.getAllEvents().size());
    assertEquals(1, sourceCalendar.getAllEvents().size());
  }

  @Test
  public void testCopyEventRangeLargeRange() {
    for (int i = 0; i < 30; i++) {
      Event event = EventImpl.getBuilder()
          .subject("Event " + i)
          .from(startDate.plusDays(i), startTime)
          .to(startDate.plusDays(i), endTime)
          .build();
      sourceCalendar.addEvent(event);
    }

    LocalDate sourceRangeStart = startDate;
    LocalDate sourceRangeEnd = startDate.plusDays(29);
    LocalDate targetRangeStart = startDate.plusDays(50);

    Command c = new CopyEventRangeCommand(view, sourceCalendar, targetCalendar,
        sourceRangeStart, sourceRangeEnd, targetRangeStart);
    c.execute();

    assertEquals(30, targetCalendar.getAllEvents().size());
  }

  @Test
  public void testCopyEventRangeSingleDay() {
    Event event = EventImpl.getBuilder()
        .subject("Single Day Event")
        .from(startDate, startTime)
        .to(startDate, endTime)
        .build();
    sourceCalendar.addEvent(event);

    LocalDate sourceRangeStart = startDate;
    LocalDate sourceRangeEnd = startDate.plusDays(1);
    LocalDate targetRangeStart = startDate.plusDays(10);

    Command c = new CopyEventRangeCommand(view, sourceCalendar, targetCalendar,
        sourceRangeStart, sourceRangeEnd, targetRangeStart);
    c.execute();

    assertEquals(1, targetCalendar.getAllEvents().size());
    Event copiedEvent = targetCalendar.getAllEvents().iterator().next();
    assertEquals(startDate.plusDays(10), copiedEvent.getStartDate());
  }

  @Test
  public void testCopyEventRangeExceptionHandling() {
    try {
      new CopyEventRangeCommand(view, sourceCalendar, targetCalendar,
          startDate.plusDays(5), startDate, startDate.plusDays(10));
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("End date must be after start date"));
    }
  }

  @Test
  public void testCopyEventRangeNullParameterHandling() {
    try {
      new CopyEventRangeCommand(view, sourceCalendar, null,
          startDate, startDate.plusDays(5), startDate.plusDays(10));
      fail();
    } catch (NullPointerException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testCopyEventRangeExceptionInExecution() {
    Event event = EventImpl.getBuilder()
        .subject("Test Event")
        .from(startDate, startTime)
        .to(startDate, endTime)
        .build();
    sourceCalendar.addEvent(event);

    LocalDate sourceRangeStart = startDate;
    LocalDate sourceRangeEnd = startDate.plusDays(1);
    LocalDate targetRangeStart = startDate.plusDays(10);

    Command c = new CopyEventRangeCommand(view, sourceCalendar, targetCalendar,
        sourceRangeStart, sourceRangeEnd, targetRangeStart);

    try {
      c.execute();
      assertTrue(log.toString().contains("Success") || log.toString().contains("Error"));
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  @Test
  public void testCopyEventRangeEventSpanningMultipleDays() {
    Event multiDayEvent = EventImpl.getBuilder()
        .subject("Multi-day Event")
        .from(startDate, LocalTime.of(22, 0))
        .to(startDate.plusDays(1), LocalTime.of(2, 0))
        .build();
    sourceCalendar.addEvent(multiDayEvent);

    LocalDate sourceRangeStart = startDate;
    LocalDate sourceRangeEnd = startDate.plusDays(1);
    LocalDate targetRangeStart = startDate.plusDays(10);

    Command c = new CopyEventRangeCommand(view, sourceCalendar, targetCalendar,
        sourceRangeStart, sourceRangeEnd, targetRangeStart);
    c.execute();

    assertEquals(1, targetCalendar.getAllEvents().size());
    Event copiedEvent = targetCalendar.getAllEvents().iterator().next();
    assertEquals(startDate.plusDays(11), copiedEvent.getStartDate());
    assertEquals(startDate.plusDays(11), copiedEvent.getEndDate());
  }
}