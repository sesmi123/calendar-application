package calendar.controllers;

import static org.junit.Assert.assertEquals;

import calendar.controllers.commands.Command;
import calendar.controllers.commands.CreateEventCommand;
import calendar.controllers.mocks.MockCalendar;
import calendar.controllers.mocks.MockView;
import calendar.models.Calendar;
import calendar.views.ObservableView;
import controllers.mocks.MockCalendarException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CreateEventCommand.
 */
public class CreateEventCommandTest {

  private StringBuilder log;
  private ObservableView view;
  private Calendar model;

  /**
   * Test set up.
   */
  @Before
  public void setUp() {
    log = new StringBuilder();
    model = new MockCalendar(log, 123456);
    view = new MockView(log, 234567);

  }

  @Test
  public void testIllegalArgument() {
    LocalDate date = LocalDate.of(2025, 10, 12);
    LocalTime time = LocalTime.of(13, 0);
    Calendar cal = new MockCalendarException(log, 123456);
    Command c = new CreateEventCommand(cal, view, "illegal subject",
        LocalDateTime.of(date, time), LocalDateTime.of(date.plusDays(1), time));
    c.execute();
    assertEquals(log.toString(),
        "Add event: illegal subjectError: Failed to create event: mock exception"
            + System.lineSeparator());
  }

}
