package calendar.views.gui;

import static calendar.views.gui.AppStyle.fontName;
import static calendar.views.gui.AppStyle.green;
import static calendar.views.gui.AppStyle.mainBgClr;
import static calendar.views.gui.AppStyle.mutedGreen;
import static calendar.views.gui.AppStyle.neutralGrey;
import static calendar.views.gui.AppStyle.translucentGreen;
import static calendar.views.gui.AppStyle.txtClr;
import static calendar.views.gui.AppStyle.vibrantGreen;

import calendar.models.Event;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Represents a single day cell in the calendar grid. Displays the day number and up to 3 events
 * with visual indicators.
 */
public class CalendarDayCell extends JPanel {

  private static final int MAX_EVENTS_DISPLAY = 3;
  private static final int DEFAULT_BORDER_WIDTH = 1;
  private static final int HOVER_BORDER_WIDTH = 2;

  private JLabel dayLabel;
  private JPanel eventsPanel;

  private LocalDate date;
  private int dayNumber;
  private boolean isEnabled;
  private boolean isSelected;
  private Runnable clickListener;

  /**
   * Constructs a calendar day cell with default styling.
   */
  public CalendarDayCell() {
    initializeLayout();
    initializeDayLabel();
    initializeEventsPanel();
    attachMouseListeners();
  }

  private void initializeLayout() {
    setLayout(new BorderLayout(2, 2));
    setBackground(mainBgClr);
    setBorder(BorderFactory.createLineBorder(green, DEFAULT_BORDER_WIDTH));
  }

  private void initializeDayLabel() {
    dayLabel = new JLabel("", SwingConstants.CENTER);
    dayLabel.setFont(new Font(fontName, Font.BOLD, 12));
    dayLabel.setForeground(txtClr);
    dayLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
    add(dayLabel, BorderLayout.NORTH);
  }

  private void initializeEventsPanel() {
    eventsPanel = new JPanel();
    eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
    eventsPanel.setBackground(mainBgClr);
    eventsPanel.setOpaque(false);
    add(eventsPanel, BorderLayout.CENTER);
  }

  private void attachMouseListeners() {
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        handleMouseClick();
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        handleMouseEnter();
      }

      @Override
      public void mouseExited(MouseEvent e) {
        handleMouseExit();
      }
    });
  }

  private void handleMouseClick() {
    if (isEnabled && clickListener != null) {
      clickListener.run();
    }
  }

  private void handleMouseEnter() {
    if (isEnabled && !isSelected) {
      setBorder(BorderFactory.createLineBorder(green, HOVER_BORDER_WIDTH));
    }
  }

  private void handleMouseExit() {
    if (isEnabled && !isSelected) {
      setBorder(BorderFactory.createLineBorder(green, DEFAULT_BORDER_WIDTH));
    }
  }

  /**
   * Gets the day of the month for this cell.
   *
   * @return the day number (1-31)
   */
  public int getDay() {
    return dayNumber;
  }

  /**
   * Sets the day of the month for this cell.
   *
   * @param day the day number to display (1-31)
   */
  public void setDay(int day) {
    this.dayNumber = day;
    dayLabel.setText(String.valueOf(day));
  }

  /**
   * Gets the full date associated with this cell.
   *
   * @return the LocalDate of this cell
   */
  public LocalDate getDate() {
    return date;
  }

  /**
   * Sets the full date for this cell.
   *
   * @param date the date to associate with this cell
   */
  public void setDate(LocalDate date) {
    this.date = date;
  }

  /**
   * Displays events in this cell, showing up to 3 events and a "more" indicator.
   *
   * @param events the list of events to display
   */
  public void setEvents(List<Event> events) {
    eventsPanel.removeAll();

    int displayCount = Math.min(events.size(), MAX_EVENTS_DISPLAY);
    displayEventLabels(events, displayCount);

    if (events.size() > MAX_EVENTS_DISPLAY) {
      displayMoreIndicator(events.size() - MAX_EVENTS_DISPLAY);
    }

    eventsPanel.revalidate();
    eventsPanel.repaint();
  }

  private void displayEventLabels(List<Event> events, int count) {
    for (int i = 0; i < count; i++) {
      JLabel eventLabel = createEventLabel(events.get(i).getSubject());
      eventsPanel.add(eventLabel);
      eventsPanel.add(Box.createVerticalStrut(1));
    }
  }

  private JLabel createEventLabel(String subject) {
    JLabel eventLabel = new JLabel(subject);
    eventLabel.setFont(new Font(fontName, Font.PLAIN, 9));
    eventLabel.setForeground(txtClr);
    eventLabel.setBackground(translucentGreen);
    eventLabel.setOpaque(true);
    eventLabel.setBorder(new EmptyBorder(1, 3, 1, 3));
    eventLabel.setAlignmentX(LEFT_ALIGNMENT);
    return eventLabel;
  }

  private void displayMoreIndicator(int remaining) {
    JLabel moreLabel = new JLabel(remaining + " more");
    moreLabel.setFont(new Font(fontName, Font.ITALIC, 9));
    moreLabel.setForeground(neutralGrey);
    moreLabel.setAlignmentX(LEFT_ALIGNMENT);
    eventsPanel.add(moreLabel);
  }

  /**
   * Highlights this cell as today's date.
   *
   * @param isToday true if this cell represents today
   */
  public void setToday(boolean isToday) {
    if (isToday) {
      dayLabel.setForeground(green);
      dayLabel.setFont(new Font(fontName, Font.BOLD, 14));
    }
  }

  /**
   * Marks this cell as selected with enhanced visual styling.
   *
   * @param selected true if this cell is selected
   */
  public void setSelected(boolean selected) {
    this.isSelected = selected;
    if (selected) {
      setBackground(mutedGreen);
      setBorder(BorderFactory.createLineBorder(vibrantGreen, HOVER_BORDER_WIDTH));
    } else {
      setBackground(mainBgClr);
      setBorder(BorderFactory.createLineBorder(green, DEFAULT_BORDER_WIDTH));
    }
  }

  @Override
  public boolean isEnabled() {
    return isEnabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.isEnabled = enabled;
    super.setEnabled(enabled);
    if (!enabled) {
      clearContent();
    }
  }

  private void clearContent() {
    dayLabel.setText("");
    eventsPanel.removeAll();
    setBackground(mainBgClr);
  }

  /**
   * Resets the cell to its default state.
   */
  public void clear() {
    dayLabel.setText("");
    eventsPanel.removeAll();
    resetState();
    resetStyling();
  }

  private void resetState() {
    date = null;
    dayNumber = 0;
    isEnabled = false;
    isSelected = false;
  }

  private void resetStyling() {
    setBackground(mainBgClr);
    dayLabel.setForeground(txtClr);
    dayLabel.setFont(new Font(fontName, Font.BOLD, 12));
    setBorder(BorderFactory.createLineBorder(green, DEFAULT_BORDER_WIDTH));
  }

  /**
   * Registers a click listener for this cell.
   *
   * @param listener the runnable to execute on click
   */
  public void addClickListener(Runnable listener) {
    this.clickListener = listener;
  }
}