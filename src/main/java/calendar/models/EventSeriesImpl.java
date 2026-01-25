package calendar.models;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Represents a series of events that have same start and end times.
 */
public class EventSeriesImpl implements EventSeries {

  private final String seriesId;
  private final LocalDate startDate;
  private final RecurrenceRule rule;
  private final Event prototypeEvent;

  /**
   * Initialize a new series of events.
   *
   * @param prototypeEvent template of an event used to make copies of events in the series
   * @param startDate      start date of the series
   * @param recurrenceRule rule that defines the frequency and number of events in the series
   */
  public EventSeriesImpl(Event prototypeEvent, LocalDate startDate, RecurrenceRule recurrenceRule) {
    if (prototypeEvent.getSeriesId() == null) {
      this.seriesId = UUID.randomUUID().toString();
    } else {
      this.seriesId = prototypeEvent.getSeriesId();
    }
    this.startDate = Objects.requireNonNull(startDate, "startDate must not be null");
    this.rule = Objects.requireNonNull(recurrenceRule, "Recurrence Rule must not be null");
    this.prototypeEvent = Objects.requireNonNull(prototypeEvent, "PrototypeEvent must not be null");
    if (!this.prototypeEvent.getStartDate().isEqual(this.prototypeEvent.getEndDate())) {
      throw new IllegalArgumentException("Events in a series must start and end on the same day.");
    }
  }

  @Override
  public String getSeriesId() {
    return this.seriesId;
  }

  @Override
  public RecurrenceRule getRecurrenceRule() {
    return this.rule;
  }

  @Override
  public Set<Event> generateEvents() {
    Set<Event> events = new TreeSet<>();
    List<LocalDate> dates = rule.generateDates(startDate);
    for (LocalDate date : dates) {
      Event event = prototypeEvent.toBuilder().from(date, prototypeEvent.getStartTime())
          .to(date, prototypeEvent.getEndTime()).seriesId(this.seriesId).build();
      events.add(event);
    }
    return events;
  }
}