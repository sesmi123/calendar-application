package calendar.controllers.commands;

import calendar.models.Calendar;
import calendar.models.Event;
import calendar.models.enums.EventProperty;
import calendar.views.ObservableView;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Handles the command to edit a series of events. If the event not part of a series, it edits only
 * the single event.
 */
public class EditSeriesCommand implements Command {

  private final Calendar model;
  private final ObservableView view;
  private final EventProperty property;
  private final String subject;
  private final LocalDateTime startDateTime;
  private final String newValue;


  /**
   * EditSeriesCommand constructor.
   *
   * @param model         the calendar model containing the event
   * @param view          the view for the calendar to send the output
   * @param property      the property of the event to be edited
   * @param subject       the subject of the event to be edited
   * @param startDateTime the startDateTime of the event to be edited
   * @param newValue      the new value of the property
   */
  public EditSeriesCommand(Calendar model, ObservableView view, EventProperty property,
      String subject, LocalDateTime startDateTime, String newValue) {
    this.model = Objects.requireNonNull(model, "model cannot be null");
    this.view = Objects.requireNonNull(view, "view cannot be null");
    this.property = Objects.requireNonNull(property, "property cannot be null");
    this.subject = Objects.requireNonNull(subject, "subject cannot be null");
    this.startDateTime = Objects.requireNonNull(startDateTime, "startDateTime cannot be null");
    this.newValue = Objects.requireNonNull(newValue, "newValue cannot be null");
  }

  @Override
  public void execute() {
    try {
      Set<Event> matchedEvents = model.findEventBySubjectAndStart(subject, startDateTime);
      if (matchedEvents.isEmpty()) {
        view.displayError(
            "Event with subject " + subject + " startDateTime " + startDateTime + " not found");
        return;
      }
      if (matchedEvents.size() > 1) {
        view.displayError(
            "Multiple events found with subject '" + subject + "' starting at " + startDateTime
                + ". Cannot uniquely identify event!");
        return;
      }
      Event event = matchedEvents.stream().findFirst().orElse(null);
      if (!event.isPartOfSeries()) {
        model.editSingleEvent(event, property, newValue);
        return;
      }
      model.editSeriesEvent(event, property, newValue);
      view.displaySuccess("Event/Series edited successfully");
    } catch (IllegalArgumentException e) {
      view.displayError("Failed to edit event: " + e.getMessage());
    }
  }
}
