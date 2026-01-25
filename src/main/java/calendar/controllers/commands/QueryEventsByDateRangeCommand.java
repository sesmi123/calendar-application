package calendar.controllers.commands;

import calendar.models.Event;
import calendar.models.FilterByDateTimeRange;
import calendar.models.FilterCondition;
import calendar.models.ObservableCalendar;
import calendar.views.ObservableView;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Handles the command to view the list of events present between a given range of datetimes.
 */
public class QueryEventsByDateRangeCommand implements Command {

  private final ObservableCalendar model;
  private final ObservableView view;
  private final LocalDateTime start;
  private final LocalDateTime end;

  /**
   * Initialize a command class to query the events between a given range of datetimes.
   *
   * @param start the start date of range
   * @param end   the end date of range
   */
  public QueryEventsByDateRangeCommand(ObservableCalendar model, ObservableView view,
      LocalDateTime start, LocalDateTime end) {
    this.model = Objects.requireNonNull(model);
    this.view = Objects.requireNonNull(view);
    this.start = Objects.requireNonNull(start);
    this.end = Objects.requireNonNull(end);
  }

  @Override
  public void execute() {
    try {
      FilterCondition condition = new FilterByDateTimeRange(start, end);
      Set<Event> events = model.filterEvents(condition);
      view.displayEventsInRange(events);
    } catch (IllegalArgumentException e) {
      view.displayError("Failed to query events: " + e.getMessage());
    }
  }
}
