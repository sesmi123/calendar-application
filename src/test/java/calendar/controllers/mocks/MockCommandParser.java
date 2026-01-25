package calendar.controllers.mocks;

import calendar.controllers.CommandParser;
import calendar.controllers.commands.Command;

/**
 * Mock of command parser.
 */
public class MockCommandParser implements CommandParser {

  private final int uniqueCode;
  private final StringBuilder log;

  /**
   * Instatiate a mock command parser.
   *
   * @param log        dummy logs
   * @param uniqueCode unique code
   */
  public MockCommandParser(StringBuilder log, int uniqueCode) {
    this.log = log;
    this.uniqueCode = uniqueCode;
  }

  @Override
  public Command parse(String command) throws IllegalArgumentException {
    log.append(command);
    if (command.equals("invalid command")) {
      throw new IllegalArgumentException(command);
    }
    return new controllers.mocks.MockCommand(log, 7661827);
  }
}

