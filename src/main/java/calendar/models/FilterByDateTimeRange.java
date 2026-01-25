package calendar.models;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A filter condition that checks whether an event occurs within a specified date-time range.
 *
 * <p>This class implements {@link FilterCondition} and can be used to select events whose
 * start and end times overlap with the given start and end date-times.
 * </p>
 */
public class FilterByDateTimeRange implements FilterCondition {

  private final LocalDateTime start;
  private final LocalDateTime end;

  /**
   * Constructs a {@code FilterByDateRange} with the specified start and end date-times.
   *
   * @param start the start of the date-time range; must not be null
   * @param end   the end of the date-time range; must not be null
   * @throws NullPointerException if either start or end is null
   */
  public FilterByDateTimeRange(LocalDateTime start, LocalDateTime end) {
    this.start = Objects.requireNonNull(start);
    this.end = Objects.requireNonNull(end);
  }

  @Override
  public boolean evaluate(Event event) throws IllegalArgumentException {
    if (event == null) {
      throw new IllegalArgumentException("event must not be null");
    }
    LocalDateTime eventStart = event.getStartDateTime();
    LocalDateTime eventEnd = event.getEndDateTime();
    return !eventStart.isAfter(end) && !eventEnd.isBefore(start);
  }
}
