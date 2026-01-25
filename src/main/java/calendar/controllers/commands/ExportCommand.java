package calendar.controllers.commands;

import calendar.controllers.exporters.CalendarExporter;
import calendar.models.ObservableCalendar;
import calendar.views.ObservableView;
import java.io.IOException;
import java.util.Objects;

/**
 * Handles the command to export calendar events to a file.
 */
public class ExportCommand implements Command {

  private final CalendarExporter exporter;
  private final ObservableCalendar model;
  private final ObservableView view;
  private final String fileName;

  /**
   * Initialize an export command executor.
   *
   * @param fileName file name to export the events to
   */
  public ExportCommand(CalendarExporter exporter, ObservableCalendar model, ObservableView view,
      String fileName) {
    this.exporter = Objects.requireNonNull(exporter);
    this.model = Objects.requireNonNull(model);
    this.view = Objects.requireNonNull(view);
    this.fileName = Objects.requireNonNull(fileName);
  }

  @Override
  public void execute() {
    try {
      String filepath = exporter.export(model, this.fileName);
      view.displaySuccess("Calendar " + model.getTitle() + " exported successfully to "
          + filepath);
    } catch (IOException e) {
      view.displayError("Failed to export events: " + e.getMessage());
    }
  }
}
