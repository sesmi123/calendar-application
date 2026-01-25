package calendar.views.gui;

import static calendar.views.gui.AppStyle.bgClr;

import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Represents the south panel of the calendar.
 */
public class SouthPanel extends JPanel {

  private final JLabel messageLbl;

  /**
   * Create a new south panel.
   */
  public SouthPanel() {
    setLayout(new FlowLayout(FlowLayout.LEFT));
    setBackground(bgClr);

    messageLbl = new JLabel();
    add(messageLbl);
  }

  /**
   * Display a message in the south panel.
   *
   * @param message the message to be displayed
   * @param color   the foreground color
   * @param ms      the delay in ms before the message is cleared
   */
  public void showMessage(String message, Color color, int ms) {
    messageLbl.setForeground(color);
    messageLbl.setText(message);

    Timer timer = new Timer(ms, e -> messageLbl.setText(""));
    timer.setRepeats(false);
    timer.start();
  }

}
