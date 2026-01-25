package calendar.views;

import calendar.controllers.Features;

/**
 * Represents a graphical user interface (GUI) view in the calendar application.
 *
 * <p>This interface extends {@link ObservableView} to allow the view to notify observers
 * about user actions. It also provides a method to add features/callbacks that the view can use to
 * communicate with the controller.
 * </p>
 */
public interface GuiView extends ObservableView {

  /**
   * Add features/callbacks to this view. The view will use these callbacks to communicate user
   * actions back to the controller. Used by GUI views to register callbacks.
   *
   * @param features the features interface containing callbacks
   */
  void addFeatures(Features features);
}
