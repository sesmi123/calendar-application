package calendar.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import calendar.controllers.commands.Command;
import calendar.controllers.commands.QueryEventsByDateCommand;
import calendar.controllers.mocks.MockCalendar;
import calendar.controllers.mocks.MockView;
import calendar.models.Calendar;
import calendar.models.ObservableCalendar;
import calendar.views.ObservableView;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for QueryEventsByDateCommand.
 */
public class QueryEventsByDateCommandTest {

  private StringBuilder log;
  private ObservableView view;
  private Calendar model;

  /**
   * Set up tests.
   */
  @Before
  public void setUp() {
    log = new StringBuilder();
    model = new MockCalendar(log, 123466);
    view = new MockView(log, 234667);
  }

  @Test
  public void testIllegalArgument() {
    ObservableCalendar cal = new controllers.mocks.MockCalendarException(log, 12466);
    Command c = new QueryEventsByDateCommand(cal, view, LocalDate.now());
    c.execute();

    assertEquals("Filter eventsError: Failed to query events: mock exception"
        + System.lineSeparator(), log.toString());
  }

  @Test
  public void testSuccessfulExecutionLogsFilterAndDisplay() {
    LocalDate date = LocalDate.of(2025, 11, 8);
    QueryEventsByDateCommand cmd = new QueryEventsByDateCommand(model, view, date);

    cmd.execute();

    String output = log.toString();
    assertTrue(output.contains("Filter events"));
  }
}
