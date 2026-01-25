package calendar.controllers;

import static org.junit.Assert.assertEquals;

import calendar.controllers.mocks.MockCommandFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link CommandParserImpl}.
 */
public class CommandParserImplTest {

  private MockCommandFactory factory;
  private CommandParserImpl parser;
  private StringBuilder log;

  /**
   * Test setup.
   */
  @Before
  public void setUp() {
    log = new StringBuilder();
    factory = new MockCommandFactory(log, 99451);
    parser = new CommandParserImpl(factory);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseNullCommandThrowsException() throws Exception {
    parser.parse(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEmptyCommandThrowsException() throws Exception {
    parser.parse("   ");
  }

  @Test
  public void testParseEditCalendarTimezone() throws Exception {
    parser.parse(
        "edit calendar --name School --property timezone America/New_York");
    assertEquals("editCalendar School timezone America/New_York", log.toString());
  }

  @Test
  public void testParseEditCalendarName() throws Exception {
    parser.parse(
        "edit calendar --name School --property name Home");
    assertEquals("editCalendar School name Home", log.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditCalendarInvalidProperty() throws Exception {
    parser.parse(
        "edit calendar --name School --property invalidprop America/New_York");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditCalendarInvalidTimezone() throws Exception {
    parser.parse(
        "edit calendar --name School --property timezone area/location");
  }

  @Test
  public void testParseCreateCalendar() throws Exception {
    parser.parse(
        "create calendar --name School --timezone America/New_York");
    assertEquals("createCalendar School America/New_York", log.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateCalendarInvalidCommand() throws Exception {
    parser.parse(
        "create calendar --name --timezone America/New_York");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateCalendarInvalidTimezone() throws Exception {
    parser.parse(
        "create calendar --name Chores --timezone area/location");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateCalendarInvalidTimezone2() throws Exception {
    parser.parse(
        "create calendar --name Chores --timezone ");
  }

  @Test
  public void testParseUseCalendar() throws Exception {
    parser.parse(
        "use calendar --name School");
    assertEquals("useCalendar School", log.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseUseCalendarInvalidCommand() throws Exception {
    parser.parse("use calendar --name");
  }

  @Test
  public void testParseCreateEvent() throws Exception {
    parser.parse(
        "create event DoctorAppointment from 2025-11-02T15:00 to 2025-11-02T16:00");
    assertEquals("createEvent: DoctorAppointment from 2025-11-02T15:00 to 2025-11-02T16:00",
        log.toString());
  }

  @Test
  public void testParseCreateEventLongSubject() throws Exception {
    parser.parse(
        "create event \"Doctor Appointment\" from 2025-11-02T15:00 to 2025-11-02T16:00");
    assertEquals("createEvent: Doctor Appointment from 2025-11-02T15:00 to 2025-11-02T16:00",
        log.toString());
  }

  @Test
  public void testParseCreateAllDayEvent() throws Exception {
    parser.parse("create event DoctorAppointment on 2025-11-02");
    assertEquals("createAllDayEvent: DoctorAppointment on 2025-11-02", log.toString());
  }

  @Test
  public void testParseCreateAllDayEventRecurringEventByCount() throws Exception {
    parser.parse(
        "create event DoctorAppointment on 2025-11-02 repeats MTF for 3 times");
    assertEquals("createEventSeries", log.toString());
  }

  @Test
  public void testParseCreateAllDayEventRecurringEventByEndDate() throws Exception {
    parser.parse(
        "create event DoctorAppointment on 2025-11-02 repeats MTF until 2025-11-30");
    assertEquals("createEventSeries", log.toString());
  }

  @Test
  public void testParseCreateAllDayEventLongSubject() throws Exception {
    parser.parse("create event \"Doctor Appointment\" on 2025-11-02");
    assertEquals("createAllDayEvent: Doctor Appointment on 2025-11-02", log.toString());
  }

  @Test
  public void testParseCreateAllDayEventLongSubjectRecurByCount() throws Exception {
    parser.parse(
        "create event \"Doctor Appointment\" on 2025-11-02 repeats MTF for 3 times");
    assertEquals("createEventSeries", log.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateEventInvalidDay() throws Exception {
    parser.parse(
        "create event \"Doctor Appointment\" on 2025-11-02 repeats XYZ for 3 times");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateEventNoDays() throws Exception {
    parser.parse(
        "create event \"Doctor Appointment\" on 2025-11-02 repeats  for 3 times");
  }

  @Test
  public void testParseCreateAllDayEventLongSubjectRecurUntilEndDate() throws Exception {
    parser.parse(
        "create event \"Doctor Appointment\" on 2025-11-02 repeats MTF until 2025-11-30");
    assertEquals("createEventSeries", log.toString());
  }

  @Test
  public void testParseCreateRecurrringEventByCount() throws Exception {
    parser.parse(
        "create event DoctorAppointment2 from 2025-11-02T15:00 to "
            + "2025-11-02T16:00 repeats MTF for 3 times");
    assertEquals("createEventSeries", log.toString());
  }

  @Test
  public void testParseCreateRecurrringEventByEndDate() throws Exception {
    parser.parse(
        "create event DoctorAppointment2 from 2025-11-02T15:00 to "
            + "2025-11-02T16:00 repeats MTF until 2025-11-30");
    assertEquals("createEventSeries", log.toString());
  }

  @Test
  public void testParseCreateRecurrringEventByCountLongSubject() throws Exception {
    parser.parse(
        "create event \"Doctor Appointment2\" from 2025-11-02T15:00 to "
            + "2025-11-02T16:00 repeats WRU for 3 times");
    assertEquals("createEventSeries", log.toString());
  }

  @Test
  public void testParseCreateRecurrringEventByEndDateLongSubject() throws Exception {
    parser.parse(
        "create event \"Doctor Appointment2\" from 2025-11-02T15:00 to "
            + "2025-11-02T16:00 repeats WRS until 2025-11-30");
    assertEquals("createEventSeries", log.toString());
  }

  @Test
  public void testParsePrintEventsOn() throws Exception {
    parser.parse("print events on 2025-10-29");
    assertEquals("printOn", log.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventInvalidCommand() throws Exception {
    parser.parse("edit event DoctorAppointment from 2025-11-02T15:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventInvalidPropertyCommand() throws Exception {
    parser.parse("edit event abcd DoctorAppointment from 2025-11-02T15:00 to "
        + "2025-11-02T16:00 with Doctor_Appointment");
  }

  @Test
  public void testEditSubjectOfEvent() throws Exception {
    parser.parse("edit event subject DoctorAppointment from 2025-11-02T15:00 to "
        + "2025-11-02T16:00 with Doctor_Appointment");
    assertEquals("editEvent: subject with Doctor_Appointment", log.toString());
  }

  @Test
  public void testEditLongSubjectOfEvent() throws Exception {
    parser.parse("edit event subject \"Doctor Appointment\" from 2025-11-02T15:00 to "
        + "2025-11-02T16:00 with Doctor_Appointment");
    assertEquals("editEvent: subject with Doctor_Appointment", log.toString());
  }

  @Test
  public void testEditStartOfEvent() throws Exception {
    parser.parse("edit event start DoctorAppointment from 2025-11-02T15:00 to "
        + "2025-11-02T16:00 with 2025-11-02T15:30");
    assertEquals("editEvent: start with 2025-11-02T15:30", log.toString());
  }

  @Test
  public void testEditEndOfEvent() throws Exception {
    parser.parse("edit event end DoctorAppointment from 2025-11-02T15:00 to "
        + "2025-11-02T16:00 with 2025-11-02T16:30");
    assertEquals("editEvent: end with 2025-11-02T16:30", log.toString());
  }

  @Test
  public void testEditDescriptionOfEventWithStartQuotes() throws Exception {
    parser.parse("edit event description DoctorAppointment from 2025-11-02T15:00 to "
        + "2025-11-02T16:00 with \"updated description");
    assertEquals("editEvent: description with \"updated description", log.toString());
  }

  @Test
  public void testEditDescriptionOfEventWithEndQuotes() throws Exception {
    parser.parse("edit event description DoctorAppointment from 2025-11-02T15:00 to "
        + "2025-11-02T16:00 with updated description\"");
    assertEquals("editEvent: description with updated description\"", log.toString());
  }

  @Test
  public void testEditDescriptionOfEvent() throws Exception {
    parser.parse("edit event description DoctorAppointment from 2025-11-02T15:00 to "
        + "2025-11-02T16:00 with \"updated description\"");
    assertEquals("editEvent: description with updated description", log.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditDescriptionOfEventEmpty() throws Exception {
    parser.parse("edit event description DoctorAppointment from 2025-11-02T15:00 to "
        + "2025-11-02T16:00 with \" \"");
  }

  @Test
  public void testEditDescriptionOfEventEmpty2() throws Exception {
    parser.parse("edit event description DoctorAppointment from 2025-11-02T15:00 to "
        + "2025-11-02T16:00 with \"");
    assertEquals("editEvent: description with \"", log.toString());
  }

  @Test
  public void testEditLocationOfEvent() throws Exception {
    parser.parse("edit event location DoctorAppointment from 2025-11-02T15:00 to "
        + "2025-11-02T16:00 with online");
    assertEquals("editEvent: location with online", log.toString());
  }

  @Test
  public void testEditStatusOfEvent() throws Exception {
    parser.parse("edit event status DoctorAppointment from 2025-11-02T15:00 to "
        + "2025-11-02T16:00 with public");
    assertEquals("editEvent: status with public", log.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditLocationOfEventInvalidLocation() throws Exception {
    parser.parse("edit event location DoctorAppointment from 2025-11-02T15:00 to "
        + "2025-11-02T16:00 with zoom");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditStatusOfEventInvalidStatus() throws Exception {
    parser.parse("edit event status DoctorAppointment from 2025-11-02T15:00 to "
        + "2025-11-02T16:00 with busy");
  }

  @Test
  public void testParsePrintEventsFrom() throws Exception {
    parser.parse("print events from 2025-10-29T10:00 to 2025-10-29T12:00");
    assertEquals("printInRange: 2025-10-29T10:00 to 2025-10-29T12:00", log.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsePrintEventsFromInvalidFormatThrowsException() throws Exception {
    parser.parse("print events from 2025-10-29 10:00 to 2025-10-29T12:00");
  }

  @Test
  public void testParseExportCalCsv() throws Exception {
    parser.parse("export cal calendar.csv");
    assertEquals("export csv to calendar.csv", log.toString());
  }

  @Test
  public void testParseExportCalIcal() throws Exception {
    parser.parse("export cal calendar.ical");
    assertEquals("export ical to calendar.ical", log.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseExportCalMissingFilenameThrowsException() throws Exception {
    parser.parse("export cal ");
  }

  @Test
  public void testParseShowStatus() throws Exception {
    parser.parse("show status on 2025-10-29T09:00");
    assertEquals("showStatus at 2025-10-29T09:00", log.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseDateTimeError() throws Exception {
    parser.parse("show status on 10-29-0000T09:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventError() throws Exception {
    parser.parse("copy event ABC on 10-29-0000T09:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsOnError() throws Exception {
    parser.parse("copy events on 10-29-0000T09:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsBetweenError() throws Exception {
    parser.parse("copy events between 10-29-0000T09:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseDateError() throws Exception {
    parser.parse("print events on 10-29-0000");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditSeriesInvalidCommand() throws Exception {
    parser.parse(
        "edit series subject DoctorAppointment from 2025-11-02T08:00 with");
    assertEquals("editEventSeries", log.toString());
  }

  @Test
  public void testEditSeries() throws Exception {
    parser.parse(
        "edit series subject DoctorAppointment from 2025-11-02T08:00 with Doctor_Appointment");
    assertEquals(
        "editEventSeries property subjectDoctorAppointment2025-11-02T08:00 with value "
            + "Doctor_Appointment",
        log.toString());
  }

  @Test
  public void testEditSeries2() throws Exception {
    parser.parse(
        "edit series subject DoctorAppointment from 2025-11-02T08:00 with "
            + "\"Doctor_Appointment");
    assertEquals(
        "editEventSeries property subjectDoctorAppointment2025-11-02T08:00 with value "
            + "\"Doctor_Appointment",
        log.toString());
  }

  @Test
  public void testEditSeries3() throws Exception {
    parser.parse(
        "edit series subject DoctorAppointment from 2025-11-02T08:00 with Doctor_Appointment\"");
    assertEquals(
        "editEventSeries property subjectDoctorAppointment2025-11-02T08:00 with value "
            + "Doctor_Appointment\"",
        log.toString());
  }

  @Test
  public void testEditSeries4() throws Exception {
    parser.parse(
        "edit series subject \"DoctorAppointment\" from 2025-11-02T08:00 with "
            + "Doctor_Appointment\"");
    assertEquals(
        "editEventSeries property subjectDoctorAppointment2025-11-02T08:00 with value "
            + "Doctor_Appointment\"",
        log.toString());
  }
}
