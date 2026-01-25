package calendar.views.gui;

import static calendar.views.gui.AppStyle.bgClr;
import static calendar.views.gui.AppStyle.fontName;
import static calendar.views.gui.AppStyle.hamburgerIconPath;
import static calendar.views.gui.AppStyle.timezoneIconPath;
import static calendar.views.gui.AppStyle.txtClr;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * North panel of the main application window.
 */
public class NorthPanel extends JPanel {

  private JButton toggleWestPnlBtn;
  private JLabel timeZoneLbl;

  /**
   * Constructs the north panel.
   */
  public NorthPanel() {
    setLayout(new BorderLayout());
    setBackground(bgClr);
    add(createWestPnl(), BorderLayout.WEST);
    add(createEastPanel(), BorderLayout.EAST);
  }

  /**
   * Returns the button to toggle the west panel.
   */
  public JButton getWestPnlToggleBtn() {
    return toggleWestPnlBtn;
  }

  /**
   * Update the label of timezone of calendar.
   *
   * @param tz timezone
   */
  public void setTimeZone(String tz) {
    timeZoneLbl.setText(tz);
  }

  private JPanel createWestPnl() {
    JPanel jp = new JPanel();
    jp.setLayout(new FlowLayout(FlowLayout.LEFT));
    jp.setBackground(bgClr);

    createToggleWestPanelButton();
    jp.add(toggleWestPnlBtn);

    JLabel mainLabel = new JLabel();
    mainLabel.setText("Calendar");
    mainLabel.setForeground(txtClr);
    mainLabel.setFont(new Font(fontName, Font.BOLD, 18));

    jp.add(mainLabel);

    return jp;
  }

  private JPanel createEastPanel() {
    JPanel jp = new JPanel();
    jp.setLayout(new FlowLayout(FlowLayout.RIGHT));
    jp.setBackground(bgClr);

    timeZoneLbl = new JLabel("");
    timeZoneLbl.setBackground(bgClr);
    timeZoneLbl.setForeground(txtClr);
    timeZoneLbl.setFont(new Font(fontName, Font.BOLD, 14));

    ImageIcon icon = new ImageIcon(timezoneIconPath.toString());
    Image scaledImg = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
    jp.add(new JLabel(new ImageIcon(scaledImg)));
    jp.add(timeZoneLbl);

    return jp;
  }

  private void createToggleWestPanelButton() {
    ImageIcon icon = new ImageIcon(hamburgerIconPath.toString());
    Image scaledImg = icon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
    toggleWestPnlBtn = new JButton("", new ImageIcon(scaledImg));
    toggleWestPnlBtn.setBackground(bgClr);
    toggleWestPnlBtn.setForeground(txtClr);
    toggleWestPnlBtn.setFocusPainted(false);
    toggleWestPnlBtn.setBorderPainted(false);
    toggleWestPnlBtn.setOpaque(true);
  }
}
