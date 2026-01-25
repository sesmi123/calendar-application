package calendar.controllers.commands;

/**
 * Represents a command that can be executed on a calendar model with a view.
 *
 * <p>Implementations of this interface encapsulate a specific action that can be
 * performed in the context of the calendar application, such as creating, editing, or querying
 * events. This follows the Command design pattern.
 * </p>
 */
@FunctionalInterface
public interface Command {

  /**
   * Executes the command.
   */
  void execute();
}
