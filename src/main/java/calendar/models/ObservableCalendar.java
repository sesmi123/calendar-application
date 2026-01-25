package calendar.models;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;


/**
 * Read-only version of the calendar. It is immutable.
 */
public interface ObservableCalendar {

  /**
   * Returns the title or name of this calendar.
   *
   * @return the calendar title; never {@code null}.
   */
  String getTitle();

  /**
   * Returns the timezone of this calendar.
   *
   * @return the calendar timezone; never {@code null}.
   */
  ZoneId getTimezone();

  /**
   * Returns all events of this calendar.
   *
   * @return the events.
   */
  Set<Event> getAllEvents();

  /**
   * Filters and retrieves events that satisfy the given condition.
   *
   * @param condition the filtering condition to apply; must not be {@code null}.
   * @return a set of events that match the specified condition; never {@code null}.
   */
  Set<Event> filterEvents(FilterCondition condition);


  /**
   * Checks whether the calendar is busy at the specified date and time.
   *
   * <p>A calendar is considered busy if there exists an event or event series
   * overlapping with the provided {@link LocalDateTime}.
   * </p>
   *
   * @param dateTime the date and time to check; must not be {@code null}.
   * @return {@code true} if the calendar has an active event at the specified time.
   */
  boolean isBusy(LocalDateTime dateTime);

  /**
   * Finds a single event in the calendar for the given subject, start and end dateTimes.
   *
   * @param subject       the subject of the event to search for
   * @param startDateTime the startDateTime of the event to search for
   * @param endDateTime   the endDateTime of the event to search for
   * @return event if found
   */
  Event findSingleEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime);

  /**
   * Finds events in the calendar for the given subject and startDate times.
   *
   * @param subject       the subject of the event to search for
   * @param startDateTime the startDateTime of the event to search for
   * @return Set of Events
   */
  Set<Event> findEventBySubjectAndStart(String subject, LocalDateTime startDateTime);

}
