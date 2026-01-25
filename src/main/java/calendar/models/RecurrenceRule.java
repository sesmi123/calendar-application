package calendar.models;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Represents a recurrence rule for generating a series of dates for recurring events.
 *
 * <p>Implementations of this interface define the logic to calculate the sequence of
 * dates based on recurrence patterns such as specific weekdays, number of occurrences, or an end
 * date.
 * </p>
 */
public interface RecurrenceRule {

  /**
   * Generates a list of dates starting from the specified start date according to the recurrence
   * rule.
   *
   * @param startDate the date from which to start generating recurring dates; must not be null
   * @return a list of {@link LocalDate} objects representing the recurring dates
   */
  List<LocalDate> generateDates(LocalDate startDate);

  /**
   * Returns the days of the week the events in the series should recur.
   *
   * @return set of DayOfWeek representing the recur days
   */
  Set<DayOfWeek> getDaysOfWeek();

  /**
   * Returns the endDate of the series, if the recurrence rule is defined with an endDate.
   *
   * @return endDate
   */
  LocalDate getEndDate();

  /**
   * Returns the number of occurrences of the series, if the recurrence rule is defined with
   * occurrences.
   *
   * @return occurrences
   */
  Integer getOccurrences();
}
