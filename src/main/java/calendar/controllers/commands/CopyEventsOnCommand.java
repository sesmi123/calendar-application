package calendar.controllers.commands;

import calendar.models.Calendar;
import calendar.models.Event;
import calendar.models.FilterByDate;
import calendar.views.ObservableView;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

/**
 * Handles the command to copy a all events scheduled on a day to a target calendar with a new start
 * date.
 */
public class CopyEventsOnCommand implements Command {

  private final ObservableView view;
  private final Calendar sourceCalendar;
  private final Calendar targetCalendar;
  private final LocalDate sourceStartDate;
  private final LocalDate targetStartDate;

  /**
   * Instantiate CopyEventsOnCommand.
   *
   * @param view            the view
   * @param sourceCalendar  the source calendar
   * @param targetCalendar  the calendar to copy to
   * @param sourceStartDate start date on source calendar
   * @param targetStartDate target date on target calendar
   */
  public CopyEventsOnCommand(ObservableView view,
      Calendar sourceCalendar, Calendar targetCalendar,
      LocalDate sourceStartDate, LocalDate targetStartDate) {
    this.view = Objects.requireNonNull(view);
    this.targetCalendar = Objects.requireNonNull(targetCalendar);
    this.sourceStartDate = Objects.requireNonNull(sourceStartDate);
    this.targetStartDate = Objects.requireNonNull(targetStartDate);
    this.sourceCalendar = Objects.requireNonNull(sourceCalendar);
  }

  @Override
  public void execute() {
    try {
      Set<Event> events = sourceCalendar.filterEvents(new FilterByDate(sourceStartDate));
      if (events.isEmpty()) {
        view.displayError(
            "Events with startDate " + sourceStartDate + " not found");
        return;
      }
      sourceCalendar.copyEventsOn(targetCalendar, events, sourceStartDate, targetStartDate);
      view.displaySuccess(
          "Copied events on " + sourceStartDate + " to " + targetCalendar.getTitle() + " on "
              + targetStartDate);
    } catch (IllegalArgumentException e) {
      view.displayError("Failed to copy event: " + e.getMessage());
    }
  }
}
