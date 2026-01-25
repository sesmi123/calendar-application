package calendar.models;

import calendar.models.enums.EventProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

/**
 * Represents a calendar that can store and manage events and event series.
 *
 * <p>A calendar provides methods for adding individual events or recurring event series,
 * filtering events based on specific conditions, checking availability at a given time, and
 * exporting stored data.
 * </p>
 */
public interface Calendar extends ObservableCalendar {

  /**
   * Edit the title of the calendar.
   *
   * @param name new title
   */
  void setTitle(String name);

  /**
   * Edit the timezone of the calendar.
   *
   * @param timezone new timezone
   */
  void setTimezone(ZoneId timezone);

  /**
   * Adds a single event to the calendar.
   *
   * <p>Implementations should prevent adding duplicate events with the same subject
   * and exact start and end times.
   * </p>
   *
   * @param event the event to add; must not be {@code null}.
   * @return true if event was successfully added; false if an event conflicts with existing ones.
   */
  boolean addEvent(Event event);

  /**
   * Adds a recurring series of events to the calendar.
   *
   * <p>This may represent events defined by a recurrence rule or pattern.
   * </p>
   *
   * @param series the event series to add; must not be {@code null}.
   */
  void addEventSeries(EventSeries series);

  /**
   * Edits a single event's property in the calendar.
   *
   * @param event    the event to be edited
   * @param property the property of the event to be edited
   * @param newValue the new value of the property
   * @return new event
   */
  Event editSingleEvent(Event event, EventProperty property, String newValue);

  /**
   * Edits a series' (all events part of the series) property in the calendar. If the event is not
   * part of a series, it edits only the single event.
   *
   * @param event    the event to be edited
   * @param property the property of the event to be edited
   * @param newValue the new value of the property
   */
  EventSeries editSeriesEvent(Event event, EventProperty property, String newValue);

  /**
   * Edits the current event and the following events of the series. If the event is not part of a
   * series, it edits only the single event.
   *
   * @param event    the event to be edited
   * @param property the property of the event to be edited
   * @param newValue the new value of the property
   */
  EventSeries editThisAndFollowingEvents(Event event, EventProperty property, String newValue);

  /**
   * Copies a single event from this calendar to other calendar.
   *
   * <p>If the source event is part of a series, it is copied as a standalone event.
   * If it is standalone, only that event is copied with the offset applied.
   * </p>
   *
   * @param other               the other calendar to copy to
   * @param event               the event to copy
   * @param targetStartDateTime the target start date/time for the copied event
   */
  void copyEvent(Calendar other, Event event, LocalDateTime targetStartDateTime);

  /**
   * Copies a set of events from this calendar to other calendar.
   *
   * <p>If the source event is part of a series, it is copied as a standalone event.
   * If it is standalone, only that event is copied with the offset applied.
   * </p>
   *
   * @param other           the other calendar to copy to
   * @param events          the set of events to copy
   * @param targetStartDate the target start date for the copied event
   */
  void copyEventsOn(Calendar other, Set<Event> events, LocalDate sourceRangeStart,
      LocalDate targetStartDate);

  /**
   * Copies all events within a date range from this calendar to other calendar.
   *
   * <p>Events are partitioned by series. Each series is copied as a new series in this calendar
   * with dates shifted by the offset. Standalone events are copied individually.
   * </p>
   *
   * @param other            the other calendar
   * @param events           the set of events to copy
   * @param sourceRangeStart the source start date for copied events
   * @param targetRangeStart the target start date for copied events
   * @throws IllegalArgumentException if parameters are invalid
   */
  void copyEventsInRange(Calendar other, Set<Event> events, LocalDate sourceRangeStart,
      LocalDate targetRangeStart);

}
