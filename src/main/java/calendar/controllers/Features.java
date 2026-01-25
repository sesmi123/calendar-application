package calendar.controllers;

import calendar.models.ObservableCalendar;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

/**
 * Represents all features (callbacks) that the controller supports.
 */
public interface Features {

  /**
   * Callback method to create a calendar.
   *
   * @param name     unique name of calendar
   * @param timezone timezone of calendar
   */
  void createCalendar(String name, ZoneId timezone);

  /**
   * Get list of all calendars.
   *
   * @return list of all observable calendars
   */
  List<ObservableCalendar> listCalendars();

  /**
   * Get active calendar.
   *
   * @return currently active calendar
   */
  ObservableCalendar getActiveCalendar();

  /**
   * Callback method to use a particular calendar.
   *
   * @param name the name of calendar to use
   */
  void useCalendar(String name);

  /**
   * Callback method to edit a particular calendar.
   *
   * @param name     the name of calendar
   * @param property the property to edit
   * @param value    the new value to ste for the property
   */
  void editCalendar(String name, String property, String value);

  /**
   * Callback method to create an event.
   *
   * @param subject       event subject
   * @param startDateTime event start date and time
   * @param endDateTime   event end date and time
   */
  void createEvent(String subject, String startDateTime, String endDateTime);

  /**
   * Callback method to creates an all day event.
   *
   * @param subject   event subject
   * @param startDate event start date
   */
  void createAllDayEvent(String subject, String startDate);

  /**
   * Callback method to create a series event.
   *
   * @param subject     series subject
   * @param startDate   each event's start date
   * @param startTime   each event's start time
   * @param endTime     each event's end time
   * @param days        recurrence days
   * @param occurrences number of occurrences
   * @param untilDate   recur until date
   */
  void createEventSeries(String subject, String startDate, String startTime, String endTime,
      Set<DayOfWeek> days, int occurrences, String untilDate);

  /**
   * Callback method to edit an event.
   *
   * @param property      event property to edit
   * @param subject       subject of the event which is to be edited
   * @param startDateTime startDateTime of the event which is to be edited
   * @param endDateTime   newValue new value for the property
   * @param newValue      new value for the property
   */
  void editEvent(String property, String subject, String startDateTime, String endDateTime,
      String newValue);

  /**
   * Callback method to edit events.
   *
   * @param property      event property to edit
   * @param subject       subject of the events to be edited
   * @param startDateTime startDateTime of the events to be edited
   * @param newValue      new value for the property
   */
  void editEvents(String property, String subject, String startDateTime, String newValue);

  /**
   * Callback method to edit a series event.
   *
   * @param property      event property to edit
   * @param subject       subject of the event which is to be edited
   * @param startDateTime startDateTime of the event which is to be edited
   * @param newValue      newValue new value for the property
   */
  void editSeries(String property, String subject, String startDateTime, String newValue);


  /**
   * Callback method to query events by date.
   *
   * @param date date to query events
   */
  void queryByDate(LocalDate date);

  /**
   * Callback method to query events by date range.
   *
   * @param start start date and time of the range
   * @param end   end date and time of the range
   */
  void queryByDateRange(LocalDateTime start, LocalDateTime end);

  /**
   * Callback method to show availability status of the calendar at a given date-time.
   *
   * @param date date and time to show status
   */
  void showStatus(LocalDateTime date);
}