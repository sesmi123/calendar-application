package calendar.controllers.mocks;

import calendar.controllers.exporters.CalendarExporter;
import calendar.models.ObservableCalendar;
import java.io.IOException;

/**
 * Mock calendar exporter.
 */
public class MockCalendarExporter implements CalendarExporter {

  @Override
  public String export(ObservableCalendar calendar, String fileName) throws IOException {
    if (fileName.equals("illegalFile")) {
      throw new IOException("mock exception");
    }
    return fileName;
  }
}
