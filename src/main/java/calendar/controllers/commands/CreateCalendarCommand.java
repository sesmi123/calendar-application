package calendar.controllers.commands;

import calendar.controllers.CalendarManager;
import calendar.models.Calendar;
import calendar.models.CalendarImpl;
import calendar.views.ObservableView;
import java.time.ZoneId;

/**
 * Handles create calendar command.
 */
public class CreateCalendarCommand implements Command {

  private final String name;
  private final ZoneId timezone;
  private final CalendarManager db;
  private final ObservableView view;

  /**
   * Initialize create calander command object.
   *
   * @param view     the view
   * @param name     the name of the calendar
   * @param timezone the timezone of the calendar
   * @param db       the calendar database manager
   */
  public CreateCalendarCommand(ObservableView view, String name, ZoneId timezone,
      CalendarManager db) {
    this.view = view;
    this.name = name;
    this.timezone = timezone;
    this.db = db;
  }

  @Override
  public void execute() {
    Calendar cal = new CalendarImpl(name, timezone);
    this.db.set(cal);
    view.displaySuccess("Created new calendar " + name + " with timezone " + timezone);
  }
}
