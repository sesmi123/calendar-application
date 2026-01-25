package calendar.controllers;

import static org.junit.Assert.assertEquals;

import calendar.controllers.commands.Command;
import calendar.controllers.commands.CreateCalendarCommand;
import calendar.controllers.mocks.MockCalendar;
import calendar.controllers.mocks.MockView;
import calendar.models.Calendar;
import calendar.views.ObservableView;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CreateCalendarCommand.
 */
public class CreateCalendarCommandTest {

  private StringBuilder log;
  private ObservableView view;
  private Calendar model;
  private CalendarManager db;

  /**
   * Test set up.
   */
  @Before
  public void setUp() {
    log = new StringBuilder();
    model = new MockCalendar(log, 123456);
    view = new MockView(log, 234567);
    db = new CalendarManagerImpl();
  }

  @Test
  public void testCommand() {
    Command c = new CreateCalendarCommand(view, "new cal", ZoneId.of("America/New_York"), db);
    c.execute();
    assertEquals("Success: Created new calendar new cal with timezone America/New_York"
        + System.lineSeparator(), log.toString());
  }

}
