package calendar.controllers;

import static org.junit.Assert.assertTrue;

import calendar.controllers.commands.Command;
import calendar.controllers.commands.EditEventCommand;
import calendar.controllers.mocks.MockCalendar;
import calendar.controllers.mocks.MockView;
import calendar.models.Calendar;
import calendar.models.enums.EventProperty;
import calendar.views.ObservableView;
import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for EditEventCommand.
 */
public class EditEventCommandTest {

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
    Calendar cal = new controllers.mocks.MockCalendarException(log, 12466);
    Command c = new EditEventCommand(cal, view, EventProperty.SUBJECT,
        "subject", LocalDateTime.now(), LocalDateTime.now().plusHours(1),
        "new subject");
    c.execute();

    assertTrue(log.toString().contains("Error: Failed to edit event: mock exception"));
  }
}
