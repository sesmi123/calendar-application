package calendar.controllers;

import calendar.controllers.commands.Command;

/**
 * Parses a string command line into an {@link Command} object.
 *
 * <p>Implementations of this interface are responsible for interpreting user input
 * (or commands from a file in headless mode) and converting it into executable commands for the
 * calendar application.
 * </p>
 */
@FunctionalInterface
public interface CommandParser {

  /**
   * Parses the given command line string and returns an {@link Command} representing the requested
   * action.
   *
   * @param commandLine the input string containing the command to parse
   * @return an {@link Command} representing the parsed command
   * @throws IllegalArgumentException if the command line is invalid or cannot be parsed
   */
  Command parse(String commandLine) throws IllegalArgumentException;
}
