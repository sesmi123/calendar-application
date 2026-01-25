package calendar.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import calendar.models.Calendar;
import calendar.models.CalendarImpl;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for CalendarManagerImpl.
 */
public class CalendarManagerImplTest {

  private CalendarManagerImpl db;
  private Calendar sampleCalendar;

  /**
   * Set up.
   */
  @Before
  public void setUp() {
    db = new CalendarManagerImpl();
    sampleCalendar = new CalendarImpl("Work", ZoneId.of("America/New_York"));
  }

  @Test
  public void testSetAndGetCalendar() {
    db.set(sampleCalendar);
    Calendar retrieved = db.get("Work");
    assertEquals(sampleCalendar, retrieved);
  }

  @Test
  public void testRemoveCalendar() {
    db.set(sampleCalendar);
    db.remove("Work");
    assertThrows(IllegalArgumentException.class, () -> db.get("Work"));
  }

  @Test
  public void testRemoveNonExistentCalendar() {
    db.set(sampleCalendar);
    assertThrows(IllegalArgumentException.class, () -> db.get("Hospital"));
    db.remove("Hospital");
    assertThrows(IllegalArgumentException.class, () -> db.get("Hospital"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetNonExistentCalendarThrowsException() {
    db.get("NonExistent");
  }

  @Test
  public void testActivateCalendar() {
    db.set(sampleCalendar);
    db.activate("Work");
    assertEquals(sampleCalendar, db.getActive());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetActiveWithoutActivationThrowsException() {
    db.getActive();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testActivateNonExistentCalendarThrowsException() {
    db.activate("MissingCalendar");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCannotOverwriteExistingCalendar() {
    Calendar cal1 = new CalendarImpl("Personal", ZoneId.of("America/New_York"));
    Calendar cal2 = new CalendarImpl("Personal", ZoneId.of("Canada/Pacific"));

    db.set(cal1);
    db.set(cal2);
  }
}
