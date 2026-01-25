package calendar.controllers;

import calendar.controllers.commands.Command;
import calendar.models.Event;
import calendar.models.RecurrenceRule;
import calendar.models.enums.EventProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Factory interface for creating command instances.
 */
public interface CommandFactory {

  /**
   * Creates a calendar.
   *
   * @param name     unique name of calendar
   * @param timezone timezone of calendar
   * @return CreateCalendarCommand instance
   */
  Command createCalendar(String name, ZoneId timezone);

  /**
   * Use a particular calendar.
   *
   * @param name the name of calendar to use
   * @return UseCalendarCommand instance
   */
  Command useCalendar(String name);

  /**
   * Edit a particular calendar.
   *
   * @param name     the name of calendar
   * @param property the property to edit
   * @param value    the new value to ste for the property
   * @return EditCalendarCommand instance
   */
  Command editCalendar(String name, String property, String value);

  /**
   * Creates an ExportCommand for exporting calendar.
   *
   * @param filePath path to export file
   * @return ExportCommand instance
   */
  Command createExporter(String filePath);

  /**
   * Creates a CreateAllDayEventCommand.
   *
   * @param subject   event subject
   * @param startDate event start date
   * @return CreateAllDayEventCommand instance
   */
  Command createAllDayEvent(String subject, LocalDate startDate);

  /**
   * Creates a CreateEventCommand.
   *
   * @param subject       event subject
   * @param startDateTime event start date and time
   * @param endDateTime   event end date and time
   * @return CreateEventCommand instance
   */
  Command createEvent(String subject, LocalDateTime startDateTime,
      LocalDateTime endDateTime);

  /**
   * Creates a CreateEventSeriesCommand.
   *
   * @param templateEvent template event for the series
   * @param rule          recurrence rule for the series
   * @return CreateEventSeriesCommand instance
   */
  Command createEventSeries(Event templateEvent, RecurrenceRule rule);

  /**
   * Creates a EditEventCommand.
   *
   * @param property      event property to edit
   * @param subject       subject of the event which is to be edited
   * @param startDateTime startDateTime of the event which is to be edited
   * @param endDateTime   newValue new value for the property
   * @param newValue      new value for the property
   * @return EditEventCommand instance
   */
  Command editEvent(EventProperty property, String subject,
      LocalDateTime startDateTime, LocalDateTime endDateTime,
      String newValue);

  /**
   * Creates a EditEventsCommand.
   *
   * @param property      event property to edit
   * @param subject       subject of the events to be edited
   * @param startDateTime startDateTime of the events to be edited
   * @param newValue      new value for the property
   * @return EditEventsCommand instance
   */
  Command editEvents(EventProperty property, String subject,
      LocalDateTime startDateTime, String newValue);

  /**
   * Creates an EditSeriesCommand.
   *
   * @param property      event property to edit
   * @param subject       subject of the event which is to be edited
   * @param startDateTime startDateTime of the event which is to be edited
   * @param newValue      newValue new value for the property
   * @return EditSeriesCommand instance
   */
  Command editSeries(EventProperty property, String subject,
      LocalDateTime startDateTime, String newValue);

  /**
   * Creates a QueryEventsByDateCommand.
   *
   * @param date date to query events
   * @return QueryEventsByDateCommand instance
   */
  Command queryByDate(LocalDate date);

  /**
   * Creates a QueryEventsByDateRangeCommand.
   *
   * @param start start date and time of the range
   * @param end   end date and time of the range
   * @return QueryEventsByDateRangeCommand instance
   */
  Command queryByDateRange(LocalDateTime start, LocalDateTime end);

  /**
   * Creates a ShowStatusCommand.
   *
   * @param date date and time to show status
   * @return ShowStatusCommand instance
   */
  Command showStatus(LocalDateTime date);

  /**
   * Creates a CopyEventCommand to copy a single event from one calendar to another.
   *
   * @param targetCalendarName  the name of the target calendar
   * @param eventSubject        the subject of the event to copy
   * @param sourceStartDateTime the start date/time of the event in the source calendar
   * @param targetStartDateTime the target start date/time for the copied event
   * @return CopyEventCommand instance
   */
  Command copyEvent(String targetCalendarName,
      String eventSubject, LocalDateTime sourceStartDateTime,
      LocalDateTime targetStartDateTime);

  /**
   * Creates a CopyEventsOnCommand to copy events on a day from one calendar to another.
   *
   * @param targetCalendarName the name of the target calendar
   * @param sourceStartDate    the start date/time of the event in the source calendar
   * @param targetStartDate    the target start date/time for the copied event
   * @return CopyEventsOnCommand instance
   */
  Command copyEventsOn(String targetCalendarName, LocalDate sourceStartDate,
      LocalDate targetStartDate);

  /**
   * Creates a CopyEventRangeCommand to copy all events within a date range from one calendar to
   * another.
   *
   * @param targetCalendarName the name of the target calendar
   * @param sourceRangeStart   the start of the date range in the source calendar
   * @param sourceRangeEnd     the end of the date range in the source calendar
   * @param targetRangeStart   the target start date for copied events
   * @return CopyEventRangeCommand instance
   */
  Command copyEventRange(String targetCalendarName,
      LocalDate sourceRangeStart, LocalDate sourceRangeEnd,
      LocalDate targetRangeStart);

}
