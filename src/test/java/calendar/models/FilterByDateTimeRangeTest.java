package calendar.models;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests filter condition - FilterByDateTimeRangeTest.
 */
public class FilterByDateTimeRangeTest {

  private Event eventWithinRange;
  private Event eventBeforeRange;
  private Event eventAfterRange;
  private Event eventOverlappingStart;
  private Event eventOverlappingEnd;
  private LocalDateTime rangeStart;
  private LocalDateTime rangeEnd;

  /**
   * Test setup.
   */
  @Before
  public void setUp() {
    rangeStart = LocalDateTime.of(2025, 10, 20, 10, 0);
    rangeEnd = LocalDateTime.of(2025, 10, 20, 12, 0);

    eventWithinRange = EventImpl.getBuilder().from(LocalDate.of(2025, 10, 20), LocalTime.of(10, 30))
        .to(LocalDate.of(2025, 10, 20), LocalTime.of(11, 30)).subject("Within Range").build();

    eventBeforeRange = EventImpl.getBuilder().from(LocalDate.of(2025, 10, 20), LocalTime.of(8, 0))
        .to(LocalDate.of(2025, 10, 20), LocalTime.of(9, 0)).subject("Before Range").build();

    eventAfterRange = EventImpl.getBuilder().from(LocalDate.of(2025, 10, 20), LocalTime.of(13, 0))
        .to(LocalDate.of(2025, 10, 20), LocalTime.of(14, 0)).subject("After Range").build();

    eventOverlappingStart =
        EventImpl.getBuilder().from(LocalDate.of(2025, 10, 20), LocalTime.of(9, 30))
            .to(LocalDate.of(2025, 10, 20), LocalTime.of(10, 30)).subject("Overlap Start").build();

    eventOverlappingEnd =
        EventImpl.getBuilder().from(LocalDate.of(2025, 10, 20), LocalTime.of(11, 30))
            .to(LocalDate.of(2025, 10, 20), LocalTime.of(12, 30)).subject("Overlap End").build();
  }

  @Test
  public void testEventWithinRange() {
    FilterByDateTimeRange filter = new FilterByDateTimeRange(rangeStart, rangeEnd);
    assertTrue(filter.evaluate(eventWithinRange));
  }

  @Test
  public void testEventBeforeRange() {
    FilterByDateTimeRange filter = new FilterByDateTimeRange(rangeStart, rangeEnd);
    assertFalse(filter.evaluate(eventBeforeRange));
  }

  @Test
  public void testEventAfterRange() {
    FilterByDateTimeRange filter = new FilterByDateTimeRange(rangeStart, rangeEnd);
    assertFalse(filter.evaluate(eventAfterRange));
  }

  @Test
  public void testEventOverlappingStart() {
    FilterByDateTimeRange filter = new FilterByDateTimeRange(rangeStart, rangeEnd);
    assertTrue(filter.evaluate(eventOverlappingStart));
  }

  @Test
  public void testEventOverlappingEnd() {
    FilterByDateTimeRange filter = new FilterByDateTimeRange(rangeStart, rangeEnd);
    assertTrue(filter.evaluate(eventOverlappingEnd));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullEventThrowsException() {
    FilterByDateTimeRange filter = new FilterByDateTimeRange(rangeStart, rangeEnd);
    filter.evaluate(null);
  }

  @Test(expected = NullPointerException.class)
  public void testNullStartThrowsException() {
    new FilterByDateTimeRange(null, rangeEnd);
  }

  @Test(expected = NullPointerException.class)
  public void testNullEndThrowsException() {
    new FilterByDateTimeRange(rangeStart, null);
  }
}
