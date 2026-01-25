package calendar.controllers.commands;

import calendar.controllers.CalendarManager;
import calendar.models.Calendar;
import calendar.models.CalendarImpl;
import calendar.models.Event;
import calendar.views.ObservableView;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Set;

/**
 * Handles the edit calendar commands.
 */
public class EditCalendarCommand implements Command {

  private final String name;
  private final String property;
  private final String value;
  private final ObservableView view;
  private final CalendarManager models;

  /**
   * Initialize the edit calendar command instance.
   *
   * @param view     the view
   * @param name     the name of the calendar
   * @param property property to edit
   * @param value    value to set to the property
   */
  public EditCalendarCommand(ObservableView view, String name, String property, String value,
      CalendarManager db) {
    this.view = view;
    this.name = name;
    this.property = property;
    this.value = value;
    this.models = db;
  }

  @Override
  public void execute() {
    Calendar model = this.models.get(name);
    if (property.equals("timezone")) {
      models.remove(name);
      Calendar cal = new CalendarImpl(name, ZoneId.of(value));
      models.set(cal);
      Set<Event> allEvents = model.getAllEvents();

      if (allEvents.isEmpty()) {
        view.displaySuccess("Edited " + property + " of calendar " + name + " to " + value);
        return;
      }
      Event earliestEvent = allEvents.stream()
          .min(Comparator.comparing(Event::getStartDate).thenComparing(Event::getStartTime)).get();

      model.copyEventsInRange(cal, allEvents, earliestEvent.getStartDate(),
          earliestEvent.getStartDate());

      if (models.getActive().getTitle().equals(name)) {
        models.activate(name);
      }
    } else {
      String currentName = model.getTitle();
      models.remove(currentName);
      model.setTitle(value);
      models.set(model);
    }
    view.displaySuccess("Edited " + property + " of calendar " + name + " to " + value);
  }
}
