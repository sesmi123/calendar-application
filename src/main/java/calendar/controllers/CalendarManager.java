package calendar.controllers;

import calendar.models.Calendar;
import calendar.models.ObservableCalendar;
import java.util.List;

/**
 * Maintains multiple calendar objects.
 */
public interface CalendarManager {

  /**
   * Get all calendars.
   *
   * @return ObservableCalendar all calendars
   */
  List<ObservableCalendar> getAll();

  /**
   * Get a particular calendar object using its name.
   *
   * @param name name of the calendar
   * @return Calendar
   * @throws IllegalArgumentException if calendar doesn't exist
   */
  Calendar get(String name) throws IllegalArgumentException;

  /**
   * Add calendar to the database.
   *
   * @param calendar calendar object to add
   */
  void set(Calendar calendar);

  /**
   * Remove calendar from the database.
   *
   * @param name name of the calendar to remove
   */
  void remove(String name);

  /**
   * Activate a calendar of given name.
   */
  void activate(String name);

  /**
   * Get the active calendar.
   *
   * @return the active calendar
   * @throws IllegalArgumentException if there is no active calendar
   */
  Calendar getActive() throws IllegalArgumentException;
}
