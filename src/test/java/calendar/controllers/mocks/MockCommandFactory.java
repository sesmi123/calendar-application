package calendar.controllers.mocks;

import calendar.controllers.CommandFactory;
import calendar.controllers.commands.Command;
import calendar.models.Event;
import calendar.models.RecurrenceRule;
import calendar.models.enums.EventProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Mock class for command factory.
 */
public class MockCommandFactory implements CommandFactory {

  private final int uniqueCode;
  private final StringBuilder log;
  String lastCalled;
  LocalDate dateArg;
  LocalDateTime startArg;
  LocalDateTime endArg;
  LocalDateTime dateTimeArg;
  String filenameArg;
  Command command;

  /**
   * Instatiate a mock view.
   *
   * @param log        dummy logs
   * @param uniqueCode unique code
   */
  public MockCommandFactory(StringBuilder log, int uniqueCode) {
    this.log = log;
    this.uniqueCode = uniqueCode;
    this.command = new controllers.mocks.MockCommand(log, 78165);
  }

  @Override
  public Command queryByDate(LocalDate date) {
    log.append("printOn");
    return command;
  }

  @Override
  public Command queryByDateRange(LocalDateTime start, LocalDateTime end) {
    log.append("printInRange: ").append(start).append(" to ").append(end);
    return command;
  }

  @Override
  public Command createCalendar(String name, ZoneId timezone) {
    log.append("createCalendar ").append(name).append(" ").append(timezone);
    return command;
  }

  @Override
  public Command useCalendar(String name) {
    log.append("useCalendar ").append(name);
    return command;
  }

  @Override
  public Command editCalendar(String name, String property, String value) {
    log.append("editCalendar ").append(name).append(" ")
        .append(property).append(" ").append(value);
    return command;
  }

  @Override
  public Command createExporter(String filename) {
    if (filename.endsWith(".csv")) {
      log.append("export csv to ");
    } else if (filename.endsWith(".ical")) {
      log.append("export ical to ");
    }
    log.append(filename);
    return command;
  }

  @Override
  public Command createAllDayEvent(String subject, LocalDate startDate) {
    log.append("createAllDayEvent: ").append(subject).append(" on ").append(startDate);
    return command;
  }

  @Override
  public Command createEvent(String subject, LocalDateTime startDateTime,
      LocalDateTime endDateTime) {
    log.append("createEvent: ").append(subject).append(" from ")
        .append(startDateTime).append(" to ").append(endDateTime);
    return command;
  }

  @Override
  public Command createEventSeries(Event templateEvent, RecurrenceRule rule) {
    log.append("createEventSeries");
    return command;
  }

  @Override
  public Command editEvent(EventProperty property, String subject,
      LocalDateTime startDateTime, LocalDateTime endDateTime, String newValue) {
    log.append("editEvent: " + property + " with " + newValue);
    return command;
  }

  @Override
  public Command editEvents(EventProperty property, String subject,
      LocalDateTime startDateTime, String newValue) {
    return null;
  }

  @Override
  public Command editSeries(EventProperty property, String subject,
      LocalDateTime startDateTime, String newValue) {
    log.append("editEventSeries ").append("property ").append(property)
        .append(subject).append(startDateTime).append(" with value ").append(newValue);
    return command;
  }

  @Override
  public Command showStatus(LocalDateTime dateTime) {
    log.append("showStatus at ").append(dateTime);
    return command;
  }

  @Override
  public Command copyEvent(String targetCalendarName,
      String eventSubject, LocalDateTime sourceStartDateTime, LocalDateTime targetStartDateTime) {
    log.append("copyEvent ").append(eventSubject)
        .append(" to ").append(targetCalendarName).append(" starting ").append(sourceStartDateTime)
        .append(" ending ").append(targetStartDateTime);
    return command;
  }

  @Override
  public Command copyEventsOn(String targetCalendarName, LocalDate sourceStartDate,
      LocalDate targetStartDate) {
    return command;
  }

  @Override
  public Command copyEventRange(String targetCalendarName,
      LocalDate sourceRangeStart, LocalDate sourceRangeEnd,
      LocalDate targetRangeStart) {
    log.append("copyEventRange to ")
        .append(targetCalendarName).append(" starting ").append(sourceRangeStart).append(" to ")
        .append(sourceRangeEnd).append(" target rnge start ").append(targetRangeStart);
    return command;
  }

}