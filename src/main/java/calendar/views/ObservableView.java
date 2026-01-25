package calendar.views;

import calendar.models.Event;
import java.util.Set;

/**
 * Represents the view component in the MVC architecture of the calendar application.
 *
 * <p>The {@code View} interface defines methods for displaying various types of
 * output to the user, such as success and error messages, event listings, and status information.
 * Implementations of this interface handle how information is presented—whether through the
 * console, GUI, or another medium.
 * </p>
 *
 * @see Event
 * @see calendar.controllers.Controller
 */
public interface ObservableView {

  /**
   * Displays a success message to the user.
   *
   * @param message the success message to display; must not be {@code null}.
   */
  void displaySuccess(String message);

  /**
   * Displays an error message to the user.
   *
   * @param message the error message to display; must not be {@code null}.
   */
  void displayError(String message);

  /**
   * Displays a collection of events on a day to the user.
   *
   * @param events the set of {@link Event} objects to display; may be empty but not {@code null}.
   */
  void displayEventsOn(Set<Event> events);

  /**
   * Displays a collection of events between a range of dates to the user.
   *
   * @param events the set of {@link Event} objects to display; may be empty but not {@code null}.
   */
  void displayEventsInRange(Set<Event> events);

  /**
   * Displays the result of a status query, such as whether a calendar time slot is busy.
   *
   * @param status {@code true} if the calendar is busy, {@code false} otherwise.
   */
  void displayStatus(boolean status);
}