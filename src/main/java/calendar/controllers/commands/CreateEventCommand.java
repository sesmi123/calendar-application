package calendar.controllers.commands;

import calendar.models.Calendar;
import calendar.models.Event;
import calendar.models.EventImpl;
import calendar.views.ObservableView;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Handle the command to create a single event.
 */
public class CreateEventCommand implements Command {

  private final Calendar model;
  private final ObservableView view;
  private final String subject;
  private final LocalDateTime startDate;
  private final LocalDateTime endDate;

  /**
   * Initialize the command class with the details required to create an event.
   *
   * @param model     the calendar model
   * @param view      the view
   * @param subject   title of the event
   * @param startDate start date/time of the event
   * @param endDate   end date/time of the event
   */
  public CreateEventCommand(
      Calendar model,
      ObservableView view,
      String subject,
      LocalDateTime startDate,
      LocalDateTime endDate) {
    this.model = Objects.requireNonNull(model);
    this.view = Objects.requireNonNull(view);
    this.subject = Objects.requireNonNull(subject);
    this.startDate = Objects.requireNonNull(startDate);
    this.endDate = Objects.requireNonNull(endDate);
  }

  @Override
  public void execute() {
    try {
      Event event = EventImpl.getBuilder()
          .subject(this.subject)
          .from(startDate.toLocalDate(), startDate.toLocalTime())
          .to(endDate.toLocalDate(), endDate.toLocalTime())
          .build();
      boolean created = model.addEvent(event);
      if (created) {
        view.displaySuccess("Event created: " + event.getSubject());
      } else {
        view.displayError("Event already exists: " + event.getSubject());
      }
    } catch (IllegalArgumentException e) {
      view.displayError("Failed to create event: " + e.getMessage());
    }
  }
}
