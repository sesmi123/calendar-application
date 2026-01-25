package calendar.models;

import calendar.models.enums.Location;
import calendar.models.enums.Status;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a calendar event with a subject, start and end date/time, optional description,
 * location, and status.
 *
 * <p>Implementations of this interface (such as {@link EventImpl}) should ensure that
 * the end date/time is after the start date/time and that all required fields (subject, start
 * date/time, end date/time) are non-null.
 * </p>
 *
 * <p>This interface also provides a {@link #toBuilder()} method to obtain an editable builder
 * pre-populated with the event’s current values.
 * </p>
 */
public interface Event extends Comparable<Event> {

  /**
   * Returns the subject or title of the event.
   *
   * @return the event subject; never {@code null}.
   */
  String getSubject();

  /**
   * Returns the date on which the event starts.
   *
   * @return the start date of the event; never {@code null}.
   */
  LocalDate getStartDate();

  /**
   * Returns the date-time on which the event starts.
   *
   * @return the start date-time of the event; never {@code null}.
   */
  LocalDateTime getStartDateTime();

  /**
   * Returns the date on which the event ends.
   *
   * @return the end date of the event; never {@code null}.
   */
  LocalDate getEndDate();

  /**
   * Returns the date-time on which the event ends.
   *
   * @return the end date-time of the event; never {@code null}.
   */
  LocalDateTime getEndDateTime();

  /**
   * Returns the time at which the event starts.
   *
   * @return the start time of the event; never {@code null}.
   */
  LocalTime getStartTime();

  /**
   * Returns the time at which the event ends.
   *
   * @return the end time of the event; never {@code null}.
   */
  LocalTime getEndTime();

  /**
   * Returns the description or details of the event.
   *
   * @return an optional description of the event, or {@code null} if none is provided.
   */
  String getDescription();

  /**
   * Returns the location associated with the event - Physical or Online.
   *
   * @return the location of the event, or {@code null} if no location is specified.
   */
  Location getLocation();

  /**
   * Returns the status of the event - Public or Private.
   *
   * @return the event status, or {@code null} if no status is set.
   */
  Status getStatus();

  /**
   * Creates a new {@link EventImpl.EventBuilder} initialized with the values of this event.
   *
   * <p>This method allows easy modification or duplication of an existing event.
   * </p>
   *
   * @return a builder pre-populated with the current event’s data.
   */
  EventImpl.EventBuilder toBuilder();

  /**
   * Gets the series ID if this event is part of a series.
   *
   * @return the series ID, or null if not part of a series
   */
  String getSeriesId();

  /**
   * Checks if this event is part of a series.
   *
   * @return true if part of a series, false otherwise
   */
  boolean isPartOfSeries();
}
