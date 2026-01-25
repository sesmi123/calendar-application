package calendar.models;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests filter condition - FilterByDateRangeTest.
 */
public class FilterByDateRangeTest {

  private Event singleDayEvent;
  private Event multiDayEvent;
  private Event pastEvent;
  private Event futureEvent;

  /**
   * Test setup.
   */
  @Before
  public void setUp() {
    LocalTime start = LocalTime.of(9, 0);
    LocalTime end = LocalTime.of(10, 0);
    singleDayEvent = EventImpl.getBuilder()
        .from(LocalDate.of(2025, 11, 10), start)
        .to(LocalDate.of(2025, 11, 10), end)
        .subject("Single Day Event")
        .build();

    multiDayEvent = EventImpl.getBuilder()
        .from(LocalDate.of(2025, 11, 8), start)
        .to(LocalDate.of(2025, 11, 12), end)
        .subject("Multi Day Event")
        .build();

    pastEvent = EventImpl.getBuilder()
        .from(LocalDate.of(2025, 10, 1), start)
        .to(LocalDate.of(2025, 10, 5), end)
        .subject("Past Event")
        .build();

    futureEvent = EventImpl.getBuilder()
        .from(LocalDate.of(2025, 12, 1), start)
        .to(LocalDate.of(2025, 12, 5), end)
        .subject("Future Event")
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorNullStartThrowsException() {
    new FilterByDateRange(null, LocalDate.now());
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorNullEndThrowsException() {
    new FilterByDateRange(LocalDate.now(), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEvaluateNullEventThrowsException() {
    FilterByDateRange filter = new FilterByDateRange(LocalDate.now(), LocalDate.now().plusDays(1));
    filter.evaluate(null);
  }

  @Test
  public void testEvaluateEventWithinRange() {
    FilterByDateRange filter = new FilterByDateRange(LocalDate.of(2025, 11, 9),
        LocalDate.of(2025, 11, 11));
    assertTrue(filter.evaluate(singleDayEvent));
    assertTrue(filter.evaluate(multiDayEvent));
  }

  @Test
  public void testEvaluateEventOutsideRange() {
    FilterByDateRange filter = new FilterByDateRange(LocalDate.of(2025, 11, 11),
        LocalDate.of(2025, 11, 15));
    assertFalse(filter.evaluate(singleDayEvent));
    assertFalse(filter.evaluate(pastEvent));
  }

  @Test
  public void testEvaluateEventOverlapStart() {
    FilterByDateRange filter = new FilterByDateRange(LocalDate.of(2025, 11, 10),
        LocalDate.of(2025, 11, 15));
    assertTrue(filter.evaluate(singleDayEvent)); // starts on range start
    assertTrue(filter.evaluate(multiDayEvent));  // overlaps
  }

  @Test
  public void testEvaluateEventOverlapEnd() {
    FilterByDateRange filter = new FilterByDateRange(LocalDate.of(2025, 11, 5),
        LocalDate.of(2025, 11, 10));
    assertTrue(filter.evaluate(singleDayEvent)); // ends on range end
    assertTrue(filter.evaluate(multiDayEvent));  // overlaps
  }
}
