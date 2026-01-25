package calendar.controllers;

import static org.junit.Assert.assertEquals;

import calendar.controllers.commands.Command;
import calendar.controllers.commands.ExportCommand;
import calendar.controllers.exporters.CalendarExporter;
import calendar.controllers.mocks.MockCalendar;
import calendar.controllers.mocks.MockView;
import calendar.models.Calendar;
import calendar.views.ObservableView;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ExportCommand.
 */
public class ExportCommandTest {

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
    CalendarExporter ce = new calendar.controllers.mocks.MockCalendarExporter();
    Command c = new ExportCommand(ce, model, view, "illegalFile");
    c.execute();
    assertEquals(log.toString(), "Error: Failed to export events: mock exception"
        + System.lineSeparator());
  }

}
