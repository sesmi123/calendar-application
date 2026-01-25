package calendar.controllers;

import static org.junit.Assert.assertTrue;

import calendar.controllers.commands.Command;
import calendar.controllers.commands.UseCalendarCommand;
import calendar.controllers.mocks.MockCalendar;
import calendar.controllers.mocks.MockView;
import calendar.models.Calendar;
import calendar.views.ObservableView;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for UseCalendarCommand.
 */
public class UseCalendarCommandTest {

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
    db.set(model);
  }

  @Test
  public void testCommand() {
    Command c = new UseCalendarCommand(view, "title", db);
    c.execute();
    assertTrue(log.toString().contains("Success: Activated title calendar for use"));
  }

}
