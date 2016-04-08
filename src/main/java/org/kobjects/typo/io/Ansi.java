package org.kobjects.typo.io;

public class Ansi {
  public static final String RESET = "\u001b[0m";

  public static String fgColor(int r, int g, int b) {
    return "\u001b[38;2;" + (r & 255) + ";" + (g & 255) + ";" + (b & 255) + "m";
  }

  public static String bgColor(int r, int g, int b) {
    return "\u001b[48;2;" + (r & 255) + ";" + (g & 255) + ";" + (b & 255) + "m";
  }

}
