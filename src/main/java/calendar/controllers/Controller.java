package calendar.controllers;

import calendar.views.GuiView;

/**
 * Represents a controller in the calendar application that handles user interactions and
 * coordinates between the model and the view.
 *
 * <p>Implementations of this interface manage the main execution flow, processing
 * commands from the user (interactive mode) or from a command file (headless mode).
 * </p>
 */
public interface Controller {

  /**
   * Starts the controller and runs the main application loop.
   *
   * <p>In interactive mode, this method typically reads commands from the user
   * and executes them until an exit command is received. In headless mode, it reads commands from a
   * provided input source and executes them sequentially.
   * </p>
   */
  void go();

  /**
   * Set the view and provide it with all the callbacks.
   *
   * @param view the given view to be set
   */
  void setView(GuiView view);
}

