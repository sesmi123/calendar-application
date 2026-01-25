package calendar.views.gui;

import java.awt.Color;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Holds the style constants for the GUI.
 */
public final class AppStyle {

  public static final Color green = new Color(47, 164, 110);
  public static final Color blue = new Color(58, 129, 219);
  public static final Color red = new Color(237, 28, 37);
  public static final Color vibrantGreen = new Color(100, 200, 100);
  public static final Color translucentGreen = new Color(64, 111, 64);
  public static final Color mutedGreen = new Color(86, 102, 86);
  public static final Color neutralGrey = new Color(150, 150, 150);
  public static final Color bgClr = new Color(23, 23, 23);
  public static final Color mainBgClr = new Color(13, 13, 13);
  public static final Color btnClr = new Color(59, 59, 59);
  public static final Color txtClr = new Color(245, 245, 245);
  public static final String fontName = "Arial";

  public static final Path appIconPath = Paths.get("static", "images", "calendar.png");
  public static final Path hamburgerIconPath = Paths.get("static", "images", "hamburger.png");
  public static final Path timezoneIconPath = Paths.get("static", "images", "time-zone.png");
  public static final Path editIconPath = Paths.get("static", "images", "edit.png");

  /**
   * Prevent instantiation.
   */
  private AppStyle() {
  }
}
