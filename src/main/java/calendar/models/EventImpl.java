package calendar.models;

import calendar.models.enums.Location;
import calendar.models.enums.Status;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Implements the Event interface. This class represents a standalone event that can be added to a
 * calendar or a series.
 */
public class EventImpl implements Event {

  private final String subject;
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final LocalTime startTime;
  private final LocalTime endTime;
  private final String description;
  private final Location location;
  private final Status status;
  private final String seriesId;

  private EventImpl(String subject, LocalDate startDate, LocalTime startTime, LocalDate endDate,
      LocalTime endTime, String description, Location location, Status status,
      String seriesId) throws NullPointerException, IllegalArgumentException {

    this.subject = Objects.requireNonNull(subject, "Subject must not be null");
    this.startDate = Objects.requireNonNull(startDate, "Start date must not be null");
    this.startTime = Objects.requireNonNull(startTime, "Start time must not be null");
    this.endDate = Objects.requireNonNull(endDate, "End date must not be null");
    this.endTime = Objects.requireNonNull(endTime, "End time must not be null");

    this.description = description;
    this.location = location;
    this.status = status;
    this.seriesId = seriesId;

    verify();
  }

  /**
   * Get the builder class to create an event.
   *
   * @return an object of the builder class to create an event.
   */
  public static EventBuilder getBuilder() {
    return new EventBuilder();
  }

  private void verify() {
    LocalDateTime startDateTime = LocalDateTime.of(this.startDate, this.startTime);
    LocalDateTime endDateTime = LocalDateTime.of(this.endDate, this.endTime);

    if (!endDateTime.isAfter(startDateTime)) {
      throw new IllegalArgumentException("End date/time must be after start date/time");
    }
  }

  @Override
  public String getSubject() {
    return this.subject;
  }

  @Override
  public LocalDate getStartDate() {
    return this.startDate;
  }

  @Override
  public LocalDateTime getStartDateTime() {
    return LocalDateTime.of(this.startDate, this.startTime);
  }

  @Override
  public LocalDateTime getEndDateTime() {
    return LocalDateTime.of(this.endDate, this.endTime);
  }

  @Override
  public LocalDate getEndDate() {
    return this.endDate;
  }

  @Override
  public LocalTime getStartTime() {
    return this.startTime;
  }

  @Override
  public LocalTime getEndTime() {
    return this.endTime;
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public Location getLocation() {
    return this.location;
  }

  @Override
  public Status getStatus() {
    return this.status;
  }

  @Override
  public String getSeriesId() {
    return this.seriesId;
  }

  @Override
  public boolean isPartOfSeries() {
    return this.seriesId != null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EventImpl)) {
      return false;
    }
    EventImpl other = (EventImpl) o;
    return subject.equalsIgnoreCase(other.subject)
        && startDate.equals(other.startDate)
        && startTime.equals(other.startTime)
        && endDate.equals(other.endDate)
        && endTime.equals(other.endTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject.toLowerCase(), startDate, startTime, endDate, endTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getSubject())
        .append(" starting on ")
        .append(getStartDate())
        .append(" at ")
        .append(getStartTime())
        .append(", ending on ")
        .append(getEndDate())
        .append(" at ")
        .append(getEndTime());
    if (location != null) {
      sb.append(". Location: ").append(location);
    }
    sb.append(System.lineSeparator());
    return sb.toString().trim();
  }


  @Override
  public EventBuilder toBuilder() {
    return new EventBuilder().subject(subject).from(startDate, startTime).to(endDate, endTime)
        .description(description).location(location).status(status).seriesId(seriesId);
  }

  @Override
  public int compareTo(Event other) {
    if (other == null) {
      throw new NullPointerException("Cannot compare to null Event");
    }

    int cmp = this.subject.compareToIgnoreCase(other.getSubject());
    if (cmp != 0) {
      return cmp;
    }

    cmp = this.startDate.compareTo(other.getStartDate());
    if (cmp != 0) {
      return cmp;
    }

    cmp = this.startTime.compareTo(other.getStartTime());
    if (cmp != 0) {
      return cmp;
    }

    cmp = this.endDate.compareTo(other.getEndDate());
    if (cmp != 0) {
      return cmp;
    }

    cmp = this.endTime.compareTo(other.getEndTime());
    return cmp;
  }

  /**
   * Event builder class helps ease the construction of events with different configurations.
   */
  public static class EventBuilder {

    private String subject;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String description;
    private Location location;
    private Status status;
    private String seriesId;

    private EventBuilder() {
      subject = "(No subject)";
      startTime = LocalTime.of(8, 0);
      endTime = LocalTime.of(17, 0);
      seriesId = null;
    }

    /**
     * Set the subject or title for an event.
     *
     * @param subject the title of the event
     * @return EventBuilder object to allow chaining of methods
     */
    public EventBuilder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Set the date for an event. Use for single day events.
     *
     * @param date the title of the event
     * @return EventBuilder object to allow chaining of methods
     */
    public EventBuilder on(LocalDate date) {
      this.startDate = date;
      this.endDate = date;
      return this;
    }

    /**
     * Set the start date and time for an event.
     *
     * @param startDate start date
     * @param startTime start time
     * @return EventBuilder object to allow chaining of methods
     */
    public EventBuilder from(LocalDate startDate, LocalTime startTime) {
      this.startDate = startDate;
      this.startTime = startTime;
      return this;
    }


    /**
     * Set the end date and time for an event.
     *
     * @param endDate end date
     * @param endTime end time
     * @return EventBuilder object to allow chaining of methods
     */
    public EventBuilder to(LocalDate endDate, LocalTime endTime) {
      this.endDate = endDate;
      this.endTime = endTime;
      return this;
    }

    /**
     * Optionally set some description or additional details about an event.
     *
     * @param description details of an event
     * @return EventBuilder object to allow chaining of methods
     */
    public EventBuilder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Optionally set the location of an event - Physical or Online.
     *
     * @param location location of an event
     * @return EventBuilder object to allow chaining of methods
     */
    public EventBuilder location(Location location) {
      this.location = location;
      return this;
    }

    /**
     * Optionally set the status of an event - Public or Private.
     *
     * @param status status of an event
     * @return EventBuilder object to allow chaining of methods
     */
    public EventBuilder status(Status status) {
      this.status = status;
      return this;
    }

    /**
     * Set the series ID for this event.
     *
     * @param seriesId the series ID (null if not part of series)
     * @return EventBuilder object to allow chaining of methods
     */
    public EventBuilder seriesId(String seriesId) {
      this.seriesId = seriesId;
      return this;
    }

    /**
     * Construct the event object with the set configurations.
     *
     * @return Event object
     */
    public EventImpl build() {
      return new EventImpl(subject, startDate, startTime, endDate, endTime, description, location,
          status, seriesId);
    }
  }

}