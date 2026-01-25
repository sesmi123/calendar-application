package calendar.controllers.mocks;

import calendar.controllers.Features;
import calendar.models.Event;
import calendar.views.GuiView;
import java.util.Set;

/**
 * A mock view for testing.
 */
public class MockView implements GuiView {

  private final int uniqueCode;
  private final StringBuilder log;

  /**
   * Instatiate a mock view.
   *
   * @param log        dummy logs
   * @param uniqueCode unique code
   */
  public MockView(StringBuilder log, int uniqueCode) {
    this.log = log;
    this.uniqueCode = uniqueCode;
  }

  @Override
  public void displaySuccess(String message) {
    log.append("Success: " + message + System.lineSeparator());
  }

  @Override
  public void displayError(String message) {
    log.append("Error: " + message + System.lineSeparator());
  }

  @Override
  public void displayEventsOn(Set<Event> events) {
    log.append("Display Events On" + System.lineSeparator());
  }

  @Override
  public void displayEventsInRange(Set<Event> events) {
    log.append("Display Events In Range" + System.lineSeparator());
  }

  @Override
  public void displayStatus(boolean status) {
    log.append("Display Status" + System.lineSeparator());
  }

  @Override
  public void addFeatures(Features features) {
    log.append("Added features" + System.lineSeparator());
  }
}
