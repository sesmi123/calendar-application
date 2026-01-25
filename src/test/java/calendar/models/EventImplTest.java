package calendar.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import calendar.models.enums.Location;
import calendar.models.enums.Status;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link EventImpl}.
 */
public class EventImplTest {

  private LocalDate startDate;
  private LocalDate endDate;
  private LocalTime startTime;
  private LocalTime endTime;

  /**
   * Setup common variables for tetsing.
   */
  @Before
  public void setUp() {
    startDate = LocalDate.of(2024, 10, 1);
    endDate = LocalDate.of(2024, 10, 1);
    startTime = LocalTime.of(9, 0);
    endTime = LocalTime.of(10, 0);
  }

  @Test
  public void testBuildAllDayEventSuccessfully() {
    EventImpl event = EventImpl.getBuilder().subject("Meeting").on(startDate).build();

    assertEquals("Meeting", event.getSubject());
    assertEquals(startDate, event.getStartDate());
    assertEquals(startDate, event.getEndDate());
    assertEquals(LocalTime.of(8, 0), event.getStartTime());
    assertEquals(LocalTime.of(17, 0), event.getEndTime());
  }

  @Test
  public void testBuildEventSuccessfully() {
    EventImpl event =
        EventImpl.getBuilder().subject("Meeting").from(startDate, startTime).to(endDate, endTime)
            .description("Project discussion").location(Location.PHYSICAL).status(Status.PUBLIC)
            .build();

    assertEquals("Meeting", event.getSubject());
    assertEquals(startDate, event.getStartDate());
    assertEquals(endDate, event.getEndDate());
    assertEquals(startTime, event.getStartTime());
    assertEquals(endTime, event.getEndTime());
    assertEquals("Project discussion", event.getDescription());
    assertEquals(Location.PHYSICAL, event.getLocation());
    assertEquals(Status.PUBLIC, event.getStatus());
    assertNull(event.getSeriesId());
    assertFalse(event.isPartOfSeries());
  }

  @Test(expected = NullPointerException.class)
  public void testBuildEventWithNullSubjectThrowsException() {
    EventImpl.getBuilder().subject(null).from(startDate, startTime).to(endDate, endTime).build();
  }

  @Test(expected = NullPointerException.class)
  public void testBuildEventWithNullStartDateThrowsException() {
    EventImpl.getBuilder().subject("Invalid Event").from(null, startTime).to(endDate, endTime)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEndBeforeStartThrowsException() {
    EventImpl.getBuilder().subject("Bad Event").from(endDate, endTime).to(startDate, startTime)
        .build();
  }

  @Test
  public void testEqualsWithSameInstance() {
    EventImpl e1 =
        EventImpl.getBuilder().subject("Meeting").from(startDate, startTime).to(endDate, endTime)
            .build();

    assertEquals(e1, e1);
    assertEquals(e1.hashCode(), e1.hashCode());
  }

  @Test
  public void testEqualsWithSameSeriesId() {
    UUID u = UUID.randomUUID();
    EventImpl e1 =
        EventImpl.getBuilder().subject("Meeting").from(startDate, startTime).to(endDate, endTime)
            .seriesId(u.toString()).build();
    EventImpl e2 =
        EventImpl.getBuilder().subject("Meeting").from(startDate, startTime).to(endDate, endTime)
            .seriesId(u.toString()).build();

    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
  }

  @Test
  public void testEqualsWithDiffSeriesId() {
    UUID u1 = UUID.randomUUID();
    UUID u2 = UUID.randomUUID();
    EventImpl e1 =
        EventImpl.getBuilder().subject("Meeting").from(startDate, startTime).to(endDate, endTime)
            .seriesId(u1.toString()).build();
    EventImpl e2 =
        EventImpl.getBuilder().subject("Meeting").from(startDate, startTime).to(endDate, endTime)
            .seriesId(u2.toString()).build();

    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
  }

  @Test
  public void testEqualsWithDifferentTypeObject() {
    EventImpl e1 =
        EventImpl.getBuilder().subject("Meeting").from(startDate, startTime).to(endDate, endTime)
            .build();
    //noinspection SimplifiableAssertion
    assertFalse(e1.equals(100));
  }

  @Test
  public void testEqualsAndHashCode() {
    EventImpl e1 =
        EventImpl.getBuilder().subject("Meeting").from(startDate, startTime).to(endDate, endTime)
            .build();

    EventImpl e2 = EventImpl.getBuilder().subject("meeting")
        .from(startDate, startTime).to(endDate, endTime).build();

    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
  }

  @Test
  public void testEqualsWithDifferentSubject() {
    EventImpl e1 =
        EventImpl.getBuilder().subject("Meeting").from(startDate, startTime).to(endDate, endTime)
            .build();

    EventImpl e2 =
        EventImpl.getBuilder().subject("meeting2").from(startDate, startTime).to(endDate, endTime)
            .build();

    assertNotEquals(e1, e2);
  }

  @Test
  public void testEqualsWithDifferentStartDate() {
    EventImpl e1 =
        EventImpl.getBuilder().subject("Meeting").from(startDate, startTime).to(endDate, endTime)
            .build();

    EventImpl e2 = EventImpl.getBuilder().subject("meeting").from(startDate.plusDays(-1), startTime)
        .to(endDate, endTime).build();

    assertNotEquals(e1, e2);
  }

  @Test
  public void testEqualsWithDifferentEndDate() {
    EventImpl e1 =
        EventImpl.getBuilder().subject("Meeting").from(startDate, startTime).to(endDate, endTime)
            .build();

    EventImpl e2 = EventImpl.getBuilder().subject("meeting").from(startDate, startTime)
        .to(endDate.plusDays(1), endTime).build();

    assertNotEquals(e1, e2);
  }

  @Test
  public void testEqualsWithDifferentEndTime() {
    EventImpl e1 =
        EventImpl.getBuilder().subject("Meeting").from(startDate, startTime).to(endDate, endTime)
            .build();

    EventImpl e2 = EventImpl.getBuilder().subject("meeting").from(startDate, startTime)
        .to(endDate, endTime.plusMinutes(1)).build();

    assertNotEquals(e1, e2);
  }

  @Test
  public void testNotEqualDifferentTimes() {
    EventImpl e1 =
        EventImpl.getBuilder().subject("Meeting").from(startDate, startTime).to(endDate, endTime)
            .build();

    EventImpl e2 = EventImpl.getBuilder().subject("Meeting").from(startDate, startTime.plusHours(1))
        .to(endDate, endTime.plusHours(1)).build();

    assertNotEquals(e1, e2);
  }

  @Test
  public void testToStringIncludesKeyDetails() {
    EventImpl event =
        EventImpl.getBuilder().subject("Workshop").from(startDate, startTime).to(endDate, endTime)
            .location(Location.ONLINE).build();

    String expectedString =
        "Workshop starting on " + startDate + " at " + startTime + ", ending on " + endDate
            + " at " + endTime + ". Location: ONLINE";
    String text = event.toString();
    assertEquals(expectedString, text);
  }

  @Test
  public void testToStringWithoutLocation() {
    EventImpl event =
        EventImpl.getBuilder().subject("Workshop").from(startDate, startTime).to(endDate, endTime)
            .build();
    String expectedString =
        "Workshop starting on " + startDate + " at " + startTime + ", ending on " + endDate
            + " at " + endTime;
    String text = event.toString();
    assertEquals(expectedString, text);
  }

  @Test
  public void testToBuilderCreatesEquivalentEvent() {
    EventImpl original =
        EventImpl.getBuilder().subject("Conference").from(startDate, startTime).to(endDate, endTime)
            .description("Annual event").location(Location.PHYSICAL).status(Status.PRIVATE).build();

    EventImpl copy = original.toBuilder().build();

    assertEquals(original, copy);
    assertEquals(original.getDescription(), copy.getDescription());
    assertEquals(original.getLocation(), copy.getLocation());
    assertEquals(original.getStatus(), copy.getStatus());
  }

  @Test
  public void testDefaultValuesInBuilder() {
    EventImpl event = EventImpl.getBuilder().on(startDate).build();

    assertEquals("(No subject)", event.getSubject());
    assertEquals(LocalTime.of(8, 0), event.getStartTime());
    assertEquals(LocalTime.of(17, 0), event.getEndTime());
  }

  @Test(expected = NullPointerException.class)
  public void testCompareToNullThrowsException() {
    EventImpl e1 =
        EventImpl.getBuilder().subject("Meeting").from(startDate, startTime).to(endDate, endTime)
            .build();

    e1.compareTo(null);
  }

  @Test
  public void testCompareToOrdersBySubjectFirst() {
    EventImpl e1 = EventImpl.getBuilder().subject("All_Employees_Meet").from(startDate, startTime)
        .to(endDate, endTime).build();

    EventImpl e2 =
        EventImpl.getBuilder().subject("ZZZ").from(startDate, startTime).to(endDate, endTime)
            .build();

    assertTrue(e1.compareTo(e2) < 0);
    assertTrue(e2.compareTo(e1) > 0);
  }

  @Test
  public void testCompareToDifferentStartDateWhenSubjectSame() {
    EventImpl e1 =
        EventImpl.getBuilder().subject("Meeting").from(startDate, startTime).to(endDate, endTime)
            .build();

    EventImpl e2 = EventImpl.getBuilder().subject("Meeting").from(startDate.plusDays(5), startTime)
        .to(endDate.plusDays(5), endTime).build();

    assertTrue(e1.compareTo(e2) < 0);
  }

  @Test
  public void testCompareToDifferentStartTime() {
    EventImpl e1 = EventImpl.getBuilder()
        .subject("Meeting")
        .from(startDate, LocalTime.of(9, 0))
        .to(endDate, endTime)
        .build();

    EventImpl e2 = EventImpl.getBuilder()
        .subject("Meeting")
        .from(startDate, LocalTime.of(9, 30))
        .to(endDate, endTime)
        .build();

    assertTrue(e1.compareTo(e2) < 0);
  }
}

