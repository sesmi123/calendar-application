package calendar.controllers;

import calendar.controllers.commands.Command;
import calendar.models.Event;
import calendar.models.EventImpl;
import calendar.models.ObservableCalendar;
import calendar.models.RecurrenceRule;
import calendar.models.RecurrenceRuleImpl;
import calendar.models.enums.EventProperty;
import calendar.views.GuiView;
import calendar.views.ObservableView;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * The controller that orchestrates the communication between the model and the view.
 */
public class ControllerImpl implements Controller, Features {

  private final Readable in;
  private final CommandParser parser;
  private final CommandFactory factory;
  private final CalendarManager db;
  private ObservableView view;

  /**
   * Initialize the controller with appropriate model and view.
   *
   * @param view   the view that displays info to the user
   * @param in     the input from which user commands can be read from
   * @param parser the command parser
   */
  public ControllerImpl(ObservableView view, Readable in, CommandParser parser) {
    this.view = view;
    this.in = in;
    this.parser = parser;
    this.factory = null;
    this.db = null;
  }

  /**
   * Constructor for GUI mode.
   *
   * @param factory the command factory
   * @param db      the calendar manager
   */
  public ControllerImpl(CommandFactory factory, CalendarManager db) {
    this.view = null;
    this.in = null;
    this.parser = null;
    this.factory = factory;
    this.db = db;
  }

  @Override
  public void setView(GuiView view) {
    this.view = view;
    view.addFeatures(this);
  }

  @Override
  public void go() {
    boolean exitCommandEncountered = false;

    try (Scanner scanner = new Scanner(this.in)) {
      while (scanner.hasNextLine()) {
        String command = scanner.nextLine();
        if (command.trim().equalsIgnoreCase("exit")) {
          exitCommandEncountered = true;
          break;
        }
        Command executor;
        try {
          executor = parser.parse(command);
          executor.execute();
        } catch (IllegalArgumentException e) {
          view.displayError("Error parsing command: " + e.getMessage());
        } catch (IllegalStateException e) {
          view.displayError("Illegal State: " + e.getMessage());
        }
      }
    }

    if (!exitCommandEncountered) {
      view.displayError("Input ended without an 'exit' command. Terminating program...");
    }
  }

  /**
   * Helper method to invoke execute on the obtained command object.
   *
   * @param cmd the command object to execute
   */
  private void executeCommand(Command cmd) {
    try {
      cmd.execute();
    } catch (IllegalArgumentException e) {
      view.displayError("Error: " + e.getMessage());
    }
  }

  @Override
  public void createCalendar(String name, ZoneId timezone) {
    Command cmd = factory.createCalendar(name, timezone);
    executeCommand(cmd);
  }

  private void checkCalendarManagerAvailability() {
    if (db == null) {
      throw new IllegalArgumentException("Cannot find calendars");
    }
  }

  @Override
  public List<ObservableCalendar> listCalendars() {
    checkCalendarManagerAvailability();
    return this.db.getAll();
  }

  @Override
  public ObservableCalendar getActiveCalendar() {
    checkCalendarManagerAvailability();
    return this.db.getActive();
  }

  @Override
  public void useCalendar(String name) {
    Command cmd = factory.useCalendar(name);
    executeCommand(cmd);
  }

  @Override
  public void editCalendar(String name, String property, String value) {
    Command cmd = factory.editCalendar(name, property, value);
    executeCommand(cmd);
  }

  @Override
  public void createEvent(String subject, String startDateTime, String endDateTime) {
    try {
      LocalDateTime start = ParsingUtils.parseDateTime(startDateTime);
      LocalDateTime end = ParsingUtils.parseDateTime(endDateTime);

      Command cmd = factory.createEvent(subject, start, end);
      cmd.execute();
    } catch (IllegalArgumentException e) {
      view.displayError("Error: " + e.getMessage());
    }
  }

  @Override
  public void createAllDayEvent(String subject, String startDate) {
    try {
      LocalDate date = ParsingUtils.parseDate(startDate);

      Command cmd = factory.createAllDayEvent(subject, date);
      cmd.execute();
    } catch (IllegalArgumentException e) {
      view.displayError("Error: " + e.getMessage());
    }
  }

  @Override
  public void createEventSeries(String subject, String startDate, String startTime, String endTime,
      Set<DayOfWeek> days, int occurrences, String untilDate) {
    try {
      Event templateEvent;

      if (startDate != null && startTime != null && endTime != null) {
        LocalDate eventStartDate = ParsingUtils.parseDate(startDate);
        LocalTime eventStartTime = ParsingUtils.parseTime(startTime);
        LocalTime eventEndTime = ParsingUtils.parseTime(endTime);

        templateEvent = EventImpl.getBuilder().subject(subject).from(eventStartDate, eventStartTime)
            .to(eventStartDate, eventEndTime).build();
      } else if (startDate != null && startTime == null && endTime == null) {
        LocalDate date = ParsingUtils.parseDate(startDate);
        templateEvent = EventImpl.getBuilder().subject(subject).on(date).build();
      } else {
        throw new IllegalArgumentException(
            "Either start time and end time should be set or both should not be set.");
      }

      RecurrenceRule rule;
      if (untilDate != null) {
        LocalDate until = ParsingUtils.parseDate(untilDate);
        rule = new RecurrenceRuleImpl(days, until);
      } else {
        rule = new RecurrenceRuleImpl(days, occurrences);
      }

      Command cmd = factory.createEventSeries(templateEvent, rule);
      cmd.execute();
    } catch (IllegalArgumentException e) {
      view.displayError("Error: " + e.getMessage());
    }
  }

  @Override
  public void editEvent(String property, String subject, String startDateTime, String endDateTime,
      String newValue) {
    try {
      EventProperty eventProperty = ParsingUtils.getEventProperty(property);

      LocalDateTime eventStart = ParsingUtils.parseDateTime(startDateTime);
      LocalDateTime eventEnd = ParsingUtils.parseDateTime(endDateTime);

      String value = ParsingUtils.removeQuotes(newValue.trim());

      ParsingUtils.validateNewValue(eventProperty, value);

      Command cmd = factory.editEvent(eventProperty, subject, eventStart, eventEnd, value);
      cmd.execute();
    } catch (IllegalArgumentException e) {
      view.displayError("Error: " + e.getMessage());
    }
  }

  @Override
  public void editEvents(String property, String subject, String startDateTime, String newValue) {
    try {
      EventProperty eventProperty = ParsingUtils.getEventProperty(property);

      LocalDateTime eventStart = ParsingUtils.parseDateTime(startDateTime);

      String value = ParsingUtils.removeQuotes(newValue.trim());

      ParsingUtils.validateNewValue(eventProperty, value);

      Command cmd = factory.editEvents(eventProperty, subject, eventStart, value);
      cmd.execute();
    } catch (IllegalArgumentException e) {
      view.displayError("Error: " + e.getMessage());
    }
  }

  @Override
  public void editSeries(String property, String subject, String startDateTime, String newValue) {
    try {
      EventProperty eventProperty = ParsingUtils.getEventProperty(property);

      LocalDateTime eventStart = ParsingUtils.parseDateTime(startDateTime);

      String value = ParsingUtils.removeQuotes(newValue.trim());

      ParsingUtils.validateNewValue(eventProperty, value);

      Command cmd = factory.editSeries(eventProperty, subject, eventStart, value);
      cmd.execute();
    } catch (IllegalArgumentException e) {
      view.displayError("Error: " + e.getMessage());
    }
  }

  @Override
  public void queryByDate(LocalDate date) {
    Command cmd = factory.queryByDate(date);
    executeCommand(cmd);
  }

  @Override
  public void queryByDateRange(LocalDateTime start, LocalDateTime end) {
    Command cmd = factory.queryByDateRange(start, end);
    executeCommand(cmd);
  }

  @Override
  public void showStatus(LocalDateTime date) {
    Command cmd = factory.showStatus(date);
    executeCommand(cmd);
  }

}