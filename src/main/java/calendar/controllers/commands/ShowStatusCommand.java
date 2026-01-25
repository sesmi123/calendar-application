package calendar.controllers.commands;

import calendar.models.ObservableCalendar;
import calendar.views.ObservableView;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Handles the show availability status of the calendar at a given date-time.
 */
public class ShowStatusCommand implements Command {

  private final ObservableCalendar model;
  private final ObservableView view;
  private final LocalDateTime dateTime;

  /**
   * Initialize the command object to handle availability status query.
   *
   * @param dateTime the date-time to check availablity at
   */
  public ShowStatusCommand(ObservableCalendar model, ObservableView view, LocalDateTime dateTime) {
    this.model = Objects.requireNonNull(model);
    this.view = Objects.requireNonNull(view);
    this.dateTime = Objects.requireNonNull(dateTime);
  }

  @Override
  public void execute() {
    Boolean isBusy = model.isBusy(this.dateTime);
    view.displayStatus(isBusy);
  }
}
