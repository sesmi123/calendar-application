package calendar.controllers.commands;

import calendar.models.Calendar;
import calendar.models.Event;
import calendar.models.enums.EventProperty;
import calendar.views.ObservableView;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Handles the command to edit an event.
 */
public class EditEventCommand implements Command {

  private final Calendar model;
  private final ObservableView view;
  private final EventProperty property;
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final String newValue;

  /**
   * Constructs an EditEventCommand.
   *
   * @param model         the calendar model containing the event
   * @param view          the view for the calendar to send the output
   * @param property      the property of the event to be edited
   * @param subject       the subject of the event to be edited
   * @param startDateTime the startDateTime of the event to be edited
   * @param endDateTime   the endDateTime of the event to be edited
   * @param newValue      the new value of the property
   */
  public EditEventCommand(Calendar model, ObservableView view, EventProperty property,
      String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
      String newValue) {
    this.model = Objects.requireNonNull(model, "model cannot be null");
    this.view = Objects.requireNonNull(view, "view cannot be null");
    this.property = Objects.requireNonNull(property, "property cannot be null");
    this.subject = Objects.requireNonNull(subject, "subject cannot be null");
    this.startDateTime = Objects.requireNonNull(startDateTime, "startDateTime cannot be null");
    this.endDateTime = Objects.requireNonNull(endDateTime, "endDateTime cannot be null");
    this.newValue = Objects.requireNonNull(newValue, "newValue cannot be null");
  }

  @Override
  public void execute() {
    try {
      Event event = model.findSingleEvent(subject, startDateTime, endDateTime);

      if (event == null) {
        view.displayError(
            "Event with subject " + subject + " startDateTime " + startDateTime + " endDateTime "
                + endDateTime + " not found");
        return;
      }
      model.editSingleEvent(event, property, newValue);
      view.displaySuccess("Event edited successfully");
    } catch (IllegalArgumentException e) {
      view.displayError("Failed to edit event: " + e.getMessage());
    }
  }
}
