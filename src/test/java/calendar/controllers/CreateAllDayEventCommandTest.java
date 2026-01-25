package calendar.controllers;

import static org.junit.Assert.assertEquals;

import calendar.controllers.commands.Command;
import calendar.controllers.commands.CreateAllDayEventCommand;
import calendar.controllers.mocks.MockCalendar;
import calendar.controllers.mocks.MockView;
import calendar.models.Calendar;
import calendar.views.ObservableView;
import controllers.mocks.MockCalendarException;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CreateAllDayEventCommand.
 */
public class CreateAllDayEventCommandTest {

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
    Calendar cal = new MockCalendarException(log, 4884);
    Command c = new CreateAllDayEventCommand(cal, view, "illegal subject",
        LocalDate.of(2025, 10, 12));
    c.execute();
    assertEquals(log.toString(),
        "Add event: illegal subjectError: Failed to create event: mock exception"
            + System.lineSeparator());
  }

}
