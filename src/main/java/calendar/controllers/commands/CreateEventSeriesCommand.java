package calendar.controllers.commands;

import calendar.models.Calendar;
import calendar.models.Event;
import calendar.models.EventSeries;
import calendar.models.EventSeriesImpl;
import calendar.models.RecurrenceRule;
import calendar.views.ObservableView;
import java.util.Objects;

/**
 * Handles the command to create a series of events.
 */
public class CreateEventSeriesCommand implements Command {

  private final Calendar model;
  private final ObservableView view;
  private final Event templateEvent;
  private final RecurrenceRule recurrenceRule;

  /**
   * Initialize the command class to create a series of events using the template event and
   * recurrence rule.
   *
   * @param templateEvent prototype of an event for the series
   * @param rule          defines the frequency and limit of the events in the series
   */
  public CreateEventSeriesCommand(Calendar model, ObservableView view, Event templateEvent,
      RecurrenceRule rule) {
    this.model = Objects.requireNonNull(model);
    this.view = Objects.requireNonNull(view);
    this.templateEvent = Objects.requireNonNull(templateEvent);
    this.recurrenceRule = Objects.requireNonNull(rule);
  }

  @Override
  public void execute() {
    try {
      EventSeries newSeries = new EventSeriesImpl(templateEvent, templateEvent.getStartDate(),
          recurrenceRule);
      model.addEventSeries(newSeries);
      view.displaySuccess("Event series created: " + templateEvent.getSubject());
    } catch (IllegalArgumentException e) {
      view.displayError("Failed to create event series: " + e.getMessage());
    }
  }
}
