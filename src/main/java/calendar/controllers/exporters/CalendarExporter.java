package calendar.controllers.exporters;

import calendar.models.ObservableCalendar;
import java.io.IOException;

/**
 * Interface for exporting calendar data to various file formats.
 */
@FunctionalInterface
public interface CalendarExporter {

  /**
   * Exports the calendar’s contents to a file.
   *
   * <p>Implementations should ensure that the exported file path and format are
   * platform-independent. The format (e.g., text, iCalendar) is determined by the implementing
   * class.
   * </p>
   *
   * @param calendar the observable calendar from which events need to be exported
   * @param fileName the name or path of the file to export to; must not be {@code null}.
   * @return Absolute file path
   * @throws IOException if writing to file fails
   */
  String export(ObservableCalendar calendar, String fileName) throws IOException;
}
