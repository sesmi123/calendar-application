package calendar.models;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The recurrence rule to generate dates for recurring events in a series.
 */
public class RecurrenceRuleImpl implements RecurrenceRule {

  private final Set<DayOfWeek> daysOfWeek;
  private final LocalDate endDate;
  private final Integer occurrences;

  /**
   * Initialize a RecurrenceRuleImpl with the frequency configurations.
   *
   * @param daysOfWeek the days on which the event should occur
   * @param endDate    the date until which the events should recur
   * @throws IllegalArgumentException if configurations are illegal
   */
  public RecurrenceRuleImpl(Set<DayOfWeek> daysOfWeek, LocalDate endDate)
      throws IllegalArgumentException {
    this.daysOfWeek = daysOfWeek;
    this.endDate = endDate;
    this.occurrences = null;
    validate();
  }

  /**
   * Initialize a RecurrenceRuleImpl with the frequency configurations.
   *
   * @param daysOfWeek  the days on which the event should occur
   * @param occurrences the number of times the events should recur
   * @throws IllegalArgumentException if configurations are illegal
   */
  public RecurrenceRuleImpl(Set<DayOfWeek> daysOfWeek, Integer occurrences)
      throws IllegalArgumentException {
    this.daysOfWeek = daysOfWeek;
    this.occurrences = occurrences;
    this.endDate = null;
    validate();
  }

  @Override
  public List<LocalDate> generateDates(LocalDate startDate) {
    List<LocalDate> dates = new ArrayList<>();
    LocalDate currentDate = startDate;
    int count = 0;

    while (shouldContinue(currentDate, count)) {
      if (daysOfWeek.contains(currentDate.getDayOfWeek())) {
        dates.add(currentDate);
        count++;
      }
      currentDate = currentDate.plusDays(1);
    }
    return dates;
  }

  @Override
  public Set<DayOfWeek> getDaysOfWeek() {
    return daysOfWeek;
  }

  @Override
  public LocalDate getEndDate() {
    return endDate;
  }

  @Override
  public Integer getOccurrences() {
    return occurrences;
  }

  private void validate() {
    if (daysOfWeek.isEmpty()) {
      throw new IllegalArgumentException("Days of week cannot be empty");
    }
    if (endDate == null && occurrences == null) {
      throw new IllegalArgumentException("Specify either end date or occurrences");
    }
    if (occurrences != null && occurrences < 0) {
      throw new IllegalArgumentException("Occurrences must be positive");
    }
  }

  private boolean shouldContinue(LocalDate currentDate, int count) {
    if (occurrences != null) {
      return count < occurrences;
    } else {
      return !currentDate.isAfter(endDate);
    }
  }

}

