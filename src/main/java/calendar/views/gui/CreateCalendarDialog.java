package calendar.views.gui;

import static calendar.views.gui.AppStyle.bgClr;
import static calendar.views.gui.AppStyle.blue;
import static calendar.views.gui.AppStyle.btnClr;
import static calendar.views.gui.AppStyle.green;
import static calendar.views.gui.AppStyle.mainBgClr;
import static calendar.views.gui.AppStyle.txtClr;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.ZoneId;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for creating a new calendar.
 */
public class CreateCalendarDialog extends JDialog {

  private JTextField nameField;
  private JComboBox<String> timezoneBox;
  private JButton createCalendar;

  /**
   * Constructs the dialog.
   */
  public CreateCalendarDialog(JFrame parent) {
    super(parent, "Create Calendar", true);
    setSize(400, 220);
    setLocationRelativeTo(this);
    setLayout(new BorderLayout());

    JPanel form = createForm();
    add(form, BorderLayout.CENTER);

    JPanel buttons = dialogButtons();
    add(buttons, BorderLayout.SOUTH);
  }

  /**
   * Add a listener for create calendar button clicks.
   *
   * @param callback the callback to invoke when the button is clicked
   */
  public void addCreateCalendarActionListener(BiConsumer<String, ZoneId> callback) {
    createCalendar.addActionListener(e -> {
      String calName = nameField.getText();
      ZoneId tz = ZoneId.of((String) timezoneBox.getSelectedItem());
      callback.accept(calName, tz);
    });
  }

  private JPanel createForm() {
    JPanel form = new JPanel(new GridBagLayout());
    form.setBackground(mainBgClr);

    GridBagConstraints gbc = baseGbc();
    int row = 0;

    nameField = textField();
    timezoneBox = timezoneComboBox();

    addRow(form, gbc, row++, "Name", nameField);
    addRow(form, gbc, row++, "Timezone", timezoneBox);

    return form;
  }

  private JPanel dialogButtons() {
    JPanel south = new JPanel();
    south.setBackground(mainBgClr);
    south.setBorder(new EmptyBorder(10, 10, 10, 10));

    createCalendar = new JButton("Create Calendar");
    createCalendar.setBackground(blue);
    createCalendar.setForeground(txtClr);
    createCalendar.setOpaque(true);
    createCalendar.setFocusPainted(false);
    createCalendar.setBorderPainted(false);

    south.add(createCalendar);

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


  private JComboBox<String> timezoneComboBox() {
    Set<String> zones = new TreeSet<>(ZoneId.getAvailableZoneIds());

    JComboBox<String> comboBox = new JComboBox<>(zones.toArray(new String[0]));

    comboBox.setEditable(true);
    comboBox.getEditor().getEditorComponent().setBackground(bgClr);
    comboBox.getEditor().getEditorComponent().setForeground(txtClr);

    return comboBox;
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
}
