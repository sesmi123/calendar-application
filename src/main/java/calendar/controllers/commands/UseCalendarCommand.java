package calendar.controllers.commands;

import calendar.controllers.CalendarManager;
import calendar.views.ObservableView;

/**
 * Handles the use calendar command.
 */
public class UseCalendarCommand implements Command {

  private final String name;
  private final CalendarManager models;
  private final ObservableView view;

  /**
   * Initialize use calendar command.
   *
   * @param view   the view
   * @param name   name of the calendar
   * @param models the calendar database
   */
  public UseCalendarCommand(ObservableView view, String name, CalendarManager models) {
    this.view = view;
    this.models = models;
    this.name = name;
  }

  @Override
  public void execute() {
    this.models.activate(name);
    this.view.displaySuccess("Activated " + name + " calendar for use");
  }
}
