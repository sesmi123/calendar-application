package calendar.controllers.exporters;

import calendar.models.Event;
import calendar.models.ObservableCalendar;
import calendar.models.enums.Status;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * Exports calendar events into an iCalendar (.ical) file format.
 */
public class IcalCalendarExporter extends AbstractCalendarExporter {

  private static final DateTimeFormatter ICS_DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
  private static final String ICALENDAR_VERSION = "2.0";
  private static final String PRODID = "-//Calendar Application//EN";

  @Override
  protected String getFileExtension() {
    return ".ical";
  }

  @Override
  protected void writeContent(ObservableCalendar calendar, BufferedWriter writer)
      throws IOException {
    writeCalendarHeader(writer, calendar);
    writeEvents(calendar, writer);
    writeCalendarFooter(writer);
  }

  /**
   * Writes the iCalendar header.
   */
  private void writeCalendarHeader(BufferedWriter writer, ObservableCalendar calendar)
      throws IOException {
    writeLine(writer, "BEGIN:VCALENDAR");
    writeLine(writer, "VERSION:" + ICALENDAR_VERSION);
    writeLine(writer, "PRODID:" + PRODID);
  }

  /**
   * Writes all events to the iCalendar file.
   */
  private void writeEvents(ObservableCalendar calendar, BufferedWriter writer)
      throws IOException {
    Set<Event> events = calendar.getAllEvents();

    for (Event event : events) {
      writeEvent(event, calendar.getTimezone(), writer);
    }
  }

  /**
   * Writes a single event as a VEVENT component.
   */
  private void writeEvent(Event event, ZoneId timezone, BufferedWriter writer) throws IOException {
    writeLine(writer, "BEGIN:VEVENT");

    String uid = generateUid(event);
    writeLine(writer, "UID:" + uid);

    String timestamp = LocalDateTime.now().format(ICS_DATE_TIME_FORMAT);
    writeLine(writer, "DTSTAMP:" + timestamp);

    LocalDateTime startDateTime = event.getStartDateTime();

    writeLine(writer, "DTSTART:" + startDateTime.atZone(timezone)
        .withZoneSameInstant(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")));

    LocalDateTime endDateTime = event.getEndDateTime();
    writeLine(writer, "DTEND:" + endDateTime.atZone(timezone)
        .withZoneSameInstant(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")));

    writeLine(writer, "SUMMARY:" + escapeText(event.getSubject()));

    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      writeLine(writer, "DESCRIPTION:" + escapeText(event.getDescription()));
    }

    if (event.getLocation() != null) {
      writeLine(writer, "LOCATION:" + escapeText(event.getLocation().toString()));
    }

    String classValue = (event.getStatus() == Status.PRIVATE) ? "PRIVATE" : "PUBLIC";
    writeLine(writer, "CLASS:" + classValue);

    writeLine(writer, "STATUS:CONFIRMED");

    writeLine(writer, "TRANSP:OPAQUE");

    writeLine(writer, "END:VEVENT");
  }

  private String generateUid(Event event) {
    String uniqueString = event.getSubject()
        + event.getStartDate().toString()
        + event.getStartTime().toString();

    return UUID.nameUUIDFromBytes(uniqueString.getBytes(StandardCharsets.UTF_8))
        + "@double-dispatch-calendar";
  }

  /**
   * Writes the iCalendar footer.
   */
  private void writeCalendarFooter(BufferedWriter writer) throws IOException {
    writeLine(writer, "END:VCALENDAR");
  }

  /**
   * Escapes special characters in text.
   *
   * @param text the text to escape
   * @return escaped text
   */
  private String escapeText(String text) {
    if (text == null) {
      return "";
    }

    return text.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\n", "\\n")
        .replace("\r", "");
  }

  /**
   * Writes a line with proper line ending.
   *
   * @param writer the writer
   * @param line   the line to write
   */
  private void writeLine(BufferedWriter writer, String line) throws IOException {
    writer.write(line);
    writer.write(System.lineSeparator());
  }
}