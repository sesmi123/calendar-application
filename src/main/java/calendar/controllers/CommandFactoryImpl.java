package calendar.controllers;

import calendar.controllers.commands.Command;
import calendar.controllers.commands.CopyEventCommand;
import calendar.controllers.commands.CopyEventRangeCommand;
import calendar.controllers.commands.CopyEventsOnCommand;
import calendar.controllers.commands.CreateAllDayEventCommand;
import calendar.controllers.commands.CreateCalendarCommand;
import calendar.controllers.commands.CreateEventCommand;
import calendar.controllers.commands.CreateEventSeriesCommand;
import calendar.controllers.commands.EditCalendarCommand;
import calendar.controllers.commands.EditEventCommand;
import calendar.controllers.commands.EditEventsCommand;
import calendar.controllers.commands.EditSeriesCommand;
import calendar.controllers.commands.ExportCommand;
import calendar.controllers.commands.QueryEventsByDateCommand;
import calendar.controllers.commands.QueryEventsByDateRangeCommand;
import calendar.controllers.commands.ShowStatusCommand;
import calendar.controllers.commands.UseCalendarCommand;
import calendar.controllers.exporters.CalendarExporter;
import calendar.controllers.exporters.CsvCalendarExporter;
import calendar.controllers.exporters.IcalCalendarExporter;
import calendar.models.Event;
import calendar.models.RecurrenceRule;
import calendar.models.enums.EventProperty;
import calendar.views.ObservableView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Factory for creating command instances.
 */
public class CommandFactoryImpl implements CommandFactory {

  private final CalendarManager models;
  private final ObservableView view;
  private final Map<String, CalendarExporter> exporters;

  /**
   * Constructor for CommandFactory.
   *
   * @param models calendar model database
   * @param view   calendar view
   */
  public CommandFactoryImpl(CalendarManager models, ObservableView view) {
    this.models = Objects.requireNonNull(models);
    this.view = Objects.requireNonNull(view);
    this.exporters = new HashMap<>();
    this.exporters.put(".csv", new CsvCalendarExporter());
    this.exporters.put(".ical", new IcalCalendarExporter());
  }

  @Override
  public Command createCalendar(String name, ZoneId timezone) {
    return new CreateCalendarCommand(view, name, timezone, models);
  }

  @Override
  public Command useCalendar(String name) {
    return new UseCalendarCommand(view, name, models);
  }

  @Override
  public Command editCalendar(String name, String property, String value) {
    return new EditCalendarCommand(view, name, property, value, models);
  }

  @Override
  public Command createExporter(String filePath) {
    CalendarExporter exporter = null;

    for (String extension : exporters.keySet()) {
      if (filePath.endsWith(extension)) {
        exporter = exporters.get(extension);
        break;
      }
    }

    if (exporter == null) {
      throw new IllegalArgumentException("Unknown export file type: " + filePath);
    }

    return new ExportCommand(exporter, models.getActive(), view, filePath);
  }

  @Override
  public Command createAllDayEvent(String subject, LocalDate startDate) {
    return new CreateAllDayEventCommand(models.getActive(), view, subject, startDate);
  }

  @Override
  public Command createEvent(String subject, LocalDateTime startDateTime,
      LocalDateTime endDateTime) {
    return new CreateEventCommand(models.getActive(), view, subject, startDateTime, endDateTime);
  }

  @Override
  public Command createEventSeries(Event templateEvent, RecurrenceRule rule) {
    return new CreateEventSeriesCommand(models.getActive(), view, templateEvent, rule);
  }

  @Override
  public Command editEvent(EventProperty property, String subject,
      LocalDateTime startDateTime, LocalDateTime endDateTime,
      String newValue) {
    return new EditEventCommand(models.getActive(), view, property, subject, startDateTime,
        endDateTime,
        newValue);
  }

  @Override
  public Command editEvents(EventProperty property, String subject,
      LocalDateTime startDateTime, String newValue) {
    return new EditEventsCommand(models.getActive(), view, property, subject, startDateTime,
        newValue);
  }

  @Override
  public Command editSeries(EventProperty property, String subject,
      LocalDateTime startDateTime, String newValue) {
    return new EditSeriesCommand(models.getActive(), view, property, subject, startDateTime,
        newValue);
  }

  @Override
  public Command queryByDate(LocalDate date) {
    return new QueryEventsByDateCommand(models.getActive(), view, date);
  }

  @Override
  public Command queryByDateRange(LocalDateTime start, LocalDateTime end) {
    return new QueryEventsByDateRangeCommand(models.getActive(), view, start, end);
  }

  @Override
  public Command showStatus(LocalDateTime date) {
    return new ShowStatusCommand(models.getActive(), view, date);
  }

  @Override
  public Command copyEvent(String targetCalendarName,
      String eventSubject, LocalDateTime sourceStartDateTime,
      LocalDateTime targetStartDateTime) {
    return new CopyEventCommand(view, models.getActive(), models.get(targetCalendarName),
        eventSubject, sourceStartDateTime, targetStartDateTime);
  }

  @Override
  public Command copyEventsOn(String targetCalendarName, LocalDate sourceStartDate,
      LocalDate targetStartDate) {
    return new CopyEventsOnCommand(view, models.getActive(), models.get(targetCalendarName),
        sourceStartDate, targetStartDate);
  }

  @Override
  public Command copyEventRange(String targetCalendarName,
      LocalDate sourceRangeStart, LocalDate sourceRangeEnd,
      LocalDate targetRangeStart) {
    return new CopyEventRangeCommand(view, models.getActive(), models.get(targetCalendarName),
        sourceRangeStart, sourceRangeEnd, targetRangeStart);
  }

}
