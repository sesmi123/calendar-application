package calendar.controllers.exporters;

import calendar.models.Event;
import calendar.models.ObservableCalendar;
import calendar.models.enums.Status;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Exports calendar events into a CSV file compatible with Google Calendar import.
 */
public class CsvCalendarExporter extends AbstractCalendarExporter {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");
  private static final String CSV_HEADER =
      "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private";

  @Override
  protected String getFileExtension() {
    return ".csv";
  }

  @Override
  protected void writeContent(ObservableCalendar calendar, BufferedWriter writer)
      throws IOException {
    writer.write(CSV_HEADER);
    writer.newLine();
    writeEvents(calendar, writer);
  }

  private void writeEvents(ObservableCalendar calendar, BufferedWriter writer) throws IOException {
    Set<Event> events = calendar.getAllEvents();
    for (Event event : events) {
      writeEvent(event, writer);
    }
  }

  private void writeEvent(Event event, BufferedWriter writer) throws IOException {
    String row = String.join(",",
        sanitize(event.getSubject()),
        event.getStartDate().format(DATE_FORMAT),
        event.getStartTime().format(TIME_FORMAT),
        event.getEndDate().format(DATE_FORMAT),
        event.getEndTime().format(TIME_FORMAT),
        "False",
        sanitize(event.getDescription()),
        event.getLocation() != null ? sanitize(event.getLocation().toString()) : "",
        (event.getStatus() == Status.PRIVATE) ? "True" : "False"
    );
    writer.write(row);
    writer.newLine();
  }

  private String sanitize(String s) {
    if (s == null) {
      return "";
    }
    return "\"" + s.replace("\"", "\"\"") + "\"";
  }
}
