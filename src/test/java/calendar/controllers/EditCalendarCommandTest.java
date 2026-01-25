package calendar.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import calendar.controllers.commands.Command;
import calendar.controllers.commands.EditCalendarCommand;
import calendar.controllers.mocks.MockView;
import calendar.models.Calendar;
import calendar.models.CalendarImpl;
import calendar.views.ObservableView;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for EditCalendarCommand.
 */
public class EditCalendarCommandTest {

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
    model = new CalendarImpl("cal", ZoneId.of("Canada/Pacific"));
    view = new MockView(log, 234567);
    db = new CalendarManagerImpl();
    db.set(model);
  }

  @Test
  public void testEditTimezone() {
    assertEquals(ZoneId.of("Canada/Pacific"), model.getTimezone());
    Command c = new EditCalendarCommand(view, "cal", "timezone", "America/New_York", db);
    c.execute();
    model = db.get(model.getTitle());
    assertEquals(ZoneId.of("America/New_York"), model.getTimezone());
    assertEquals("Success: Edited timezone of calendar cal to America/New_York"
        + System.lineSeparator(), log.toString());
  }

  @Test
  public void testEditName() {
    assertEquals("cal", model.getTitle());
    Command c = new EditCalendarCommand(view, "cal", "name", "new cal", db);
    c.execute();
    assertEquals("new cal", model.getTitle());
    assertEquals("Success: Edited name of calendar cal to new cal"
        + System.lineSeparator(), log.toString());
    assertThrows(IllegalArgumentException.class, () -> db.get("cal"));
    assertNotNull(db.get("new cal"));
  }

}
