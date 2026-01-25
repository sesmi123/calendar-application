package calendar.models.enums;

/**
 * The list of editable properties on an event.
 */
public enum EventProperty {
  SUBJECT("subject"),
  START_DATE_TIME("start"),
  END_DATE_TIME("end"),
  DESCRIPTION("description"),
  LOCATION("location"),
  STATUS("status");

  private final String value;

  EventProperty(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.value;
  }
}
