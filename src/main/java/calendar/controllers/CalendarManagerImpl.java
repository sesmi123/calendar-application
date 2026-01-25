package calendar.controllers;

import calendar.models.Calendar;
import calendar.models.ObservableCalendar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A database for holding all calandar objects. There can be one active calendar.
 */
public class CalendarManagerImpl implements CalendarManager {

  private final Map<String, Calendar> calendars;
  private Calendar active;

  /**
   * Initialize calendar database.
   */
  public CalendarManagerImpl() {
    this.calendars = new HashMap<>();
    this.active = null;
  }

  @Override
  public List<ObservableCalendar> getAll() {
    return new ArrayList<>(calendars.values());
  }

  @Override
  public Calendar get(String name) throws IllegalArgumentException {
    Calendar cal = calendars.get(name);
    if (cal == null) {
      throw new IllegalArgumentException("Calendar not found");
    }
    return cal;
  }

  @Override
  public void set(Calendar calendar) {
    if (calendars.get(calendar.getTitle()) != null) {
      throw new IllegalArgumentException("A calendar with the same name exists in the database");
    }
    this.calendars.put(calendar.getTitle(), calendar);
  }

  @Override
  public void remove(String name) {
    if (calendars.get(name) == null) {
      return;
    }
    this.calendars.remove(name);
  }

  @Override
  public void activate(String name) {
    this.active = this.get(name);
  }

  @Override
  public Calendar getActive() throws IllegalArgumentException {
    if (this.active == null) {
      throw new IllegalArgumentException("An active calendar is not set");
    }
    return this.active;
  }
}
