package calendar.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.controllers.exporters.IcalCalendarExporter;
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
 * Tests for exporting events to ICal format.
 */
public class IcalCalendarExporterTest {

  private IcalCalendarExporter exporter;
  private Calendar calendar;
  private File tempFile;

  /**
   * Set up.
   *
   * @throws IOException if fail to create temp file
   */
  @Before
  public void setUp() throws IOException {
    exporter = new IcalCalendarExporter();
    calendar = new CalendarImpl("Test Calendar", ZoneId.of("America/New_York"));
    tempFile = File.createTempFile("calendar_export", ".ical");
    tempFile.deleteOnExit();
  }

  /**
   * Tear down.
   */
  @After
  public void tearDown() {
    if (tempFile != null && tempFile.exists()) {
      tempFile.delete();
    }
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

    String content = Files.readString(tempFile.toPath());

    assertTrue(content.contains("BEGIN:VCALENDAR"));
    assertTrue(content.contains("VERSION:2.0"));
    assertTrue(content.contains("END:VCALENDAR"));

    assertTrue(content.contains("BEGIN:VEVENT"));
    assertTrue(content.contains("END:VEVENT"));
    assertTrue(content.contains("SUMMARY:Team Meeting"));
    assertTrue(content.contains("DTSTART:20251030T140000Z"));
    assertTrue(content.contains("DTEND:20251030T150000Z"));
    assertTrue(content.contains("DESCRIPTION:Weekly sync-up"));
    assertTrue(content.contains("LOCATION:ONLINE"));
    assertTrue(content.contains("CLASS:PRIVATE"));
    assertTrue(content.contains("DTSTAMP:"));
    assertTrue(content.contains("UID:"));
  }

  @Test
  public void testUidGeneration() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("UID Test")
        .from(LocalDate.of(2025, 11, 1), LocalTime.of(10, 0))
        .to(LocalDate.of(2025, 11, 1), LocalTime.of(11, 0))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("UID:"));
    assertTrue(content.contains("@double-dispatch-calendar"));

    String uidLine = content.lines()
        .filter(line -> line.startsWith("UID:"))
        .findFirst()
        .orElse("");

    assertFalse(uidLine.isEmpty());
    assertTrue(uidLine.length() > 10);
  }

  @Test
  public void testExportPublicEvent() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Public Meeting")
        .from(LocalDate.of(2025, 11, 1), LocalTime.of(14, 0))
        .to(LocalDate.of(2025, 11, 1), LocalTime.of(15, 0))
        .status(Status.PUBLIC)
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("CLASS:PUBLIC"));
  }

  @Test
  public void testExportEventWithNullDescription() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("No Description Event")
        .from(LocalDate.of(2025, 11, 1), LocalTime.of(9, 0))
        .to(LocalDate.of(2025, 11, 1), LocalTime.of(10, 0))
        .description(null)
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("SUMMARY:No Description Event"));
    assertFalse(content.contains("DESCRIPTION:No Description Event"));
  }

  @Test
  public void testExportEventWithNullLocation() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("No Location Event")
        .from(LocalDate.of(2025, 11, 1), LocalTime.of(9, 0))
        .to(LocalDate.of(2025, 11, 1), LocalTime.of(10, 0))
        .location(null)
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("SUMMARY:No Location Event"));
    int locationCount = content.split("LOCATION:", -1).length - 1;
    assertTrue(locationCount <= 1);
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
    Event e3 = EventImpl.getBuilder()
        .subject("Event 3")
        .on(LocalDate.of(2025, 10, 22))
        .build();

    calendar.addEvent(e1);
    calendar.addEvent(e2);
    calendar.addEvent(e3);

    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());

    int eventCount = content.split("BEGIN:VEVENT", -1).length - 1;
    assertEquals(3, eventCount);

    assertTrue(content.contains("SUMMARY:Event 1"));
    assertTrue(content.contains("SUMMARY:Event 2"));
    assertTrue(content.contains("SUMMARY:Event 3"));
  }

  @Test
  public void testExportEmptyCalendar() throws IOException {
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("BEGIN:VCALENDAR"));
    assertTrue(content.contains("END:VCALENDAR"));
    assertFalse(content.contains("BEGIN:VEVENT"));
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

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("SUMMARY:Daily Standup"));
    assertTrue(content.contains("DTSTART:20251030T130000Z"));
    assertTrue(content.contains("DTEND:20251030T131500Z"));
  }

  @Test
  public void testEscapeSpecialCharacters() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Meeting; with, special\\characters")
        .on(LocalDate.of(2025, 11, 1))
        .description("Line1\nLine2\nLine3")
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("SUMMARY:Meeting\\; with\\, special\\\\characters"));
    assertTrue(content.contains("DESCRIPTION:Line1\\nLine2\\nLine3"));
  }

  @Test
  public void testCrlfLineEndings() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Test Event")
        .on(LocalDate.of(2025, 11, 1))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    byte[] bytes = Files.readAllBytes(tempFile.toPath());
    String content = new String(bytes);

    assertTrue(content.contains(System.lineSeparator()));
  }

  @Test
  public void testAppendIcalExtensionIfMissing() throws IOException {
    File noExtFile = new File(tempFile.getParentFile(), "calendar_output");
    noExtFile.deleteOnExit();

    Event event = EventImpl.getBuilder()
        .subject("Test Event")
        .on(LocalDate.of(2025, 11, 1))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, noExtFile.getAbsolutePath());

    File expectedFile = new File(noExtFile.getAbsolutePath() + ".ical");
    assertTrue(expectedFile.exists());
    expectedFile.delete();
  }

  @Test
  public void testInvalidFilePathThrowsIoException() {
    String invalidPath = "\0invalid.ical";
    assertThrows(IOException.class, () -> exporter.export(calendar, invalidPath));
  }

  @Test
  public void testCreateParentDirectoriesIfNotExist() throws IOException {
    File newDir = new File(tempFile.getParentFile(), "newdir/subdir");
    File outputFile = new File(newDir, "calendar.ical");
    outputFile.deleteOnExit();

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
    String unnormalizedPath = tempFile.getParent() + File.separator + "."
        + File.separator + ".." + File.separator
        + tempFile.getParentFile().getName() + File.separator + "normalized.ical";

    Event event = EventImpl.getBuilder()
        .subject("Normalization Test")
        .on(LocalDate.of(2025, 11, 1))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, unnormalizedPath);

    File normalizedFile = new File(tempFile.getParent(), "normalized.ical");
    assertTrue(normalizedFile.exists());
    normalizedFile.delete();
  }

  @Test
  public void testDeterministicUidGeneration() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Same Event")
        .from(LocalDate.of(2025, 11, 1), LocalTime.of(10, 0))
        .to(LocalDate.of(2025, 11, 1), LocalTime.of(11, 0))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    tempFile.delete();
    tempFile = File.createTempFile("calendar_export2", ".ical");
    tempFile.deleteOnExit();

    Calendar calendar2 = new CalendarImpl("Test Calendar 2", ZoneId.of("America/New_York"));
    Event event2 = EventImpl.getBuilder()
        .subject("Same Event")
        .from(LocalDate.of(2025, 11, 1), LocalTime.of(10, 0))
        .to(LocalDate.of(2025, 11, 1), LocalTime.of(11, 0))
        .build();

    calendar2.addEvent(event2);
    exporter.export(calendar2, tempFile.getAbsolutePath());

    String content1 = Files.readString(tempFile.toPath());
    String content2 = Files.readString(tempFile.toPath());

    String uid1 = content1.lines()
        .filter(line -> line.startsWith("UID:"))
        .findFirst()
        .orElse("");
    String uid2 = content2.lines()
        .filter(line -> line.startsWith("UID:"))
        .findFirst()
        .orElse("");

    assertEquals(uid1, uid2);
  }

  @Test
  public void testRequirediCalendarProperties() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Test Event")
        .on(LocalDate.of(2025, 11, 1))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());

    assertTrue(content.contains("VERSION:2.0"));
    assertTrue(content.contains("PRODID:"));

    assertTrue(content.contains("UID:"));
    assertTrue(content.contains("DTSTAMP:"));
    assertTrue(content.contains("DTSTART:"));
    assertTrue(content.contains("DTEND:"));
    assertTrue(content.contains("SUMMARY:"));
  }

  @Test
  public void testEventStatusConfirmed() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Confirmed Event")
        .on(LocalDate.of(2025, 11, 1))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("STATUS:CONFIRMED"));
  }

  @Test
  public void testTransparencyOpaque() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Busy Event")
        .on(LocalDate.of(2025, 11, 1))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("TRANSP:OPAQUE"));
  }

  @Test
  public void testMidnightTime() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Midnight Event")
        .from(LocalDate.of(2025, 11, 1), LocalTime.MIDNIGHT)
        .to(LocalDate.of(2025, 11, 1), LocalTime.of(1, 0))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("DTSTART:20251101T040000Z"));
  }

  @Test
  public void testMultiDayEvent() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Conference")
        .from(LocalDate.of(2025, 11, 1), LocalTime.of(9, 0))
        .to(LocalDate.of(2025, 11, 3), LocalTime.of(17, 0))
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("DTSTART:20251101T130000Z"));
    assertTrue(content.contains("DTEND:20251103T220000Z"));
  }

  @Test
  public void testAllLocations() throws IOException {
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
      assertTrue(content.contains("LOCATION:" + loc.toString()));
    }
  }

  @Test
  public void testEmptyDescription() throws IOException {
    Event event = EventImpl.getBuilder()
        .subject("Empty Description")
        .on(LocalDate.of(2025, 11, 1))
        .description("")
        .build();

    calendar.addEvent(event);
    exporter.export(calendar, tempFile.getAbsolutePath());

    String content = Files.readString(tempFile.toPath());
    assertTrue(content.contains("SUMMARY:Empty Description"));
  }

  @Test
  public void testIcalExtensionCaseInsensitive() throws IOException {
    File upperCaseFile = new File(tempFile.getParentFile(), "test.ICAL");
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
  public void testExportWithFileNameOnly() throws IOException {
    File testFile = new File("test_calendar_no_parent.ical");
    testFile.deleteOnExit();

    try {
      Event event = EventImpl.getBuilder()
          .subject("No Parent Dir")
          .on(LocalDate.of(2025, 11, 1))
          .build();

      calendar.addEvent(event);
      exporter.export(calendar, "test_calendar_no_parent.ical");

      assertTrue(testFile.exists());
    } finally {
      testFile.delete();
    }
  }
}