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
public class FilterByDateTest {

  private LocalDate filterDate;
  private Event eventOnDate;
  private Event eventOnOtherDate;
  private Event eventEndsOnDate;
  private Event eventInProgressOnDate;

  /**
   * Test setup.
   */
  @Before
  public void setUp() {
    filterDate = LocalDate.of(2025, 10, 20);

    eventOnDate = EventImpl.getBuilder()
        .subject("Event On Filter Date")
        .from(filterDate, LocalTime.of(10, 0))
        .to(filterDate, LocalTime.of(11, 0))
        .build();

    eventEndsOnDate = EventImpl.getBuilder()
        .subject("Event On Filter Date")
        .from(filterDate.plusDays(-1), LocalTime.of(10, 0))
        .to(filterDate, LocalTime.of(11, 0))
        .build();

    eventInProgressOnDate = EventImpl.getBuilder()
        .subject("Event On Filter Date")
        .from(filterDate.plusDays(-1), LocalTime.of(10, 0))
        .to(filterDate.plusDays(1), LocalTime.of(11, 0))
        .build();

    eventOnOtherDate = EventImpl.getBuilder()
        .subject("Event On Other Date")
        .from(LocalDate.of(2025, 10, 21), LocalTime.of(10, 0))
        .to(LocalDate.of(2025, 10, 21), LocalTime.of(11, 0))
        .build();
  }

  @Test
  public void testEventMatchesStartDate() {
    FilterByDate filter = new FilterByDate(filterDate);
    assertTrue(filter.evaluate(eventOnDate));
  }

  @Test
  public void testEventEndsOnDate() {
    FilterByDate filter = new FilterByDate(filterDate);
    assertTrue(filter.evaluate(eventEndsOnDate));
  }

  @Test
  public void testEventInProgressOnDate() {
    FilterByDate filter = new FilterByDate(filterDate);
    assertTrue(filter.evaluate(eventInProgressOnDate));
  }

  @Test
  public void testEventDoesNotMatchStartDate() {
    FilterByDate filter = new FilterByDate(filterDate);
    assertFalse(filter.evaluate(eventOnOtherDate));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullEventThrowsException() {
    FilterByDate filter = new FilterByDate(filterDate);
    filter.evaluate(null);
  }

  @Test(expected = NullPointerException.class)
  public void testNullConstructorDateThrowsException() {
    new FilterByDate(null);
  }
}
