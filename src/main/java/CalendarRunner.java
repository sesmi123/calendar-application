import calendar.controllers.CalendarManager;
import calendar.controllers.CalendarManagerImpl;
import calendar.controllers.CommandFactory;
import calendar.controllers.CommandFactoryImpl;
import calendar.controllers.CommandParser;
import calendar.controllers.CommandParserImpl;
import calendar.controllers.Controller;
import calendar.controllers.ControllerImpl;
import calendar.controllers.commands.Command;
import calendar.views.ConsoleView;
import calendar.views.GuiView;
import calendar.views.ObservableView;
import calendar.views.SwingGuiView;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.ZoneId;
import javax.swing.SwingUtilities;

/**
 * The entry point of the Calendar application.
 *
 * <p>This class initializes and runs the calendar program in either {@code interactive},
 * {@code headless} or {@code gui} mode based on command-line arguments.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 *   java -jar {JAR file} --mode interactive
 *   java -jar {JAR file} --mode headless commands.txt
 *   java -jar {JAR file}
 * </pre>
 *
 * <ul>
 *   <li><b>Interactive mode:</b> Runs the program with user input from the console.</li>
 *   <li><b>Headless mode:</b> Executes commands from a given file without user interaction.</li>
 *   <li><b>GUI mode:</b> Runs the program with a graphical user interface.</li>
 * </ul>
 */
public class CalendarRunner {

  /**
   * The main method that starts the application in interactive, headless mode or gui mode.
   *
   * @param args command-line arguments. Expected formats:
   *             <ul>
   *               <li>{@code --mode interactive}</li>
   *               <li>{@code --mode headless <commandsFile>}</li>
   *               <li>no arg defaults to GUI mode</li>
   *             </ul>
   */
  public static void main(String[] args) {

    if (args.length == 0) {
      runGui();
      return;
    }

    if (args.length < 2) {
      System.err.println("Invalid arguments!");
      printUsage();
      return;
    }

    String mode = args[1].toLowerCase();

    switch (mode) {
      case "interactive":
        runInteractive();
        break;

      case "headless":
        if (args.length < 3) {
          System.err.println("Headless mode requires a command file path.");
          return;
        }
        runHeadless(args[2]);
        break;

      default:
        System.err.println("Unknown mode: " + mode);
        printUsage();
        break;
    }
  }

  private static void printUsage() {
    System.err.println("Usage:");
    System.err.println("  java <JAR file> --mode interactive");
    System.err.println("  java <JAR file> --mode headless <commandsFile>");
    System.err.println("  java <JAR file>");
  }

  private static void runInteractive() {
    runWithReadable(new InputStreamReader(System.in), "interactive");
  }

  private static void runHeadless(String filePath) {
    try (Reader fileReader = new FileReader(filePath)) {
      runWithReadable(fileReader, "headless");
    } catch (IOException e) {
      System.err.println("Error reading command file: " + e.getMessage());
    }
  }

  private static void runWithReadable(Readable input, String mode) {
    ObservableView view = new ConsoleView(System.out);
    CalendarManager db = new CalendarManagerImpl();
    CommandFactory factory = new CommandFactoryImpl(db, view);
    CommandParser parser = new CommandParserImpl(factory);
    Controller controller = new ControllerImpl(view, input, parser);

    try {
      controller.go();
    } catch (Exception e) {
      System.err.println("Error running in " + mode + " mode: " + e.getMessage());
    }
  }

  private static void runGui() {
    SwingUtilities.invokeLater(() -> {
      CalendarManager db = new CalendarManagerImpl();
      String calendarAppName = "CS5010 Double-Dispatch Calendar";
      GuiView view = new SwingGuiView(calendarAppName);
      CommandFactory factory = new CommandFactoryImpl(db, view);
      String defaultCalName = "Default";
      Command createCalCommand = factory.createCalendar(defaultCalName, ZoneId.systemDefault());
      createCalCommand.execute();
      db.activate(defaultCalName);
      Controller controller = new ControllerImpl(factory, db);
      controller.setView(view);
    });
  }
}