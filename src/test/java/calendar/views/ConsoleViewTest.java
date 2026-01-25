package calendar.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import calendar.models.Event;
import calendar.models.EventImpl;
import calendar.models.enums.Location;
import calendar.models.enums.Status;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ConsoleView}.
 */
public class ConsoleViewTest {

  private static final Pattern ANSI_PATTERN = Pattern.compile("\u001B\\[[;\\d]*m");
  private ByteArrayOutputStream outContent;
  private ConsoleView view;

  /**
   * Test set up.
   */
  @Before
  public void setUp() {
    outContent = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(outContent);
    view = new ConsoleView(printStream);
  }

  private String stripAnsi(String s) {
    return ANSI_PATTERN.matcher(s).replaceAll("");
  }

  @Test
  public void testDisplaySuccessPrintsMessage() {
    view.displaySuccess("Operation successful");
    String output = stripAnsi(outContent.toString()).trim();
    assertEquals("Operation successful", output);
  }

  @Test
  public void testDisplayErrorPrintsMessage() {
    view.displayError("Something went wrong");
    String output = stripAnsi(outContent.toString()).trim();
    assertEquals("Something went wrong", output);
  }

  @Test
  public void testDisplayStatusBusyPrintsBusyMessage() {
    view.displayStatus(true);
    String output = stripAnsi(outContent.toString()).trim();
    assertEquals("Status: Busy", output);
  }

  @Test
  public void testDisplayStatusAvailablePrintsAvailableMessage() {
    view.displayStatus(false);
    String output = stripAnsi(outContent.toString()).trim();
    assertEquals("Status: Available", output);
  }

  @Test
  public void testDisplayEventsPrintsEachEvent() {
    Event event = EventImpl.getBuilder()
        .subject("Team Meeting")
        .from(LocalDate.of(2025, 11, 5), LocalTime.of(9, 0))
        .to(LocalDate.of(2025, 11, 5), LocalTime.of(10, 0))
        .location(Location.ONLINE)
        .status(Status.PUBLIC)
        .build();

    Set<Event> events = new TreeSet<>(Collections.singletonList(event));
    view.displayEventsOn(events);

    String output = stripAnsi(outContent.toString()).trim();
    assertTrue(output.contains("- " + event.toString()));
  }

  @Test
  public void testDisplayEventsWithEmptySet() {
    Set<Event> events = Collections.emptySet();
    view.displayEventsOn(events);
    String output = stripAnsi(outContent.toString()).trim();
    assertTrue(output.contains("- No events"));
  }
}
