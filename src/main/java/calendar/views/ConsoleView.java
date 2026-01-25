package calendar.views;

import calendar.models.Event;
import java.io.IOException;
import java.util.Set;

/**
 * Console-based implementation of {@link ObservableView} that displays messages and events to a
 * provided {@link Appendable}.
 */
public class ConsoleView implements ObservableView {

  private static final String RESET = "\u001B[0m";
  private static final String BLUE = "\u001B[34m";
  private static final String PINK = "\u001B[95m";
  private static final String YELLOW = "\u001B[33m";
  private final Appendable out;

  /**
   * Constructs a {@code ConsoleView} that writes to the given {@link Appendable}.
   *
   * @param out the output stream to write console messages to
   */
  public ConsoleView(Appendable out) {
    this.out = out;
  }

  @Override
  public void displaySuccess(String message) {
    safeAppend(BLUE + message + RESET + System.lineSeparator());
  }

  @Override
  public void displayError(String message) {
    safeAppend(PINK + message + RESET + System.lineSeparator());
  }

  @Override
  public void displayEventsOn(Set<Event> events) {
    printEvents(events);
  }

  @Override
  public void displayEventsInRange(Set<Event> events) {
    printEvents(events);
  }

  @Override
  public void displayStatus(boolean isBusy) {
    if (isBusy) {
      safeAppend(PINK + "Status: Busy" + RESET + System.lineSeparator());
      return;
    }
    safeAppend(BLUE + "Status: Available" + RESET + System.lineSeparator());
  }

  private void printEvents(Set<Event> events) {
    if (events.isEmpty()) {
      safeAppend(YELLOW + "- No events" + RESET + System.lineSeparator());
    }
    for (Event event : events) {
      safeAppend(YELLOW + "- " + event + RESET + System.lineSeparator());
    }
  }


  private void safeAppend(String text) {
    try {
      out.append(text);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to append output", e);
    }
  }

}
