package controllers.mocks;

import calendar.models.Calendar;
import calendar.models.Event;
import calendar.models.EventSeries;
import calendar.models.FilterCondition;
import calendar.models.enums.EventProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

/**
 * Mock instance for calendar.
 */
public class MockCalendarException implements Calendar {

  private final int uniqueCode;
  private final StringBuilder log;

  /**
   * Instatiate a mock calendar model.
   *
   * @param log        dummy logs
   * @param uniqueCode unique code
   */
  public MockCalendarException(StringBuilder log, int uniqueCode) {
    this.log = log;
    this.uniqueCode = uniqueCode;
  }

  @Override
  public Set<Event> getAllEvents() {
    log.append("Get all events");
    return Set.of();
  }

  @Override
  public boolean addEvent(Event event) {
    log.append("Add event: ").append(event.getSubject());
    throw new IllegalArgumentException("mock exception");
  }

  @Override
  public void addEventSeries(EventSeries series) {
    log.append("Add event series: ").append(series.getSeriesId());
    throw new IllegalArgumentException("mock exception");
  }

  @Override
  public Event findSingleEvent(String subject, LocalDateTime startDateTime,
      LocalDateTime endDateTime) {
    log.append("Find single event: ").append(subject).append(" from ")
        .append(startDateTime).append(" to ").append(endDateTime);
    throw new IllegalArgumentException("mock exception");
  }

  @Override
  public Set<Event> findEventBySubjectAndStart(String subject, LocalDateTime startDateTime) {
    log.append("Find event by subject and start: ").append(subject).append(" from ")
        .append(startDateTime);
    throw new IllegalArgumentException("mock exception");
  }

  @Override
  public Event editSingleEvent(Event event, EventProperty property, String newValue) {
    log.append("Edit single event: ").append(event.getSubject()).append(" property ")
        .append(property).append(" with value ").append(newValue);
    throw new IllegalArgumentException("mock exception");
  }

  @Override
  public EventSeries editSeriesEvent(Event event, EventProperty property, String newValue) {
    log.append("Edit series event: ").append(event.getSubject()).append(" property ")
        .append(property).append(" with value ").append(newValue);
    throw new IllegalArgumentException("mock exception");
  }

  @Override
  public EventSeries editThisAndFollowingEvents(Event event, EventProperty property,
      String newValue) {
    log.append("Edit this and following events: ").append(event.getSubject()).append(" property ")
        .append(property).append(" with value ").append(newValue);
    throw new IllegalArgumentException("mock exception");
  }

  @Override
  public void copyEvent(Calendar other, Event event, LocalDateTime targetStartDateTime) {

  }

  @Override
  public void copyEventsOn(Calendar other, Set<Event> events, LocalDate sourceRangeStart,
      LocalDate targetStartDate) {

  }

  @Override
  public void copyEventsInRange(Calendar other, Set<Event> events, LocalDate sourceRangeStart,
      LocalDate targetRangeStart) {

  }

  @Override
  public String getTitle() {
    log.append("Title");
    throw new IllegalArgumentException("mock exception");
  }

  @Override
  public void setTitle(String name) {
    log.append("setTitle: ").append(name);
  }

  @Override
  public ZoneId getTimezone() {
    return null;
  }

  @Override
  public void setTimezone(ZoneId timezone) {
    log.append("setTimezone: ").append(timezone);
  }

  @Override
  public Set<Event> filterEvents(FilterCondition condition) {
    log.append("Filter events");
    throw new IllegalArgumentException("mock exception");
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    log.append("Is busy at ").append(dateTime);
    throw new IllegalArgumentException("mock exception");
  }
}
