package calendar.controllers;

import calendar.models.enums.EventProperty;
import calendar.models.enums.Location;
import calendar.models.enums.Status;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for common parsing operations.
 */
public final class ParsingUtils {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
  private static final Map<String, EventProperty> PROPERTY_MAP = createPropertyMap();
  private static final Map<Character, DayOfWeek> weekdayMap = createWeekdayMap();

  /**
   * Prevent instantiation.
   */
  private ParsingUtils() {
  }

  /**
   * Creates and initializes the property name to EventProperty mapping.
   *
   * @return an unmodifiable map of property strings to EventProperty enums
   */
  private static Map<String, EventProperty> createPropertyMap() {
    Map<String, EventProperty> map = new HashMap<>();
    map.put("subject", EventProperty.SUBJECT);
    map.put("start", EventProperty.START_DATE_TIME);
    map.put("end", EventProperty.END_DATE_TIME);
    map.put("description", EventProperty.DESCRIPTION);
    map.put("location", EventProperty.LOCATION);
    map.put("status", EventProperty.STATUS);
    return Collections.unmodifiableMap(map);
  }

  /**
   * Creates and initializes the character to Weekday mapping.
   *
   * @return an unmodifiable map of character strings to Weekday enums
   */
  private static Map<Character, DayOfWeek> createWeekdayMap() {
    Map<Character, DayOfWeek> map = new HashMap<>();
    map.put('M', DayOfWeek.MONDAY);
    map.put('T', DayOfWeek.TUESDAY);
    map.put('W', DayOfWeek.WEDNESDAY);
    map.put('R', DayOfWeek.THURSDAY);
    map.put('F', DayOfWeek.FRIDAY);
    map.put('S', DayOfWeek.SATURDAY);
    map.put('U', DayOfWeek.SUNDAY);
    return Collections.unmodifiableMap(map);
  }

  /**
   * Parse date string in YYYY-MM-DD format.
   *
   * @param dateStr the date string to parse
   * @return LocalDate object
   * @throws IllegalArgumentException if the date format is invalid
   */
  public static LocalDate parseDate(String dateStr) throws IllegalArgumentException {
    try {
      return LocalDate.parse(dateStr, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid date format: " + dateStr + ". Expected format: YYYY-MM-DD");
    }
  }

  /**
   * Parse time string in HH:mm format.
   *
   * @param timeStr the time string to parse
   * @return LocalTime object
   * @throws IllegalArgumentException if the time format is invalid
   */
  public static LocalTime parseTime(String timeStr) throws IllegalArgumentException {
    try {
      return LocalTime.parse(timeStr, TIME_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid time format: " + timeStr + ". Expected format: HH:mm");
    }
  }

  /**
   * Parse datetime string in YYYY-MM-DDTHH:mm format.
   *
   * @param dateTimeStr the datetime string to parse
   * @return LocalDateTime object
   * @throws IllegalArgumentException if the datetime format is invalid
   */
  public static LocalDateTime parseDateTime(String dateTimeStr) throws IllegalArgumentException {
    try {
      return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid datetime format: " + dateTimeStr + ". Expected format: YYYY-MM-DDTHH:mm");
    }
  }

  /**
   * Parse weekday string like "MWF" into Set of DayOfWeek.
   */
  public static Set<DayOfWeek> parseWeekdays(String weekdayStr) throws IllegalArgumentException {
    Set<DayOfWeek> days = new LinkedHashSet<>();

    for (char c : weekdayStr.toCharArray()) {
      DayOfWeek day = weekdayMap.get(c);
      if (day == null) {
        throw new IllegalArgumentException(
            "Invalid weekday character: " + c + ". Valid: M, T, W, R, F, S, U");
      }
      days.add(day);
    }

    return days;
  }

  /**
   * Converts a property string to an EventProperty enum.
   *
   * @param propertyStr the property string (case-insensitive)
   * @return EventProperty enum
   * @throws IllegalArgumentException if the property is not recognized
   */
  public static EventProperty getEventProperty(String propertyStr) throws IllegalArgumentException {
    EventProperty property = PROPERTY_MAP.get(propertyStr.toLowerCase());
    if (property == null) {
      throw new IllegalArgumentException("Invalid property: " + propertyStr);
    }
    return property;
  }

  /**
   * Validates the new value based on property type being edited.
   *
   * @param property the EventProperty being edited
   * @param value    the new value to validate
   * @throws IllegalArgumentException if the value is invalid for the property type
   */
  public static void validateNewValue(EventProperty property, String value) {
    String trimmedValue = value.trim();

    switch (property) {
      case SUBJECT:
      case DESCRIPTION:
        if (trimmedValue.isEmpty()) {
          throw new IllegalArgumentException(property + " cannot be empty");
        }
        break;

      case START_DATE_TIME:
      case END_DATE_TIME:
        parseDateTime(trimmedValue);
        break;

      case LOCATION:
        validateEnum(Location.class, trimmedValue, "location");
        break;

      case STATUS:
        validateEnum(Status.class, trimmedValue, "status");
        break;

      default:
        throw new IllegalArgumentException("Unknown property: " + property);
    }
  }

  /**
   * Validates that a string value is a valid enum constant.
   *
   * @param enumClass the enum class
   * @param value     the string value to validate
   * @param type      the type name for error messages
   * @param <E>       the enum type
   * @throws IllegalArgumentException if the value is not a valid enum constant
   */
  public static <E extends Enum<E>> void validateEnum(Class<E> enumClass, String value,
      String type) {
    try {
      Enum.valueOf(enumClass, value.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid " + type + ": " + value);
    }
  }

  /**
   * Removes surrounding quotes from a string if present.
   *
   * @param str the string to process
   * @return the string with quotes removed, or the original string if no quotes
   */
  public static String removeQuotes(String str) {
    if (str.startsWith("\"") && str.endsWith("\"") && str.length() >= 2) {
      return str.substring(1, str.length() - 1);
    }
    return str;
  }
}