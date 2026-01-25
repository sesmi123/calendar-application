package calendar.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link EventSeriesImpl}.
 */
public class EventSeriesImplTest {

  private EventImpl prototypeEvent;
  private RecurrenceRule mockRule;
  private LocalDate startDate;

  /**
   * Set up mocks for testing.
   */
  @Before
  public void setUp() {
    startDate = LocalDate.of(2024, 10, 1);

    prototypeEvent = EventImpl.getBuilder()
        .subject("Daily Standup")
        .from(startDate, LocalTime.of(9, 0))
        .to(startDate, LocalTime.of(9, 30))
        .build();

    mockRule = new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 3);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorThrowsWhenPrototypeEventIsNull() {
    new EventSeriesImpl(null, startDate, mockRule);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorThrowsWhenStartDateIsNull() {
    new EventSeriesImpl(prototypeEvent, null, mockRule);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorThrowsWhenRecurrenceRuleIsNull() {
    new EventSeriesImpl(prototypeEvent, startDate, null);
  }

  @Test
  public void testEmptyRecurrenceRuleGeneratesNoEvents() {
    RecurrenceRule emptyRule = new RecurrenceRule() {
      @Override
      public List<LocalDate> generateDates(LocalDate startDate) {
        return Collections.emptyList();
      }

      @Override
      public Set<DayOfWeek> getDaysOfWeek() {
        return Collections.emptySet();
      }

      @Override
      public LocalDate getEndDate() {
        return null;
      }

      @Override
      public Integer getOccurrences() {
        return 0;
      }
    };

    EventSeriesImpl series = new EventSeriesImpl(prototypeEvent, startDate, emptyRule);
    Set<Event> events = series.generateEvents();

    assertTrue(events.isEmpty());
  }

  @Test
  public void testGeneratedEventsAreIndependentObjects() {
    EventSeriesImpl series = new EventSeriesImpl(prototypeEvent, startDate, mockRule);
    Set<Event> events = series.generateEvents();

    List<Event> eventList = new ArrayList<>(events);

    assertNotSame(eventList.get(0), eventList.get(1));
    assertNotSame(eventList.get(1), eventList.get(2));
  }

  @Test
  public void testGeneratedEventsBasedOnCount() {
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class), 5);
    EventSeriesImpl series = new EventSeriesImpl(prototypeEvent, startDate, rule);
    Set<Event> events = series.generateEvents();

    List<Event> eventList = new ArrayList<>(events);

    assertEquals(5, eventList.size());
  }

  @Test
  public void testGeneratedEventsBasedOnEndDate() {
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.allOf(DayOfWeek.class),
        startDate.plusDays(4));
    EventSeriesImpl series = new EventSeriesImpl(prototypeEvent, startDate, rule);
    Set<Event> events = series.generateEvents();

    List<Event> eventList = new ArrayList<>(events);

    assertEquals(5, eventList.size());
  }

  @Test
  public void testGeneratedEventsAreOnCorrectDays() {
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
        startDate.plusDays(3));
    EventSeriesImpl series = new EventSeriesImpl(prototypeEvent, startDate, rule);
    Set<Event> events = series.generateEvents();

    List<Event> eventList = new ArrayList<>(events);
    eventList.sort((e1, e2) -> e1.getStartDate().compareTo(e2.getStartDate()));

    assertEquals(2, eventList.size());
    assertEquals(startDate, eventList.get(0).getStartDate());
    assertEquals(startDate.plusDays(2), eventList.get(1).getStartDate());
  }

  @Test
  public void testSeriesIdOnEvents() {
    EventSeriesImpl series = new EventSeriesImpl(prototypeEvent, startDate, mockRule);
    Set<Event> events = series.generateEvents();

    List<Event> eventList = new ArrayList<>(events);

    assertNotNull(eventList.get(0).getSeriesId());
    assertNotNull(eventList.get(1).getSeriesId());
    assertNotNull(eventList.get(2).getSeriesId());

    assertTrue(eventList.get(0).isPartOfSeries());
    assertTrue(eventList.get(1).isPartOfSeries());
    assertTrue(eventList.get(2).isPartOfSeries());
  }

  @Test
  public void testConstructorUsesExistingSeriesIdFromPrototype() {
    String existingSeriesId = "series789";
    EventImpl prototypeWithSeriesId = EventImpl.getBuilder()
        .subject("Daily Standup")
        .from(startDate, LocalTime.of(9, 0))
        .to(startDate, LocalTime.of(9, 30))
        .seriesId(existingSeriesId)
        .build();

    EventSeriesImpl series = new EventSeriesImpl(prototypeWithSeriesId, startDate, mockRule);

    assertEquals(existingSeriesId, series.getSeriesId());
  }
}
