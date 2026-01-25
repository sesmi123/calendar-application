package calendar.controllers.commands;

import calendar.models.Calendar;
import calendar.models.Event;
import calendar.models.EventImpl;
import calendar.views.ObservableView;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Handles the command to create an all day event on a given date from 8AM to 5PM.
 */
public class CreateAllDayEventCommand implements Command {

  private final Calendar model;
  private final ObservableView view;

  private final String subject;
  private final LocalDate startDate;

  /**
   * Initialize the command object to handle the creation of an all day event.
   *
   * @param subject   the title for the event
   * @param startDate the date on which the event should be created
   */
  public CreateAllDayEventCommand(Calendar model, ObservableView view, String subject,
      LocalDate startDate) {
    this.model = Objects.requireNonNull(model);
    this.view = Objects.requireNonNull(view);
    this.subject = Objects.requireNonNull(subject);
    this.startDate = Objects.requireNonNull(startDate);
  }

  @Override
  public void execute() {
    try {
      Event event = EventImpl.getBuilder()
          .subject(this.subject)
          .on(startDate)
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
