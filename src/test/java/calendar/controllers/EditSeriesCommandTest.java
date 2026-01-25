package calendar.controllers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.controllers.commands.Command;
import calendar.controllers.commands.EditSeriesCommand;
import calendar.controllers.mocks.MockView;
import calendar.models.Calendar;
import calendar.models.CalendarImpl;
import calendar.models.Event;
import calendar.models.EventImpl;
import calendar.models.enums.EventProperty;
import calendar.views.ObservableView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for EditSeriesCommand - error/exception cases only.
 */
public class EditSeriesCommandTest {

  private StringBuilder log;
  private ObservableView view;
  private Calendar model;
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

    model = new CalendarImpl("Test Calendar", ZoneId.of("America/New_York"));
    view = new MockView(log, 234567);
  }

  @Test
  public void testEditSeriesEventNotFound() {
    LocalDateTime startDateTime = LocalDateTime.of(date, startTime);

    Command c = new EditSeriesCommand(model, view, EventProperty.SUBJECT,
        "Nonexistent Event", startDateTime, "New Subject");
    c.execute();

    assertTrue(log.toString().contains("Error: Event with subject Nonexistent Event"));
    assertTrue(log.toString().contains("not found"));
  }

  @Test
  public void testEditSeriesMultipleEventsFound() {
    Event event1 = EventImpl.getBuilder()
        .subject("Meeting")
        .from(date, startTime)
        .to(date, endTime)
        .build();

    Event event2 = EventImpl.getBuilder()
        .subject("Meeting")
        .from(date, startTime)
        .to(date, LocalTime.of(11, 30))
        .build();

    model.addEvent(event1);
    model.addEvent(event2);

    LocalDateTime startDateTime = LocalDateTime.of(date, startTime);

    Command c = new EditSeriesCommand(model, view, EventProperty.SUBJECT,
        "Meeting", startDateTime, "New Meeting");
    c.execute();

    assertTrue(log.toString().contains("Error: Multiple events found"));
    assertTrue(log.toString().contains("Cannot uniquely identify event"));
  }

  @Test
  public void testEditSeriesSingleEventNotPartOfSeries() {
    Event event = EventImpl.getBuilder()
        .subject("Standalone Meeting")
        .from(date, startTime)
        .to(date, endTime)
        .build();

    model.addEvent(event);

    LocalDateTime startDateTime = LocalDateTime.of(date, startTime);

    log.setLength(0);
    Command c = new EditSeriesCommand(model, view, EventProperty.SUBJECT,
        "Standalone Meeting", startDateTime, "Updated Meeting");
    c.execute();

    Event editedEvent = model.findSingleEvent("Updated Meeting",
        LocalDateTime.of(date, startTime), LocalDateTime.of(date, endTime));

    assertNotNull(editedEvent);
  }

  @Test
  public void testEditSeriesIllegalArgumentExceptionCaught() {
    Event event = EventImpl.getBuilder()
        .subject("Test Event")
        .from(date, startTime)
        .to(date, endTime)
        .build();

    model.addEvent(event);

    LocalDateTime startDateTime = LocalDateTime.of(date, startTime);

    Command c = new EditSeriesCommand(model, view, EventProperty.LOCATION,
        "Test Event", startDateTime, "INVALID_LOCATION");
    c.execute();

    assertTrue(log.toString().contains("Error: Failed to edit event:"));
  }
}