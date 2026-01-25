package calendar.controllers.commands;

import calendar.models.Calendar;
import calendar.models.Event;
import calendar.models.FilterByDateRange;
import calendar.models.FilterCondition;
import calendar.views.ObservableView;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

/**
 * Handles the command to copy all events within a specified date range to a target calendar with a
 * new start date.
 *
 * <p>This command allows bulk copying of events from a source calendar within a specified date
 * range to a target calendar. All events in the range are shifted by the difference between the
 * source range start date and the target start date. Events are treated as new events in the target
 * calendar.
 * </p>
 */
public class CopyEventRangeCommand implements Command {

  private final ObservableView view;
  private final Calendar sourceCalendar;
  private final Calendar targetCalendar;
  private final LocalDate sourceRangeStart;
  private final LocalDate sourceRangeEnd;
  private final LocalDate targetRangeStart;

  /**
   * Initialize a command to copy all events within a date range to a target calendar.
   *
   * @param view             view for displaying messages
   * @param sourceCalendar   the source calendar
   * @param targetCalendar   the target calendar to copy events to
   * @param sourceRangeStart the start of the date range in the source calendar
   * @param sourceRangeEnd   the end of the date range in the source calendar
   * @param targetRangeStart the target start date/time for copied events
   */
  public CopyEventRangeCommand(ObservableView view,
      Calendar sourceCalendar, Calendar targetCalendar,
      LocalDate sourceRangeStart, LocalDate sourceRangeEnd, LocalDate targetRangeStart) {
    this.view = Objects.requireNonNull(view);
    this.sourceCalendar = Objects.requireNonNull(sourceCalendar);
    this.targetCalendar = Objects.requireNonNull(targetCalendar);
    this.sourceRangeStart = Objects.requireNonNull(sourceRangeStart);
    this.sourceRangeEnd = Objects.requireNonNull(sourceRangeEnd);
    this.targetRangeStart = Objects.requireNonNull(targetRangeStart);

    if (!sourceRangeEnd.isAfter(sourceRangeStart)) {
      throw new IllegalArgumentException("End date must be after start date");
    }
  }

  @Override
  public void execute() {
    try {
      FilterCondition condition = new FilterByDateRange(sourceRangeStart, sourceRangeEnd);
      Set<Event> eventsInRange = sourceCalendar.filterEvents(condition);
      if (eventsInRange.isEmpty()) {
        view.displaySuccess(
            "No events found in the specified range to copy");
        return;
      }
      sourceCalendar.copyEventsInRange(targetCalendar, eventsInRange, sourceRangeStart,
          targetRangeStart);
      view.displaySuccess("Successfully copied events to " + targetCalendar.getTitle());
    } catch (IllegalArgumentException e) {
      view.displayError("Failed to copy events in date range: " + e.getMessage());
    }
  }
}
