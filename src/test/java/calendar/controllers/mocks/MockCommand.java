package controllers.mocks;

import calendar.controllers.commands.Command;

/**
 * Mock for Command classes.
 */
public class MockCommand implements Command {

  private final int uniqueCode;
  private final StringBuilder log;

  /**
   * Instatiate a mock command model.
   *
   * @param log        dummy logs
   * @param uniqueCode unique code
   */
  public MockCommand(StringBuilder log, int uniqueCode) {
    this.log = log;
    this.uniqueCode = uniqueCode;
  }

  @Override
  public void execute() {
    log.append("Execute");
  }
}

