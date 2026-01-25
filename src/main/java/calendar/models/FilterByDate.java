package calendar.models;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A filter condition that checks whether an event occurs on a specified date.
 *
 * <p>This class implements {@link FilterCondition} and can be used to select events
 * that starts, ends, or is in progress on the given date.
 * </p>
 */
public class FilterByDate implements FilterCondition {

  private final LocalDate startDate;

  /**
   * Constructs a {@code FilterByDateRange} with the specified start date.
   *
   * @param startDate the date; must not be null
   * @throws NullPointerException if either startDate is null
   */
  public FilterByDate(LocalDate startDate) {
    this.startDate = Objects.requireNonNull(startDate, "startDate cannot be null");
  }

  @Override
  public boolean evaluate(Event event) throws IllegalArgumentException {
    if (event == null) {
      throw new IllegalArgumentException("event must not be null");
    }
    LocalDate eventStart = event.getStartDate();
    LocalDate eventEnd = event.getEndDate();
    return (!startDate.isBefore(eventStart) && !startDate.isAfter(eventEnd));
  }
}
