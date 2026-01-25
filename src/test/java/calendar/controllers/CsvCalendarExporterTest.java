package calendar.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.controllers.exporters.CsvCalendarExporter;
import calendar.models.Calendar;
import calendar.models.CalendarImpl;
import calendar.models.Event;
import calendar.models.EventImpl;
import calendar.models.EventSeries;
import calendar.models.EventSeriesImpl;
import calendar.models.RecurrenceRule;
import calendar.models.RecurrenceRuleImpl;
import calendar.models.enums.Location;
import calendar.models.enums.Status;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for exporting events to CSV.
 */
public class CsvCalendarExporterTest {

  private CsvCalendarExporter exporter;
  private Calendar calendar;
  private File tempFile;

  /**
   * Setup test.
   *
   * @throws IOException if file operation fails
   */
  @Before
  public void setUp() throws IOException {
    exporter = new CsvCalendarExporter();
    calendar = new CalendarImpl("Test Calendar", ZoneId.of("America/New_York"));

    tempFile = File.createTempFile("calendar_export", ".csv");
    tempFile.deleteOnExit();
  }

  /**
   * Teardown after test execution.
   */
  @After
  public void tearDown() {
    tempFile.delete();
  }

  @Test
  public void testExportSingleEvent() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Team Meeting")
        .from(LocalDate.of(2025, 10, 30), LocalTime.of(10, 0))
        .to(LocalDate.of(2025, 10, 30), LocalTime.of(11, 0))
        .description("Weekly sync-up")
        .location(Location.ONLINE)
        .status(Status.PRIVATE)
        .build();

    calendar.addEvent(event);

    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = new String(Files.readAllBytes(tempFile.toPath()));
    assertTrue(content.contains(
        "Subject,Start Date,Start Time,End Date,End Time,"
            + "All Day Event,Description,Location,Private"));
    assertTrue(content.contains("\"Team Meeting\""));
    assertTrue(content.contains("10/30/2025"));
    assertTrue(content.contains("10:00 AM"));
    assertTrue(content.contains("11:00 AM"));
    assertTrue(content.contains("\"Weekly sync-up\""));
    assertTrue(content.contains("\"ONLINE\""));
    assertTrue(content.contains("True"));
    String expected = "\"Team Meeting\",10/30/2025,10:00 AM,10/30/2025,11:00 AM,False,"
        + "\"Weekly sync-up\",\"ONLINE\",True";
    assertTrue(content.contains(expected));

  }

  @Test
  public void testExportEventWithNullsAndQuotes() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Important \"Event\"")
        .from(LocalDate.of(2025, 11, 1), LocalTime.of(9, 30))
        .to(LocalDate.of(2025, 11, 1), LocalTime.of(10, 30))
        .description(null)
        .location(null)
        .status(Status.PUBLIC)
        .build();

    calendar.addEvent(event);

    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = new String(Files.readAllBytes(tempFile.toPath()));
    assertTrue(content.contains("\"Important \"\"Event\"\"\""));
    assertTrue(content.contains("False"));
    String expected = "\"Important \"\"Event\"\"\",11/01/2025,09:30 AM,11/01/2025,10:30 AM,"
        + "False,,,False";
    assertTrue(content.contains(expected));
  }

  @Test
  public void testExportMultipleEvents() throws IOException {
    Event e1 = EventImpl.getBuilder()
        .subject("Event 1")
        .on(LocalDate.of(2025, 10, 20))
        .build();
    Event e2 = EventImpl.getBuilder()
        .subject("Event 2")
        .on(LocalDate.of(2025, 10, 21))
        .build();

    calendar.addEvent(e1);
    calendar.addEvent(e2);

    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = new String(Files.readAllBytes(tempFile.toPath()));
    assertTrue(content.contains("\"Event 1\""));
    assertTrue(content.contains("\"Event 2\""));
  }

  @Test
  public void testExportEventSeries() throws IOException {
    LocalDate date = LocalDate.of(2025, 10, 29);
    Event seriesEvent = EventImpl.getBuilder()
        .subject("Daily Standup")
        .from(date, LocalTime.of(9, 0))
        .to(date, LocalTime.of(9, 15))
        .build();
    Set<DayOfWeek> days = Set.of(DayOfWeek.THURSDAY);

    RecurrenceRule rule = new RecurrenceRuleImpl(days, LocalDate.of(2025, 11, 7));
    EventSeries series = new EventSeriesImpl(seriesEvent, date, rule);
    calendar.addEventSeries(series);
    exporter.export(calendar, tempFile.getAbsolutePath());
    String content = new String(Files.readAllBytes(tempFile.toPath()));

    assertTrue(content.contains("10/30/2025"));
    assertTrue(content.contains("11/06/2025"));
  }

  @Test
  public void testExportEmptyCalendarWritesOnlyHeader() throws IOException {
    exporter.export(calendar, tempFile.getAbsolutePath());
    String content = Files.readString(tempFile.toPath());
    assertTrue(content.trim().startsWith("Subject,Start Date"));
    assertEquals(1, content.lines().count());
  }

  @Test
  public void testSanitizeHandlesCommasAndNewlines() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Meeting, Planning\nSession")
        .on(LocalDate.of(2025, 11, 2))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());
    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("\"Meeting, Planning\nSession\""));
  }

  @Test
  public void testInvalidFilePathThrowsIoException() {
    String invalidPath = "\0invalid.csv";
    assertThrows(IOException.class, () -> exporter.export(calendar, invalidPath));
  }

  @Test
  public void testAppendsCsvExtensionIfMissing() throws IOException {
    File noExtFile = new File(tempFile.getParentFile(), "calendar_output");
    noExtFile.deleteOnExit();

    exporter.export(calendar, noExtFile.getAbsolutePath());
    File expectedFile = new File(noExtFile.getAbsolutePath() + ".csv");

    assertTrue(expectedFile.exists());
    expectedFile.delete();
  }


  @Test
  public void testPrivateVsPublicEventsCsv() throws IOException {
    Event privateEvent = EventImpl.getBuilder()
        .subject("Private Event")
        .on(LocalDate.of(2025, 11, 5))
        .status(Status.PRIVATE)
        .build();
    Event publicEvent = EventImpl.getBuilder()
        .subject("Public Event")
        .on(LocalDate.of(2025, 11, 6))
        .status(Status.PUBLIC)
        .build();

    calendar.addEvent(privateEvent);
    calendar.addEvent(publicEvent);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("\"Private Event\""));
    assertTrue(content.contains("\"Public Event\""));
    assertTrue(content.contains("True"));
    assertTrue(content.contains("False"));
  }

  @Test
  public void testCreateParentDirectoriesIfNotExist() throws IOException {
    File newDir = new File(tempFile.getParentFile(), "newdir/subdir");
    File outputFile = new File(newDir, "calendar.csv");
    outputFile.deleteOnExit();
    newDir.deleteOnExit();

    Event event = EventImpl.getBuilder()
        .subject("Test Event")
        .on(LocalDate.of(2025, 11, 1))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, outputFile.getAbsolutePath());

    assertTrue(outputFile.exists());
    assertTrue(outputFile.getParentFile().exists());

    outputFile.delete();
    outputFile.getParentFile().delete();
    outputFile.getParentFile().getParentFile().delete();
  }

  @Test
  public void testPathNormalization() throws IOException {
    String unnormalizedPath = tempFile.getParent() + File.separator + "." + File.separator
        + ".." + File.separator + tempFile.getParentFile().getName() + File.separator
        + "normalized_output.csv";

    Event event = EventImpl.getBuilder()
        .subject("Normalization Test")
        .on(LocalDate.of(2025, 11, 1))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, unnormalizedPath);

    File normalizedFile = new File(tempFile.getParent(), "normalized_output.csv");
    assertTrue(normalizedFile.exists());
    normalizedFile.delete();
  }

  @Test
  public void testExportWithEmptyStrings() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("")
        .from(LocalDate.of(2025, 11, 1), LocalTime.of(10, 0))
        .to(LocalDate.of(2025, 11, 1), LocalTime.of(11, 0))
        .description("")
        .status(Status.PUBLIC)
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("\"\""));
  }

  @Test
  public void testExportWithSpecialCharactersInDescription() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Special Chars")
        .on(LocalDate.of(2025, 11, 1))
        .description("Line1\nLine2\rLine3\r\nComma,Semicolon;Quote\"End")
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("\"Line1\nLine2\rLine3\r\nComma,Semicolon;Quote\"\"End\""));
  }

  @Test
  public void testExportWithMultipleConsecutiveQuotes() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("\"\"\"Triple Quotes\"\"\"")
        .on(LocalDate.of(2025, 11, 1))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("\"\"\"\"\"\"Triple Quotes\"\"\"\"\"\"\""));
  }

  @Test
  public void testExportAllDayEventField() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Regular Event")
        .from(LocalDate.of(2025, 11, 1), LocalTime.of(9, 0))
        .to(LocalDate.of(2025, 11, 1), LocalTime.of(17, 0))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    String[] lines = content.split("\n");
    for (int i = 1; i < lines.length; i++) {
      String[] fields = lines[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
      if (fields.length > 5) {
        assertEquals("False", fields[5]);
      }
    }
  }

  @Test
  public void testExportWithMidnightTimes() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Midnight Event")
        .from(LocalDate.of(2025, 11, 1), LocalTime.MIDNIGHT)
        .to(LocalDate.of(2025, 11, 1), LocalTime.of(1, 0))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("12:00 AM"));
  }

  @Test
  public void testExportWithNoonTime() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Noon Event")
        .from(LocalDate.of(2025, 11, 1), LocalTime.NOON)
        .to(LocalDate.of(2025, 11, 1), LocalTime.of(13, 0))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("12:00 PM"));
  }

  @Test
  public void testExportMultiDayEvent() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Conference")
        .from(LocalDate.of(2025, 11, 1), LocalTime.of(9, 0))
        .to(LocalDate.of(2025, 11, 3), LocalTime.of(17, 0))
        .description("Three-day event")
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("11/01/2025"));
    assertTrue(content.contains("11/03/2025"));
    assertTrue(content.contains("\"Conference\""));
  }

  @Test
  public void testExportOverwritesExistingFile() throws IOException {
    Event event1 = EventImpl.getBuilder()
        .subject("First Export")
        .on(LocalDate.of(2025, 11, 1))
        .build();

    calendar.addEvent(event1);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String firstContent = Files.readString(tempFile.toPath());
    assertTrue(firstContent.contains("\"First Export\""));

    calendar = new CalendarImpl("New Calendar", ZoneId.of("America/New_York"));
    Event event2 = EventImpl.getBuilder()
        .subject("Second Export")
        .on(LocalDate.of(2025, 11, 2))
        .build();

    calendar.addEvent(event2);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String secondContent = Files.readString(tempFile.toPath());
    assertTrue(secondContent.contains("\"Second Export\""));
    assertFalse(secondContent.contains("\"First Export\""));
  }

  @Test
  public void testExportAllLocations() throws IOException {
    Location[] locations = Location.values();

    for (int i = 0; i < locations.length; i++) {
      Event event = EventImpl.getBuilder()
          .subject("Event " + i)
          .on(LocalDate.of(2025, 11, 1).plusDays(i))
          .location(locations[i])
          .build();
      calendar.addEvent(event);
    }

    exporter.export(calendar, tempFile.getAbsolutePath());
    String content = Files.readString(tempFile.toPath());

    for (Location loc : locations) {
      assertTrue(content.contains("\"" + loc.toString() + "\""));
    }
  }

  @Test
  public void testCsvFileExtensionCaseInsensitive() throws IOException {
    File upperCaseFile = new File(tempFile.getParentFile(), "test.CSV");
    upperCaseFile.deleteOnExit();

    Event event = EventImpl.getBuilder()
        .subject("Case Test")
        .on(LocalDate.of(2025, 11, 1))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, upperCaseFile.getAbsolutePath());

    assertTrue(upperCaseFile.exists());
    upperCaseFile.delete();
  }

  @Test
  public void testExportWithLeadingTrailingSpaces() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("  Spaces Around  ")
        .on(LocalDate.of(2025, 11, 1))
        .description("  Description with spaces  ")
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("\"  Spaces Around  \""));
    assertTrue(content.contains("\"  Description with spaces  \""));
  }

  @Test
  public void testExportPreservesEventOrder() throws IOException {
    for (int i = 1; i <= 5; i++) {
      Event event = EventImpl.getBuilder()
          .subject("Event " + i)
          .on(LocalDate.of(2025, 11, i))
          .build();
      calendar.addEvent(event);
    }

    exporter.export(calendar, tempFile.getAbsolutePath());
    String content = Files.readString(tempFile.toPath());

    for (int i = 1; i <= 5; i++) {
      assertTrue(content.contains("\"Event " + i + "\""));
    }
  }

  @Test
  public void testExportWithVeryLongDescription() throws IOException {
    StringBuilder longDesc = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longDesc.append("Very long description text. ");
    }

    Event event = EventImpl.getBuilder()
        .subject("Long Description Event")
        .on(LocalDate.of(2025, 11, 1))
        .description(longDesc.toString())
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    assertTrue(tempFile.exists());
    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("\"Long Description Event\""));
  }

  @Test
  public void testExportWithFileNameOnly() throws IOException {
    String currentDir = System.getProperty("user.dir");
    String originalDir = currentDir;
    File testFile = new File("test_calendar_no_parent.csv");
    testFile.deleteOnExit();

    try {
      Event event = EventImpl.getBuilder()
          .subject("No Parent Dir")
          .on(LocalDate.of(2025, 11, 1))
          .build();

      calendar.addEvent(event);
      exporter.export(calendar, "test_calendar_no_parent.csv");

      assertTrue(testFile.exists());
      String content = Files.readString(testFile.toPath());
      assertTrue(content.contains("\"No Parent Dir\""));
    } finally {
      testFile.delete();
    }
  }

  @Test
  public void testExportWhenParentDirectoryExists() throws IOException {
    File existingDir = tempFile.getParentFile();
    File outputFile = new File(existingDir, "existing_parent_test.csv");
    outputFile.deleteOnExit();

    Event event = EventImpl.getBuilder()
        .subject("Existing Parent Test")
        .on(LocalDate.of(2025, 11, 1))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, outputFile.getAbsolutePath());

    assertTrue(outputFile.exists());
    outputFile.delete();
  }

}
