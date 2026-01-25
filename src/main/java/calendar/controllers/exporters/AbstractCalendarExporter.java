package calendar.controllers.exporters;

import calendar.models.ObservableCalendar;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Abstract base class for calendar exporters providing common file handling and export structure.
 */
public abstract class AbstractCalendarExporter implements CalendarExporter {

  /**
   * File extension expected by the exporter (e.g., .ical, .csv).
   */
  protected abstract String getFileExtension();

  /**
   * Writes the file content specific to the export format.
   */
  protected abstract void writeContent(ObservableCalendar calendar, BufferedWriter writer)
      throws IOException;

  @Override
  public String export(ObservableCalendar calendar, String filePath) throws IOException {
    Path path = validateAndNormalizePath(filePath);
    createParentDirectories(path);

    try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      writeContent(calendar, writer);
    }
    return path.toAbsolutePath().toString();
  }

  /**
   * Validates and normalizes the file path for the current platform.
   *
   * @param filePath the input file path
   * @return normalized Path object
   * @throws IOException if path is invalid
   */
  protected Path validateAndNormalizePath(String filePath) throws IOException {
    try {
      Path path = Paths.get(filePath);
      path = path.normalize();

      if (!path.toString().toLowerCase().endsWith(getFileExtension())) {
        path = Paths.get(path + getFileExtension());
      }

      return path;
    } catch (InvalidPathException e) {
      throw new IOException("Invalid file path: " + filePath, e);
    }
  }

  /**
   * Creates parent directories if they don't exist.
   */
  protected void createParentDirectories(Path path) throws IOException {
    Path parent = path.getParent();
    if (parent != null && !Files.exists(parent)) {
      Files.createDirectories(parent);
    }
  }
}
