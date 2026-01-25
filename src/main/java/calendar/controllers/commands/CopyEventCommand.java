package calendar.controllers.commands;

import calendar.models.Calendar;
import calendar.models.Event;
import calendar.views.ObservableView;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Handles the command to copy a single event to a target calendar with a new start date.
 */
public class CopyEventCommand implements Command {

  private final ObservableView view;
  private final Calendar sourceCalendar;
  private final Calendar targetCalendar;
  private final String eventSubject;
  private final LocalDateTime sourceStartDateTime;
  private final LocalDateTime targetStartDateTime;

  /**
   * Instantiate CopyEventCommand.
   *
   * @param view                the view
   * @param sourceCalendar      the source calendar
   * @param targetCalendar      the calendar to copy to
   * @param eventSubject        event title
   * @param sourceStartDateTime start date/time on source calendar
   * @param targetStartDateTime target date/time on target calendar
   */
  public CopyEventCommand(
      ObservableView view,
      Calendar sourceCalendar,
      Calendar targetCalendar,
      String eventSubject,
      LocalDateTime sourceStartDateTime,
      LocalDateTime targetStartDateTime) {
    this.sourceCalendar = Objects.requireNonNull(sourceCalendar);
    this.view = Objects.requireNonNull(view);
    this.targetCalendar = Objects.requireNonNull(targetCalendar);
    this.eventSubject = Objects.requireNonNull(eventSubject);
    this.sourceStartDateTime = Objects.requireNonNull(sourceStartDateTime);
    this.targetStartDateTime = Objects.requireNonNull(targetStartDateTime);
  }

  @Override
  public void execute() {
    try {
      Set<Event> matches = sourceCalendar.findEventBySubjectAndStart(eventSubject,
          sourceStartDateTime);

      if (matches.isEmpty()) {
        view.displayError("Event not found: " + eventSubject + " at " + sourceStartDateTime);
      } else if (matches.size() > 1) {
        view.displayError("Multiple events found: " + eventSubject + " at " + sourceStartDateTime);
      } else {
        sourceCalendar.copyEvent(targetCalendar, matches.iterator().next(), targetStartDateTime);
        view.displaySuccess("Successfully copied event to calendar " + targetCalendar.getTitle());
      }
    } catch (IllegalArgumentException e) {
      view.displayError("Failed to copy event: " + e.getMessage());
    }
  }
}
