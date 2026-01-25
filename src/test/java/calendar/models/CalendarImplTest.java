package calendar.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import calendar.models.enums.EventProperty;
import calendar.models.enums.Location;
import calendar.models.enums.Status;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link CalendarImpl}.
 */
public class CalendarImplTest {

  private CalendarImpl calendar;
  private CalendarImpl calendar2;
  private Event event1;
  private Event event2;
  private Event event3;
  private Event event4;
  private LocalDate date;
  private LocalTime startTime;
  private LocalTime endTime;
  private String zone1;
  private String zone2;

  /**
   * Test set up.
   */
  @Before
  public void setUp() {
    zone1 = "America/New_York";
    zone2 = "Asia/Kolkata";
    calendar = new CalendarImpl("Work Calendar", ZoneId.of(zone1));
    calendar2 = new CalendarImpl("School Calendar", ZoneId.of(zone2));
    date = LocalDate.of(2025, 10, 26);
    startTime = LocalTime.of(10, 0);
    endTime = LocalTime.of(11, 0);

    event1 = EventImpl.getBuilder()
        .subject("Meeting")
        .from(date, startTime)
        .to(date, endTime)
        .status(Status.PRIVATE)
        .build();
    event2 = EventImpl.getBuilder()
        .subject("Standup")
        .from(date, LocalTime.of(12, 0))
        .to(date, LocalTime.of(12, 30))
        .build();
    event3 = EventImpl.getBuilder()
        .subject("Meeting")
        .from(LocalDate.of(2025, 10, 29), LocalTime.of(10, 0))
        .to(LocalDate.of(2025, 10, 29), LocalTime.of(11, 0))
        .build();

    event4 = EventImpl.getBuilder()
        .subject("Workshop")
        .from(LocalDate.of(2025, 10, 30), LocalTime.of(9, 0))
        .to(LocalDate.of(2025, 10, 30), LocalTime.of(10, 0))
        .build();
  }

  @Test
  public void testGetTitle() {
    assertEquals("Work Calendar", calendar.getTitle());
  }

  @Test
  public void testAddEventSuccess() {
    assertTrue(calendar.addEvent(event1));
  }

  @Test
  public void testAddEventDuplicateFails() {
    calendar.addEvent(event1);
    assertFalse("Duplicate event should not be added", calendar.addEvent(event1));
  }

  @Test
  public void testCreateAndEditTitleAndTimezone() {
    ZoneId timezone = ZoneId.of("America/New_York");
    Calendar cal = new CalendarImpl("Study", timezone);
    assertEquals("Study", cal.getTitle());
    assertEquals(timezone, cal.getTimezone());

    cal.setTitle("Play");
    assertEquals("Play", cal.getTitle());

    timezone = ZoneId.of("Canada/Pacific");
    cal.setTimezone(timezone);
    assertEquals(timezone, cal.getTimezone());
  }

  @Test
  public void testFilterEventsReturnsMatchingEvents() {
    calendar.addEvent(event1);
    calendar.addEvent(event2);

    Set<Event> result = calendar.filterEvents(
        e -> e.getSubject().equals("Meeting")
    );

    assertEquals(1, result.size());
    assertTrue(result.contains(event1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFilterEventsWithoutCondition() {

    Set<Event> result = calendar.filterEvents(null);
  }

  @Test
  public void testIsBusyWhenBusy() {
    calendar.addEvent(event1);

    LocalDateTime time = LocalDateTime.of(date, LocalTime.of(10, 30));
    assertTrue(calendar.isBusy(time));
  }

  @Test
  public void testIsBusyWhenFree() {
    calendar.addEvent(event1);

    LocalDateTime time = LocalDateTime.of(date, LocalTime.of(9, 30));
    assertFalse(calendar.isBusy(time));
  }

  @Test
  public void testIsBusyWhenFree2() {
    calendar.addEvent(event1);

    LocalDateTime time = LocalDateTime.of(date, LocalTime.of(11, 30));
    assertFalse(calendar.isBusy(time));
  }

  @Test
  public void testAddEventSeriesAddsAllGeneratedEvents() {
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3);
    Event prototype = EventImpl.getBuilder()
        .subject("Daily Sync")
        .from(date, startTime)
        .to(date, endTime)
        .build();
    EventSeriesImpl series = new EventSeriesImpl(prototype, date, rule);

    calendar.addEventSeries(series);

    Set<Event> allEvents = calendar.filterEvents(e -> e.getSubject().equals("Daily Sync"));
    assertEquals(3, allEvents.size());

    LocalTime expectedStartTime = prototype.getStartTime();
    for (Event event : allEvents) {
      assertEquals(expectedStartTime, event.getStartTime());
    }

    LocalTime expectedEndTime = prototype.getEndTime();
    for (Event event : allEvents) {
      assertEquals(expectedEndTime, event.getEndTime());
    }
  }

  @Test
  public void testAddTwoSimilarEventSeries() {
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3);

    Event prototype = EventImpl.getBuilder()
        .subject("Daily Sync")
        .from(date, startTime)
        .to(date, endTime)
        .build();
    EventSeriesImpl series = new EventSeriesImpl(prototype, date, rule);

    calendar.addEventSeries(series);

    assertEquals(3, calendar.filterEvents(e -> e.getSubject().equals("Daily Sync")).size());

    assertThrows(IllegalArgumentException.class, () -> calendar.addEventSeries(series));

    EventSeriesImpl series2 = new EventSeriesImpl(prototype, date, rule);

    assertThrows(IllegalArgumentException.class, () -> calendar.addEventSeries(series2));
  }

  @Test
  public void testFilterEventsNoMatches() {
    calendar.addEvent(event1);
    Set<Event> result = calendar.filterEvents(e -> e.getSubject().equals("Nonexistent"));
    assertTrue(result.isEmpty());
  }

  @Test
  public void testIsBusyWithSeriesEvent() {
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 1);
    Event prototype = EventImpl.getBuilder()
        .subject("SeriesEvent")
        .from(date, startTime)
        .to(date, endTime)
        .build();
    EventSeriesImpl series = new EventSeriesImpl(prototype, date, rule);
    calendar.addEventSeries(series);

    assertTrue(calendar.isBusy(LocalDateTime.of(date, LocalTime.of(10, 15))));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSeriesEventCannotBeMoreThanOneDay() {
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 1);
    Event prototype = EventImpl.getBuilder()
        .subject("SeriesEvent")
        .from(date, startTime)
        .to(date.plusDays(1), endTime)
        .build();
    EventSeriesImpl series = new EventSeriesImpl(prototype, date, rule);
  }

  @Test
  public void testEditNonExistentEvent() {
    Event edited = calendar.editSingleEvent(event1, EventProperty.SUBJECT, "Review");
    assertEquals("Review", edited.getSubject());
  }

  @Test
  public void testEditSubject() {
    calendar.addEvent(event1);
    Event edited = calendar.editSingleEvent(event1, EventProperty.SUBJECT, "Review");
    assertEquals("Review", edited.getSubject());
  }

  @Test
  public void testEditDescription() {
    calendar.addEvent(event1);
    Event edited = calendar.editSingleEvent(event1, EventProperty.DESCRIPTION, "Updated desc");
    assertEquals("Updated desc", edited.getDescription());
  }

  @Test
  public void testEditLocation() {
    calendar.addEvent(event1);
    Event edited = calendar.editSingleEvent(event1, EventProperty.LOCATION, "ONLINE");
    assertEquals(Location.ONLINE, edited.getLocation());
  }

  @Test
  public void testEditStatus() {
    calendar.addEvent(event1);
    Event edited = calendar.editSingleEvent(event1, EventProperty.STATUS, "PUBLIC");
    assertEquals(Status.PUBLIC, edited.getStatus());
  }

  @Test
  public void testEditStartDateTime() {
    calendar.addEvent(event1);
    LocalDateTime newStart = LocalDateTime.of(2025, 10, 26, 10, 30);
    Event edited = calendar.editSingleEvent(event1, EventProperty.START_DATE_TIME,
        newStart.toString());
    assertEquals(newStart.toLocalDate(), edited.getStartDate());
    assertEquals(newStart.toLocalTime(), edited.getStartTime());
  }

  @Test
  public void testEditEndDateTime() {
    calendar.addEvent(event1);
    LocalDateTime newEnd = LocalDateTime.of(2025, 12, 29, 12, 0);
    Event edited = calendar.editSingleEvent(event1, EventProperty.END_DATE_TIME, newEnd.toString());
    assertEquals(newEnd.toLocalDate(), edited.getEndDate());
    assertEquals(newEnd.toLocalTime(), edited.getEndTime());
  }

  @Test
  public void testEditRecurringEvent() {
    EventImpl seriesEvent1 = EventImpl.getBuilder()
        .subject("Yoga")
        .from(LocalDate.of(2025, 12, 29), LocalTime.of(6, 0))
        .to(LocalDate.of(2025, 12, 29), LocalTime.of(7, 0))
        .seriesId("series-1")
        .build();
    EventImpl seriesEvent2 = EventImpl.getBuilder()
        .subject("Yoga2")
        .from(LocalDate.of(2025, 12, 29), LocalTime.of(6, 0))
        .to(LocalDate.of(2025, 12, 29), LocalTime.of(7, 0))
        .seriesId("series-1")
        .build();
    calendar.addEvent(seriesEvent1);
    calendar.addEvent(seriesEvent2);

    LocalDateTime newStart = LocalDateTime.of(2025, 12, 29, 6, 30);
    Event edited = calendar.editSingleEvent(seriesEvent1, EventProperty.SUBJECT, "New subject");
    edited = calendar.editSingleEvent(edited, EventProperty.DESCRIPTION, "New description");
    edited = calendar.editSingleEvent(edited, EventProperty.LOCATION, "ONLINE");
    edited = calendar.editSingleEvent(edited, EventProperty.STATUS, "PRIVATE");

    assertEquals("series-1", edited.getSeriesId());
    assertEquals("New subject", edited.getSubject());
    assertEquals("New description", edited.getDescription());
    assertEquals(Location.ONLINE, edited.getLocation());
    assertEquals(Status.PRIVATE, edited.getStatus());
  }

  @Test
  public void testEditStartDateTimeDoesNotBreakSeriesWithSingleEvent() {
    EventImpl seriesEvent = EventImpl.getBuilder()
        .subject("Yoga")
        .from(LocalDate.of(2025, 12, 29), LocalTime.of(6, 0))
        .to(LocalDate.of(2025, 12, 29), LocalTime.of(7, 0))
        .seriesId("series-1")
        .build();
    calendar.addEvent(seriesEvent);

    LocalDateTime newStart = LocalDateTime.of(2025, 12, 29, 6, 30);
    Event edited = calendar.editSingleEvent(seriesEvent, EventProperty.START_DATE_TIME,
        newStart.toString());

    assertEquals(edited.getSeriesId(), "series-1");
  }

  @Test
  public void testEditStartDateTimeDoesNotBreakSeriesWithMultipleEvent() {
    EventImpl seriesEvent1 = EventImpl.getBuilder()
        .subject("Yoga")
        .from(LocalDate.of(2025, 12, 29), LocalTime.of(6, 0))
        .to(LocalDate.of(2025, 12, 29), LocalTime.of(7, 0))
        .seriesId("series-1")
        .build();
    EventImpl seriesEvent2 = EventImpl.getBuilder()
        .subject("Yoga2")
        .from(LocalDate.of(2025, 12, 29), LocalTime.of(6, 0))
        .to(LocalDate.of(2025, 12, 29), LocalTime.of(7, 0))
        .seriesId("series-1")
        .build();
    calendar.addEvent(seriesEvent1);
    calendar.addEvent(seriesEvent2);

    LocalDateTime newStart = LocalDateTime.of(2025, 12, 29, 6, 30);
    Event edited = calendar.editSingleEvent(seriesEvent1, EventProperty.START_DATE_TIME,
        newStart.toString());

    assertEquals(edited.getSeriesId(), "series-1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditWithInvalidProperty() {
    calendar.addEvent(event1);
    calendar.editSingleEvent(event1, null, "some value");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditWithInvalidEvent() {
    calendar.addEvent(event1);
    calendar.editSingleEvent(null, EventProperty.DESCRIPTION, "some value");
  }

  @Test
  public void testEditSeriesEventUpdatesAllEventsInSeries() {
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3);

    LocalDate startDate = LocalDate.of(2025, 11, 8);
    LocalTime startTime = LocalTime.of(9, 0);
    LocalTime endTime = LocalTime.of(10, 0);

    Event prototype = EventImpl.getBuilder()
        .subject("Daily Standup")
        .from(startDate, startTime)
        .to(startDate, endTime)
        .description("Daily team sync-up")
        .status(Status.PRIVATE)
        .build();

    EventSeriesImpl series = new EventSeriesImpl(prototype, startDate, rule);
    calendar.addEventSeries(series);

    EventSeries updatedSeries = calendar.editSeriesEvent(
        calendar.filterEvents(
            e -> e.getSeriesId().equals(series.getSeriesId())).iterator().next(),
        EventProperty.SUBJECT,
        "Updated Standup"
    );

    Set<Event> updatedEvents = calendar.filterEvents(
        e -> e.getSeriesId().equals(updatedSeries.getSeriesId()));

    assertEquals(3, updatedEvents.size());

    for (Event e : updatedEvents) {
      assertEquals("Updated Standup", e.getSubject());
    }

    for (Event e : updatedEvents) {
      assertEquals(series.getSeriesId(), e.getSeriesId());
      assertEquals(startTime, e.getStartTime());
      assertEquals(endTime, e.getEndTime());
      assertEquals("Daily team sync-up", e.getDescription());
    }
  }

  @Test
  public void testEditStartDateTimeDoesNotBreakSeriesWhenSameStartTime() {
    EventImpl seriesEvent = EventImpl.getBuilder()
        .subject("Yoga")
        .from(LocalDate.of(2024, 10, 29), LocalTime.of(6, 0))
        .to(LocalDate.of(2024, 10, 29), LocalTime.of(7, 0))
        .seriesId("series-1")
        .build();
    calendar.addEvent(seriesEvent);

    LocalDateTime newStart = LocalDateTime.of(2024, 10, 28, 6, 00);
    Event edited = calendar.editSingleEvent(seriesEvent, EventProperty.START_DATE_TIME,
        newStart.toString());

    assertEquals("series-1", edited.getSeriesId());
  }

  @Test
  public void testFindSingleEventExactMatch() {
    calendar.addEvent(event3);
    calendar.addEvent(event4);
    LocalDateTime start = LocalDateTime.of(2025, 10, 29, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 10, 29, 11, 0);
    Event found = calendar.findSingleEvent("Meeting", start, end);
    assertEquals(event3, found);
  }

  @Test
  public void testFindSingleEventDifferentSubject() {
    calendar.addEvent(event3);
    calendar.addEvent(event3);
    LocalDateTime start = LocalDateTime.of(2025, 10, 29, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 10, 29, 11, 0);
    Event found = calendar.findSingleEvent("Conference", start, end);
    assertNull(found);
  }

  @Test
  public void testFindSingleEventDifferentStartTime() {
    calendar.addEvent(event3);
    calendar.addEvent(event4);
    LocalDateTime start = LocalDateTime.of(2025, 10, 29, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 10, 29, 10, 0);
    Event found = calendar.findSingleEvent("Meeting", start, end);
    assertNull(found);
  }

  @Test
  public void testFindSingleEventDifferentEndTime() {
    calendar.addEvent(event3);
    calendar.addEvent(event4);
    LocalDateTime start = LocalDateTime.of(2025, 10, 29, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 10, 29, 11, 1);
    Event found = calendar.findSingleEvent("Meeting", start, end);
    assertNull(found);
  }

  @Test
  public void testFindSingleEventDifferentStartDate() {
    calendar.addEvent(event3);
    calendar.addEvent(event4);
    LocalDateTime start = LocalDateTime.of(2025, 10, 19, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 10, 29, 10, 0);
    Event found = calendar.findSingleEvent("Meeting", start, end);
    assertNull(found);
  }

  @Test
  public void testFindSingleEventDifferentEndDate() {
    calendar.addEvent(event3);
    calendar.addEvent(event4);
    LocalDateTime start = LocalDateTime.of(2025, 10, 29, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 10, 30, 10, 0);
    Event found = calendar.findSingleEvent("Meeting", start, end);
    assertNull(found);
  }

  @Test
  public void testFindSingleEventCaseInsensitiveSubject() {
    calendar.addEvent(event3);
    calendar.addEvent(event4);
    LocalDateTime start = LocalDateTime.of(2025, 10, 29, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 10, 29, 11, 0);
    Event found = calendar.findSingleEvent("meeting", start, end);
    assertEquals(event3, found);
  }

  @Test
  public void testfindEventBySubjectAndStart() {
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3);

    Event prototype = EventImpl.getBuilder()
        .subject("Daily Standup")
        .from(date, startTime)
        .to(date, endTime)
        .build();

    EventSeriesImpl series = new EventSeriesImpl(prototype, date, rule);
    calendar.addEventSeries(series);

    Set<Event> es = calendar.findEventBySubjectAndStart("Daily Standup",
        LocalDateTime.of(date, startTime));

    for (Event e : es) {
      assertEquals("Daily Standup", e.getSubject());
      assertEquals(date, e.getStartDate());
      assertEquals(startTime, e.getStartTime());

    }
  }

  @Test
  public void testfindEventBySubjectAndStart2() {
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3);

    Event prototype = EventImpl.getBuilder()
        .subject("Daily Standup")
        .from(date, startTime)
        .to(date, endTime)
        .build();

    EventSeriesImpl series = new EventSeriesImpl(prototype, date, rule);
    calendar.addEventSeries(series);

    Set<Event> e = calendar.findEventBySubjectAndStart("Daily Standup",
        LocalDateTime.of(date, startTime.plusHours(1)));
    assertEquals(0, e.size());
  }

  @Test
  public void testEditThisAndFollowingEventsWithSameStartDateTime() {
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3);

    Event prototype = EventImpl.getBuilder()
        .subject("Daily Standup")
        .from(date, startTime)
        .to(date, endTime)
        .build();

    EventSeriesImpl series = new EventSeriesImpl(prototype, date, rule);
    calendar.addEventSeries(series);

    Event firstEvent = calendar.findEventBySubjectAndStart("Daily Standup",
        LocalDateTime.of(date, startTime)).iterator().next();

    EventSeries updatedSeries = calendar.editThisAndFollowingEvents(
        firstEvent, EventProperty.START_DATE_TIME, LocalDateTime.of(date, startTime).toString()
    );

    FilterCondition condition = (e -> e.getSeriesId().equals(series.getSeriesId()));
    List<Event> sortedEvents = calendar.filterEvents(condition).stream()
        .sorted(Comparator.comparing(Event::getStartDate)
            .thenComparing(Event::getStartTime))
        .collect(Collectors.toList());

    for (Event e : sortedEvents) {
      assertEquals(startTime, e.getStartTime());
      assertEquals(endTime, e.getEndTime());
      assertEquals("Daily Standup", e.getSubject());
    }
  }

  @Test
  public void testEditThisAndFollowingEventsSingleEvent() {
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3);

    Event prototype = EventImpl.getBuilder()
        .subject("Daily Standup")
        .from(date, startTime)
        .to(date, endTime)
        .build();

    EventSeriesImpl series = new EventSeriesImpl(prototype, date, rule);
    calendar.addEventSeries(series);

    Event firstEvent = calendar.findEventBySubjectAndStart("Daily Standup",
        LocalDateTime.of(date, startTime)).iterator().next();

    EventSeries updatedSeries = calendar.editThisAndFollowingEvents(
        firstEvent, EventProperty.DESCRIPTION, "Updated description"
    );

    FilterCondition condition = (e -> e.getSeriesId().equals(series.getSeriesId()));
    List<Event> sortedEvents = calendar.filterEvents(condition).stream()
        .sorted(Comparator.comparing(Event::getStartDate)
            .thenComparing(Event::getStartTime))
        .collect(Collectors.toList());

    for (Event e : sortedEvents) {
      assertEquals("Updated description", e.getDescription());
    }
  }

  @Test
  public void testEditThisAndFollowingEventsMiddleEvent() {
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3);

    Event prototype = EventImpl.getBuilder()
        .subject("Project Meeting")
        .from(date, startTime)
        .to(date, endTime)
        .build();

    EventSeriesImpl series = new EventSeriesImpl(prototype, date, rule);
    calendar.addEventSeries(series);

    LocalDate secondDate = date.plusDays(1);
    Event secondEvent = calendar.findEventBySubjectAndStart("Project Meeting",
        LocalDateTime.of(secondDate, startTime)).iterator().next();

    EventSeries updatedSeries = calendar.editThisAndFollowingEvents(
        secondEvent, EventProperty.LOCATION, "ONLINE"
    );

    FilterCondition condition = (e -> e.getSeriesId().equals(series.getSeriesId()));
    List<Event> sortedEvents = calendar.filterEvents(condition).stream()
        .sorted(Comparator.comparing(Event::getStartDate)
            .thenComparing(Event::getStartTime))
        .collect(Collectors.toList());

    Event firstEvent = sortedEvents.get(0);
    assertNotEquals(Location.ONLINE, firstEvent.getLocation());

    assertEquals(Location.ONLINE, sortedEvents.get(1).getLocation());
    assertEquals(Location.ONLINE, sortedEvents.get(2).getLocation());
  }

  @Test
  public void testEditThisAndFollowingEventsStartTimeChangeSplitsSeries() {
    LocalDate date = LocalDate.of(2025, 10, 29);
    RecurrenceRule rule = new RecurrenceRuleImpl(
        EnumSet.allOf(DayOfWeek.class), date.plusDays(3));

    Event prototype = EventImpl.getBuilder()
        .subject("Morning Standup")
        .from(date, startTime)
        .to(date, endTime)
        .build();

    EventSeriesImpl series = new EventSeriesImpl(prototype, date, rule);
    calendar.addEventSeries(series);

    Event prototype2 = EventImpl.getBuilder()
        .subject("Evening Standup")
        .from(date, startTime)
        .to(date, endTime)
        .build();

    EventSeriesImpl series2 = new EventSeriesImpl(prototype2, date, rule);
    calendar.addEventSeries(series2);

    Event firstEvent = calendar.findEventBySubjectAndStart("Morning Standup",
        LocalDateTime.of(date, startTime)).iterator().next();

    LocalDate thirdDate = date.plusDays(2);
    Event thirdEvent = calendar.findEventBySubjectAndStart("Morning Standup",
        LocalDateTime.of(thirdDate, startTime)).iterator().next();

    LocalDateTime newStartTime = LocalDateTime.of(thirdDate, LocalTime.of(9, 0));
    EventSeries newSeries = calendar.editThisAndFollowingEvents(
        thirdEvent, EventProperty.START_DATE_TIME, newStartTime.toString()
    );

    FilterCondition condition = (e -> e.getSeriesId().equals(newSeries.getSeriesId()));
    List<Event> sortedNewSeriesEvents = calendar.filterEvents(condition).stream()
        .sorted(Comparator.comparing(Event::getStartDate)
            .thenComparing(Event::getStartTime))
        .collect(Collectors.toList());

    Event updatedSecondEvent = sortedNewSeriesEvents.get(0);
    assertEquals(LocalTime.of(9, 0), updatedSecondEvent.getStartTime());

    EventSeries oldSeries = calendar.editThisAndFollowingEvents(
        firstEvent, EventProperty.DESCRIPTION, firstEvent.getDescription()
    );

    condition = (e -> e.getSeriesId().equals(oldSeries.getSeriesId()));
    List<Event> sortedOldSeriesEvents = calendar.filterEvents(condition).stream()
        .sorted(Comparator.comparing(Event::getStartDate)
            .thenComparing(Event::getStartTime))
        .collect(Collectors.toList());
    assertEquals(2, sortedOldSeriesEvents.size());
  }

  @Test
  public void testEditThisAndFollowingEventsInvalidEvent() {
    Event nonSeriesEvent = EventImpl.getBuilder()
        .subject("Solo Event")
        .from(date, startTime)
        .to(date, endTime)
        .build();

    EventSeries result = calendar.editThisAndFollowingEvents(
        nonSeriesEvent, EventProperty.DESCRIPTION, "Updated"
    );

    assertNull(result);
  }

  @Test
  public void testCopyEvent() {
    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);
    calendar.addEvent(event4);
    assertEquals(0, calendar2.getAllEvents().size());
    calendar.copyEvent(calendar2, event1, LocalDateTime.of(2025, 10, 21, 9, 30));
    Set<Event> es = calendar2.getAllEvents();
    assertEquals(1, es.size());
    Event e = es.iterator().next();
    assertEquals("Meeting", e.getSubject());
    assertNull(e.getSeriesId());
    assertEquals(LocalDate.of(2025, 10, 21), e.getStartDate());
    assertEquals(LocalTime.of(9, 30), e.getStartTime());
    assertEquals(LocalDate.of(2025, 10, 21), e.getEndDate());
    assertEquals(LocalTime.of(10, 30), e.getEndTime());
  }

  @Test
  public void testCopySeriesEvent() {
    Event prototype = EventImpl.getBuilder()
        .subject("Daily Sync")
        .from(date, startTime)
        .to(date, endTime)
        .build();
    EventSeriesImpl series = new EventSeriesImpl(prototype, date,
        new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3));

    calendar.addEventSeries(series);

    assertEquals(0, calendar2.getAllEvents().size());
    Event copyEvent = calendar.findSingleEvent("Daily Sync", LocalDateTime.of(date, startTime),
        LocalDateTime.of(date, endTime));
    calendar.copyEvent(calendar2, copyEvent, LocalDateTime.of(2025, 10, 21, 9, 30));
    Set<Event> es = calendar2.getAllEvents();
    assertEquals(1, es.size());
    Event e = es.iterator().next();
    assertEquals("Daily Sync", e.getSubject());
    assertNull(e.getSeriesId());
    assertEquals(LocalDate.of(2025, 10, 21), e.getStartDate());
    assertEquals(LocalTime.of(9, 30), e.getStartTime());
    assertEquals(LocalDate.of(2025, 10, 21), e.getEndDate());
    assertEquals(LocalTime.of(10, 30), e.getEndTime());
  }

  @Test
  public void testCreateUpdatedRuleWithOccurrences() {
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3);
    Event prototype = EventImpl.getBuilder()
        .subject("Weekly Meeting")
        .from(date, startTime)
        .to(date, endTime)
        .build();

    EventSeriesImpl series = new EventSeriesImpl(prototype, date, rule);
    calendar.addEventSeries(series);

    LocalDate thirdEventDate = date.plusDays(2);
    Event thirdEvent = calendar.findEventBySubjectAndStart("Weekly Meeting",
        LocalDateTime.of(thirdEventDate, startTime)).iterator().next();

    LocalDateTime newStartTime = LocalDateTime.of(thirdEventDate, LocalTime.of(9, 0));
    EventSeries newSeries = calendar.editThisAndFollowingEvents(
        thirdEvent, EventProperty.START_DATE_TIME, newStartTime.toString());

    FilterCondition condition = (e -> e.getSeriesId().equals(newSeries.getSeriesId()));
    Set<Event> newSeriesEvents = calendar.filterEvents(condition);
    assertEquals(1, newSeriesEvents.size());
  }

  @Test
  public void testCopyEventDifferentCalendarPreservesEventDuration() {
    Event longEvent = EventImpl.getBuilder()
        .subject("Workshop")
        .from(date, LocalTime.of(10, 0))
        .to(date, LocalTime.of(12, 0))
        .build();
    calendar.addEvent(longEvent);

    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 14, 0);
    calendar.copyEvent(calendar2, longEvent, targetStart);

    Event copiedEvent = calendar2.getAllEvents().iterator().next();
    assertEquals(LocalTime.of(14, 0), copiedEvent.getStartTime());
    assertEquals(LocalTime.of(16, 0), copiedEvent.getEndTime());
  }

  @Test
  public void testCopyEventSameCalendarPreservesEventDuration() {
    Event longEvent = EventImpl.getBuilder()
        .subject("Workshop")
        .from(date, LocalTime.of(10, 0))
        .to(date, LocalTime.of(12, 0))
        .build();
    calendar.addEvent(longEvent);
    int originalSize = calendar.getAllEvents().size();

    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 14, 0);
    calendar.copyEvent(calendar, longEvent, targetStart);

    assertEquals(originalSize + 1, calendar.getAllEvents().size());

    Event copiedEvent = calendar.findSingleEvent("Workshop", targetStart,
        LocalDateTime.of(2025, 11, 1, 16, 0));
    assertNotNull(copiedEvent);
    assertEquals(LocalTime.of(14, 0), copiedEvent.getStartTime());
    assertEquals(LocalTime.of(16, 0), copiedEvent.getEndTime());
  }

  @Test
  public void testCopyEventDifferentCalendarCopiedEventIsStandalone() {
    Event prototype = EventImpl.getBuilder()
        .subject("Recurring")
        .from(date, startTime)
        .to(date, endTime)
        .build();
    EventSeriesImpl series = new EventSeriesImpl(prototype, date,
        new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3));
    calendar.addEventSeries(series);

    Event seriesEvent = calendar.findSingleEvent("Recurring", LocalDateTime.of(date, startTime),
        LocalDateTime.of(date, endTime));

    calendar.copyEvent(calendar2, seriesEvent, LocalDateTime.of(2025, 11, 1, 10, 0));

    Event copiedEvent = calendar2.getAllEvents().iterator().next();
    assertNull(copiedEvent.getSeriesId());
    assertFalse(copiedEvent.isPartOfSeries());
  }

  @Test
  public void testCopyEventSameCalendarCopiedEventIsStandalone() {
    Event prototype = EventImpl.getBuilder()
        .subject("Recurring")
        .from(date, startTime)
        .to(date, endTime)
        .build();
    EventSeriesImpl series = new EventSeriesImpl(prototype, date,
        new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3));
    calendar.addEventSeries(series);

    Event seriesEvent = calendar.findSingleEvent("Recurring", LocalDateTime.of(date, startTime),
        LocalDateTime.of(date, endTime));

    int originalSize = calendar.getAllEvents().size();
    calendar.copyEvent(calendar, seriesEvent, LocalDateTime.of(2025, 11, 1, 13, 0));

    assertEquals(originalSize + 1, calendar.getAllEvents().size());

    Event copiedEvent = calendar.findSingleEvent("Recurring", LocalDateTime.of(2025, 11, 1, 13, 0),
        LocalDateTime.of(2025, 11, 1, 14, 0));

    assertNotNull(copiedEvent);
    assertNull(copiedEvent.getSeriesId());
    assertFalse(copiedEvent.isPartOfSeries());
  }

  @Test
  public void testCopyEventToSameCalendar() {
    calendar.addEvent(event1);
    int originalSize = calendar.getAllEvents().size();

    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 10, 0);
    calendar.copyEvent(calendar, event1, targetStart);

    assertEquals(originalSize + 1, calendar.getAllEvents().size());
  }

  @Test
  public void testCopyEventDifferentCalendarSpansMidnight() {
    LocalDate eventDate = LocalDate.of(2026, 11, 1);
    Event eveningEvent = EventImpl.getBuilder()
        .subject("Evening Event")
        .from(eventDate, LocalTime.of(12, 30))
        .to(eventDate, LocalTime.of(14, 0))
        .build();
    calendar.addEvent(eveningEvent);

    LocalDateTime targetStart = LocalDateTime.of(2026, 11, 1, 23, 0);
    calendar.copyEvent(calendar2, eveningEvent, targetStart);

    Event copiedEvent = calendar2.getAllEvents().iterator().next();
    assertEquals(LocalDate.of(2026, 11, 1), copiedEvent.getStartDate());
    assertEquals(LocalTime.of(23, 0), copiedEvent.getStartTime());
    assertEquals(LocalDate.of(2026, 11, 2), copiedEvent.getEndDate());
    assertEquals(LocalTime.of(0, 30), copiedEvent.getEndTime());
  }

  @Test
  public void testCopyEventSameCalendarSpansMidnight() {
    LocalDate eventDate = LocalDate.of(2026, 10, 1);
    Event eveningEvent = EventImpl.getBuilder()
        .subject("Evening Event")
        .from(eventDate, LocalTime.of(22, 0))
        .to(eventDate, LocalTime.of(23, 30))
        .build();
    calendar.addEvent(eveningEvent);
    int originalSize = calendar.getAllEvents().size();

    LocalDateTime targetStart = LocalDateTime.of(2026, 11, 1, 23, 0);
    LocalDateTime targetEnd = LocalDateTime.of(2026, 11, 2, 0, 30);
    calendar.copyEvent(calendar, eveningEvent, targetStart);

    assertEquals(originalSize + 1, calendar.getAllEvents().size());

    Event copiedEvent = calendar.findSingleEvent("Evening Event", targetStart, targetEnd);

    assertNotNull(copiedEvent);
    assertEquals(LocalDate.of(2026, 11, 1), copiedEvent.getStartDate());
    assertEquals(LocalTime.of(23, 0), copiedEvent.getStartTime());
    assertEquals(LocalDate.of(2026, 11, 2), copiedEvent.getEndDate());
    assertEquals(LocalTime.of(0, 30), copiedEvent.getEndTime());
  }

  @Test
  public void testCopyEventDifferentCalendarPreservesEventProperties() {
    Event eventWithProperties = EventImpl.getBuilder()
        .subject("Important Meeting")
        .from(date, startTime)
        .to(date, endTime)
        .description("Q4 Planning")
        .location(Location.PHYSICAL)
        .status(Status.PRIVATE)
        .build();
    calendar.addEvent(eventWithProperties);

    calendar.copyEvent(calendar2, eventWithProperties, LocalDateTime.of(2025, 11, 1, 10, 0));

    Event copiedEvent = calendar2.getAllEvents().iterator().next();
    assertEquals("Important Meeting", copiedEvent.getSubject());
    assertEquals("Q4 Planning", copiedEvent.getDescription());
    assertEquals(Location.PHYSICAL, copiedEvent.getLocation());
    assertEquals(Status.PRIVATE, copiedEvent.getStatus());
  }

  @Test
  public void testCopyEventSameCalendarPreservesEventProperties() {
    Event eventWithProperties = EventImpl.getBuilder()
        .subject("Important Meeting")
        .from(date, startTime)
        .to(date, endTime)
        .description("Q4 Planning")
        .location(Location.PHYSICAL)
        .status(Status.PRIVATE)
        .build();
    calendar.addEvent(eventWithProperties);
    int originalSize = calendar.getAllEvents().size();

    calendar.copyEvent(calendar, eventWithProperties, LocalDateTime.of(2025, 11, 1, 10, 0));

    assertEquals(originalSize + 1, calendar.getAllEvents().size());

    Event copiedEvent = calendar.findSingleEvent("Important Meeting",
        LocalDateTime.of(2025, 11, 1, 10, 0),
        LocalDateTime.of(2025, 11, 1, 11, 0));

    assertNotNull(copiedEvent);
    assertEquals("Important Meeting", copiedEvent.getSubject());
    assertEquals("Q4 Planning", copiedEvent.getDescription());
    assertEquals(Location.PHYSICAL, copiedEvent.getLocation());
    assertEquals(Status.PRIVATE, copiedEvent.getStatus());
  }

  @Test
  public void testCopyEventsOnMultipleEventsDifferentCalendarPreservesProperties() {
    LocalDate eventDate = LocalDate.of(2027, 10, 27);
    Event sampleEvent1 = EventImpl.getBuilder()
        .subject("Tourney1")
        .from(eventDate, LocalTime.of(8, 0))
        .to(eventDate, LocalTime.of(9, 0))
        .status(Status.PRIVATE)
        .description("Chess Tourney1")
        .build();
    Event sampleEvent2 = EventImpl.getBuilder()
        .subject("Tourney2")
        .from(eventDate, LocalTime.of(10, 0))
        .to(eventDate, LocalTime.of(11, 0))
        .description("Chess Tourney2")
        .location(Location.PHYSICAL)
        .build();
    Event sampleEvent3 = EventImpl.getBuilder()
        .subject("Tourney3")
        .from(eventDate, LocalTime.of(13, 0))
        .to(eventDate, LocalTime.of(14, 0))
        .build();
    calendar.addEvent(sampleEvent1);
    calendar.addEvent(sampleEvent2);
    calendar.addEvent(sampleEvent3);

    Set<Event> eventsToCopy = new TreeSet<>();
    eventsToCopy.add(sampleEvent1);
    eventsToCopy.add(sampleEvent2);

    calendar.copyEventsOn(calendar2, eventsToCopy, eventDate, eventDate.plusDays(5));

    Event copiedSampleEvent1 = calendar2
        .filterEvents(e -> e.getSubject().equals("Tourney1")).iterator().next();
    Event copiedSampleEvent2 = calendar2
        .filterEvents(e -> e.getSubject().equals("Tourney2")).iterator().next();
    assertEquals(2, calendar2.getAllEvents().size());
    assertEquals(Status.PRIVATE, copiedSampleEvent1.getStatus());
    assertEquals(Location.PHYSICAL, copiedSampleEvent2.getLocation());
    assertEquals("Chess Tourney1", copiedSampleEvent1.getDescription());
    assertEquals("Chess Tourney2", copiedSampleEvent2.getDescription());
  }

  @Test
  public void testCopyEventsOnMultipleEventsSameCalendarPreservesProperties() {
    LocalDate eventDate = LocalDate.of(2027, 10, 27);
    Event sampleEvent1 = EventImpl.getBuilder()
        .subject("Tourney1")
        .from(eventDate, LocalTime.of(8, 0))
        .to(eventDate, LocalTime.of(9, 0))
        .status(Status.PRIVATE)
        .description("Chess Tourney1")
        .build();
    Event sampleEvent2 = EventImpl.getBuilder()
        .subject("Tourney2")
        .from(eventDate, LocalTime.of(10, 0))
        .to(eventDate, LocalTime.of(11, 0))
        .description("Chess Tourney2")
        .location(Location.PHYSICAL)
        .build();
    Event sampleEvent3 = EventImpl.getBuilder()
        .subject("Tourney3")
        .from(eventDate, LocalTime.of(13, 0))
        .to(eventDate, LocalTime.of(14, 0))
        .build();
    calendar.addEvent(sampleEvent1);
    calendar.addEvent(sampleEvent2);
    calendar.addEvent(sampleEvent3);

    Set<Event> eventsToCopy = new TreeSet<>();
    eventsToCopy.add(sampleEvent1);
    eventsToCopy.add(sampleEvent2);

    calendar.copyEventsOn(calendar, eventsToCopy, eventDate, eventDate.plusDays(5));

    Event copiedSampleEvent1 = calendar.findSingleEvent("Tourney1",
        LocalDateTime.of(eventDate, LocalTime.of(8, 0)),
        LocalDateTime.of(eventDate, LocalTime.of(9, 0)));
    Event copiedSampleEvent2 = calendar.findSingleEvent("Tourney2",
        LocalDateTime.of(eventDate, LocalTime.of(10, 0)),
        LocalDateTime.of(eventDate, LocalTime.of(11, 0)));
    assertEquals(5, calendar.getAllEvents().size());
    assertEquals(Status.PRIVATE, copiedSampleEvent1.getStatus());
    assertEquals(Location.PHYSICAL, copiedSampleEvent2.getLocation());
    assertEquals("Chess Tourney1", copiedSampleEvent1.getDescription());
    assertEquals("Chess Tourney2", copiedSampleEvent2.getDescription());
  }

  @Test
  public void testCopyEventsOnDifferentCalendarConvertsTimezones() {
    calendar.addEvent(event1);

    Set<Event> eventsToCopy = Collections.singleton(event1);

    LocalDate newDate = date.plusDays(1);
    calendar.copyEventsOn(calendar2, eventsToCopy, date, newDate);

    Event copiedEvent = calendar2.getAllEvents().iterator().next();

    ZonedDateTime originalStart = ZonedDateTime.of(date, startTime, ZoneId.of(zone1));
    ZonedDateTime originalEnd = ZonedDateTime.of(date, endTime, ZoneId.of(zone1));

    ZonedDateTime expectedStart = originalStart
        .withZoneSameInstant(ZoneId.of(zone2))
        .with(newDate);
    ZonedDateTime expectedEnd = originalEnd
        .withZoneSameInstant(ZoneId.of(zone2))
        .with(newDate);

    assertEquals(expectedStart.toLocalDate(), copiedEvent.getStartDate());
    assertEquals(expectedStart.toLocalTime(), copiedEvent.getStartTime());
    assertEquals(expectedEnd.toLocalTime(), copiedEvent.getEndTime());
  }


  @Test
  public void testCopyEventsOnSameCalendarDoesNotConvertTimezones() {
    calendar.addEvent(event1);
    int originalSize = calendar.getAllEvents().size();

    Set<Event> eventsToCopy = Collections.singleton(event1);
    calendar.copyEventsOn(calendar, eventsToCopy, date, date.plusDays(1));

    assertEquals(originalSize + 1, calendar.getAllEvents().size());

    Set<Event> copied = calendar.findEventBySubjectAndStart("Meeting",
        LocalDateTime.of(date.plusDays(1), LocalTime.of(10, 0)));

    assertEquals(1, copied.size());
    Event copiedEvent = copied.iterator().next();
    assertEquals(date.plusDays(1), copiedEvent.getStartDate());
    assertEquals(LocalTime.of(10, 0), copiedEvent.getStartTime());
    assertEquals(LocalTime.of(11, 0), copiedEvent.getEndTime());
  }

  @Test
  public void testCopyEventsOnDifferentCalendarPreservesSeriesStatus() {
    // create a series and a single event, both on the same date and
    // copy them to a different calendar
    LocalDate eventDate = LocalDate.of(2027, 10, 27);
    Event prototype = EventImpl.getBuilder()
        .subject("Series Event")
        .from(eventDate, startTime)
        .to(eventDate, endTime)
        .build();
    EventSeriesImpl series = new EventSeriesImpl(prototype, eventDate,
        new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3));
    calendar.addEventSeries(series);

    Event event5 = EventImpl.getBuilder()
        .subject("Workshop")
        .from(eventDate, LocalTime.of(16, 0))
        .to(eventDate, LocalTime.of(17, 0))
        .build();
    calendar.addEvent(event5);

    Event eventOfSeries = calendar.findSingleEvent("Series Event",
        LocalDateTime.of(eventDate, startTime),
        LocalDateTime.of(eventDate, endTime));

    Set<Event> eventsToCopy = Set.of(event5, eventOfSeries);
    calendar.copyEventsOn(calendar2, eventsToCopy, eventDate, eventDate.plusDays(10));

    Event copiedEventOfSeries = calendar2
        .filterEvents(e -> e.getSubject().equals("Series Event")).iterator().next();
    Event copiedEvent5 = calendar2
        .filterEvents(e -> e.getSubject().equals("Workshop")).iterator().next();
    assertEquals(2, calendar2.getAllEvents().size());
    assertEquals(eventDate.plusDays(10), copiedEventOfSeries.getStartDate());
    assertEquals(eventDate.plusDays(11), copiedEvent5.getStartDate());
    assertTrue(copiedEventOfSeries.isPartOfSeries());
  }

  @Test
  public void testCopyEventsOnSameCalendarPreservesSeriesStatus() {
    // create a series and a single event, both on the same date and
    // copy them to the same calendar
    LocalDate eventDate = LocalDate.of(2027, 10, 27);
    Event prototype = EventImpl.getBuilder()
        .subject("Series Event")
        .from(eventDate, startTime)
        .to(eventDate, endTime)
        .build();
    EventSeriesImpl series = new EventSeriesImpl(prototype, eventDate,
        new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3));
    calendar.addEventSeries(series);

    Event event5 = EventImpl.getBuilder()
        .subject("Workshop")
        .from(eventDate, LocalTime.of(16, 0))
        .to(eventDate, LocalTime.of(17, 0))
        .build();
    calendar.addEvent(event5);

    Event eventOfSeries = calendar.findSingleEvent("Series Event",
        LocalDateTime.of(eventDate, startTime),
        LocalDateTime.of(eventDate, endTime));

    Set<Event> eventsToCopy = Set.of(event5, eventOfSeries);
    calendar.copyEventsOn(calendar, eventsToCopy, eventDate, eventDate.plusDays(10));

    Event copiedEventOfSeries = calendar.findSingleEvent("Series Event",
        LocalDateTime.of(eventDate.plusDays(10), startTime),
        LocalDateTime.of(eventDate.plusDays(10), endTime));
    Event copiedEvent5 = calendar.findSingleEvent("Workshop",
        LocalDateTime.of(eventDate.plusDays(10), LocalTime.of(16, 0)),
        LocalDateTime.of(eventDate.plusDays(10), LocalTime.of(17, 0)));
    assertEquals(6, calendar.getAllEvents().size());
    assertEquals(eventDate.plusDays(10), copiedEventOfSeries.getStartDate());
    assertEquals(eventDate.plusDays(10), copiedEvent5.getStartDate());
    assertTrue(copiedEventOfSeries.isPartOfSeries());
  }

  @Test
  public void testCopyEventsOnDifferentCalendarPreservesDuration() {
    Event twoHourEvent = EventImpl.getBuilder()
        .subject("Long Session")
        .from(date, LocalTime.of(9, 0))
        .to(date, LocalTime.of(11, 0))
        .build();
    calendar.addEvent(twoHourEvent);

    Set<Event> eventsToCopy = Collections.singleton(twoHourEvent);
    calendar.copyEventsOn(calendar2, eventsToCopy, date, date.plusDays(5));

    Event copiedEvent = calendar2.getAllEvents().iterator().next();
    assertEquals(copiedEvent.getStartTime().plusHours(2), copiedEvent.getEndTime());
  }

  @Test
  public void testCopyEventsOnSameCalendarPreservesDuration() {
    Event twoHourEvent = EventImpl.getBuilder()
        .subject("Long Session")
        .from(date, LocalTime.of(9, 0))
        .to(date, LocalTime.of(11, 0))
        .build();
    calendar.addEvent(twoHourEvent);
    int originalSize = calendar.getAllEvents().size();

    Set<Event> eventsToCopy = Collections.singleton(twoHourEvent);
    calendar.copyEventsOn(calendar, eventsToCopy, date, date.plusDays(5));

    assertEquals(originalSize + 1, calendar.getAllEvents().size());

    Event copiedEvent = calendar.findSingleEvent("Long Session",
        LocalDateTime.of(date.plusDays(5), LocalTime.of(9, 0)),
        LocalDateTime.of(date.plusDays(5), LocalTime.of(11, 0)));

    assertNotNull(copiedEvent);
    assertEquals(copiedEvent.getStartTime().plusHours(2), copiedEvent.getEndTime());
  }

  @Test
  public void testCopyEventsOnSingleEvent() {
    calendar.addEvent(event1);

    Set<Event> eventsToCopy = Collections.singleton(event1);
    calendar.copyEventsOn(calendar2, eventsToCopy, date, date.plusDays(3));

    assertEquals(1, calendar2.getAllEvents().size());
    Event copiedEvent = calendar2.getAllEvents().iterator().next();
    assertEquals("Meeting", copiedEvent.getSubject());
  }

  @Test
  public void testCopyEventsInRangeDifferentCalendarSingleEvent() {
    LocalDate eventDate = LocalDate.of(2027, 10, 27);
    Event sampleEvent1 = EventImpl.getBuilder()
        .subject("Tourney1")
        .from(eventDate, LocalTime.of(8, 0))
        .to(eventDate, LocalTime.of(9, 0))
        .status(Status.PRIVATE)
        .location(Location.ONLINE)
        .description("Chess Tourney1")
        .build();
    calendar.addEvent(sampleEvent1);

    Set<Event> eventsToCopy = Collections.singleton(sampleEvent1);
    calendar.copyEventsInRange(calendar2, eventsToCopy, eventDate, eventDate.plusDays(5));

    Event copiedSampleEvent1 = calendar2.getAllEvents().iterator().next();
    assertEquals(1, calendar2.getAllEvents().size());

    ZonedDateTime originalStart = ZonedDateTime.of(eventDate, LocalTime.of(8, 0), ZoneId.of(zone1));
    ZonedDateTime originalEnd = ZonedDateTime.of(eventDate, LocalTime.of(9, 0), ZoneId.of(zone1));

    ZonedDateTime expectedStart = originalStart
        .withZoneSameInstant(ZoneId.of(zone2))
        .with(eventDate.plusDays(5));
    ZonedDateTime expectedEnd = originalEnd
        .withZoneSameInstant(ZoneId.of(zone2))
        .with(eventDate.plusDays(5));

    assertEquals(expectedStart.toLocalDate(), copiedSampleEvent1.getStartDate());
    assertEquals(expectedStart.toLocalTime(), copiedSampleEvent1.getStartTime());
    assertEquals(expectedEnd.toLocalTime(), copiedSampleEvent1.getEndTime());
    assertEquals("Tourney1", copiedSampleEvent1.getSubject());
    assertEquals(Status.PRIVATE, copiedSampleEvent1.getStatus());
    assertEquals("Chess Tourney1", copiedSampleEvent1.getDescription());
    assertEquals(Location.ONLINE, copiedSampleEvent1.getLocation());
  }

  @Test
  public void testCopyEventsInRangeSameCalendarSingleEvent() {
    LocalDate eventDate = LocalDate.of(2027, 10, 27);
    Event sampleEvent1 = EventImpl.getBuilder()
        .subject("Tourney1")
        .from(eventDate, LocalTime.of(8, 0))
        .to(eventDate, LocalTime.of(9, 0))
        .status(Status.PRIVATE)
        .location(Location.ONLINE)
        .description("Chess Tourney1")
        .build();
    calendar.addEvent(sampleEvent1);

    Set<Event> eventsToCopy = Collections.singleton(sampleEvent1);
    calendar.copyEventsInRange(calendar, eventsToCopy, eventDate, eventDate.plusDays(5));

    Event copiedSampleEvent1 = calendar.findSingleEvent("Tourney1",
        LocalDateTime.of(eventDate, LocalTime.of(8, 0)),
        LocalDateTime.of(eventDate, LocalTime.of(9, 0)));

    // copied event plus existing event should give 2 total events in the calendar
    assertEquals(2, calendar.getAllEvents().size());
    assertEquals(eventDate, copiedSampleEvent1.getStartDate());
    assertEquals(LocalTime.of(8, 0), copiedSampleEvent1.getStartTime());
    assertEquals(LocalTime.of(9, 0), copiedSampleEvent1.getEndTime());
    assertEquals("Tourney1", copiedSampleEvent1.getSubject());
    assertEquals(Status.PRIVATE, copiedSampleEvent1.getStatus());
    assertEquals("Chess Tourney1", copiedSampleEvent1.getDescription());
    assertEquals(Location.ONLINE, copiedSampleEvent1.getLocation());
  }

  @Test
  public void testCopyEventsInRangeSeriesDifferentCalendarPreservesSeriesStatus() {
    // create a series and a single event, both on the same date (series start) and
    // copy them to a different calendar
    LocalDate eventDate = LocalDate.of(2027, 10, 27);
    LocalTime eventStartTime = LocalTime.of(10, 0);
    LocalTime eventEndTime = LocalTime.of(11, 0);
    Event prototype = EventImpl.getBuilder()
        .subject("Series Event")
        .from(eventDate, eventStartTime)
        .to(eventDate, eventEndTime)
        .build();
    EventSeriesImpl series = new EventSeriesImpl(prototype, eventDate,
        new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3));
    calendar.addEventSeries(series);

    Event sampleEvent = EventImpl.getBuilder()
        .subject("Workshop")
        .from(eventDate, LocalTime.of(16, 0))
        .to(eventDate, LocalTime.of(17, 0))
        .build();
    calendar.addEvent(sampleEvent);

    Set<Event> eventsToCopy = calendar
        .filterEvents(new FilterByDateRange(eventDate, eventDate.plusDays(3)));

    calendar.copyEventsInRange(calendar2, eventsToCopy, eventDate, eventDate.plusDays(10));

    // Check if all 4 events are copied
    assertEquals(4, calendar2.getAllEvents().size());

    List<Event> copiedSeriesEvents = calendar2
        .filterEvents(e -> e.getSubject().equals("Series Event"))
        .stream()
        .sorted()
        .collect(Collectors.toList());
    Event copiedFirstEventOfSeries = copiedSeriesEvents.get(0);
    Event copiedSecondEventOfSeries = copiedSeriesEvents.get(1);
    Event copiedThirdEventOfSeries = copiedSeriesEvents.get(2);

    // check if the series events are copied to the correct dates
    assertEquals(eventDate.plusDays(10), copiedFirstEventOfSeries.getStartDate());
    assertEquals(eventDate.plusDays(11), copiedSecondEventOfSeries.getStartDate());
    assertEquals(eventDate.plusDays(12), copiedThirdEventOfSeries.getStartDate());

    Event copiedSampleEvent = calendar2
        .filterEvents(e -> e.getSubject().equals("Workshop")).iterator().next();

    // check if the single event is copied to the correct date
    assertEquals(eventDate.plusDays(11), copiedSampleEvent.getStartDate());

    // check if the series events are still part of the same series
    String seriesId = copiedFirstEventOfSeries.getSeriesId();
    assertEquals(seriesId, copiedSecondEventOfSeries.getSeriesId());
    assertEquals(seriesId, copiedThirdEventOfSeries.getSeriesId());
  }

  @Test
  public void testCopyEventsInRangeSeriesSameCalendarPreservesSeriesStatus() {
    // create a series and a single event, both on the same date (series start) and
    // copy them to the same calendar
    LocalDate eventDate = LocalDate.of(2027, 10, 27);
    LocalTime eventStartTime = LocalTime.of(10, 0);
    LocalTime eventEndTime = LocalTime.of(11, 0);
    Event prototype = EventImpl.getBuilder()
        .subject("Series Event")
        .from(eventDate, eventStartTime)
        .to(eventDate, eventEndTime)
        .build();
    EventSeriesImpl series = new EventSeriesImpl(prototype, eventDate,
        new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3));
    calendar.addEventSeries(series);

    Event sampleEvent = EventImpl.getBuilder()
        .subject("Workshop")
        .from(eventDate, LocalTime.of(16, 0))
        .to(eventDate, LocalTime.of(17, 0))
        .build();
    calendar.addEvent(sampleEvent);

    Set<Event> eventsToCopy = calendar
        .filterEvents(new FilterByDateRange(eventDate, eventDate.plusDays(3)));

    calendar.copyEventsInRange(calendar, eventsToCopy, eventDate, eventDate.plusDays(10));

    // Check if all 4 events are copied, ie - calendar should have 8 events in total
    assertEquals(8, calendar.getAllEvents().size());

    Event copiedFirstEventOfSeries = calendar.findSingleEvent("Series Event",
        LocalDateTime.of(eventDate.plusDays(10), eventStartTime),
        LocalDateTime.of(eventDate.plusDays(10), eventEndTime));
    Event copiedSecondEventOfSeries = calendar.findSingleEvent("Series Event",
        LocalDateTime.of(eventDate.plusDays(11), eventStartTime),
        LocalDateTime.of(eventDate.plusDays(11), eventEndTime));
    Event copiedThirdEventOfSeries = calendar.findSingleEvent("Series Event",
        LocalDateTime.of(eventDate.plusDays(12), eventStartTime),
        LocalDateTime.of(eventDate.plusDays(12), eventEndTime));

    // check if the series events are copied to the correct dates
    assertEquals(eventDate.plusDays(10), copiedFirstEventOfSeries.getStartDate());
    assertEquals(eventDate.plusDays(11), copiedSecondEventOfSeries.getStartDate());
    assertEquals(eventDate.plusDays(12), copiedThirdEventOfSeries.getStartDate());

    Event copiedSampleEvent = calendar.findSingleEvent("Workshop",
        LocalDateTime.of(eventDate.plusDays(10), LocalTime.of(16, 0)),
        LocalDateTime.of(eventDate.plusDays(10), LocalTime.of(17, 0)));

    // check if the single event is copied to the correct date
    assertEquals(eventDate.plusDays(10), copiedSampleEvent.getStartDate());

    // check if the series events are still part of the same series
    assertNotNull(copiedFirstEventOfSeries.getSeriesId());
    String seriesId = copiedFirstEventOfSeries.getSeriesId();
    assertEquals(seriesId, copiedSecondEventOfSeries.getSeriesId());
    assertEquals(seriesId, copiedThirdEventOfSeries.getSeriesId());
  }

  @Test
  public void testCopyEventsInRangeOnlySeriesDifferentCalendarPreservesDuration() {
    Event twoHourEvent = EventImpl.getBuilder()
        .subject("Long Session")
        .from(date, LocalTime.of(9, 0))
        .to(date, LocalTime.of(11, 0))
        .build();
    calendar.addEvent(twoHourEvent);

    Set<Event> eventsToCopy = Collections.singleton(twoHourEvent);
    calendar.copyEventsInRange(calendar2, eventsToCopy, date, date.plusDays(5));

    Event copiedEvent = calendar2.getAllEvents().iterator().next();
    assertEquals(copiedEvent.getStartTime().plusHours(2), copiedEvent.getEndTime());
  }

  @Test
  public void testCopyEventsInRangeSameCalendarPreservesDuration() {
    Event twoHourEvent = EventImpl.getBuilder()
        .subject("Long Session")
        .from(date, LocalTime.of(9, 0))
        .to(date, LocalTime.of(11, 0))
        .build();
    calendar.addEvent(twoHourEvent);
    int originalSize = calendar.getAllEvents().size();

    Set<Event> eventsToCopy = Collections.singleton(twoHourEvent);
    calendar.copyEventsInRange(calendar, eventsToCopy, date, date.plusDays(5));

    assertEquals(originalSize + 1, calendar.getAllEvents().size());

    Event copiedEvent = calendar.findSingleEvent("Long Session",
        LocalDateTime.of(date.plusDays(5), LocalTime.of(9, 0)),
        LocalDateTime.of(date.plusDays(5), LocalTime.of(11, 0)));

    assertNotNull(copiedEvent);
    assertEquals(copiedEvent.getStartTime().plusHours(2), copiedEvent.getEndTime());
  }

  @Test
  public void testCopyEventsInRangeDifferentCalendarSeriesEventRetainsStatus() {
    Event prototype = EventImpl.getBuilder()
        .subject("Series Event")
        .from(date, startTime)
        .to(date, endTime)
        .build();
    EventSeriesImpl series = new EventSeriesImpl(prototype, date,
        new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3));
    calendar.addEventSeries(series);

    Set<Event> allEvents = calendar.getAllEvents();
    calendar.copyEventsInRange(calendar2, allEvents, date, date.plusDays(10));

    for (Event e : calendar2.getAllEvents()) {
      assertNotNull(e.getSeriesId());
      assertTrue(e.isPartOfSeries());
    }
  }

  @Test
  public void testCopyEventsOnDifferentCalendarSpansMidnight() {
    // Event spans across midnight in different calendar after timezone conversion
    // America/New_York to Asia/Kolkata on given date has 10.5 hours difference
    LocalDate eventDate = LocalDate.of(2025, 11, 5);
    Event eveningEvent = EventImpl.getBuilder()
        .subject("Evening Event")
        .from(eventDate, LocalTime.of(13, 0))
        .to(eventDate, LocalTime.of(15, 0))
        .build();
    calendar.addEvent(eveningEvent);

    Set<Event> eventsToCopy = Collections.singleton(eveningEvent);
    calendar.copyEventsOn(calendar2, eventsToCopy, eventDate, eventDate.plusDays(5));

    Event copiedEvent = calendar2.getAllEvents().iterator().next();
    assertEquals(eventDate.plusDays(5), copiedEvent.getStartDate());
    assertEquals(LocalTime.of(23, 30), copiedEvent.getStartTime());
    assertEquals(eventDate.plusDays(6), copiedEvent.getEndDate());
    assertEquals(LocalTime.of(1, 30), copiedEvent.getEndTime());
  }

  @Test
  public void testCopyEventsOnSameCalendarSpansMidnight() {
    LocalDate eventDate = LocalDate.of(2025, 11, 5);
    Event eveningEvent = EventImpl.getBuilder()
        .subject("Evening Event")
        .from(eventDate, LocalTime.of(23, 0))
        .to(eventDate.plusDays(1), LocalTime.of(1, 0))
        .build();
    calendar.addEvent(eveningEvent);
    int originalSize = calendar.getAllEvents().size();

    Set<Event> eventsToCopy = Collections.singleton(eveningEvent);
    calendar.copyEventsOn(calendar, eventsToCopy, eventDate, eventDate.plusDays(5));

    assertEquals(originalSize + 1, calendar.getAllEvents().size());

    Event copiedEvent = calendar.findSingleEvent("Evening Event",
        LocalDateTime.of(eventDate.plusDays(5), LocalTime.of(23, 0)),
        LocalDateTime.of(eventDate.plusDays(6), LocalTime.of(1, 0)));

    assertNotNull(copiedEvent);
    assertEquals(eventDate.plusDays(5), copiedEvent.getStartDate());
    assertEquals(LocalTime.of(23, 0), copiedEvent.getStartTime());
    assertEquals(eventDate.plusDays(6), copiedEvent.getEndDate());
    assertEquals(LocalTime.of(1, 0), copiedEvent.getEndTime());
  }

  @Test
  public void testCopyEventsInRangeDifferentCalendarSpansMidnight() {
    // Event spans across midnight in different calendar after timezone conversion
    // America/New_York to Asia/Kolkata on given date has 10.5 hours difference
    LocalDate eventDate = LocalDate.of(2025, 11, 5);
    Event eveningEvent = EventImpl.getBuilder()
        .subject("Evening Event")
        .from(eventDate, LocalTime.of(13, 0))
        .to(eventDate, LocalTime.of(15, 0))
        .build();
    calendar.addEvent(eveningEvent);

    Set<Event> eventsToCopy = Collections.singleton(eveningEvent);
    calendar.copyEventsInRange(calendar2, eventsToCopy, eventDate, eventDate.plusDays(5));

    Event copiedEvent = calendar2.getAllEvents().iterator().next();
    assertEquals(eventDate.plusDays(5), copiedEvent.getStartDate());
    assertEquals(LocalTime.of(23, 30), copiedEvent.getStartTime());
    assertEquals(eventDate.plusDays(6), copiedEvent.getEndDate());
    assertEquals(LocalTime.of(1, 30), copiedEvent.getEndTime());
  }

  @Test
  public void testCopyEventsInRangeSameCalendarSpansMidnight() {
    LocalDate eventDate = LocalDate.of(2025, 11, 5);
    Event eveningEvent = EventImpl.getBuilder()
        .subject("Evening Event")
        .from(eventDate, LocalTime.of(23, 0))
        .to(eventDate.plusDays(1), LocalTime.of(1, 0))
        .build();
    calendar.addEvent(eveningEvent);
    int originalSize = calendar.getAllEvents().size();

    Set<Event> eventsToCopy = Collections.singleton(eveningEvent);
    calendar.copyEventsInRange(calendar, eventsToCopy, eventDate, eventDate.plusDays(5));

    assertEquals(originalSize + 1, calendar.getAllEvents().size());

    Event copiedEvent = calendar.findSingleEvent("Evening Event",
        LocalDateTime.of(eventDate.plusDays(5), LocalTime.of(23, 0)),
        LocalDateTime.of(eventDate.plusDays(6), LocalTime.of(1, 0)));

    assertNotNull(copiedEvent);
    assertEquals(eventDate.plusDays(5), copiedEvent.getStartDate());
    assertEquals(LocalTime.of(23, 0), copiedEvent.getStartTime());
    assertEquals(eventDate.plusDays(6), copiedEvent.getEndDate());
    assertEquals(LocalTime.of(1, 0), copiedEvent.getEndTime());
  }

  @Test
  public void testCopyEventsInRangeDifferentCalendarPartialSeries() {
    LocalDate eventDate = LocalDate.of(2025, 11, 5);
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 5);
    Event prototype = EventImpl.getBuilder()
        .subject("Daily Meeting")
        .from(eventDate, startTime)
        .to(eventDate, endTime)
        .build();
    EventSeriesImpl series = new EventSeriesImpl(prototype, date, rule);
    calendar.addEventSeries(series);

    List<Event> allSeriesEvents = calendar.getAllEvents()
        .stream()
        .sorted()
        .collect(Collectors.toList());

    // Copy only 4 events from the series (exclude the last one)
    Set<Event> eventsToCopy = new TreeSet<>();
    for (int i = 0; i < 4; i++) {
      eventsToCopy.add(allSeriesEvents.get(i));
    }

    calendar.copyEventsInRange(calendar2, eventsToCopy, eventDate, eventDate.plusDays(10));

    assertEquals(4, calendar2.getAllEvents().size());

    Event copiedEvent = calendar
        .filterEvents(e -> e.getSubject().equals("Daily Meeting")).iterator().next();
    assertNotNull(copiedEvent.getSeriesId());
    String seriesId = copiedEvent.getSeriesId();

    // copied events should be part of the same series
    for (Event e : calendar2.getAllEvents()) {
      assertEquals(seriesId, e.getSeriesId());
    }
  }

  @Test
  public void testCopyEventsInRangeSameCalendarPartialSeries() {
    LocalDate eventDate = LocalDate.of(2025, 11, 5);
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 5);
    Event prototype = EventImpl.getBuilder()
        .subject("Daily Meeting")
        .from(eventDate, startTime)
        .to(eventDate, endTime)
        .build();
    EventSeriesImpl series = new EventSeriesImpl(prototype, date, rule);
    calendar.addEventSeries(series);

    List<Event> allSeriesEvents = calendar.getAllEvents()
        .stream()
        .sorted()
        .collect(Collectors.toList());

    // Copy only 4 events from the series (exclude the last one)
    Set<Event> eventsToCopy = new TreeSet<>();
    for (int i = 0; i < 4; i++) {
      eventsToCopy.add(allSeriesEvents.get(i));
    }

    calendar.copyEventsInRange(calendar, eventsToCopy, eventDate, eventDate.plusDays(10));

    // existing 5 events of the series and the copied 4 events of the series
    assertEquals(9, calendar.getAllEvents().size());

    Set<Event> copiedEvents = calendar.filterEvents(
        new FilterByDateRange(eventDate.plusDays(10),
            eventDate.plusDays(20)));
    Event copiedEvent = calendar.findSingleEvent("Daily Meeting",
        LocalDateTime.of(eventDate.plusDays(10), startTime),
        LocalDateTime.of(eventDate.plusDays(10), endTime));
    String seriesId = copiedEvent.getSeriesId();
    // copied events should be part of the same series
    for (Event e : copiedEvents) {
      assertEquals(seriesId, e.getSeriesId());
    }
  }

  @Test
  public void testCopyEventDuplicateDifferentCalendarFails() {
    Event sampleEvent = EventImpl.getBuilder()
        .subject("Workshop")
        .from(date, LocalTime.of(10, 0))
        .to(date, LocalTime.of(12, 0))
        .build();
    calendar.addEvent(sampleEvent);

    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 14, 0);
    calendar.copyEvent(calendar2, sampleEvent, targetStart);

    assertThrows(IllegalArgumentException.class,
        () -> calendar.copyEvent(calendar2, sampleEvent, targetStart));
  }

  @Test
  public void testCopyEventDuplicateSameCalendarFails() {
    Event sampleEvent = EventImpl.getBuilder()
        .subject("Workshop")
        .from(date, LocalTime.of(10, 0))
        .to(date, LocalTime.of(12, 0))
        .build();
    calendar.addEvent(sampleEvent);

    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 1, 14, 0);
    calendar.copyEvent(calendar, sampleEvent, targetStart);

    assertThrows(IllegalArgumentException.class,
        () -> calendar.copyEvent(calendar, sampleEvent, targetStart));

  }
}
