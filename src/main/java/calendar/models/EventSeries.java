package calendar.models;

import java.util.Set;

/**
 * Represents a recurring series of related events in a calendar.
 *
 * <p>An {@code EventSeries} groups multiple {@link Event} instances that follow
 * a common recurrence pattern — for example, weekly team meetings or daily reminders. Each
 * individual event in the series can be retrieved as part of the set.
 * </p>
 */
public interface EventSeries {

  /**
   * Generates all the events for the series based on the recurrence rule.
   *
   * @return set of {@link Event} objects representing each event in the series; never {@code null}
   */
  Set<Event> generateEvents();

  /**
   * Gets the unique identifier for this series.
   *
   * @return the series ID
   */
  String getSeriesId();

  /**
   * Get Recurrence Rule of the series.
   *
   * @return the Recurrence Rule
   */
  RecurrenceRule getRecurrenceRule();
}