package calendar.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

/**
 * Unit tests for {@link RecurrenceRuleImpl}.
 */
public class RecurrenceRuleImplTest {

  @Test
  public void testGenerateDatesWithEndDateGeneratesCorrectDays() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
    LocalDate start = LocalDate.of(2025, 10, 20);
    LocalDate end = LocalDate.of(2025, 10, 30);

    RecurrenceRuleImpl rule = new RecurrenceRuleImpl(days, end);
    List<LocalDate> dates = rule.generateDates(start);

    List<LocalDate> expected = Arrays.asList(
        LocalDate.of(2025, 10, 20),
        LocalDate.of(2025, 10, 22),
        LocalDate.of(2025, 10, 27),
        LocalDate.of(2025, 10, 29)
    );

    assertEquals(expected, dates);
  }

  @Test
  public void testGenerateDatesWithOccurrencesGeneratesCorrectCount() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.TUESDAY);
    LocalDate start = LocalDate.of(2025, 10, 21);

    RecurrenceRuleImpl rule = new RecurrenceRuleImpl(days, 3);
    List<LocalDate> dates = rule.generateDates(start);

    List<LocalDate> expected = Arrays.asList(
        LocalDate.of(2025, 10, 21),
        LocalDate.of(2025, 10, 28),
        LocalDate.of(2025, 11, 4)
    );

    assertEquals(3, dates.size());
    assertEquals(expected, dates);
  }

  @Test
  public void testGenerateDatesSkipsNonMatchingDays() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.FRIDAY);
    LocalDate start = LocalDate.of(2025, 10, 20);
    RecurrenceRuleImpl rule = new RecurrenceRuleImpl(days, 2);

    List<LocalDate> dates = rule.generateDates(start);
    assertEquals(Arrays.asList(
        LocalDate.of(2025, 10, 24),
        LocalDate.of(2025, 10, 31)
    ), dates);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorEmptyDaysOfWeekThrowsException() {
    new RecurrenceRuleImpl(Collections.emptySet(), LocalDate.of(2025, 12, 31));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNoEndDateOrOccurrencesThrowsException() {
    new RecurrenceRuleImpl(EnumSet.of(DayOfWeek.MONDAY), (LocalDate) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNegativeOccurrence() {
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.of(DayOfWeek.MONDAY), -1);
  }

  @Test
  public void testConstructorWithZeroOccurrence() {
    RecurrenceRule rule = new RecurrenceRuleImpl(EnumSet.of(DayOfWeek.MONDAY), 0);
    LocalDate start = LocalDate.of(2025, 10, 20);
    List<LocalDate> result = rule.generateDates(start);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGenerateDatesWithEndDateBeforeStartEmptyList() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.MONDAY);
    LocalDate start = LocalDate.of(2025, 10, 20);
    LocalDate end = LocalDate.of(2025, 10, 10);
    RecurrenceRuleImpl rule = new RecurrenceRuleImpl(days, end);

    List<LocalDate> result = rule.generateDates(start);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGenerateDatesStopsAtEndDate() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY);
    LocalDate start = LocalDate.of(2025, 10, 20);
    LocalDate end = LocalDate.of(2025, 10, 23);
    RecurrenceRuleImpl rule = new RecurrenceRuleImpl(days, end);

    List<LocalDate> result = rule.generateDates(start);
    assertEquals(Arrays.asList(
        LocalDate.of(2025, 10, 20),
        LocalDate.of(2025, 10, 23)
    ), result);
  }
}
