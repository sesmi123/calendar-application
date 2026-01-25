package calendar.controllers;

import calendar.controllers.commands.Command;
import calendar.models.Event;
import calendar.models.EventImpl;
import calendar.models.RecurrenceRule;
import calendar.models.RecurrenceRuleImpl;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsible for parsing commands and returning an {@link Command} representing the parsed
 * command.
 */
public class CommandParserImpl implements CommandParser {

  private final CommandFactory factory;
  private final Map<String, Function<String, Command>> commandMap = createCommandMap();

  /**
   * Constructor to initialize CommandParserImpl object with CommandFactory.
   *
   * @param factory instance of command factory
   */
  public CommandParserImpl(CommandFactory factory) {
    this.factory = Objects.requireNonNull(factory, "Factory must not be null");
  }

  /**
   * Initializes the command parsing strategies map. Uses LinkedHashMap to maintain insertion order
   * for prefix matching.
   *
   * @return map of command prefixes to parsing strategies
   */
  private Map<String, Function<String, Command>> createCommandMap() {
    Map<String, Function<String, Command>> knownCommands = new LinkedHashMap<>();

    knownCommands.put("create calendar ", cmd -> parseCreateCalendar(cmd));
    knownCommands.put("use calendar ", cmd -> parseUseCalendar(cmd));
    knownCommands.put("edit calendar ", cmd -> parseEditCalendar(cmd));
    knownCommands.put("create event ", cmd -> parseCreateEvent(cmd));
    knownCommands.put("edit events ", cmd -> parseEditEvents(cmd));
    knownCommands.put("edit event ", cmd -> parseEditEvent(cmd));
    knownCommands.put("edit series ", cmd -> parseEditSeries(cmd));
    knownCommands.put("print events on ", cmd -> parsePrintEventsOn(cmd));
    knownCommands.put("print events from ", cmd -> parsePrintEventsFrom(cmd));
    knownCommands.put("export cal ", cmd -> parseExportCal(cmd));
    knownCommands.put("show status on ", cmd -> parseShowStatus(cmd));
    knownCommands.put("copy event ", cmd -> parseCopyEvent(cmd));
    knownCommands.put("copy events on ", cmd -> parseCopyEventsOn(cmd));
    knownCommands.put("copy events between ", cmd -> parseCopyEventRange(cmd));

    return Collections.unmodifiableMap(knownCommands);
  }


  @Override
  public Command parse(String commandLine) throws IllegalArgumentException {
    if (commandLine == null || commandLine.trim().isEmpty()) {
      throw new IllegalArgumentException("Command cannot be empty");
    }

    String trimmed = commandLine.trim();

    for (Map.Entry<String, Function<String, Command>> entry : commandMap.entrySet()) {
      if (!trimmed.startsWith(entry.getKey())) {
        continue;
      }
      try {
        return entry.getValue().apply(trimmed);
      } catch (RuntimeException e) {
        if (e.getCause() instanceof IllegalArgumentException) {
          throw (IllegalArgumentException) e.getCause();
        }
        throw e;
      }
    }

    throw new IllegalArgumentException("Unknown command: " + trimmed);
  }

  private Command parseCreateCalendar(String commandLine) throws IllegalArgumentException {
    String rest = commandLine.substring("create calendar ".length()).trim();

    Pattern pattern = Pattern.compile("--name\\s+(\\S+)\\s+--timezone\\s+(\\S+)$");
    Matcher matcher = pattern.matcher(rest);

    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          "Invalid create calendar command format. Expected: create calendar "
              + "--name <calName> --timezone area/location");
    }

    String calendarName = matcher.group(1);
    String timezone = matcher.group(2);
    if (!isValidTimezone(timezone)) {
      throw new IllegalArgumentException("Invalid time zone: " + timezone);
    }
    return factory.createCalendar(calendarName, ZoneId.of(timezone));
  }

  private boolean isValidTimezone(String timezone) {
    try {
      ZoneId.of(timezone);
      return true;
    } catch (ZoneRulesException | NullPointerException e) {
      return false;
    }
  }

  private Command parseUseCalendar(String commandLine) throws IllegalArgumentException {
    String rest = commandLine.substring("use calendar ".length()).trim();

    Pattern pattern = Pattern.compile("--name\\s+(\\S+)$");
    Matcher matcher = pattern.matcher(rest);

    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          "Invalid use calendar command format. Expected: use calendar --name <name-of-calendar>");
    }

    String calendarName = matcher.group(1);

    return factory.useCalendar(calendarName);
  }

  private Command parseEditCalendar(String commandLine) throws IllegalArgumentException {
    String rest = commandLine.substring("edit calendar ".length()).trim();

    Pattern pattern =
        Pattern.compile("--name\\s+(\\S+)\\s+--property\\s+(name|timezone)" + "\\s+(\\S+)$");
    Matcher matcher = pattern.matcher(rest);

    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          "Invalid edit calendar command format. Expected: edit calendar --name <name-of-calendar>"
              + " --property <property-name> <new-property-value>");
    }

    String calendarName = matcher.group(1);
    String property = matcher.group(2);
    String newValue = matcher.group(3);

    if (property.equals("timezone") && !isValidTimezone(newValue)) {
      throw new IllegalArgumentException("Invalid time zone: " + newValue);
    }
    return factory.editCalendar(calendarName, property, newValue);
  }

  private Command parseCreateEvent(String commandLine) throws IllegalArgumentException {
    String rest = commandLine.substring("create event ".length());

    List<Function<String, Command>> parsers =
        List.of(this::tryParseSingleEvent, this::tryParseRepeatingEventCount,
            this::tryParseRepeatingEventUntil, this::tryParseAllDayEvent,
            this::tryParseAllDayRepeatingCount, this::tryParseAllDayRepeatingUntil);

    for (Function<String, Command> parser : parsers) {
      Command cmd = parser.apply(rest);
      if (cmd != null) {
        return cmd;
      }
    }

    throw new IllegalArgumentException("Invalid create event command: " + commandLine);
  }

  private Command tryParseSingleEvent(String rest) throws IllegalArgumentException {
    String regex = "(?:\"([^\"]+)\"|(\\S+)) from (\\S+) to (\\S+)$";
    Matcher m = Pattern.compile(regex).matcher(rest);
    if (m.matches()) {
      String subject = m.group(1) != null ? m.group(1) : m.group(2);
      LocalDateTime start = ParsingUtils.parseDateTime(m.group(3));
      LocalDateTime end = ParsingUtils.parseDateTime(m.group(4));
      return factory.createEvent(subject, start, end);
    }
    return null;
  }

  private Command tryParseRepeatingEventCount(String rest) throws IllegalArgumentException {
    String regex = "(?:\"([^\"]+)\"|(\\S+)) from (\\S+) to (\\S+) repeats (\\S+) for (\\d+) times$";
    Matcher m = Pattern.compile(regex).matcher(rest);
    if (m.matches()) {
      String subject = m.group(1) != null ? m.group(1) : m.group(2);
      LocalDateTime start = ParsingUtils.parseDateTime(m.group(3));
      LocalDateTime end = ParsingUtils.parseDateTime(m.group(4));
      Set<DayOfWeek> days = ParsingUtils.parseWeekdays(m.group(5));
      int count = Integer.parseInt(m.group(6));

      Event event =
          EventImpl.getBuilder().subject(subject).from(start.toLocalDate(), start.toLocalTime())
              .to(end.toLocalDate(), end.toLocalTime()).build();
      RecurrenceRule rule = new RecurrenceRuleImpl(days, count);
      return factory.createEventSeries(event, rule);
    }
    return null;
  }

  private Command tryParseRepeatingEventUntil(String rest) throws IllegalArgumentException {
    String regex = "(?:\"([^\"]+)\"|(\\S+)) from (\\S+) to (\\S+) repeats (\\S+) until (\\S+)$";
    Matcher m = Pattern.compile(regex).matcher(rest);
    if (m.matches()) {
      String subject = m.group(1) != null ? m.group(1) : m.group(2);
      LocalDateTime start = ParsingUtils.parseDateTime(m.group(3));
      LocalDateTime end = ParsingUtils.parseDateTime(m.group(4));
      Set<DayOfWeek> days = ParsingUtils.parseWeekdays(m.group(5));
      LocalDate until = ParsingUtils.parseDate(m.group(6));

      Event event =
          EventImpl.getBuilder().subject(subject).from(start.toLocalDate(), start.toLocalTime())
              .to(end.toLocalDate(), end.toLocalTime()).build();
      RecurrenceRule rule = new RecurrenceRuleImpl(days, until);
      return factory.createEventSeries(event, rule);
    }
    return null;
  }

  private Command tryParseAllDayEvent(String rest) throws IllegalArgumentException {
    String regex = "(?:\"([^\"]+)\"|(\\S+)) on (\\S+)$";
    Matcher m = Pattern.compile(regex).matcher(rest);
    if (m.matches()) {
      String subject = m.group(1) != null ? m.group(1) : m.group(2);
      LocalDate date = ParsingUtils.parseDate(m.group(3));
      return factory.createAllDayEvent(subject, date);
    }
    return null;
  }

  private Command tryParseAllDayRepeatingCount(String rest) throws IllegalArgumentException {
    String regex = "(?:\"([^\"]+)\"|(\\S+)) on (\\S+) repeats (\\S+) for (\\d+) times$";
    Matcher m = Pattern.compile(regex).matcher(rest);
    if (m.matches()) {
      String subject = m.group(1) != null ? m.group(1) : m.group(2);
      LocalDate date = ParsingUtils.parseDate(m.group(3));
      Set<DayOfWeek> days = ParsingUtils.parseWeekdays(m.group(4));
      int count = Integer.parseInt(m.group(5));

      Event event = EventImpl.getBuilder().subject(subject).on(date).build();
      RecurrenceRule rule = new RecurrenceRuleImpl(days, count);
      return factory.createEventSeries(event, rule);
    }
    return null;
  }

  private Command tryParseAllDayRepeatingUntil(String rest) throws IllegalArgumentException {
    String regex = "(?:\"([^\"]+)\"|(\\S+)) on (\\S+) repeats (\\S+) until (\\S+)$";
    Matcher m = Pattern.compile(regex).matcher(rest);
    if (m.matches()) {
      String subject = m.group(1) != null ? m.group(1) : m.group(2);
      LocalDate date = ParsingUtils.parseDate(m.group(3));
      Set<DayOfWeek> days = ParsingUtils.parseWeekdays(m.group(4));
      LocalDate until = ParsingUtils.parseDate(m.group(5));

      Event event = EventImpl.getBuilder().subject(subject).on(date).build();
      RecurrenceRule rule = new RecurrenceRuleImpl(days, until);
      return factory.createEventSeries(event, rule);
    }
    return null;
  }

  /**
   * Parses edit event command using regex pattern matching.
   */
  private Command parseEditEvent(String commandLine) throws IllegalArgumentException {
    return parseEditCommand(commandLine, "edit event ",
        "(subject|start|end|description|location|status)\\s+(?:\"([^\"]+)\"|(\\S+))"
            + "\\s+from\\s+(\\S+)\\s+to\\s+(\\S+)\\s+with\\s+(.+)$",
        "edit event <property> <subject> from <dateTime> to <dateTime> with <value>", true);
  }

  /**
   * Parses edit series command using regex pattern matching.
   */
  private Command parseEditSeries(String commandLine) throws IllegalArgumentException {
    return parseEditCommand(commandLine, "edit series ",
        "(subject|start|end|description|location|status)\\s+(?:\"([^\"]+)\"|(\\S+))"
            + "\\s+from\\s+(\\S+)\\s+with\\s+(.+)$",
        "edit series <property> <subject> from <dateTime> with <value>", false);
  }

  /**
   * Parses edit events command using regex pattern matching.
   */
  private Command parseEditEvents(String commandLine) throws IllegalArgumentException {
    return parseEditCommand(commandLine, "edit events ",
        "(subject|start|end|description|location|status)\\s+(?:\"([^\"]+)\"|(\\S+))"
            + "\\s+from\\s+(\\S+)\\s+with\\s+(.+)$",
        "edit events <property> <subject> from <dateTime> with <value>", false);
  }

  /**
   * Generic method to parse edit commands with common structure.
   */
  private Command parseEditCommand(String commandLine, String prefix, String regex,
      String expectedFormat, boolean requiresEndDateTime) {
    String rest = commandLine.substring(prefix.length()).trim();
    Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(rest);

    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          "Invalid " + prefix.trim() + " command format. Expected: " + expectedFormat);
    }

    String propertyStr = matcher.group(1);
    String subject = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
    LocalDateTime startDateTime = ParsingUtils.parseDateTime(matcher.group(4));
    String newValueRaw = requiresEndDateTime ? matcher.group(6) : matcher.group(5);
    String newValue = ParsingUtils.removeQuotes(newValueRaw.trim());

    ParsingUtils.validateNewValue(ParsingUtils.getEventProperty(propertyStr), newValue);

    if (requiresEndDateTime) {
      LocalDateTime endDateTime = ParsingUtils.parseDateTime(matcher.group(5));
      return factory.editEvent(ParsingUtils.getEventProperty(propertyStr), subject, startDateTime,
          endDateTime, newValue);
    }

    return prefix.equals("edit series ")
        ? factory.editSeries(ParsingUtils.getEventProperty(propertyStr), subject, startDateTime,
        newValue) :
        factory.editEvents(ParsingUtils.getEventProperty(propertyStr), subject, startDateTime,
            newValue);
  }


  private Command parsePrintEventsOn(String commandLine) throws IllegalArgumentException {
    String rest = commandLine.substring("print events on ".length()).trim();
    LocalDate date = ParsingUtils.parseDate(rest);
    return factory.queryByDate(date);
  }

  private Command parsePrintEventsFrom(String commandLine) throws IllegalArgumentException {
    String rest = commandLine.substring("print events from ".length());

    Pattern p = Pattern.compile("(\\S+) to (\\S+)$");
    Matcher m = p.matcher(rest);
    if (m.matches()) {
      LocalDateTime start = ParsingUtils.parseDateTime(m.group(1));
      LocalDateTime end = ParsingUtils.parseDateTime(m.group(2));
      return factory.queryByDateRange(start, end);
    }

    throw new IllegalArgumentException("Invalid print events from command: " + commandLine);
  }

  private Command parseExportCal(String commandLine) {
    String rest = commandLine.substring("export cal ".length()).trim();
    return factory.createExporter(rest);
  }

  private Command parseShowStatus(String commandLine) throws IllegalArgumentException {
    String rest = commandLine.substring("show status on ".length()).trim();
    LocalDateTime dateTime = ParsingUtils.parseDateTime(rest);
    return factory.showStatus(dateTime);
  }

  private Command parseCopyEvent(String commandLine) throws IllegalArgumentException {
    String rest = commandLine.substring("copy event ".length()).trim();

    Pattern newPattern = Pattern.compile(
        "(?:\"([^\"]+)\"|(\\S+))\\s+on\\s+(\\S+)\\s+--target\\s+(\\S+)\\s+to\\s+(\\S+)$");
    Matcher newMatcher = newPattern.matcher(rest);

    if (newMatcher.matches()) {
      String eventSubject = newMatcher.group(1) != null ? newMatcher.group(1) : newMatcher.group(2);
      LocalDateTime sourceStartDateTime = ParsingUtils.parseDateTime(newMatcher.group(3));
      String targetCalendarName = newMatcher.group(4);
      LocalDateTime targetStartDateTime = ParsingUtils.parseDateTime(newMatcher.group(5));

      return factory.copyEvent(targetCalendarName, eventSubject, sourceStartDateTime,
          targetStartDateTime);
    }
    throw new IllegalArgumentException("Invalid copy event from command: " + commandLine);
  }

  private Command parseCopyEventsOn(String commandLine) throws IllegalArgumentException {
    String rest = commandLine.substring("copy events on ".length()).trim();

    Pattern pattern = Pattern.compile("^(\\S+)\\s+--target\\s+(\\S+)\\s+to\\s+(\\S+)$");
    Matcher matcher = pattern.matcher(rest);

    if (matcher.matches()) {
      LocalDate sourceDate = ParsingUtils.parseDate(matcher.group(1));
      String targetCalendarName = matcher.group(2);
      LocalDate targetDate = ParsingUtils.parseDate(matcher.group(3));

      return factory.copyEventsOn(targetCalendarName, sourceDate, targetDate);
    }

    throw new IllegalArgumentException("Invalid copy events command: " + commandLine);
  }


  private Command parseCopyEventRange(String commandLine) throws IllegalArgumentException {
    String rest = commandLine.substring("copy events between ".length()).trim();

    Pattern pattern = Pattern.compile("(\\S+) and (\\S+)" + " --target\\s+(\\S+) to (\\S+)$");
    Matcher matcher = pattern.matcher(rest);

    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          "Invalid copy events command format. Expected: copy events between YYYY-MM-DD and "
              + "YYYY-MM-DD --target <target_name> to YYYY-MM-DD");
    }

    LocalDate sourceRangeStart = ParsingUtils.parseDate(matcher.group(1));
    LocalDate sourceRangeEnd = ParsingUtils.parseDate(matcher.group(2));
    String targetCalendarName = matcher.group(3);
    LocalDate targetRangeStart = ParsingUtils.parseDate(matcher.group(4));

    return factory.copyEventRange(targetCalendarName, sourceRangeStart, sourceRangeEnd,
        targetRangeStart);
  }
}