package calendar.controllers;

import static org.junit.Assert.assertTrue;

import calendar.controllers.commands.Command;
import calendar.controllers.commands.CreateEventSeriesCommand;
import calendar.controllers.mocks.MockCalendar;
import calendar.controllers.mocks.MockView;
import calendar.models.Calendar;
import calendar.models.Event;
import calendar.models.EventImpl;
import calendar.models.RecurrenceRule;
import calendar.models.RecurrenceRuleImpl;
import calendar.views.ObservableView;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for CreateEventSeriesCommand.
 */
public class CreateEventSeriesCommandTest {

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
    Event e = EventImpl.getBuilder().subject("Test")
        .from(LocalDate.now(), LocalTime.of(1, 0))
        .to(LocalDate.now(), LocalTime.of(2, 0))
        .build();
    RecurrenceRule r = new RecurrenceRuleImpl(EnumSet.of(DayOfWeek.THURSDAY), 3);
    Command c = new CreateEventSeriesCommand(cal, view, e, r);
    c.execute();

    assertTrue(log.toString().contains("Error: Failed to create event series: mock exception"));
  }
}
