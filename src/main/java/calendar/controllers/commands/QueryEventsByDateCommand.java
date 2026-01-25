package calendar.controllers.commands;

import calendar.models.Event;
import calendar.models.FilterByDate;
import calendar.models.FilterCondition;
import calendar.models.ObservableCalendar;
import calendar.views.ObservableView;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

/**
 * Handles the command to view the list of events starting on a given date.
 */
public class QueryEventsByDateCommand implements Command {

  private final ObservableCalendar model;
  private final ObservableView view;
  private final LocalDate date;

  /**
   * Initialize a command class to query the events on a given date.
   *
   * @param date the date on which to filter the events
   */
  public QueryEventsByDateCommand(ObservableCalendar model, ObservableView view, LocalDate date) {
    this.model = Objects.requireNonNull(model);
    this.view = Objects.requireNonNull(view);
    this.date = Objects.requireNonNull(date);
  }

  @Override
  public void execute() {
    try {
      FilterCondition condition = new FilterByDate(date);
      Set<Event> events = model.filterEvents(condition);
      view.displayEventsOn(events);
    } catch (IllegalArgumentException e) {
      view.displayError("Failed to query events: " + e.getMessage());
    }
  }
}
