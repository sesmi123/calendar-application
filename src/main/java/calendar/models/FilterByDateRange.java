package calendar.models;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A filter condition that checks whether an event occurs within a specified date range.
 *
 * <p>This class implements {@link FilterCondition} and can be used to select events whose
 * start and end dates overlap with the given start and end dates.
 * </p>
 */
public class FilterByDateRange implements FilterCondition {

  private final LocalDate start;
  private final LocalDate end;

  /**
   * Constructs a {@code FilterByDateRange} with the specified start and end dates.
   *
   * @param start the start of the date range; must not be null
   * @param end   the end of the date range; must not be null
   * @throws NullPointerException if either start or end is null
   */
  public FilterByDateRange(LocalDate start, LocalDate end) {
    this.start = Objects.requireNonNull(start);
    this.end = Objects.requireNonNull(end);
  }

  @Override
  public boolean evaluate(Event event) throws IllegalArgumentException {
    if (event == null) {
      throw new IllegalArgumentException("event must not be null");
    }
    LocalDate eventStart = event.getStartDate();
    LocalDate eventEnd = event.getEndDate();
    return !eventStart.isAfter(end) && !eventEnd.isBefore(start);
  }
}
