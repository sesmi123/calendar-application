package calendar.views.gui;

import static calendar.views.gui.AppStyle.bgClr;
import static calendar.views.gui.AppStyle.editIconPath;
import static calendar.views.gui.AppStyle.fontName;
import static calendar.views.gui.AppStyle.green;
import static calendar.views.gui.AppStyle.mainBgClr;
import static calendar.views.gui.AppStyle.translucentGreen;
import static calendar.views.gui.AppStyle.txtClr;

import calendar.models.Event;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * East panel that displays events for a selected day.
 */
public class EastPanel extends JPanel {

  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
  Consumer<Event> editEventBtnCallback;
  private JLabel selectedDateLabel;
  private JPanel eventsContainer;
  private LocalDate currentDate;
  private JButton createEventOnBtn;

  /**
   * Creates the east panel for displaying day events.
   */
  public EastPanel() {
    setBackground(bgClr);
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(300, 0));
    setBorder(new EmptyBorder(10, 10, 10, 10));

    createDateHeader();
    createEventsPanel();
    createEventButton();

    currentDate = LocalDate.now();
    loadEventsForDate(currentDate, new HashSet<>());
  }

  public LocalDate getDate() {
    return this.currentDate;
  }

  /**
   * Loads and displays events for a specific date.
   *
   * @param date   the date to display events for
   * @param events the set of events on that date
   */
  public void loadEventsForDate(LocalDate date, Set<Event> events) {
    this.currentDate = date;
    updateDateHeader(date);
    displayEvents(events, date);
  }

  /**
   * Clears the event display and shows a message.
   */
  public void clearEvents() {
    eventsContainer.removeAll();

    JLabel noEventsLabel = new JLabel("Select a day to view events");
    noEventsLabel.setFont(new Font(fontName, Font.ITALIC, 12));
    noEventsLabel.setForeground(txtClr);
    noEventsLabel.setAlignmentX(CENTER_ALIGNMENT);

    eventsContainer.add(Box.createVerticalGlue());
    eventsContainer.add(noEventsLabel);
    eventsContainer.add(Box.createVerticalGlue());

    eventsContainer.revalidate();
    eventsContainer.repaint();
  }

  public JButton getCreateEventButton() {
    return this.createEventOnBtn;
  }

  private void createDateHeader() {
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBackground(bgClr);
    headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

    selectedDateLabel = new JLabel("Select a day");
    selectedDateLabel.setFont(new Font(fontName, Font.BOLD, 16));
    selectedDateLabel.setForeground(green);
    selectedDateLabel.setHorizontalAlignment(SwingConstants.LEFT);

    headerPanel.add(selectedDateLabel, BorderLayout.CENTER);
    add(headerPanel, BorderLayout.NORTH);
  }

  private void createEventsPanel() {
    eventsContainer = new JPanel();
    eventsContainer.setLayout(new BoxLayout(eventsContainer, BoxLayout.Y_AXIS));
    eventsContainer.setBackground(mainBgClr);

    JScrollPane scrollPane = new JScrollPane(eventsContainer);
    scrollPane.setBorder(BorderFactory.createLineBorder(green, 1));
    scrollPane.setBackground(mainBgClr);
    scrollPane.getViewport().setBackground(mainBgClr);

    add(scrollPane, BorderLayout.CENTER);

    clearEvents();
  }

  private void createEventButton() {
    createEventOnBtn = new JButton("Create Event");
    createEventOnBtn.setBackground(green);
    createEventOnBtn.setForeground(txtClr);
    createEventOnBtn.setBorderPainted(false);
    createEventOnBtn.setFocusPainted(false);
    createEventOnBtn.setOpaque(true);
    createEventOnBtn.setFont(new Font(fontName, Font.BOLD, 14));
    add(createEventOnBtn, BorderLayout.SOUTH);
  }

  private void updateDateHeader(LocalDate date) {
    String dayOfWeek = date.getDayOfWeek()
        .getDisplayName(TextStyle.FULL, Locale.getDefault());
    String month = date.getMonth()
        .getDisplayName(TextStyle.FULL, Locale.getDefault());
    int day = date.getDayOfMonth();
    int year = date.getYear();

    selectedDateLabel.setText(String.format("%s, %s %d, %d", dayOfWeek, month, day, year));
  }

  private List<Event> sortEvents(Set<Event> events) {
    List<Event> sorted = new ArrayList<>(events);
    sorted.sort(Comparator
        .comparing(Event::getStartDate)
        .thenComparing((e1, e2) -> {
          if (e1.getStartDateTime() == null && e2.getStartDateTime() == null) {
            return 0;
          }
          if (e1.getStartDateTime() == null) {
            return -1;
          }
          if (e2.getStartDateTime() == null) {
            return 1;
          }
          return e1.getStartDateTime().compareTo(e2.getStartDateTime());
        })
    );
    return sorted;
  }

  private void displayEvents(Set<Event> events, LocalDate viewingDate) {
    eventsContainer.removeAll();

    if (events == null || events.isEmpty()) {
      JLabel noEventsLabel = new JLabel("No events scheduled");
      noEventsLabel.setFont(new Font(fontName, Font.ITALIC, 12));
      noEventsLabel.setForeground(txtClr);
      noEventsLabel.setAlignmentX(LEFT_ALIGNMENT);

      eventsContainer.add(Box.createVerticalStrut(10));
      eventsContainer.add(noEventsLabel);
      eventsContainer.add(Box.createVerticalGlue());
    } else {
      List<Event> sorted = sortEvents(events);

      eventsContainer.add(Box.createVerticalStrut(5));

      for (Event event : sorted) {
        JPanel eventCard = createEventCard(event, viewingDate);
        eventsContainer.add(eventCard);
        eventsContainer.add(Box.createVerticalStrut(8));
      }

      eventsContainer.add(Box.createVerticalGlue());
    }

    eventsContainer.revalidate();
    eventsContainer.repaint();
  }

  public void setEditEventBtnCallback(Consumer<Event> callback) {
    editEventBtnCallback = callback;
  }

  private JPanel createEventCard(Event event, LocalDate viewingDate) {
    JPanel card = new JPanel();
    card.setLayout(new BorderLayout(5, 5));
    card.setBackground(translucentGreen);
    card.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(green, 1),
        new EmptyBorder(8, 10, 8, 10)
    ));
    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

    JLabel subjectLabel = new JLabel(event.getSubject());
    subjectLabel.setFont(new Font(fontName, Font.BOLD, 13));
    subjectLabel.setForeground(txtClr);

    String timeText = formatEventTimeForDate(event, viewingDate);
    JLabel timeLabel = new JLabel(timeText);
    timeLabel.setFont(new Font(fontName, Font.PLAIN, 11));
    timeLabel.setForeground(txtClr);

    ImageIcon icon = new ImageIcon(editIconPath.toString());
    Image scaledImg = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
    JButton editEventBtn = new JButton(new ImageIcon(scaledImg));
    editEventBtn.setBackground(translucentGreen);
    editEventBtn.setBorderPainted(false);
    editEventBtn.setOpaque(true);
    editEventBtn.addActionListener(e -> editEventBtnCallback.accept(event));

    card.add(subjectLabel, BorderLayout.NORTH);
    card.add(timeLabel, BorderLayout.CENTER);
    card.add(editEventBtn, BorderLayout.EAST);

    return card;
  }

  private String formatEventTimeForDate(Event event, LocalDate viewingDate) {
    LocalDate startDate = event.getStartDate();
    LocalDate endDate = event.getEndDate();

    if (event.getStartDateTime() == null || event.getEndDateTime() == null) {
      return formatAllDayEvent(startDate, endDate, viewingDate);
    }

    if (startDate.equals(endDate)) {
      String startTime = event.getStartDateTime().toLocalTime().format(TIME_FORMATTER);
      String endTime = event.getEndDateTime().toLocalTime().format(TIME_FORMATTER);
      return startTime + " - " + endTime;
    }

    return formatMultiDayEvent(event, viewingDate);
  }

  private String formatAllDayEvent(LocalDate startDate, LocalDate endDate, LocalDate viewingDate) {
    if (startDate.equals(endDate)) {
      return "All day";
    }

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d");

    if (viewingDate.equals(startDate)) {
      return "All day (starts, ends " + endDate.format(dateFormatter) + ")";
    } else if (viewingDate.equals(endDate)) {
      return "All day (started " + startDate.format(dateFormatter) + ", ends)";
    } else {
      return "All day (continues, " + startDate.format(dateFormatter) + " - " + endDate.format(
          dateFormatter) + ")";
    }
  }

  private String formatMultiDayEvent(Event event, LocalDate viewingDate) {
    LocalDate startDate = event.getStartDate();
    LocalDate endDate = event.getEndDate();
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d");

    String startTime = event.getStartDateTime().toLocalTime().format(TIME_FORMATTER);
    String endTime = event.getEndDateTime().toLocalTime().format(TIME_FORMATTER);

    if (viewingDate.equals(startDate)) {
      return startTime + " - (continues until " + endDate.format(dateFormatter) + " " + endTime
          + ")";
    } else if (viewingDate.equals(endDate)) {
      return "(started " + startDate.format(dateFormatter) + " " + startTime + ") - " + endTime;
    } else {
      return "All day (part of " + startDate.format(dateFormatter) + " - " + endDate.format(
          dateFormatter) + ")";
    }
  }
}