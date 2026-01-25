package calendar.models;


/**
 * Represents a condition used to filter events in a calendar. This functional interface defines a
 * single method, {@link #evaluate(Event)}, which determines whether a given {@link Event} satisfies
 * a particular criterion.
 */
@FunctionalInterface
public interface FilterCondition {

  /**
   * Evaluates whether the specified event satisfies this filter condition.
   *
   * @param event the {@link Event} to test; must not be {@code null}.
   * @return {@code true} if the event matches the condition; {@code false} otherwise.
   * @throws IllegalArgumentException if event is {@code null}.
   */
  boolean evaluate(Event event) throws IllegalArgumentException;
}
