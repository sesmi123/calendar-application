import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CalendarRunner.
 */
public class CalendarRunnerTest {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;
  private File tempCommandFile;

  /**
   * Set up.
   */
  @Before
  public void setUp() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  /**
   * Tear down.
   */
  @After
  public void tearDown() {
    System.setOut(originalOut);
    System.setErr(originalErr);
    if (tempCommandFile != null && tempCommandFile.exists()) {
      tempCommandFile.delete();
    }
  }

  @Test
  public void testOneArgumentOnly() {
    CalendarRunner.main(new String[]{"--mode"});

    String errOutput = errContent.toString();
    assertTrue(errOutput.contains("Usage:"));
  }

  @Test
  public void testInteractiveMode() throws IOException {
    tempCommandFile = File.createTempFile("commands", ".txt");
    try (FileWriter writer = new FileWriter(tempCommandFile)) {
      writer.write("quit\n");
    }

    System.setIn(new FileInputStream(tempCommandFile));

    CalendarRunner.main(new String[]{"--mode", "interactive"});

    String errOutput = errContent.toString();
    assertFalse(errOutput.contains("Error"));
  }

  @Test
  public void testHeadlessMode() throws IOException {
    tempCommandFile = File.createTempFile("commands", ".txt");
    try (FileWriter writer = new FileWriter(tempCommandFile)) {
      writer.write("quit\n");
    }

    CalendarRunner.main(new String[]{"--mode", "headless", tempCommandFile.getAbsolutePath()});

    String errOutput = errContent.toString();
    assertFalse(errOutput.contains("Error"));
  }

  @Test
  public void testHeadlessModeWithoutFilePath() {
    CalendarRunner.main(new String[]{"--mode", "headless"});

    String errOutput = errContent.toString();
    assertTrue(errOutput.contains("Headless mode requires a command file path"));
  }

  @Test
  public void testHeadlessModeWithNonExistentFile() {
    CalendarRunner.main(new String[]{"--mode", "headless", "/nonexistent/file.txt"});

    String errOutput = errContent.toString();
    assertTrue(errOutput.contains("Error reading command file"));
  }

  @Test
  public void testUnknownModeDisplaysError() throws IOException {

    CalendarRunner.main(new String[]{"--mode", "unknown"});

    String errOutput = errContent.toString();
    assertTrue(errOutput.contains("Unknown mode:"));
    assertTrue(errOutput.contains("Usage:"));
  }

  @Test
  public void testInteractiveModeUpperCase() throws IOException {
    tempCommandFile = File.createTempFile("commands", ".txt");
    try (FileWriter writer = new FileWriter(tempCommandFile)) {
      writer.write("quit\n");
    }

    System.setIn(new FileInputStream(tempCommandFile));

    CalendarRunner.main(new String[]{"--mode", "INTERACTIVE"});

    String errOutput = errContent.toString();
    assertFalse(errOutput.contains("Error"));
    assertFalse(errOutput.contains("Unknown mode"));
  }

  @Test
  public void testHeadlessModeUpperCase() throws IOException {
    tempCommandFile = File.createTempFile("commands", ".txt");
    try (FileWriter writer = new FileWriter(tempCommandFile)) {
      writer.write("quit\n");
    }

    CalendarRunner.main(new String[]{"--mode", "HEADLESS", tempCommandFile.getAbsolutePath()});

    String errOutput = errContent.toString();
    assertFalse(errOutput.contains("Error reading command file"));
  }

  @Test
  public void testHeadlessModeMixedCase() throws IOException {
    tempCommandFile = File.createTempFile("commands", ".txt");
    try (FileWriter writer = new FileWriter(tempCommandFile)) {
      writer.write("quit\n");
    }

    CalendarRunner.main(new String[]{"--mode", "HeAdLeSs", tempCommandFile.getAbsolutePath()});

    String errOutput = errContent.toString();
    assertFalse(errOutput.contains("Error reading command file"));
  }

  @Test
  public void testHeadlessModeExecutesCommands() throws IOException {
    tempCommandFile = File.createTempFile("commands", ".txt");
    try (FileWriter writer = new FileWriter(tempCommandFile)) {
      writer.write("create-event \"Test Event\"\n");
      writer.write("schedule-event \"Test Event\" 2025-11-10\n");
      writer.write("display-calendar 2025-11\n");
      writer.write("quit\n");
    }

    CalendarRunner.main(new String[]{"--mode", "headless", tempCommandFile.getAbsolutePath()});

    String output = outContent.toString();
    assertTrue(output.length() > 0);
  }

  @Test
  public void testHeadlessModeWithEmptyFile() throws IOException {
    tempCommandFile = File.createTempFile("commands", ".txt");

    CalendarRunner.main(new String[]{"--mode", "headless", tempCommandFile.getAbsolutePath()});

    String errOutput = errContent.toString();
    assertFalse(errOutput.contains("Error reading command file"));
  }

  @Test
  public void testHeadlessModeWithInvalidCommand() throws IOException {
    tempCommandFile = File.createTempFile("commands", ".txt");
    try (FileWriter writer = new FileWriter(tempCommandFile)) {
      writer.write("invalid-command\n");
      writer.write("quit\n");
    }

    CalendarRunner.main(new String[]{"--mode", "headless", tempCommandFile.getAbsolutePath()});

    String output = outContent.toString() + errContent;
    assertTrue(output.length() > 0);
  }

  @Test
  public void testMultipleSpacesInArguments() throws IOException {
    tempCommandFile = File.createTempFile("commands", ".txt");
    try (FileWriter writer = new FileWriter(tempCommandFile)) {
      writer.write("quit\n");
    }

    CalendarRunner.main(new String[]{"--mode", "headless", tempCommandFile.getAbsolutePath()});

    String errOutput = errContent.toString();
    assertFalse(errOutput.contains("Error reading command file"));
  }

  @Test
  public void testHeadlessModeFilePathWithSpaces() throws IOException {
    File parentDir = new File(System.getProperty("java.io.tmpdir"), "test dir");
    parentDir.mkdir();
    parentDir.deleteOnExit();

    tempCommandFile = new File(parentDir, "commands test.txt");
    try (FileWriter writer = new FileWriter(tempCommandFile)) {
      writer.write("quit\n");
    }

    CalendarRunner.main(new String[]{"--mode", "headless", tempCommandFile.getAbsolutePath()});

    String errOutput = errContent.toString();
    assertFalse(errOutput.contains("Error reading command file"));

    parentDir.delete();
  }

  @Test
  public void testExtraArgumentsIgnored() throws IOException {
    tempCommandFile = File.createTempFile("commands", ".txt");
    try (FileWriter writer = new FileWriter(tempCommandFile)) {
      writer.write("quit\n");
    }

    CalendarRunner.main(new String[]{"--mode", "headless",
        tempCommandFile.getAbsolutePath(), "extra", "args"});

    String errOutput = errContent.toString();
    assertFalse(errOutput.contains("Error reading command file"));
  }

  @Test
  public void testModeArgumentCaseSensitivity() throws IOException {
    tempCommandFile = File.createTempFile("commands", ".txt");
    try (FileWriter writer = new FileWriter(tempCommandFile)) {
      writer.write("quit\n");
    }

    System.setIn(new FileInputStream(tempCommandFile));

    CalendarRunner.main(new String[]{"--mode", "InTeRaCtIvE"});

    String errOutput = errContent.toString();
    assertFalse(errOutput.contains("Unknown mode"));
  }
}