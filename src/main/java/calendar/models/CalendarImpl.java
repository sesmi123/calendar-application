package calendar.models;

import calendar.models.enums.EventProperty;
import calendar.models.enums.Location;
import calendar.models.enums.Status;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of a calendar that stores and manages events and event series.
 */
public class CalendarImpl implements Calendar {

  private final Set<Event> events;
  private final Map<String, EventSeries> seriesMap;
  private String title;
  private ZoneId timezone;

  /**
   * Initialize a calendar with title.
   *
   * @param title name of the calendar.
   */
  public CalendarImpl(String title, ZoneId timezone) {
    this.title = Objects.requireNonNull(title);
    this.timezone = timezone;
    this.events = new TreeSet<>();
    this.seriesMap = new HashMap<>();
  }

  @Override
  public String toString() {
    return this.getTitle();
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle(String name) {
    this.title = name;
  }

  @Override
  public ZoneId getTimezone() {
    return timezone;
  }

  @Override
  public void setTimezone(ZoneId timezone) {
    this.timezone = timezone;
  }

  @Override
  public Set<Event> getAllEvents() {
    return this.events;
  }

  @Override
  public Set<Event> filterEvents(FilterCondition condition) {
    if (condition == null) {
      throw new IllegalArgumentException("Filter condition must not be null");
    }

    Set<Event> result = new TreeSet<>();
    for (Event event : events) {
      if (condition.evaluate(event)) {
        result.add(event);
      }
    }
    return result;
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    Set<Event> filteredEvents = filterEvents(
        e -> !e.getEndDateTime().isBefore(dateTime)
            && !e.getStartDateTime().isAfter(dateTime));

    return !filteredEvents.isEmpty();
  }

  @Override
  public Event findSingleEvent(String subject, LocalDateTime startDateTime,
      LocalDateTime endDateTime) {
    for (Event event : events) {
      if (event.getSubject().equalsIgnoreCase(subject)
          && event.getStartDate().equals(startDateTime.toLocalDate())
          && event.getStartTime().equals(startDateTime.toLocalTime())
          && event.getEndDate().equals(endDateTime.toLocalDate())
          && event.getEndTime().equals(endDateTime.toLocalTime())) {
        return event;
      }
    }
    return null;
  }

  @Override
  public Set<Event> findEventBySubjectAndStart(String subject, LocalDateTime startDateTime) {
    Set<Event> matches = new TreeSet<>();

    for (Event event : events) {
      if (event.getSubject().equalsIgnoreCase(subject)
          && event.getStartDate().equals(startDateTime.toLocalDate())
          && event.getStartTime().equals(startDateTime.toLocalTime())) {
        matches.add(event);
      }
    }

    return matches;
  }

  @Override
  public Event editSingleEvent(Event event, EventProperty property, String newValue) {

    if (event == null || property == null) {
      throw new IllegalArgumentException("Event and property must not be null");
    }

    EventImpl.EventBuilder builder = event.toBuilder();
    applyPropertyChange(builder, property, newValue);
    Event newEvent = builder.build();

    removeEvent(event);
    addEvent(newEvent);
    return newEvent;
  }

  @Override
  public EventSeries editSeriesEvent(Event event, EventProperty property, String newValue) {
    String seriesId = event.getSeriesId();
    Set<Event> eventsCopy = new TreeSet<>(events);
    for (Event e : eventsCopy) {
      if (seriesId.equals(e.getSeriesId())) {
        String valueToUse = adjustDateTimeValueForEvent(e, property, newValue);
        editSingleEvent(e, property, valueToUse);
      }
    }
    return seriesMap.get(seriesId);
  }

  @Override
  public EventSeries editThisAndFollowingEvents(Event event, EventProperty property,
      String newValue) {
    if (property == EventProperty.START_DATE_TIME && isStartTimeChanged(event, newValue)) {
      return splitAndCreateNewSeries(event, newValue);
    }

    String seriesId = event.getSeriesId();
    LocalDateTime targetStart = event.getStartDateTime();

    Set<Event> eventsCopy = new TreeSet<>(events);

    for (Event e : eventsCopy) {
      LocalDateTime eventStart = e.getStartDateTime();
      if (seriesId.equals(e.getSeriesId()) && !eventStart.isBefore(targetStart)) {
        editSingleEvent(e, property, newValue);
      }
    }

    return seriesMap.get(seriesId);
  }

  @Override
  public boolean addEvent(Event event) {
    return events.add(event);
  }

  @Override
  public void addEventSeries(EventSeries series) {
    Set<Event> generatedEvents = series.generateEvents();

    for (Event e : generatedEvents) {
      if (events.contains(e)) {
        throw new IllegalArgumentException(
            "Cannot add series: event from this series already exists in the calendar: "
                + e.getSubject()
                + " on " + e.getStartDate() + " at " + e.getStartTime());
      }
    }

    seriesMap.put(series.getSeriesId(), series);
    events.addAll(generatedEvents);
  }

  @Override
  public void copyEvent(Calendar other, Event event, LocalDateTime targetStartDateTime) {

    Duration duration = Duration.between(
        event.getStartDateTime(),
        event.getEndDateTime()
    );

    LocalDateTime targetEndDateTime = targetStartDateTime.plus(duration);

    Event newEvent = event.toBuilder().from(targetStartDateTime.toLocalDate(),
            targetStartDateTime.toLocalTime())
        .to(targetEndDateTime.toLocalDate(), targetEndDateTime.toLocalTime())
        .seriesId(null)
        .build();
    if (!other.addEvent(newEvent)) {
      throw new IllegalArgumentException("Duplicate event exists in the target calendar");
    }
  }

  @Override
  public void copyEventsOn(Calendar other, Set<Event> events, LocalDate sourceStartDate,
      LocalDate targetStartDate) {
    Set<Event> singleEvents = getSingleEvents(events);
    Set<Event> seriesEvents = getSeriesEvents(events);
    ZoneId sourceZone = this.timezone;
    ZoneId targetZone = other.getTimezone();
    long dayOffset = sourceStartDate.until(targetStartDate).getDays();
    Set<Event> newSingleEvents = getTimeAdjustedEvents(singleEvents, dayOffset,
        sourceZone, targetZone);

    Set<Event> allNewEvents = new HashSet<>(newSingleEvents);
    List<EventSeries> newSeries = new ArrayList<>();
    for (Event e : seriesEvents) {
      Event templateEvent = createAdjustedEvent(e, dayOffset, sourceZone, targetZone);
      allNewEvents.add(templateEvent);
      EventSeries sourceSeries = this.seriesMap.get(e.getSeriesId());
      RecurrenceRule targetRule = new RecurrenceRuleImpl(
          sourceSeries.getRecurrenceRule().getDaysOfWeek(), 1);
      newSeries.add(new EventSeriesImpl(templateEvent, targetStartDate, targetRule));
    }

    validateNoDuplicates(other, allNewEvents);
    copySingleEvents(other, newSingleEvents);
    newSeries.forEach(other::addEventSeries);
  }

  @Override
  public void copyEventsInRange(Calendar other, Set<Event> events,
      LocalDate sourceRangeStart, LocalDate targetRangeStart) {
    Map<String, List<Event>> seriesGroups = groupSeriesEvents(events);
    Set<Event> singleEvents = getSingleEvents(events);
    ZoneId sourceZone = this.timezone;
    ZoneId targetZone = other.getTimezone();
    long dayOffset = sourceRangeStart.until(targetRangeStart).getDays();

    Set<Event> newSingleEvents = getTimeAdjustedEvents(singleEvents, dayOffset,
        sourceZone, targetZone);
    Map<EventSeries, Set<Event>> newSeriesEvents = getTimeAdjustedSeriesEvents(other,
        seriesGroups, targetRangeStart, sourceZone, targetZone);

    Set<Event> allNewEvents = mergeAllEvents(newSingleEvents, newSeriesEvents);
    validateNoDuplicates(other, allNewEvents);

    copySingleEvents(other, newSingleEvents);
    for (Map.Entry<EventSeries, Set<Event>> entry : newSeriesEvents.entrySet()) {
      other.addEventSeries(entry.getKey());
      copySingleEvents(other, entry.getValue());
    }
  }

  /**
   * Helper to check if the new start time differs from current event's start time.
   */
  private boolean isStartTimeChanged(Event event, String value) {
    LocalDateTime newStart = LocalDateTime.parse(value);
    return !newStart.toLocalTime().equals(event.getStartTime());
  }

  private void applyPropertyChange(EventImpl.EventBuilder builder,
      EventProperty property, String value) {
    switch (property) {
      case SUBJECT:
        builder.subject(value);
        break;
      case DESCRIPTION:
        builder.description(value);
        break;
      case LOCATION:
        builder.location(parseLocation(value));
        break;
      case START_DATE_TIME:
        LocalDateTime start = LocalDateTime.parse(value);
        builder.from(start.toLocalDate(), start.toLocalTime());
        break;
      case END_DATE_TIME:
        LocalDateTime end = LocalDateTime.parse(value);
        builder.to(end.toLocalDate(), end.toLocalTime());
        break;
      default:
        builder.status(Status.valueOf(value.toUpperCase()));
        break;
    }
  }

  /**
   * Combines all standalone events and series events into one set.
   *
   * @param singles   set of non-series events
   * @param seriesMap map of series and its events
   * @return combined set of events
   */
  private Set<Event> mergeAllEvents(Set<Event> singles, Map<EventSeries, Set<Event>> seriesMap) {
    return Stream.concat(
        singles.stream(),
        seriesMap.values().stream().flatMap(Set::stream)
    ).collect(Collectors.toSet());
  }

  /**
   * Checks if events already exist on the target calendar before copying.
   */
  private void validateNoDuplicates(Calendar other, Set<Event> newEvents)
      throws IllegalArgumentException {
    boolean hasAnyDuplicate = newEvents.stream()
        .anyMatch(e -> other.findSingleEvent(e.getSubject(),
            e.getStartDateTime(), e.getEndDateTime()) != null);
    if (hasAnyDuplicate) {
      throw new IllegalArgumentException("One or more events already exist in target calendar");
    }
  }

  private boolean removeEvent(Event event) {
    boolean removed = events.remove(event);
    if (removed && event.isPartOfSeries()) {
      String seriesId = event.getSeriesId();

      boolean seriesHasEvents = events.stream()
          .anyMatch(e -> seriesId.equals(e.getSeriesId()));

      if (!seriesHasEvents) {
        seriesMap.remove(seriesId);
      }
    }
    return removed;
  }

  private String adjustDateTimeValueForEvent(Event event, EventProperty property, String newValue) {
    if (property == EventProperty.END_DATE_TIME || property == EventProperty.START_DATE_TIME) {
      LocalDateTime newDateTime = LocalDateTime.parse(newValue);
      LocalTime newTime = newDateTime.toLocalTime();

      LocalDate eventDate = (property == EventProperty.END_DATE_TIME)
          ? event.getEndDate()
          : event.getStartDate();

      return LocalDateTime.of(eventDate, newTime).toString();
    }

    return newValue;
  }

  private EventSeries splitAndCreateNewSeries(Event event, String newValue) {
    String seriesId = event.getSeriesId();
    LocalDateTime targetStart = event.getStartDateTime();
    LocalDateTime newStartDateTime = LocalDateTime.parse(newValue);

    Set<Event> eventsToMove = events.stream()
        .filter(e -> seriesId.equals(e.getSeriesId())
            && !e.getStartDateTime().isBefore(targetStart))
        .collect(Collectors.toCollection(TreeSet::new));
    events.removeAll(eventsToMove);

    Event firstEvent = event.toBuilder()
        .seriesId(null)
        .from(newStartDateTime.toLocalDate(), newStartDateTime.toLocalTime())
        .build();

    EventSeries oldSeries = seriesMap.get(seriesId);
    RecurrenceRule oldRule = oldSeries.getRecurrenceRule();
    RecurrenceRule newRule = createUpdatedRule(oldRule, eventsToMove.size(), oldRule.getEndDate());
    EventSeries newSeries = new EventSeriesImpl(firstEvent, firstEvent.getStartDate(), newRule);

    addEventSeries(newSeries);
    return newSeries;
  }

  private Location parseLocation(String locationStr) {
    return Location.valueOf(locationStr.trim().toUpperCase());
  }

  /**
   * Creates a new event from given event, with date and time adjusted to the target timezone.
   */
  private Event createAdjustedEvent(Event event, long dayOffset, ZoneId sourceZone,
      ZoneId targetZone) {
    LocalDateTime startDateTime = event.getStartDateTime();
    LocalDateTime endDateTime = event.getEndDateTime();

    LocalDateTime shiftedStart = startDateTime.plusDays(dayOffset);
    LocalDateTime shiftedEnd = endDateTime.plusDays(dayOffset);

    LocalDateTime startInTargetZone = convertToTargetTimezone(shiftedStart, sourceZone, targetZone);
    LocalDateTime endInTargetZone = convertToTargetTimezone(shiftedEnd, sourceZone, targetZone);

    return event.toBuilder()
        .from(startInTargetZone.toLocalDate(), startInTargetZone.toLocalTime())
        .to(endInTargetZone.toLocalDate(), endInTargetZone.toLocalTime())
        .seriesId(null)
        .build();
  }

  /**
   * Convertes given date-time in sourceZone to date-time in targetZone.
   */
  private LocalDateTime convertToTargetTimezone(LocalDateTime sourceDateTime,
      ZoneId sourceZone,
      ZoneId targetZone) {
    return sourceDateTime
        .atZone(sourceZone)
        .withZoneSameInstant(targetZone)
        .toLocalDateTime();
  }

  private RecurrenceRule createUpdatedRule(RecurrenceRule oldRule, int occurrences,
      LocalDate endDate) {
    return oldRule.getOccurrences() != null
        ? new RecurrenceRuleImpl(oldRule.getDaysOfWeek(), occurrences)
        : new RecurrenceRuleImpl(oldRule.getDaysOfWeek(), endDate);
  }

  /**
   * Splits events that are part of a series into a map grouped by seriesId. List is used instead of
   * set of events to maintain order while copying to the target calendar.
   */
  private Map<String, List<Event>> groupSeriesEvents(Set<Event> events) {
    Map<String, List<Event>> seriesGroups = new HashMap<>();
    for (Event event : events) {
      if (event.isPartOfSeries()) {
        seriesGroups.computeIfAbsent(event.getSeriesId(), k -> new ArrayList<>()).add(event);
      }
    }
    return seriesGroups;
  }

  /**
   * Extracts standalone (non-series) events.
   */
  private Set<Event> getSingleEvents(Set<Event> events) {
    return events.stream().filter(e -> !e.isPartOfSeries()).collect(Collectors.toSet());
  }

  /**
   * Extracts series events.
   */
  private Set<Event> getSeriesEvents(Set<Event> events) {
    return events.stream().filter(Event::isPartOfSeries).collect(Collectors.toSet());
  }

  /**
   * Get events adjusting timezones and offsets to the target calendar.
   */
  private Set<Event> getTimeAdjustedEvents(Set<Event> events, long dayOffset,
      ZoneId sourceZone, ZoneId targetZone) {
    return events.stream()
        .map(e -> createAdjustedEvent(e, dayOffset, sourceZone, targetZone))
        .collect(Collectors.toSet());
  }

  /**
   * Copies standalone events to the target calendar, adjusting timezones and offsets.
   */
  private void copySingleEvents(Calendar other, Set<Event> events) {
    events.forEach(other::addEvent);
  }

  /**
   * This method internally uses a List of Events instead of a set, to maintain the order of events
   * while setting dates to map to the new calendar.
   */
  private Map<EventSeries, Set<Event>> getTimeAdjustedSeriesEvents(Calendar other,
      Map<String, List<Event>> seriesGroups,
      LocalDate targetRangeStart, ZoneId sourceZone, ZoneId targetZone) {

    Map<EventSeries, Set<Event>> adjustedSeriesMap = new HashMap<>();

    for (Map.Entry<String, List<Event>> entry : seriesGroups.entrySet()) {
      List<Event> seriesEvents = entry.getValue();
      Event sampleEvent = seriesEvents.iterator().next();
      RecurrenceRule sourceRule = this.seriesMap.get(entry.getKey()).getRecurrenceRule();
      EventSeries newSeries = new EventSeriesImpl(
          sampleEvent, targetRangeStart,
          new RecurrenceRuleImpl(sourceRule.getDaysOfWeek(), 0)
      );
      RecurrenceRule targetRule = new RecurrenceRuleImpl(
          sourceRule.getDaysOfWeek(), seriesEvents.size()
      );
      List<LocalDate> eventDates = targetRule.generateDates(targetRangeStart);
      Set<Event> newSeriesEvents = buildTimeAdjustedEvents(seriesEvents, newSeries,
          eventDates, sourceZone, targetZone);
      adjustedSeriesMap.put(newSeries, newSeriesEvents);
    }
    return adjustedSeriesMap;
  }

  /**
   * Builds new Events of series adjusted for timezone and new date by shifting to given dates.
   */
  private Set<Event> buildTimeAdjustedEvents(List<Event> events, EventSeries newSeries,
      List<LocalDate> eventDates, ZoneId sourceZone, ZoneId targetZone) {
    Set<Event> newSeriesEvents = new HashSet<>();
    for (int i = 0; i < events.size() && i < eventDates.size(); i++) {
      Event event = events.get(i);
      LocalDate newDate = eventDates.get(i);
      LocalDateTime start = convertToTargetTimezone(
          LocalDateTime.of(newDate, event.getStartTime()), sourceZone, targetZone);
      LocalDateTime end = convertToTargetTimezone(
          LocalDateTime.of(newDate, event.getEndTime()), sourceZone, targetZone);

      newSeriesEvents.add(event.toBuilder()
          .from(start.toLocalDate(), start.toLocalTime())
          .to(end.toLocalDate(), end.toLocalTime())
          .seriesId(newSeries.getSeriesId())
          .build()
      );
    }
    return newSeriesEvents;
  }

}