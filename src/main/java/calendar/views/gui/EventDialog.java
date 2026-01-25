package calendar.views.gui;

import static calendar.views.gui.AppStyle.bgClr;
import static calendar.views.gui.AppStyle.blue;
import static calendar.views.gui.AppStyle.btnClr;
import static calendar.views.gui.AppStyle.green;
import static calendar.views.gui.AppStyle.mainBgClr;
import static calendar.views.gui.AppStyle.red;
import static calendar.views.gui.AppStyle.txtClr;

import calendar.controllers.Features;
import calendar.controllers.ParsingUtils;
import calendar.models.Event;
import calendar.models.enums.EventProperty;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

/**
 * Represents the create event dialog.
 */
public class EventDialog extends JDialog {

  private final JCheckBox[] dayCbs = new JCheckBox[7];
  private final Runnable reloadCalendarEvents;
  private final Runnable reloadDayEvents;
  private final EventDialogMode mode;
  private final JPanel formOuterPnl;
  private Features features;
  private JTextField subject;
  private JTextField startDate;
  private JTextField startTime;
  private JTextField endDate;
  private JTextField endTime;
  private JRadioButton endsAfter;
  private JRadioButton endsOn;
  private JSpinner numOccurrences;
  private JTextField untilDateField;
  private JButton saveBtn;
  private JLabel errorMessage;
  private JComboBox<EventProperty> editProperty;
  private JPanel editScope;
  private JRadioButton editThisOnly;
  private JRadioButton editThisAndFollowing;
  private JRadioButton editAll;
  private JTextField editField;

  /**
   * Main private constructor.
   */
  private EventDialog(JFrame parent,
      String title,
      EventDialogMode mode,
      Runnable reloadCalendarEvents,
      Runnable reloadDayEvents) {
    super(parent, title, true);
    this.reloadCalendarEvents = reloadCalendarEvents;
    this.reloadDayEvents = reloadDayEvents;
    this.mode = mode;

    setSize(750, 500);
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout());

    formOuterPnl = createForm();
    add(formOuterPnl, BorderLayout.CENTER);

    JPanel buttons = dialogButtons();
    add(buttons, BorderLayout.SOUTH);
  }

  /**
   * Creates a new create event dialog.
   *
   * @param parent               the parent frame
   * @param reloadCalendarEvents callback to refresh the calendar events
   * @param reloadDayEvents      callback to refresh the day events
   */
  public EventDialog(JFrame parent,
      Runnable reloadCalendarEvents,
      Runnable reloadDayEvents) {
    this(parent, "Create Event", EventDialogMode.CREATE,
        reloadCalendarEvents, reloadDayEvents);
    saveBtn.addActionListener(e -> handleCreateEvent());
  }

  /**
   * Creates a new edit event dialog.
   *
   * @param parent               the parent frame
   * @param event                the event to edit
   * @param reloadCalendarEvents callback to refresh the calendar events
   * @param reloadDayEvents      callback to refresh the day events
   */
  public EventDialog(JFrame parent,
      Event event,
      Runnable reloadCalendarEvents,
      Runnable reloadDayEvents) {
    this(parent, "Edit Event", EventDialogMode.EDIT,
        reloadCalendarEvents, reloadDayEvents);
    setUpEditFields(event);
  }

  /**
   * Sets the start date field.
   *
   * @param date the start date
   */
  public void setStartDate(String date) {
    startDate.setText(date);
  }

  /**
   * Sets the end date field.
   *
   * @param date the end date
   */
  public void setEndDate(String date) {
    endDate.setText(date);
  }

  /**
   * Sets the start time field.
   *
   * @param time the start time
   */
  public void setStartTime(String time) {
    startTime.setText(time);
  }

  /**
   * Sets the end time field.
   *
   * @param time the end time
   */
  public void setEndTime(String time) {
    endTime.setText(time);
  }

  private void setUpEditFields(Event event) {

    saveBtn.addActionListener(e -> handleEditEvent());

    subject.setText(event.getSubject());
    subject.setEditable(false);

    startDate.setText(event.getStartDate().toString());
    startDate.setEditable(false);

    startTime.setText(event.getStartTime().toString());
    startTime.setEditable(false);

    endDate.setText(event.getEndDate().toString());
    endDate.setEditable(false);

    endTime.setText(event.getEndTime().toString());
    endTime.setEditable(false);

    if (event.getSeriesId() != null) {
      createEditScope();
    }

  }

  private void createEditScope() {
    editScope = new JPanel();
    editScope.setLayout(new BoxLayout(editScope, BoxLayout.Y_AXIS));
    editScope.setBackground(mainBgClr);

    editThisOnly = new JRadioButton("Edit this event");
    editThisOnly.setBackground(mainBgClr);
    editThisOnly.setForeground(txtClr);
    editThisOnly.setSelected(true);

    editThisAndFollowing = new JRadioButton("Edit this and following events");
    editThisAndFollowing.setBackground(mainBgClr);
    editThisAndFollowing.setForeground(txtClr);

    editAll = new JRadioButton("Edit all events");
    editAll.setBackground(mainBgClr);
    editAll.setForeground(txtClr);

    ButtonGroup bg = new ButtonGroup();
    bg.add(editThisOnly);
    bg.add(editThisAndFollowing);
    bg.add(editAll);

    editScope.add(editThisOnly);
    editScope.add(editThisAndFollowing);
    editScope.add(editAll);
    formOuterPnl.add(editScope);
    formOuterPnl.revalidate();
    formOuterPnl.repaint();
  }

  /**
   * Set the features to be used by the CenterPanelNorthPanel.
   *
   * @param features the features
   */
  public void setFeatures(Features features) {
    this.features = features;
  }

  private boolean tryParseDate(String edValue) {
    try {
      ParsingUtils.parseDateTime(edValue);
      return true;
    } catch (IllegalArgumentException e) {
      errorMessage.setText(e.getMessage());
      return false;
    }
  }

  private void editSingleEvent(EventProperty edProperty, String edValue) {
    features.editEvent(
        edProperty.toString(),
        subject.getText(),
        startDate.getText() + "T" + startTime.getText(),
        endDate.getText() + "T" + endTime.getText(),
        edValue
    );
  }

  private void editThisAndFollowingEvents(EventProperty edProperty, String edValue) {
    features.editEvents(
        edProperty.toString(),
        subject.getText(),
        startDate.getText() + "T" + startTime.getText(),
        edValue
    );
  }

  private void editAllEvents(EventProperty edProperty, String edValue) {
    features.editSeries(
        edProperty.toString(),
        subject.getText(),
        startDate.getText() + "T" + startTime.getText(),
        edValue
    );
  }

  private void handleEditEvent() {
    EventProperty edProperty = (EventProperty) editProperty.getSelectedItem();
    String edValue = editField.getText().trim();
    if (edValue.isEmpty()) {
      errorMessage.setText("Enter a non-empty value");
      return;
    }

    if (edProperty == EventProperty.START_DATE_TIME || edProperty == EventProperty.END_DATE_TIME) {
      if (!tryParseDate(edValue)) {
        return;
      }
    }

    if (editScope == null || editThisOnly.isSelected()) {
      editSingleEvent(edProperty, edValue);
    } else {

      if (editThisAndFollowing.isSelected()) {
        editThisAndFollowingEvents(edProperty, edValue);
      }

      if (editAll.isSelected()) {
        editAllEvents(edProperty, edValue);
      }
    }
    reloadCalendarEvents.run();
    reloadDayEvents.run();
    dispose();
  }

  private void handleCreateEvent() {
    String subj = subject.getText().trim();
    String startD = startDate.getText().trim();
    String startT = startTime.getText().trim();
    String endD = endDate.getText().trim();
    String endT = endTime.getText().trim();
    Set<DayOfWeek> days = getSelectedDays();
    String untilD = untilDateField.getText().trim();

    if (!validateMandatoryFields(subj, startD)) {
      return;
    }

    if (!days.isEmpty()) {
      if (!validateRecurringFields(startD, endD, startT, endT)) {
        return;
      }
      boolean isAllDayRecurring = startT.isEmpty() && endT.isEmpty();
      createRecurringEvent(
          subj,
          startD,
          isAllDayRecurring ? null : startT,
          isAllDayRecurring ? null : endT,
          days,
          untilD
      );
    } else if (startT.isEmpty() || endT.isEmpty()) {
      if (!validateAllDayTimes(startT, endT)) {
        return;
      }
      features.createAllDayEvent(subj, startD);
    } else {
      String startDt = startD + "T" + startT;
      String endDt = endD + "T" + endT;
      features.createEvent(subj, startDt, endDt);
    }

    reloadCalendarEvents.run();
    reloadDayEvents.run();
    dispose();
  }

  private boolean validateMandatoryFields(String subj, String startD) {
    if (subj.isEmpty() || startD.isEmpty()) {
      errorMessage.setText("Subject and Start Date are required.");
      return false;
    }
    return true;
  }

  private boolean validateRecurringFields(String startD, String endD, String startT, String endT) {
    if (!endD.isEmpty() && !endD.equals(startD)) {
      errorMessage.setText(
          "End date must be the same as start date for recurring events");
      return false;
    }

    if (!endD.isEmpty() && (startT.isEmpty() || endT.isEmpty())) {
      errorMessage.setText("Start time and end time must both be set for recurring events.");
      return false;
    }

    if (endD.isEmpty() && !(startT.isEmpty() && endT.isEmpty())) {
      errorMessage.setText(
          "End date must be specified");
      return false;
    }
    return true;
  }

  private boolean validateAllDayTimes(String startT, String endT) {
    if (!(startT.isEmpty() && endT.isEmpty())) {
      errorMessage.setText("Either start time and end time should both be set or both left empty.");
      return false;
    }
    return true;
  }

  private void createRecurringEvent(String subj, String startD, String startT, String endT,
      Set<DayOfWeek> days, String untilD) {
    if (endsAfter.isSelected()) {
      features.createEventSeries(subj, startD, startT, endT, days,
          (Integer) numOccurrences.getValue(), null);
    } else {
      features.createEventSeries(subj, startD, startT, endT, days, 0, untilD);
    }
  }

  private JPanel dialogButtons() {
    JPanel south = new JPanel();
    south.setBackground(mainBgClr);
    south.setBorder(new EmptyBorder(10, 10, 10, 10));

    saveBtn = new JButton("Save");
    saveBtn.setBackground(blue);
    saveBtn.setForeground(txtClr);
    saveBtn.setOpaque(true);
    saveBtn.setFocusPainted(false);
    saveBtn.setBorderPainted(false);

    south.add(saveBtn);

    JButton cancel = new JButton("Cancel");
    cancel.setBackground(btnClr);
    cancel.setForeground(txtClr);
    cancel.setOpaque(true);
    cancel.setFocusPainted(false);
    cancel.setBorderPainted(false);
    cancel.addActionListener(e -> this.dispose());

    south.add(cancel);

    return south;
  }

  private JPanel createForm() {
    JPanel outerPnl = new JPanel();
    outerPnl.setLayout(new BoxLayout(outerPnl, BoxLayout.Y_AXIS));
    outerPnl.setBackground(mainBgClr);
    JPanel form = new JPanel(new GridBagLayout());
    form.setBackground(mainBgClr);
    GridBagConstraints gbc = baseGbc();

    int row = 0;

    subject = textField();
    addRow(form, gbc, row++, "Subject", subject);
    startDate = textField();
    addRow(form, gbc, row++, "Start Date (YYYY-MM-DD)", startDate);
    startTime = textField();
    addRow(form, gbc, row++, "Start Time (HH:MM)", startTime);
    endDate = textField();
    addRow(form, gbc, row++, "End Date (YYYY-MM-DD)", endDate);
    endTime = textField();
    addRow(form, gbc, row++, "End Time (HH:MM)", endTime);

    if (mode.equals(EventDialogMode.CREATE)) {
      addRow(form, gbc, row++, "Repeat on", weekdayPanel());
      addRow(form, gbc, row++, "Repeat ends", endsPanel());
    }

    if (mode.equals(EventDialogMode.EDIT)) {
      editProperty = new JComboBox<>(EventProperty.values());

      editProperty.setEditable(true);
      editProperty.getEditor().getEditorComponent().setBackground(bgClr);
      editProperty.getEditor().getEditorComponent().setForeground(txtClr);

      gbc.gridx = 0;
      gbc.gridy = row++;
      form.add(editProperty, gbc);

      gbc.gridx = 1;
      editField = new JTextField();
      editField.setBackground(bgClr);
      editField.setForeground(txtClr);
      editField.setCaretColor(green);
      editField.setOpaque(true);
      form.add(editField, gbc);
    }
    errorMessage = new JLabel();
    errorMessage.setForeground(red);
    addRow(form, gbc, row++, "", errorMessage);
    outerPnl.add(form);
    return outerPnl;
  }

  private GridBagConstraints baseGbc() {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    return gbc;
  }

  private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label,
      JComponent field) {
    gbc.gridx = 0;
    gbc.gridy = row;
    panel.add(buildLabel(label), gbc);

    gbc.gridx = 1;
    panel.add(field, gbc);
  }

  private JTextField textField() {
    JTextField tf = new JTextField(20);
    tf.setBackground(bgClr);
    tf.setForeground(txtClr);
    tf.setCaretColor(green);
    return tf;
  }

  private JLabel buildLabel(String text) {
    JLabel lbl = new JLabel(text);
    lbl.setForeground(txtClr);
    return lbl;
  }

  private JPanel weekdayPanel() {
    JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
    pnl.setBackground(mainBgClr);

    String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (int i = 0; i < 7; i++) {
      JCheckBox cb = new JCheckBox(days[i]);
      cb.setBackground(mainBgClr);
      cb.setForeground(txtClr);
      dayCbs[i] = cb;
      pnl.add(cb);
    }
    return pnl;
  }

  private Set<DayOfWeek> getSelectedDays() {
    Set<DayOfWeek> days = new java.util.HashSet<>();

    for (int i = 0; i < 7; i++) {
      if (dayCbs[i].isSelected()) {
        DayOfWeek dow = DayOfWeek.of((i + 7 - 1) % 7 + 1);
        days.add(dow);
      }
    }
    return days;
  }

  private JPanel endsPanel() {
    JPanel seriesEndsPnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
    seriesEndsPnl.setBackground(mainBgClr);

    endsAfter = new JRadioButton("After");
    endsAfter.setForeground(txtClr);
    endsAfter.setBackground(mainBgClr);
    endsAfter.setSelected(true);

    ButtonGroup group = new ButtonGroup();
    group.add(endsAfter);
    seriesEndsPnl.add(endsAfter);

    numOccurrences = new JSpinner(new SpinnerNumberModel(10, 0, 100, 1));
    JComponent editor = numOccurrences.getEditor();
    ((JSpinner.DefaultEditor) editor).getTextField().setBackground(bgClr);
    ((JSpinner.DefaultEditor) editor).getTextField().setForeground(txtClr);
    seriesEndsPnl.add(numOccurrences);

    endsOn = new JRadioButton("On");
    endsOn.setForeground(txtClr);
    endsOn.setBackground(mainBgClr);
    group.add(endsOn);
    seriesEndsPnl.add(endsOn);

    untilDateField = textField();
    seriesEndsPnl.add(untilDateField);

    return seriesEndsPnl;
  }

  private enum EventDialogMode {
    CREATE, EDIT
  }
}
